package com.riprod.hexcode.core.common.glyphs.utils;

import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;

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

    public static Vector3i snapIntoBlock(World world, Vector3d hit, Vector3d dir) {
        return resolveSolidBlock(world, nudgeIntoBlock(hit, dir));
    }

    public static Vector3i resolveSolidBlock(World world, Vector3d pos) {
        if (pos == null) return null;
        int bx = (int) Math.floor(pos.x);
        int by = (int) Math.floor(pos.y);
        int bz = (int) Math.floor(pos.z);
        if (world == null) return new Vector3i(bx, by, bz);
        if (world.getBlock(bx, by, bz) != BlockType.EMPTY_ID) {
            return new Vector3i(bx, by, bz);
        }
        double fx = pos.x - bx, fy = pos.y - by, fz = pos.z - bz;
        double dx = Math.min(fx, 1.0 - fx);
        double dy = Math.min(fy, 1.0 - fy);
        double dz = Math.min(fz, 1.0 - fz);
        int nx = bx + (fx < 0.5 ? -1 : 1);
        int ny = by + (fy < 0.5 ? -1 : 1);
        int nz = bz + (fz < 0.5 ? -1 : 1);
        Vector3i[] candidates = new Vector3i[]{
                new Vector3i(nx, by, bz),
                new Vector3i(bx, ny, bz),
                new Vector3i(bx, by, nz),
        };
        double[] dists = new double[]{dx, dy, dz};
        boolean[] used = new boolean[3];
        for (int iter = 0; iter < 3; iter++) {
            int best = -1;
            double bestD = Double.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                if (!used[i] && dists[i] < bestD) {
                    best = i;
                    bestD = dists[i];
                }
            }
            if (best < 0) break;
            used[best] = true;
            Vector3i c = candidates[best];
            if (world.getBlock(c.x, c.y, c.z) != BlockType.EMPTY_ID) {
                return c;
            }
        }
        return new Vector3i(bx, by, bz);
    }
}
