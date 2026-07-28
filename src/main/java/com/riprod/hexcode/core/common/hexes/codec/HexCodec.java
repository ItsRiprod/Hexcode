package com.riprod.hexcode.core.common.hexes.codec;

import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class HexCodec {

    public static final String PREFIX = HexCodecV15.FRAME_PREFIX;

    public static String serialize(Hex hex) {
        return HexCodecV15.serialize(hex);
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
