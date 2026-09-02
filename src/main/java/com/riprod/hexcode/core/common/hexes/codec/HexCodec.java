package com.riprod.hexcode.core.common.hexes.codec;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class HexCodec {

    public static final String PREFIX = HexCodecV15.FRAME_PREFIX;

    public static String serialize(Hex hex) {
        return HexCodecV15.serialize(hex);
    }

    public static String canonicalizeSnapshot(String data) {
        DecodeResult result = deserialize(data);
        Hex hex = result.getHex();
        if (hex == null || hex.getGlyphs().isEmpty()) {
            throw new HexCodecException("snapshot decode produced no glyphs");
        }
        HexUtils.validate(hex);
        if (hex.getGlyphs().isEmpty()) {
            throw new HexCodecException("snapshot empty after validation");
        }
        for (var glyph : hex.getGlyphs()) {
            glyph.getPosition().x = 0f;
            glyph.getPosition().z = 0f;
            glyph.setRotation(new Rotation3f());
        }
        HexUtils.rekeyCanonical(hex);
        return HexCodecV15.serialize(hex, false);
    }

    @SuppressWarnings("deprecation")
    public static DecodeResult deserialize(String data) {
        if (data == null) return DecodeResult.error("null input");

        DecodeResult result;
        if (data.startsWith(HexCodecV16.FRAME_PREFIX)) {
            result = HexCodecV16.deserialize(data);
        } else if (data.startsWith(HexCodecV15.FRAME_PREFIX)) {
            result = HexCodecV15.deserialize(data);
        } else if (data.startsWith(HexCodecV14.FRAME_PREFIX)) {
            result = HexCodecV14.deserialize(data);
        } else {
            return DecodeResult.error("unsupported format (expected " + HexCodecV15.FRAME_PREFIX
                    + ", " + HexCodecV16.FRAME_PREFIX + ", or " + HexCodecV14.FRAME_PREFIX + ")");
        }
        if (result.getHex() != null) {
            HexUtils.repair(result.getHex());
        }
        return result;
    }
}
