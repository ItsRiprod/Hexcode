package com.riprod.hexcode.core.common.hexes.codec;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class HexCodecV14 {

    public static final int FORMAT_VERSION = 14;
    public static final String FRAME_PREFIX = "hx:14:";

    private static final int HASH_BITS = 11;
    private static final int POS_SCALE = 20;
    private static final int MAX_POS_BITS = 15;

    private HexCodecV14() {
    }

    @Nullable
    public static byte[] encode(Hex hex) {
        List<Glyph> glyphs = canonicalOrder(hex);
        if (glyphs.isEmpty()) return null;

        int ne = glyphs.size();
        int refBits = Math.max(1, CodecUtil.nbits(ne - 1));

        Map<String, Integer> idToIdx = new HashMap<>(ne * 2);
        for (int i = 0; i < ne; i++) idToIdx.put(glyphs.get(i).getId(), i);

        List<String> globalGlyphDict = CodecUtil.buildDictionary();
        List<String> globalSlotDict = CodecUtil.buildSlotDictionary();
        int encoderGlyphHintBits = Math.max(1, CodecUtil.nbits(globalGlyphDict.size() - 1));
        int encoderSlotHintBits = Math.max(1, CodecUtil.nbits(globalSlotDict.size() - 1));

        // asset palette (insertion-order dedupe)
        List<String> assetPalette = new ArrayList<>();
        LinkedHashSet<String> seenAssets = new LinkedHashSet<>();
        for (Glyph g : glyphs) {
            if (seenAssets.add(g.getGlyphId())) assetPalette.add(g.getGlyphId());
        }
        Map<String, Integer> palMap = new HashMap<>(assetPalette.size() * 2);
        for (int i = 0; i < assetPalette.size(); i++) palMap.put(assetPalette.get(i), i);
        int palBits = Math.max(1, CodecUtil.nbits(assetPalette.size() - 1));

        // slot palette fallback (only glyphs whose slot keys != registry schema)
        List<String> slotPalette = new ArrayList<>();
        LinkedHashSet<String> seenSlots = new LinkedHashSet<>();
        for (Glyph g : glyphs) {
            Map<String, Slot> slots = g.getSlots();
            if (slots == null || slots.isEmpty()) continue;
            if (schemaMatches(g.getGlyphId(), slots)) continue;
            for (String name : slots.keySet()) {
                if (seenSlots.add(name)) slotPalette.add(name);
            }
        }
        Map<String, Integer> slotIdxMap = new HashMap<>(slotPalette.size() * 2);
        for (int i = 0; i < slotPalette.size(); i++) slotIdxMap.put(slotPalette.get(i), i);
        int spBits = Math.max(1, CodecUtil.nbits(slotPalette.size() - 1));

        // accuracy delta
        int[] accVals = new int[ne];
        int accMin = Integer.MAX_VALUE;
        int accMax = Integer.MIN_VALUE;
        for (int i = 0; i < ne; i++) {
            int q = Math.round(clamp01(glyphs.get(i).getVolatility()) * 100f);
            accVals[i] = q;
            if (q < accMin) accMin = q;
            if (q > accMax) accMax = q;
        }
        int accBits = CodecUtil.nbits(accMax - accMin);
        if (accBits > 7) accBits = 7;

        // speed default-flag
        int[] speedVals = new int[ne];
        Map<Integer, Integer> speedCounts = new HashMap<>();
        for (int i = 0; i < ne; i++) {
            int q = Math.round(clamp01(glyphs.get(i).getEfficiency()) * 100f);
            speedVals[i] = q;
            speedCounts.merge(q, 1, Integer::sum);
        }
        int defaultSpeed = 0;
        int bestCount = -1;
        for (Map.Entry<Integer, Integer> e : speedCounts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                defaultSpeed = e.getKey();
            }
        }

        // positions at 0.05 scale
        int[] xq = new int[ne];
        int[] yq = new int[ne];
        int[] zq = new int[ne];
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
            return null; // position span exceeds 4-bit width field; out of spec
        }

        BitWriter bw = new BitWriter();
        bw.write(FORMAT_VERSION, 4);
        bw.write(encoderGlyphHintBits, 5);
        bw.write(encoderSlotHintBits, 5);
        bw.writeVarInt(ne);
        bw.write(accMin, 7);
        bw.write(accBits, 4);
        bw.write(defaultSpeed, 7);
        bw.writeVarInt(CodecUtil.zigzag(minX));
        bw.writeVarInt(CodecUtil.zigzag(minY));
        bw.writeVarInt(CodecUtil.zigzag(minZ));
        bw.write(bitsX, 4);
        bw.write(bitsY, 4);
        bw.write(bitsZ, 4);

        // asset palette
        bw.writeVarInt(assetPalette.size());
        Map<String, Integer> glyphDictIdx = indexMap(globalGlyphDict);
        for (String a : assetPalette) {
            Integer hint = glyphDictIdx.get(a);
            if (hint != null) {
                bw.write(0, 1);
                bw.write(hint, encoderGlyphHintBits);
                bw.write(CodecUtil.assetHash(a), HASH_BITS);
            } else {
                bw.write(1, 1);
                CodecUtil.writePrefixedString(bw, a);
            }
            int schemaSlotCount = CodecUtil.getOrderedSlotKeys(a).size();
            bw.write(Math.min(schemaSlotCount, 15), 4);
        }

        // slot palette fallback
        bw.writeVarInt(slotPalette.size());
        Map<String, Integer> slotDictIdx = indexMap(globalSlotDict);
        for (String s : slotPalette) {
            Integer hint = slotDictIdx.get(s);
            if (hint != null) {
                bw.write(0, 1);
                bw.write(hint, encoderSlotHintBits);
                bw.write(CodecUtil.assetHash(s), HASH_BITS);
            } else {
                bw.write(1, 1);
                CodecUtil.writePrefixedString(bw, s);
            }
        }

        // per glyph
        for (int i = 0; i < ne; i++) {
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

            Map<String, Slot> slots = g.getSlots();
            if (slots == null || slots.isEmpty()) {
                bw.write(0, 1);
                continue;
            }
            bw.write(1, 1);

            if (schemaMatches(g.getGlyphId(), slots)) {
                bw.write(1, 1);
                boolean allEmpty = allSlotsEmpty(slots);
                bw.write(allEmpty ? 1 : 0, 1);
                if (!allEmpty) {
                    // iterate in the registry's slot order (LinkedHashMap of GlyphAsset)
                    for (String name : CodecUtil.getOrderedSlotKeys(g.getGlyphId())) {
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

        return bw.flush();
    }

    public static DecodeResult decode(byte[] data) {
        List<DecodeIssue> issues = new ArrayList<>();
        if (data == null || data.length == 0) {
            issues.add(new DecodeIssue("empty data", DecodeIssue.Severity.ERROR));
            return new DecodeResult(null, issues);
        }
        try {
            return decodeInner(data, issues);
        } catch (Exception e) {
            issues.add(new DecodeIssue("decode failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    DecodeIssue.Severity.ERROR));
            return new DecodeResult(null, issues);
        }
    }

    private static DecodeResult decodeInner(byte[] data, List<DecodeIssue> issues) {
        BitReader br = new BitReader(data);
        int version = br.read(4);
        if (version != FORMAT_VERSION) {
            issues.add(new DecodeIssue("unknown format version " + version, DecodeIssue.Severity.ERROR));
            return new DecodeResult(null, issues);
        }

        int encoderGlyphHintBits = br.read(5);
        int encoderSlotHintBits = br.read(5);
        int ne = br.readVarInt();
        int refBits = Math.max(1, CodecUtil.nbits(ne - 1));

        int accMin = br.read(7);
        int accBits = br.read(4);
        int defaultSpeed = br.read(7);
        int minX = CodecUtil.unzigzag(br.readVarInt());
        int minY = CodecUtil.unzigzag(br.readVarInt());
        int minZ = CodecUtil.unzigzag(br.readVarInt());
        int bitsX = br.read(4);
        int bitsY = br.read(4);
        int bitsZ = br.read(4);

        List<String> globalGlyphDict = CodecUtil.buildDictionary();
        List<String> globalSlotDict = CodecUtil.buildSlotDictionary();
        Map<Integer, List<String>> glyphHashLookup = CodecUtil.getGlyphHashLookup();
        Map<Integer, List<String>> slotHashLookup = CodecUtil.getSlotHashLookup();

        int palN = br.readVarInt();
        List<String> assetPalette = new ArrayList<>(palN);
        int[] palStoredSlotCounts = new int[palN];
        for (int i = 0; i < palN; i++) {
            boolean isPstr = br.read(1) == 1;
            if (isPstr) {
                String name = CodecUtil.readPrefixedString(br);
                if (!globalGlyphDict.contains(name)) {
                    issues.add(new DecodeIssue(
                            "asset '" + name + "' is a plugin glyph not in the local registry",
                            DecodeIssue.Severity.WARNING));
                }
                assetPalette.add(name);
            } else {
                int hint = br.read(encoderGlyphHintBits);
                int storedHash = br.read(HASH_BITS);
                String resolved = CodecUtil.resolvePaletteEntry(hint, storedHash,
                        globalGlyphDict, glyphHashLookup, issues);
                assetPalette.add(resolved);
            }
            palStoredSlotCounts[i] = br.read(4);
        }
        int palBits = Math.max(1, CodecUtil.nbits(assetPalette.size() - 1));

        int spN = br.readVarInt();
        List<String> slotPalette = new ArrayList<>(spN);
        for (int i = 0; i < spN; i++) {
            boolean isPstr = br.read(1) == 1;
            if (isPstr) {
                slotPalette.add(CodecUtil.readPrefixedString(br));
            } else {
                int hint = br.read(encoderSlotHintBits);
                int storedHash = br.read(HASH_BITS);
                slotPalette.add(CodecUtil.resolvePaletteEntry(hint, storedHash,
                        globalSlotDict, slotHashLookup, issues));
            }
        }
        int spBits = Math.max(1, CodecUtil.nbits(slotPalette.size() - 1));

        Hex hex = new Hex();
        List<String> placeholderIds = new ArrayList<>(ne);
        boolean anyUnresolved = false;

        for (int i = 0; i < ne; i++) {
            int palIdx = br.read(palBits);
            String asset = palIdx < assetPalette.size() ? assetPalette.get(palIdx) : null;
            int storedSlotCount = palIdx < palStoredSlotCounts.length ? palStoredSlotCounts[palIdx] : 0;

            int accQ = accMin + br.read(accBits);
            float volatility = accQ / 100f;

            boolean speedIsDefault = br.read(1) == 1;
            int speedQ = speedIsDefault ? defaultSpeed : br.read(7);
            float efficiency = speedQ / 100f;

            int xq = minX + br.read(bitsX);
            int yq = minY + br.read(bitsY);
            int zq = minZ + br.read(bitsZ);
            Vector3f position = new Vector3f(xq / (float) POS_SCALE, yq / (float) POS_SCALE, zq / (float) POS_SCALE);

            Map<String, Slot> decodedSlots = new LinkedHashMap<>();
            int hasSlotsFlag = br.read(1);
            if (hasSlotsFlag == 1) {
                int schemaMatchFlag = br.read(1);
                if (schemaMatchFlag == 1) {
                    List<String> schema;
                    if (asset != null) {
                        schema = CodecUtil.getOrderedSlotKeys(asset);
                        if (schema.isEmpty() && storedSlotCount > 0) {
                            schema = new ArrayList<>(storedSlotCount);
                            for (int k = 0; k < storedSlotCount; k++) schema.add("<slot_" + k + ">");
                        }
                    } else {
                        schema = new ArrayList<>(storedSlotCount);
                        for (int k = 0; k < storedSlotCount; k++) schema.add("<slot_" + k + ">");
                    }

                    int allEmptyFlag = br.read(1);
                    if (allEmptyFlag == 1) {
                        for (String name : schema) {
                            Slot slot = Slot.forAssetSlot(asset, name);
                            slot.setLinks(new String[0]);
                            decodedSlots.put(name, slot);
                        }
                    } else {
                        for (String name : schema) {
                            Slot slot = Slot.forAssetSlot(asset, name);
                            slot.setLinks(readLinkPlaceholders(br, refBits));
                            decodedSlots.put(name, slot);
                        }
                    }
                } else {
                    int entryCount = br.readVarInt();
                    for (int k = 0; k < entryCount; k++) {
                        int sIdx = br.read(spBits);
                        String name = sIdx < slotPalette.size() ? slotPalette.get(sIdx) : null;
                        if (name == null) name = "<unresolved_" + sIdx + ">";
                        Slot slot = Slot.forAssetSlot(asset, name);
                        slot.setLinks(readLinkPlaceholders(br, refBits));
                        decodedSlots.put(name, slot);
                    }
                }
            }

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

            if (asset == null) {
                anyUnresolved = true;
                g.setGlyphId("");
            } else {
                g.setGlyphId(asset);
            }

            hex.put(placeholderId, g);
        }

        // resolve link indices → placeholder ids
        for (Glyph g : hex.getGlyphs()) {
            for (Slot slot : g.getSlots().values()) {
                String[] placeholders = slot.getLinks();
                String[] resolved = new String[placeholders.length];
                for (int k = 0; k < placeholders.length; k++) {
                    int idx = Integer.parseInt(placeholders[k]);
                    resolved[k] = (idx >= 0 && idx < placeholderIds.size()) ? placeholderIds.get(idx) : null;
                }
                List<String> kept = new ArrayList<>(resolved.length);
                for (String s : resolved) if (s != null) kept.add(s);
                slot.setLinks(kept.toArray(String[]::new));
            }
        }

        // set first glyph to canonical index 0
        hex.setFirstGlyphId(placeholderIds.get(0));

        // drop unresolved glyphs from the graph
        if (anyUnresolved) {
            List<String> toDrop = new ArrayList<>();
            for (Glyph g : hex.getGlyphs()) {
                if (g.getGlyphId() == null || g.getGlyphId().isEmpty()) toDrop.add(g.getId());
            }
            for (String id : toDrop) {
                issues.add(new DecodeIssue(
                        "dropping glyph " + id + " (asset unresolved)",
                        DecodeIssue.Severity.WARNING));
                hex.removeGlyph(id);
            }
            if (hex.getGlyphs().isEmpty()) {
                issues.add(new DecodeIssue("all glyphs were removed", DecodeIssue.Severity.ERROR));
                return new DecodeResult(null, issues);
            }
            if (hex.getFirstGlyphId() == null || hex.get(hex.getFirstGlyphId()) == null) {
                hex.setFirstGlyphId(hex.getGlyphs().get(0).getId());
                issues.add(new DecodeIssue("first glyph was removed; reassigned",
                        DecodeIssue.Severity.WARNING));
            }
        }

        // give glyphs their final ids via the standard rekey + clean dangling links
        HexUtils.compress(hex);

        return new DecodeResult(hex, issues);
    }

    // --- helpers ---

    private static void writeLinks(BitWriter bw, String[] links, Map<String, Integer> idToIdx, int refBits) {
        if (links == null || links.length == 0) {
            bw.write(0, 1);
            return;
        }
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

    private static String[] readLinkPlaceholders(BitReader br, int refBits) {
        if (br.read(1) == 0) return new String[0];
        boolean multi = br.read(1) == 1;
        if (!multi) {
            int idx = br.read(refBits);
            return new String[] { Integer.toString(idx) };
        }
        int count = br.readVarInt();
        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = Integer.toString(br.read(refBits));
        return out;
    }

    private static boolean schemaMatches(String glyphId, Map<String, Slot> slots) {
        List<String> schema = CodecUtil.getOrderedSlotKeys(glyphId);
        if (schema.isEmpty()) return false;
        if (schema.size() != slots.size()) return false;
        int i = 0;
        for (String key : slots.keySet()) {
            if (!schema.get(i).equals(key)) return false;
            i++;
        }
        return true;
    }

    private static boolean allSlotsEmpty(Map<String, Slot> slots) {
        for (Slot slot : slots.values()) {
            if (slot.getLinks().length > 0) return false;
        }
        return true;
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private static Map<String, Integer> indexMap(List<String> dict) {
        Map<String, Integer> map = new HashMap<>(dict.size() * 2);
        for (int i = 0; i < dict.size(); i++) map.put(dict.get(i), i);
        return map;
    }

    private static List<Glyph> canonicalOrder(Hex hex) {
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

    // --- frame ---

    @Nullable
    public static String serialize(Hex hex) {
        byte[] raw = encode(hex);
        if (raw == null) return null;
        byte[] deflated = CodecUtil.deflate(raw);
        byte[] payload = (deflated != null && deflated.length < raw.length) ? deflated : raw;
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        return FRAME_PREFIX + encoded;
    }

    public static DecodeResult deserialize(String data) {
        if (data == null || !data.startsWith(FRAME_PREFIX)) {
            return DecodeResult.error("not a v14 frame");
        }
        String b64 = data.substring(FRAME_PREFIX.length());
        byte[] payload;
        try {
            payload = Base64.getUrlDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            return DecodeResult.error("invalid base64");
        }
        byte[] raw = CodecUtil.inflate(payload);
        if (raw == null) raw = payload;
        return decode(raw);
    }
}
