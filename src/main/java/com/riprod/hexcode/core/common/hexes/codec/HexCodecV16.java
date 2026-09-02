package com.riprod.hexcode.core.common.hexes.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

@Deprecated
public class HexCodecV16 {

    public static final int FORMAT_VERSION = 16;
    public static final String FRAME_PREFIX = "hx:16:";

    private static final byte[] MAGIC = { 'H', 'X' };
    private static final int FLAG_ZLIB = 0x01;

    private static final int SECTION_HEADER = 0x01;
    private static final int SECTION_ASSET_PALETTE = 0x02;
    private static final int SECTION_SLOT_PALETTE = 0x03;
    private static final int SECTION_GLYPH_STREAM = 0x04;

    private HexCodecV16() {}

    public static DecodeResult deserialize(String data) {
        if (data == null || !data.startsWith(FRAME_PREFIX)) {
            return DecodeResult.error("not a v16 frame");
        }
        byte[] body = HexCodecV15.unframe(data, FRAME_PREFIX, MAGIC, FLAG_ZLIB);
        if (body == null) return DecodeResult.error("frame parse failed");
        return decode(body);
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
                    slotPalette, spBits, refBits, false);

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
        HexUtils.rekeyDecodeOrder(hex, placeholderIds);
        return new DecodeResult(hex, issues);
    }
}
