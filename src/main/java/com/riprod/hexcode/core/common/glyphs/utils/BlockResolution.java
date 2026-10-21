package com.riprod.hexcode.core.common.glyphs.utils;

import org.joml.Vector3d;

public final class BlockResolution {
    private BlockResolution() {}

    private static final double INTO_BLOCK_NUDGE = 0.1;

    public static Vector3d nudgeIntoBlock(Vector3d hit, Vector3d dir) {
        if (hit == null) return null;
        Vector3d probe = new Vector3d(hit);
        if (dir != null) {
            double len = dir.length();
            if (len > 1e-9) {
                probe.add(dir.x / len * INTO_BLOCK_NUDGE,
                        dir.y / len * INTO_BLOCK_NUDGE,
                        dir.z / len * INTO_BLOCK_NUDGE);
            }
        }
        return probe;
    }
}
