package com.riprod.hexcode.core.common.hexes.codec;

import java.io.ByteArrayOutputStream;

public class BitWriter {

    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private int current = 0;
    private int bitPos = 0;

    public void write(int value, int numBits) {
        for (int i = numBits - 1; i >= 0; i--) {
            current = (current << 1) | ((value >> i) & 1);
            bitPos++;
            if (bitPos == 8) {
                buf.write(current);
                current = 0;
                bitPos = 0;
            }
        }
    }

    public void writeVarInt(int value) {
        if (value < 8) {
            write(0, 1);
            write(value, 3);
        } else if (value < 72) {
            write(1, 1);
            write(0, 1);
            write(value - 8, 6);
        } else {
            if (value > 4095) {
                throw new HexCodecException("varint value exceeds 12-bit cap: " + value);
            }
            write(1, 1);
            write(1, 1);
            write(value, 12);
        }
    }

    public byte[] flush() {
        if (bitPos > 0) {
            buf.write(current << (8 - bitPos));
        }
        return buf.toByteArray();
    }
}