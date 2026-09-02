package com.riprod.hexcode.core.common.hexes.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.Deflater;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nullable;

import org.joml.Vector3f;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class CodecUtil {

    static final int HASH_BITS = 11;
    static final int POS_BITS = 12;
    static final int V4_MAX_INPUT_KEYS = 7;
    static final float V4_MAX_POS = 20.47f;

    private static final int PFX_NONE = 0;
    private static final int PFX_GLYPH = 1;
    private static final int PFX_NUMBER = 2;

    private static List<String> cachedDictionary;
    private static List<String> cachedSlotDictionary;
    private static Map<Integer, List<String>> cachedGlyphHashLookup;
    private static Map<Integer, List<String>> cachedSlotHashLookup;

    // --- fnv-1a hash ---

    static int assetHash(String s) {
        int h = 0x811c9dc5;
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            h ^= (b & 0xFF);
            h *= 0x01000193;
        }
        return h & ((1 << HASH_BITS) - 1);
    }

    // --- dictionary ---

    public static List<String> buildDictionary() {
        if (cachedDictionary != null) return cachedDictionary;
        List<String> dict = new ArrayList<>(GlyphAsset.getAssetMap().getAssetMap().keySet());
        Collections.sort(dict);
        cachedDictionary = Collections.unmodifiableList(dict);
        return cachedDictionary;
    }

    public static List<String> buildSlotDictionary() {
        if (cachedSlotDictionary != null) return cachedSlotDictionary;
        TreeSet<String> all = new TreeSet<>();
        for (GlyphAsset asset : GlyphAsset.getAssetMap().getAssetMap().values()) {
            all.addAll(asset.getSlotKeys());
        }
        cachedSlotDictionary = List.copyOf(all);
        return cachedSlotDictionary;
    }

    static Map<Integer, List<String>> getGlyphHashLookup() {
        if (cachedGlyphHashLookup == null) {
            cachedGlyphHashLookup = buildHashLookup(buildDictionary());
        }
        return cachedGlyphHashLookup;
    }

    static Map<Integer, List<String>> getSlotHashLookup() {
        if (cachedSlotHashLookup == null) {
            cachedSlotHashLookup = buildHashLookup(buildSlotDictionary());
        }
        return cachedSlotHashLookup;
    }

    public static List<String> getOrderedSlotKeys(String glyphId) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (asset == null) return List.of();
        return new ArrayList<>(asset.getSlotKeys());
    }

    public static void invalidateCache() {
        cachedDictionary = null;
        cachedSlotDictionary = null;
        cachedGlyphHashLookup = null;
        cachedSlotHashLookup = null;
    }

    static Map<Integer, List<String>> buildHashLookup(List<String> dictionary) {
        Map<Integer, List<String>> lookup = new HashMap<>();
        for (String s : dictionary) {
            lookup.computeIfAbsent(assetHash(s), k -> new ArrayList<>()).add(s);
        }
        return lookup;
    }

    // --- palette resolution ---

    @Nullable
    static String resolvePaletteEntry(int hintIdx, int storedHash,
            List<String> dictionary, Map<Integer, List<String>> hashLookup,
            List<DecodeIssue> issues) {

        if (hintIdx < dictionary.size()) {
            String candidate = dictionary.get(hintIdx);
            if (assetHash(candidate) == storedHash) {
                return candidate;
            }
        }

        List<String> candidates = hashLookup.getOrDefault(storedHash, List.of());

        if (candidates.size() == 1) {
            String resolved = candidates.get(0);
            issues.add(new DecodeIssue(
                    "dictionary shifted: hint index " + hintIdx + " resolved to '" + resolved + "' via hash",
                    DecodeIssue.Severity.INFO));
            return resolved;
        }

        if (candidates.size() > 1) {
            String resolved = candidates.get(0);
            int bestDist = Integer.MAX_VALUE;
            for (String c : candidates) {
                int idx = dictionary.indexOf(c);
                int dist = idx >= 0 ? Math.abs(idx - hintIdx) : Integer.MAX_VALUE;
                if (dist < bestDist) {
                    bestDist = dist;
                    resolved = c;
                }
            }
            issues.add(new DecodeIssue(
                    "hash collision for hint " + hintIdx + ", resolved to '" + resolved + "' via hint proximity",
                    DecodeIssue.Severity.WARNING));
            return resolved;
        }

        issues.add(new DecodeIssue(
                "glyph (hint=" + hintIdx + ", hash=0x" + Integer.toHexString(storedHash)
                        + ") not found in current dictionary, removed from hex",
                DecodeIssue.Severity.ERROR));
        return null;
    }

    // --- scrubbing ---

    static void scrubRemoved(List<Glyph> glyphs, Set<Integer> removedIndices,
            List<DecodeIssue> issues) {
    }

    // --- v4 limit check ---

    @Nullable
    static String exceedsV4Limits(Hex hex) {
        return null;
    }

    // --- topological sort ---

    static List<Glyph> topoSort(Hex hex, List<Glyph> effects) {
        Map<String, Glyph> byId = new HashMap<>();
        for (Glyph g : effects) byId.put(g.getId(), g);

        List<Glyph> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        String firstId = hex.getFirstGlyphId();
        if (firstId != null && byId.containsKey(firstId)) {
            topoVisit(firstId, byId, visited, sorted);
        }
        for (Glyph g : effects) {
            if (!visited.contains(g.getId())) topoVisit(g.getId(), byId, visited, sorted);
        }
        return sorted;
    }

    private static void topoVisit(String id, Map<String, Glyph> byId,
            Set<String> visited, List<Glyph> sorted) {
    }

    static void setGlyphFields(Glyph g, String assetId) {
    }

    // --- prefixed string i/o ---

    static void writePrefixedString(BitWriter bw, String s) {
        int pfx;
        String suffix;
        if (s.startsWith("Glyph_")) {
            pfx = PFX_GLYPH;
            suffix = s.substring(6);
        } else if (s.startsWith("Number_")) {
            pfx = PFX_NUMBER;
            suffix = s.substring(7);
        } else {
            pfx = PFX_NONE;
            suffix = s;
        }
        byte[] bytes = suffix.getBytes(StandardCharsets.UTF_8);
        bw.write(pfx, 2);
        bw.writeVarInt(bytes.length);
        for (byte b : bytes) bw.write(b & 0xFF, 8);
    }

    static String readPrefixedString(BitReader br) {
        int pfx = br.read(2);
        int len = br.readVarInt();
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) bytes[i] = (byte) br.read(8);
        String suffix = new String(bytes, StandardCharsets.UTF_8);
        return switch (pfx) {
            case PFX_GLYPH -> "Glyph_" + suffix;
            case PFX_NUMBER -> "Number_" + suffix;
            default -> suffix;
        };
    }

    // --- byte-level prefixed string i/o (for v5) ---

    static void writePrefixedByte(DataOutputStream out, String s) throws IOException {
        int pfx;
        String suffix;
        if (s.startsWith("Glyph_")) {
            pfx = PFX_GLYPH;
            suffix = s.substring(6);
        } else if (s.startsWith("Number_")) {
            pfx = PFX_NUMBER;
            suffix = s.substring(7);
        } else {
            pfx = PFX_NONE;
            suffix = s;
        }
        byte[] bytes = suffix.getBytes(StandardCharsets.UTF_8);
        out.writeByte((pfx << 6) | (bytes.length & 0x3F));
        out.write(bytes);
    }

    static String readPrefixedByte(DataInputStream in) throws IOException {
        int header = in.readUnsignedByte();
        int pfx = (header >> 6) & 0x03;
        int len = header & 0x3F;
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        String suffix = new String(bytes, StandardCharsets.UTF_8);
        return switch (pfx) {
            case PFX_GLYPH -> "Glyph_" + suffix;
            case PFX_NUMBER -> "Number_" + suffix;
            default -> suffix;
        };
    }

    // --- math ---

    static int nbits(int maxVal) {
        if (maxVal <= 0) return 1;
        return Math.max(1, 32 - Integer.numberOfLeadingZeros(maxVal));
    }

    static int zigzag(int n) {
        return (n << 1) ^ (n >> 31);
    }

    static int unzigzag(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    // --- asset input keys ---

    static List<String> getAssetInputKeys(String glyphId) {
        return List.of();
    }

    static List<String> getAssetOutputKeys(String glyphId) {
        return List.of();
    }

    // --- palette builder ---

    static List<String> buildPalette(List<Glyph> ordered) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (Glyph g : ordered) seen.add(g.getGlyphId());
        return new ArrayList<>(seen);
    }

    // --- zlib helpers (shared by HexCodec / HexCodecV14) ---

    @Nullable
    static byte[] deflate(byte[] data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DeflaterOutputStream dos = new DeflaterOutputStream(baos,
                        new Deflater(Deflater.BEST_COMPRESSION))) {
            dos.write(data);
            dos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    static byte[] inflate(byte[] data) {
        try (InflaterInputStream iis = new InflaterInputStream(
                new ByteArrayInputStream(data))) {
            return iis.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }

    // --- v15 helpers ---

    static final int V15_HASH_BITS = 16;

    static int assetHashV15(String s) {
        int h = 0x811c9dc5;
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            h ^= (b & 0xFF);
            h *= 0x01000193;
        }
        return h & ((1 << V15_HASH_BITS) - 1);
    }

    static int registryFingerprint(List<String> dictionary) {
        int h = 0x811c9dc5;
        boolean first = true;
        for (String s : dictionary) {
            if (!first) {
                h ^= ('\n' & 0xFF);
                h *= 0x01000193;
            }
            for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
                h ^= (b & 0xFF);
                h *= 0x01000193;
            }
            first = false;
        }
        return h;
    }

    static Map<Integer, List<String>> buildHashLookupV15(List<String> dictionary) {
        Map<Integer, List<String>> lookup = new HashMap<>();
        for (String s : dictionary) {
            lookup.computeIfAbsent(assetHashV15(s), k -> new ArrayList<>()).add(s);
        }
        return lookup;
    }

    // bare-name pstr: no Glyph_/Number_ prefix optimization. names go on the
    // wire verbatim. names must be ≤256 bytes.
    static void writeBareString(BitWriter bw, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 255) {
            throw new IllegalArgumentException("pstr exceeds 256-byte cap: " + s);
        }
        bw.writeVarInt(bytes.length);
        for (byte b : bytes) bw.write(b & 0xFF, 8);
    }

    static String readBareString(BitReader br) {
        int len = br.readVarInt();
        if (len > 255) throw new IllegalStateException("pstr length " + len + " exceeds cap");
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) bytes[i] = (byte) br.read(8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static final int RAW_STRING_ESCAPE = 4095;
    static final int RAW_STRING_MAX = 0xFFFFFF;

    static void writeRawString(BitWriter bw, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > RAW_STRING_MAX) {
            throw new HexCodecException("raw string exceeds 24-bit cap: " + bytes.length);
        }
        if (bytes.length < RAW_STRING_ESCAPE) {
            bw.writeVarInt(bytes.length);
        } else {
            bw.writeVarInt(RAW_STRING_ESCAPE);
            bw.write(bytes.length, 24);
        }
        for (byte b : bytes) bw.write(b & 0xFF, 8);
    }

    static String readRawString(BitReader br) {
        int len = br.readVarInt();
        if (len == RAW_STRING_ESCAPE) {
            len = br.read(24);
        }
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) bytes[i] = (byte) br.read(8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // unsigned LEB128 byte-level varint, used by v15 for tlv section lengths.
    static void writeByteVarInt(ByteArrayOutputStream out, int v) {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v & 0x7F);
    }

    static int[] readByteVarInt(byte[] buf, int off) {
        int v = 0, shift = 0;
        while (true) {
            int b = buf[off++] & 0xFF;
            v |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return new int[] { v, off };
            shift += 7;
            if (shift > 35) throw new IllegalStateException("varint too long");
        }
    }

    static long crc32(byte[] data) {
        CRC32 c = new CRC32();
        c.update(data);
        return c.getValue();
    }
}