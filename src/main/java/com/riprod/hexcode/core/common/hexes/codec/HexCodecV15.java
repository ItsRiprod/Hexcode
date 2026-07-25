package com.riprod.hexcode.core.common.hexes.codec;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class HexCodecV15 {

    public static final int FORMAT_VERSION = 15;
    public static final String FRAME_PREFIX = "hx:15:";

    private static final int POS_SCALE = 20;
    private static final int MAX_POS_BITS = 31;
    private static final float ROT_EPS = 0.005f;
    private static final double PI = Math.PI;

    private static final byte[] MAGIC = { 'H', 'X' };
    private static final int FLAG_ZLIB = 0x01;
    private static final int FLAG_FINGERPRINT = 0x02;

    private static final int SECTION_HEADER = 0x01;
    private static final int SECTION_ASSET_PALETTE = 0x02;
    private static final int SECTION_SLOT_PALETTE = 0x03;
    private static final int SECTION_GLYPH_STREAM = 0x04;
    private static final int SECTION_EXTRAS = 0x05;
    static final int SECTION_SLOT_STATE = 0x06;
    static final int SECTION_META = 0x07;

    private static final int EXTRAS_KIND_ROTATION = 0x01;

    private HexCodecV15() {}

    public static String serialize(Hex hex) {
        return serialize(hex, true);
    }

    public static String serialize(Hex hex, boolean includeFingerprint) {
        Hex clone = hex.clone();
        HexUtils.validate(clone);
        HexUtils.compress(clone);
        byte[] body = encode(clone, includeFingerprint);
        return frame(body, includeFingerprint, MAGIC, FLAG_ZLIB, FLAG_FINGERPRINT, FRAME_PREFIX);
    }

    public static DecodeResult deserialize(String data) {
        if (data == null || !data.startsWith(FRAME_PREFIX)) {
            return DecodeResult.error("not a v15 frame");
        }
        byte[] body = unframe(data, FRAME_PREFIX, MAGIC, FLAG_ZLIB);
        if (body == null) return DecodeResult.error("frame parse failed");
        return decode(body);
    }

    // -- shared frame helpers (used by v15 and v16) --

    static String frame(byte[] body, boolean fp, byte[] magic, int flagZlib, int flagFp,
            String prefix) {
        long crc = CodecUtil.crc32(body);
        byte[] deflated = CodecUtil.deflate(body);
        boolean useZlib = deflated != null && deflated.length < body.length;
        byte[] payloadBody = useZlib ? deflated : body;
        int flags = (useZlib ? flagZlib : 0) | (fp ? flagFp : 0);
        ByteBuffer bb = ByteBuffer.allocate(magic.length + 1 + 4 + payloadBody.length);
        bb.put(magic);
        bb.put((byte) flags);
        bb.putInt((int) crc);
        bb.put(payloadBody);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    }

    @Nullable
    static byte[] unframe(String data, String prefix, byte[] magic, int flagZlib) {
        String b64 = data.substring(prefix.length());
        byte[] payload;
        try {
            payload = Base64.getUrlDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (payload.length < 7 || payload[0] != magic[0] || payload[1] != magic[1]) return null;
        int flags = payload[2] & 0xFF;
        long crcExpected = ByteBuffer.wrap(payload, 3, 4).getInt() & 0xFFFFFFFFL;
        byte[] body;
        if ((flags & flagZlib) != 0) {
            byte[] compressed = new byte[payload.length - 7];
            System.arraycopy(payload, 7, compressed, 0, compressed.length);
            body = CodecUtil.inflate(compressed);
            if (body == null) return null;
        } else {
            body = new byte[payload.length - 7];
            System.arraycopy(payload, 7, body, 0, body.length);
        }
        long crcActual = CodecUtil.crc32(body);
        if (crcActual != crcExpected) return null;
        return body;
    }

    // -- encode --

    private static byte[] encode(Hex hex, boolean includeFingerprint) {
        List<Glyph> glyphs = canonicalOrder(hex);
        if (glyphs.isEmpty()) {
            throw new HexCodecException("cannot encode empty hex");
        }

        int ne = glyphs.size();
        int refBits = Math.max(1, CodecUtil.nbits(ne - 1));

        Map<String, Integer> idToIdx = new HashMap<>(ne * 2);
        for (int i = 0; i < ne; i++) idToIdx.put(glyphs.get(i).getId(), i);

        List<String> assetPalette = buildAssetPalette(glyphs);
        Map<String, Integer> palMap = indexMap(assetPalette);
        int palBits = Math.max(1, CodecUtil.nbits(assetPalette.size() - 1));

        List<String> slotPalette = buildSlotPalette(glyphs);
        Map<String, Integer> slotIdxMap = indexMap(slotPalette);
        int spBits = Math.max(1, CodecUtil.nbits(slotPalette.size() - 1));

        int[] accVals = new int[ne];
        int accMin = Integer.MAX_VALUE, accMax = Integer.MIN_VALUE;
        for (int i = 0; i < ne; i++) {
            int q = Math.round(clamp01(glyphs.get(i).getVolatility()) * 100f);
            accVals[i] = q;
            if (q < accMin) accMin = q;
            if (q > accMax) accMax = q;
        }
        int accBits = CodecUtil.nbits(accMax - accMin);

        int[] speedVals = new int[ne];
        Map<Integer, Integer> speedCounts = new HashMap<>();
        for (int i = 0; i < ne; i++) {
            int q = Math.round(clamp01(glyphs.get(i).getEfficiency()) * 100f);
            speedVals[i] = q;
            speedCounts.merge(q, 1, Integer::sum);
        }
        int defaultSpeed = 0, bestCount = -1;
        for (Map.Entry<Integer, Integer> e : speedCounts.entrySet()) {
            if (e.getValue() > bestCount) { bestCount = e.getValue(); defaultSpeed = e.getKey(); }
        }

        int[] xq = new int[ne], yq = new int[ne], zq = new int[ne];
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int i = 0; i < ne; i++) {
            Vector3f p = glyphs.get(i).getPosition();
            xq[i] = Math.round(p.x * POS_SCALE);
            yq[i] = Math.round(p.y * POS_SCALE);
            zq[i] = Math.round(p.z * POS_SCALE);
            if (xq[i] < minX) minX = xq[i]; if (xq[i] > maxX) maxX = xq[i];
            if (yq[i] < minY) minY = yq[i]; if (yq[i] > maxY) maxY = yq[i];
            if (zq[i] < minZ) minZ = zq[i]; if (zq[i] > maxZ) maxZ = zq[i];
        }
        int bitsX = CodecUtil.nbits(maxX - minX);
        int bitsY = CodecUtil.nbits(maxY - minY);
        int bitsZ = CodecUtil.nbits(maxZ - minZ);
        if (bitsX > MAX_POS_BITS || bitsY > MAX_POS_BITS || bitsZ > MAX_POS_BITS) {
            throw new HexCodecException("position span exceeds " + MAX_POS_BITS
                    + "-bit width: bitsX=" + bitsX + " bitsY=" + bitsY + " bitsZ=" + bitsZ);
        }

        byte[] header = encodeHeader(includeFingerprint, ne, accMin, accBits, defaultSpeed,
                minX, minY, minZ, bitsX, bitsY, bitsZ);
        byte[] assetPaletteBytes = encodeAssetPalette(assetPalette);
        byte[] slotPaletteBytes = encodeSlotPalette(slotPalette);
        byte[] glyphStreamBytes = encodeGlyphStream(glyphs, palMap, palBits, accVals, accMin,
                accBits, speedVals, defaultSpeed, xq, yq, zq, minX, minY, minZ, bitsX, bitsY, bitsZ,
                slotIdxMap, spBits, idToIdx, refBits);
        byte[] extrasBytes = encodeRotationExtras(glyphs);
        byte[] slotStateBytes = encodeSlotStates(glyphs);
        byte[] metaBytes = encodeMeta(hex);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        int sectionCount = 4 + (extrasBytes != null ? 1 : 0) + (slotStateBytes != null ? 1 : 0)
                + (metaBytes != null ? 1 : 0);
        CodecUtil.writeByteVarInt(body, sectionCount);
        appendSection(body, SECTION_HEADER, header);
        appendSection(body, SECTION_ASSET_PALETTE, assetPaletteBytes);
        appendSection(body, SECTION_SLOT_PALETTE, slotPaletteBytes);
        appendSection(body, SECTION_GLYPH_STREAM, glyphStreamBytes);
        if (extrasBytes != null) appendSection(body, SECTION_EXTRAS, extrasBytes);
        if (slotStateBytes != null) appendSection(body, SECTION_SLOT_STATE, slotStateBytes);
        if (metaBytes != null) appendSection(body, SECTION_META, metaBytes);
        return body.toByteArray();
    }

    private static byte[] encodeHeader(boolean fp, int ne, int accMin, int accBits,
            int defaultSpeed, int minX, int minY, int minZ, int bitsX, int bitsY, int bitsZ) {
        BitWriter bw = new BitWriter();
        bw.write(fp ? 1 : 0, 1);
        if (fp) {
            int fingerprint = CodecUtil.registryFingerprint(buildBareGlyphDict());
            for (int i = 31; i >= 0; i--) bw.write((fingerprint >> i) & 1, 1);
        }
        bw.writeVarInt(ne);
        bw.write(accMin, 7);
        bw.write(accBits, 4);
        bw.write(defaultSpeed, 7);
        bw.writeVarInt(CodecUtil.zigzag(minX));
        bw.writeVarInt(CodecUtil.zigzag(minY));
        bw.writeVarInt(CodecUtil.zigzag(minZ));
        bw.write(bitsX, 5);
        bw.write(bitsY, 5);
        bw.write(bitsZ, 5);
        return bw.flush();
    }

    static byte[] encodeAssetPalette(List<String> assetPalette) {
        BitWriter bw = new BitWriter();
        bw.writeVarInt(assetPalette.size());
        for (String a : assetPalette) {
            CodecUtil.writeBareString(bw, a);
            bw.write(Math.min(getOrderedSlotKeys(a).size(), 15), 4);
        }
        return bw.flush();
    }

    static byte[] encodeSlotPalette(List<String> slotPalette) {
        BitWriter bw = new BitWriter();
        bw.writeVarInt(slotPalette.size());
        for (String s : slotPalette) CodecUtil.writeBareString(bw, s);
        return bw.flush();
    }

    private static byte[] encodeGlyphStream(List<Glyph> glyphs, Map<String, Integer> palMap, int palBits,
            int[] accVals, int accMin, int accBits, int[] speedVals, int defaultSpeed,
            int[] xq, int[] yq, int[] zq, int minX, int minY, int minZ,
            int bitsX, int bitsY, int bitsZ,
            Map<String, Integer> slotIdxMap, int spBits,
            Map<String, Integer> idToIdx, int refBits) {
        BitWriter bw = new BitWriter();
        bw.writeVarInt(glyphs.size());
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph g = glyphs.get(i);
            bw.write(palMap.get(g.getGlyphId()), palBits);
            bw.write(accVals[i] - accMin, accBits);
            if (speedVals[i] == defaultSpeed) {
                bw.write(1, 1);
            } else {
                bw.write(0, 1);
                bw.write(speedVals[i], 7);
            }
            bw.write(xq[i] - minX, bitsX);
            bw.write(yq[i] - minY, bitsY);
            bw.write(zq[i] - minZ, bitsZ);
            writeSlots(bw, g, slotIdxMap, spBits, idToIdx, refBits);
        }
        return bw.flush();
    }

    static void writeSlots(BitWriter bw, Glyph g, Map<String, Integer> slotIdxMap, int spBits,
            Map<String, Integer> idToIdx, int refBits) {
        Map<String, Slot> slots = g.getSlots();
        if (slots == null || slots.isEmpty()) { bw.write(0, 1); return; }
        bw.write(1, 1);
        if (schemaMatches(g.getGlyphId(), slots)) {
            bw.write(1, 1);
            boolean allEmpty = allSlotsEmpty(slots);
            bw.write(allEmpty ? 1 : 0, 1);
            if (!allEmpty) {
                for (String name : getOrderedSlotKeys(g.getGlyphId())) {
                    Slot slot = slots.get(name);
                    String[] links = slot != null ? slot.getLinks() : new String[0];
                    writeLinks(bw, links, idToIdx, refBits);
                }
            }
        } else {
            bw.write(0, 1);
            bw.writeVarInt(slots.size());
            for (Map.Entry<String, Slot> entry : slots.entrySet()) {
                bw.write(slotIdxMap.get(entry.getKey()), spBits);
                writeLinks(bw, entry.getValue().getLinks(), idToIdx, refBits);
            }
        }
    }

    @Nullable
    private static byte[] encodeRotationExtras(List<Glyph> glyphs) {
        BitWriter bw = new BitWriter();
        bw.write(EXTRAS_KIND_ROTATION, 8);
        boolean any = false;
        for (Glyph g : glyphs) {
            Rotation3f r = g.getRotation();
            boolean meaningful = r != null
                    && (Math.abs(r.x) > ROT_EPS || Math.abs(r.y) > ROT_EPS || Math.abs(r.z) > ROT_EPS);
            if (meaningful) {
                any = true;
                bw.write(1, 1);
                bw.write(quantRotByte(r.x) & 0xFF, 8);
                bw.write(quantRotByte(r.y) & 0xFF, 8);
                bw.write(quantRotByte(r.z) & 0xFF, 8);
            } else {
                bw.write(0, 1);
            }
        }
        return any ? bw.flush() : null;
    }

    private static int quantRotByte(double theta) {
        while (theta >= PI) theta -= 2 * PI;
        while (theta < -PI) theta += 2 * PI;
        int q = (int) Math.round(theta / PI * 128);
        if (q > 127) q = 127;
        if (q < -128) q = -128;
        return q;
    }

    private static double unquantRotByte(int b) {
        if (b >= 128) b -= 256;
        return b / 128.0 * PI;
    }

    @Nullable
    static byte[] encodeSlotStates(List<Glyph> glyphs) {
        List<Integer> glyphIndices = new ArrayList<>();
        List<List<Integer>> slotSchemaIndices = new ArrayList<>();
        List<List<byte[]>> slotStates = new ArrayList<>();
        for (int i = 0; i < glyphs.size(); i++) {
            Glyph g = glyphs.get(i);
            List<String> schema = getOrderedSlotKeys(g.getGlyphId());
            List<Integer> idxs = null;
            List<byte[]> states = null;
            for (Map.Entry<String, Slot> e : g.getSlots().entrySet()) {
                byte[] state = e.getValue().encodeState();
                if (state == null) continue;
                int schemaIdx = schema.indexOf(e.getKey());
                if (schemaIdx < 0) continue;
                if (idxs == null) { idxs = new ArrayList<>(); states = new ArrayList<>(); }
                idxs.add(schemaIdx);
                states.add(state);
            }
            if (idxs != null) {
                glyphIndices.add(i);
                slotSchemaIndices.add(idxs);
                slotStates.add(states);
            }
        }
        if (glyphIndices.isEmpty()) return null;

        BitWriter bw = new BitWriter();
        bw.writeVarInt(glyphIndices.size());
        for (int gi = 0; gi < glyphIndices.size(); gi++) {
            bw.writeVarInt(glyphIndices.get(gi));
            List<Integer> idxs = slotSchemaIndices.get(gi);
            List<byte[]> states = slotStates.get(gi);
            bw.writeVarInt(idxs.size());
            for (int s = 0; s < idxs.size(); s++) {
                bw.writeVarInt(idxs.get(s));
                byte[] state = states.get(s);
                bw.writeVarInt(state.length);
                for (byte b : state) bw.write(b & 0xFF, 8);
            }
        }
        return bw.flush();
    }

    static void decodeSlotStates(byte[] data, List<String> placeholderIds, Hex hex) {
        BitReader br = new BitReader(data);
        int glyphCount = br.readVarInt();
        for (int gi = 0; gi < glyphCount; gi++) {
            int glyphIdx = br.readVarInt();
            int slotCount = br.readVarInt();
            Glyph g = glyphIdx < placeholderIds.size() ? hex.get(placeholderIds.get(glyphIdx)) : null;
            List<String> schema = g != null ? getOrderedSlotKeys(g.getGlyphId()) : List.of();
            for (int s = 0; s < slotCount; s++) {
                int schemaIdx = br.readVarInt();
                int len = br.readVarInt();
                byte[] state = new byte[len];
                for (int k = 0; k < len; k++) state[k] = (byte) br.read(8);
                if (g != null && schemaIdx < schema.size()) {
                    Slot slot = g.getSlots().get(schema.get(schemaIdx));
                    if (slot != null) slot.decodeState(state);
                }
            }
        }
    }

    @Nullable
    static byte[] encodeMeta(Hex hex) {
        String name = hex.getDisplayName();
        if (name == null || name.isBlank()) return null;
        BitWriter bw = new BitWriter();
        // writeBareString throws past the 255-byte pstr cap, and a name can arrive from an
        // imported string or legacy document that never passed the page's length limit
        CodecUtil.writeBareString(bw, clampToPstr(name));
        return bw.flush();
    }

    private static String clampToPstr(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 255) return s;
        int end = 255;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) end--;
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    static void decodeMeta(byte[] data, Hex hex) {
        BitReader br = new BitReader(data);
        String name = CodecUtil.readBareString(br);
        if (name != null && !name.isBlank()) hex.setDisplayName(name);
    }

    static void writeLinks(BitWriter bw, String[] links, Map<String, Integer> idToIdx, int refBits) {
        if (links == null || links.length == 0) { bw.write(0, 1); return; }
        bw.write(1, 1);
        if (links.length == 1) {
            bw.write(0, 1);
            Integer idx = idToIdx.get(links[0]);
            bw.write(idx != null ? idx : 0, refBits);
        } else {
            bw.write(1, 1);
            bw.writeVarInt(links.length);
            for (String link : links) {
                Integer idx = idToIdx.get(link);
                bw.write(idx != null ? idx : 0, refBits);
            }
        }
    }

    static void appendSection(ByteArrayOutputStream body, int sid, byte[] sbytes) {
        body.write(sid);
        CodecUtil.writeByteVarInt(body, sbytes.length);
        body.write(sbytes, 0, sbytes.length);
    }

    // -- decode --

    private static DecodeResult decode(byte[] body) {
        List<DecodeIssue> issues = new ArrayList<>();
        try {
            return decodeInner(body, issues);
        } catch (Exception e) {
            issues.add(new DecodeIssue("decode failed: " + e.getClass().getSimpleName()
                    + ": " + e.getMessage(), DecodeIssue.Severity.ERROR));
            return new DecodeResult(null, issues);
        }
    }

    private static DecodeResult decodeInner(byte[] body, List<DecodeIssue> issues) {
        Map<Integer, byte[]> sections = splitSections(body, issues);
        if (sections.isEmpty() || !sections.containsKey(SECTION_HEADER)) {
            issues.add(new DecodeIssue("missing header section", DecodeIssue.Severity.ERROR));
            return new DecodeResult(null, issues);
        }
        if (!sections.containsKey(SECTION_GLYPH_STREAM)) {
            issues.add(new DecodeIssue("missing glyph stream section", DecodeIssue.Severity.ERROR));
            return new DecodeResult(null, issues);
        }

        BitReader hdr = new BitReader(sections.get(SECTION_HEADER));
        boolean hasFp = hdr.read(1) == 1;
        Integer fingerprint = null;
        if (hasFp) {
            int fp = 0;
            for (int i = 0; i < 32; i++) fp = (fp << 1) | hdr.read(1);
            fingerprint = fp;
        }
        int ne = hdr.readVarInt();
        int accMin = hdr.read(7);
        int accBits = hdr.read(4);
        int defaultSpeed = hdr.read(7);
        int minX = CodecUtil.unzigzag(hdr.readVarInt());
        int minY = CodecUtil.unzigzag(hdr.readVarInt());
        int minZ = CodecUtil.unzigzag(hdr.readVarInt());
        int bitsX = hdr.read(5);
        int bitsY = hdr.read(5);
        int bitsZ = hdr.read(5);

        if (fingerprint != null) {
            int local = CodecUtil.registryFingerprint(buildBareGlyphDict());
            if (fingerprint != local) {
                issues.add(new DecodeIssue(String.format(
                        "registry fingerprint mismatch (encoded=0x%08x, local=0x%08x)",
                        fingerprint, local), DecodeIssue.Severity.INFO));
            }
        }

        List<String> assetPalette = new ArrayList<>();
        List<Integer> palStoredSlotCounts = new ArrayList<>();
        if (sections.containsKey(SECTION_ASSET_PALETTE)) {
            BitReader br = new BitReader(sections.get(SECTION_ASSET_PALETTE));
            int n = br.readVarInt();
            List<String> dict = buildBareGlyphDict();
            for (int i = 0; i < n; i++) {
                String name = CodecUtil.readBareString(br);
                if (!dict.contains(name)) {
                    issues.add(new DecodeIssue("asset '" + name
                            + "' not in current registry; preserved as-is",
                            DecodeIssue.Severity.WARNING));
                }
                assetPalette.add(name);
                palStoredSlotCounts.add(br.read(4));
            }
        }
        int palBits = Math.max(1, CodecUtil.nbits(assetPalette.size() - 1));

        List<String> slotPalette = new ArrayList<>();
        if (sections.containsKey(SECTION_SLOT_PALETTE)) {
            BitReader br = new BitReader(sections.get(SECTION_SLOT_PALETTE));
            int n = br.readVarInt();
            for (int i = 0; i < n; i++) slotPalette.add(CodecUtil.readBareString(br));
        }
        int spBits = Math.max(1, CodecUtil.nbits(slotPalette.size() - 1));

        BitReader gs = new BitReader(sections.get(SECTION_GLYPH_STREAM));
        int gsCount = gs.readVarInt();
        if (gsCount != ne) {
            issues.add(new DecodeIssue(
                    "glyph count mismatch: header " + ne + " vs stream " + gsCount,
                    DecodeIssue.Severity.WARNING));
        }
        int n = Math.min(gsCount, ne);
        int refBits = Math.max(1, CodecUtil.nbits(ne - 1));

        Hex hex = new Hex();
        List<String> placeholderIds = new ArrayList<>(n);
        boolean anyUnresolved = false;
        for (int i = 0; i < n; i++) {
            int palIdx = gs.read(palBits);
            String asset = palIdx < assetPalette.size() ? assetPalette.get(palIdx) : null;
            int storedSlotCount = palIdx < palStoredSlotCounts.size() ? palStoredSlotCounts.get(palIdx) : 0;

            int accQ = accMin + gs.read(accBits);
            float volatility = accQ / 100f;

            boolean speedDefault = gs.read(1) == 1;
            int speedQ = speedDefault ? defaultSpeed : gs.read(7);
            float efficiency = speedQ / 100f;

            int x = minX + gs.read(bitsX);
            int y = minY + gs.read(bitsY);
            int z = minZ + gs.read(bitsZ);
            Vector3f position = new Vector3f(x / (float) POS_SCALE, y / (float) POS_SCALE,
                    z / (float) POS_SCALE);

            Map<String, Slot> decodedSlots = readSlots(gs, asset, storedSlotCount, slotPalette,
                    spBits, refBits);

            Glyph g = new Glyph();
            g.setVolatility(volatility);
            g.setEfficiency(efficiency);
            g.setPosition(position);
            g.setRotation(new Rotation3f());
            g.getSlots().clear();
            g.getSlots().putAll(decodedSlots);

            String placeholderId = "tmp-" + i;
            placeholderIds.add(placeholderId);
            g.setId(placeholderId);
            if (asset == null) { anyUnresolved = true; g.setGlyphId(""); }
            else g.setGlyphId(asset);
            hex.put(placeholderId, g);
        }

        resolveLinks(hex, placeholderIds);
        if (!placeholderIds.isEmpty()) hex.setFirstGlyphId(placeholderIds.get(0));

        // rotation extras
        if (sections.containsKey(SECTION_EXTRAS)) {
            try {
                BitReader br = new BitReader(sections.get(SECTION_EXTRAS));
                int kind = br.read(8);
                if (kind == EXTRAS_KIND_ROTATION) {
                    for (int i = 0; i < n; i++) {
                        if (br.read(1) == 1) {
                            double rx = unquantRotByte(br.read(8));
                            double ry = unquantRotByte(br.read(8));
                            double rz = unquantRotByte(br.read(8));
                            Glyph g = hex.get(placeholderIds.get(i));
                            if (g != null) g.setRotation(new Rotation3f((float) rx, (float) ry, (float) rz));
                        }
                    }
                } else {
                    issues.add(new DecodeIssue("extras kind 0x" + Integer.toHexString(kind)
                            + " unknown; rotation skipped", DecodeIssue.Severity.INFO));
                }
            } catch (Exception e) {
                issues.add(new DecodeIssue("extras failed: " + e.getMessage()
                        + "; rotations default to zero", DecodeIssue.Severity.INFO));
            }
        }

        if (sections.containsKey(SECTION_SLOT_STATE)) {
            try {
                decodeSlotStates(sections.get(SECTION_SLOT_STATE), placeholderIds, hex);
            } catch (Exception e) {
                issues.add(new DecodeIssue("slot state failed: " + e.getMessage()
                        + "; slot toggles default", DecodeIssue.Severity.INFO));
            }
        }

        if (sections.containsKey(SECTION_META)) {
            try {
                decodeMeta(sections.get(SECTION_META), hex);
            } catch (Exception e) {
                issues.add(new DecodeIssue("meta failed: " + e.getMessage()
                        + "; hex stays unnamed", DecodeIssue.Severity.INFO));
            }
        }

        finalizeHex(hex, anyUnresolved, issues);
        HexUtils.compress(hex);
        return new DecodeResult(hex, issues);
    }

    static Map<String, Slot> readSlots(BitReader br, String asset, int storedSlotCount,
            List<String> slotPalette, int spBits, int refBits) {
        Map<String, Slot> out = new LinkedHashMap<>();
        if (br.read(1) == 0) return out;
        int schemaMatch = br.read(1);
        if (schemaMatch == 1) {
            List<String> schema = asset != null ? getOrderedSlotKeys(asset) : List.of();
            int allEmpty = br.read(1);
            for (int k = 0; k < storedSlotCount; k++) {
                String[] links = allEmpty == 1 ? new String[0] : readLinkPlaceholders(br, refBits);
                String name = k < schema.size() ? schema.get(k) : "<slot_" + k + ">";
                Slot slot = createSlot(asset, name);
                if (slot != null) {
                    slot.setLinks(links);
                    out.put(name, slot);
                }
            }
            for (int k = storedSlotCount; k < schema.size(); k++) {
                String name = schema.get(k);
                Slot slot = createSlot(asset, name);
                if (slot != null) {
                    slot.setLinks(new String[0]);
                    out.put(name, slot);
                }
            }
        } else {
            int entryCount = br.readVarInt();
            for (int k = 0; k < entryCount; k++) {
                int sIdx = br.read(spBits);
                String name = sIdx < slotPalette.size() ? slotPalette.get(sIdx) : null;
                if (name == null) name = "<unresolved_" + sIdx + ">";
                String[] links = readLinkPlaceholders(br, refBits);
                Slot slot = createSlot(asset, name);
                if (slot != null) {
                    slot.setLinks(links);
                    out.put(name, slot);
                }
            }
        }
        return out;
    }

    static Slot createSlot(String glyphId, String slotKey) {
        GlyphAsset glyphAsset = GlyphAsset.getAssetMap().getAsset(glyphId);
        SlotConfig config = glyphAsset != null ? glyphAsset.getSlot(slotKey) : null;
        return config != null ? config.create() : null;
    }

    static String[] readLinkPlaceholders(BitReader br, int refBits) {
        if (br.read(1) == 0) return new String[0];
        boolean multi = br.read(1) == 1;
        if (!multi) return new String[] { Integer.toString(br.read(refBits)) };
        int count = br.readVarInt();
        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = Integer.toString(br.read(refBits));
        return out;
    }

    static void resolveLinks(Hex hex, List<String> placeholderIds) {
        for (Glyph g : hex.getGlyphs()) {
            for (Slot slot : g.getSlots().values()) {
                String[] placeholders = slot.getLinks();
                String[] resolved = new String[placeholders.length];
                for (int k = 0; k < placeholders.length; k++) {
                    int idx = Integer.parseInt(placeholders[k]);
                    resolved[k] = (idx >= 0 && idx < placeholderIds.size())
                            ? placeholderIds.get(idx) : null;
                }
                List<String> kept = new ArrayList<>(resolved.length);
                for (String s : resolved) if (s != null) kept.add(s);
                slot.setLinks(kept.toArray(String[]::new));
            }
        }
    }

    static void finalizeHex(Hex hex, boolean anyUnresolved, List<DecodeIssue> issues) {
        if (anyUnresolved) {
            List<String> toDrop = new ArrayList<>();
            for (Glyph g : hex.getGlyphs()) {
                if (g.getGlyphId() == null || g.getGlyphId().isEmpty()) toDrop.add(g.getId());
            }
            for (String id : toDrop) {
                issues.add(new DecodeIssue("dropping glyph " + id + " (asset unresolved)",
                        DecodeIssue.Severity.WARNING));
                hex.removeGlyph(id);
            }
            if (hex.getGlyphs().isEmpty()) {
                issues.add(new DecodeIssue("all glyphs were removed", DecodeIssue.Severity.ERROR));
                return;
            }
            if (hex.getFirstGlyphId() == null || hex.get(hex.getFirstGlyphId()) == null) {
                hex.setFirstGlyphId(hex.getGlyphs().get(0).getId());
                issues.add(new DecodeIssue("first glyph was removed; reassigned",
                        DecodeIssue.Severity.WARNING));
            }
        }
    }

    static Map<Integer, byte[]> splitSections(byte[] body, List<DecodeIssue> issues) {
        Map<Integer, byte[]> out = new LinkedHashMap<>();
        int[] r = CodecUtil.readByteVarInt(body, 0);
        int sectionCount = r[0]; int off = r[1];
        for (int i = 0; i < sectionCount; i++) {
            if (off >= body.length) {
                issues.add(new DecodeIssue("truncated section list", DecodeIssue.Severity.ERROR));
                return Map.of();
            }
            int sid = body[off++] & 0xFF;
            int[] lr = CodecUtil.readByteVarInt(body, off);
            int slen = lr[0]; off = lr[1];
            if (slen > 65536 || off + slen > body.length) {
                issues.add(new DecodeIssue("section 0x" + Integer.toHexString(sid)
                        + " bad length " + slen, DecodeIssue.Severity.ERROR));
                return Map.of();
            }
            byte[] sbytes = new byte[slen];
            System.arraycopy(body, off, sbytes, 0, slen);
            out.put(sid, sbytes);
            off += slen;
        }
        return out;
    }

    // -- shared helpers --

    static List<String> buildAssetPalette(List<Glyph> glyphs) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        for (Glyph g : glyphs) if (seen.add(g.getGlyphId())) out.add(g.getGlyphId());
        return out;
    }

    static List<String> buildSlotPalette(List<Glyph> glyphs) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        for (Glyph g : glyphs) {
            Map<String, Slot> slots = g.getSlots();
            if (slots == null || slots.isEmpty()) continue;
            if (schemaMatches(g.getGlyphId(), slots)) continue;
            for (String name : slots.keySet()) if (seen.add(name)) out.add(name);
        }
        return out;
    }

    static List<String> buildBareGlyphDict() {
        List<String> dict = new ArrayList<>(GlyphAsset.getAssetMap().getAssetMap().keySet());
        Collections.sort(dict);
        return Collections.unmodifiableList(dict);
    }

    static List<String> buildBareSlotDict() {
        TreeSet<String> all = new TreeSet<>();
        for (GlyphAsset asset : GlyphAsset.getAssetMap().getAssetMap().values()) {
            all.addAll(asset.getSlotKeys());
        }
        return List.copyOf(all);
    }

    static List<String> getOrderedSlotKeys(String glyphId) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (asset == null) return List.of();
        return new ArrayList<>(asset.getSlotKeys());
    }

    static boolean schemaMatches(String glyphId, Map<String, Slot> slots) {
        List<String> schema = getOrderedSlotKeys(glyphId);
        if (schema.isEmpty()) return false;
        if (schema.size() != slots.size()) return false;
        int i = 0;
        for (String key : slots.keySet()) {
            if (!schema.get(i).equals(key)) return false;
            i++;
        }
        return true;
    }

    static boolean allSlotsEmpty(Map<String, Slot> slots) {
        for (Slot slot : slots.values()) if (slot.getLinks().length > 0) return false;
        return true;
    }

    static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }

    static Map<String, Integer> indexMap(List<String> dict) {
        Map<String, Integer> m = new HashMap<>(dict.size() * 2);
        for (int i = 0; i < dict.size(); i++) m.put(dict.get(i), i);
        return m;
    }

    static List<Glyph> canonicalOrder(Hex hex) {
        List<Glyph> glyphs = hex.getGlyphs();
        glyphs.sort(Comparator.comparing(Glyph::getId));
        String firstId = hex.getFirstGlyphId();
        if (firstId == null) return glyphs;
        for (int i = 0; i < glyphs.size(); i++) {
            if (firstId.equals(glyphs.get(i).getId())) {
                if (i != 0) {
                    Glyph first = glyphs.remove(i);
                    glyphs.add(0, first);
                }
                break;
            }
        }
        return glyphs;
    }
}
