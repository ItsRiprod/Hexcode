package com.riprod.hexcode.core.common.hexes.codec;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class HexCodecV16 {

    public static final int FORMAT_VERSION = 16;
    public static final String FRAME_PREFIX = "hx:16:";

    private static final byte[] MAGIC = { 'H', 'X' };
    private static final int FLAG_ZLIB = 0x01;
    private static final int FLAG_FINGERPRINT = 0x02;

    private static final int SECTION_HEADER = 0x01;
    private static final int SECTION_ASSET_PALETTE = 0x02;
    private static final int SECTION_SLOT_PALETTE = 0x03;
    private static final int SECTION_GLYPH_STREAM = 0x04;

    private HexCodecV16() {}

    public static String serialize(Hex hex) {
        return serialize(hex, true);
    }

    public static String serialize(Hex hex, boolean includeFingerprint) {
        Hex clone = hex.clone();
        HexUtils.validate(clone);
        HexUtils.compress(clone);
        byte[] body = encode(clone, includeFingerprint);
        return HexCodecV15.frame(body, includeFingerprint, MAGIC, FLAG_ZLIB, FLAG_FINGERPRINT,
                FRAME_PREFIX);
    }

    public static DecodeResult deserialize(String data) {
        if (data == null || !data.startsWith(FRAME_PREFIX)) {
            return DecodeResult.error("not a v16 frame");
        }
        byte[] body = HexCodecV15.unframe(data, FRAME_PREFIX, MAGIC, FLAG_ZLIB);
        if (body == null) return DecodeResult.error("frame parse failed");
        return decode(body);
    }

    private static byte[] encode(Hex hex, boolean includeFingerprint) {
        List<Glyph> glyphs = HexCodecV15.canonicalOrder(hex);
        if (glyphs.isEmpty()) {
            throw new HexCodecException("cannot encode empty hex");
        }
        int ne = glyphs.size();
        int refBits = Math.max(1, CodecUtil.nbits(ne - 1));

        Map<String, Integer> idToIdx = new HashMap<>(ne * 2);
        for (int i = 0; i < ne; i++) idToIdx.put(glyphs.get(i).getId(), i);

        List<String> assetPalette = HexCodecV15.buildAssetPalette(glyphs);
        Map<String, Integer> palMap = HexCodecV15.indexMap(assetPalette);
        int palBits = Math.max(1, CodecUtil.nbits(assetPalette.size() - 1));

        List<String> slotPalette = HexCodecV15.buildSlotPalette(glyphs);
        Map<String, Integer> slotIdxMap = HexCodecV15.indexMap(slotPalette);
        int spBits = Math.max(1, CodecUtil.nbits(slotPalette.size() - 1));

        int[] accVals = new int[ne];
        int accMin = Integer.MAX_VALUE, accMax = Integer.MIN_VALUE;
        for (int i = 0; i < ne; i++) {
            int q = Math.round(HexCodecV15.clamp01(glyphs.get(i).getVolatility()) * 100f);
            accVals[i] = q;
            if (q < accMin) accMin = q;
            if (q > accMax) accMax = q;
        }
        int accBits = CodecUtil.nbits(accMax - accMin);

        int[] speedVals = new int[ne];
        Map<Integer, Integer> speedCounts = new HashMap<>();
        for (int i = 0; i < ne; i++) {
            int q = Math.round(HexCodecV15.clamp01(glyphs.get(i).getEfficiency()) * 100f);
            speedVals[i] = q;
            speedCounts.merge(q, 1, Integer::sum);
        }
        int defaultSpeed = 0, bestCount = -1;
        for (Map.Entry<Integer, Integer> e : speedCounts.entrySet()) {
            if (e.getValue() > bestCount) { bestCount = e.getValue(); defaultSpeed = e.getKey(); }
        }

        byte[] header = encodeHeader(includeFingerprint, ne, accMin, accBits, defaultSpeed);
        byte[] assetPaletteBytes = HexCodecV15.encodeAssetPalette(assetPalette);
        byte[] slotPaletteBytes = HexCodecV15.encodeSlotPalette(slotPalette);
        byte[] glyphStreamBytes = encodeGlyphStream(glyphs, palMap, palBits, accVals, accMin,
                accBits, speedVals, defaultSpeed, slotIdxMap, spBits, idToIdx, refBits);

        byte[] slotStateBytes = HexCodecV15.encodeSlotStates(glyphs);
        byte[] metaBytes = HexCodecV15.encodeMeta(hex);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        CodecUtil.writeByteVarInt(body,
                4 + (slotStateBytes != null ? 1 : 0) + (metaBytes != null ? 1 : 0));
        HexCodecV15.appendSection(body, SECTION_HEADER, header);
        HexCodecV15.appendSection(body, SECTION_ASSET_PALETTE, assetPaletteBytes);
        HexCodecV15.appendSection(body, SECTION_SLOT_PALETTE, slotPaletteBytes);
        HexCodecV15.appendSection(body, SECTION_GLYPH_STREAM, glyphStreamBytes);
        if (slotStateBytes != null) {
            HexCodecV15.appendSection(body, HexCodecV15.SECTION_SLOT_STATE, slotStateBytes);
        }
        if (metaBytes != null) {
            HexCodecV15.appendSection(body, HexCodecV15.SECTION_META, metaBytes);
        }
        return body.toByteArray();
    }

    private static byte[] encodeHeader(boolean fp, int ne, int accMin, int accBits, int defaultSpeed) {
        BitWriter bw = new BitWriter();
        bw.write(fp ? 1 : 0, 1);
        if (fp) {
            int fingerprint = CodecUtil.registryFingerprint(HexCodecV15.buildBareGlyphDict());
            for (int i = 31; i >= 0; i--) bw.write((fingerprint >> i) & 1, 1);
        }
        bw.writeVarInt(ne);
        bw.write(accMin, 7);
        bw.write(accBits, 4);
        bw.write(defaultSpeed, 7);
        return bw.flush();
    }

    private static byte[] encodeGlyphStream(List<Glyph> glyphs, Map<String, Integer> palMap, int palBits,
            int[] accVals, int accMin, int accBits, int[] speedVals, int defaultSpeed,
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
            HexCodecV15.writeSlots(bw, g, slotIdxMap, spBits, idToIdx, refBits);
        }
        return bw.flush();
    }

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
        Map<Integer, byte[]> sections = HexCodecV15.splitSections(body, issues);
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

        if (fingerprint != null) {
            int local = CodecUtil.registryFingerprint(HexCodecV15.buildBareGlyphDict());
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
            List<String> dict = HexCodecV15.buildBareGlyphDict();
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

            Map<String, Slot> decodedSlots = HexCodecV15.readSlots(gs, asset, storedSlotCount,
                    slotPalette, spBits, refBits);

            Glyph g = new Glyph();
            g.setVolatility(volatility);
            g.setEfficiency(efficiency);
            g.setPosition(new Vector3f(0f, 0f, 0f));
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

        HexCodecV15.resolveLinks(hex, placeholderIds);
        if (!placeholderIds.isEmpty()) hex.setFirstGlyphId(placeholderIds.get(0));

        if (sections.containsKey(HexCodecV15.SECTION_SLOT_STATE)) {
            try {
                HexCodecV15.decodeSlotStates(sections.get(HexCodecV15.SECTION_SLOT_STATE),
                        placeholderIds, hex);
            } catch (Exception e) {
                issues.add(new DecodeIssue("slot state failed: " + e.getMessage()
                        + "; slot toggles default", DecodeIssue.Severity.INFO));
            }
        }

        if (sections.containsKey(HexCodecV15.SECTION_META)) {
            try {
                HexCodecV15.decodeMeta(sections.get(HexCodecV15.SECTION_META), hex);
            } catch (Exception e) {
                issues.add(new DecodeIssue("meta failed: " + e.getMessage()
                        + "; hex stays unnamed", DecodeIssue.Severity.INFO));
            }
        }

        HexCodecV15.finalizeHex(hex, anyUnresolved, issues);
        HexUtils.compress(hex);
        return new DecodeResult(hex, issues);
    }
}
