package com.riprod.hexcode.core.common.positioning.utils;

import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.positioning.layout.HexLayoutState;
import com.riprod.hexcode.utils.GlyphMath;

public final class HexHoverCache {

    private final HexLayoutState state;
    private final float pitch;
    private final float yaw;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float hexScale;

    private final String[] ids;
    private final float[] scales;
    private final float[] dirs;
    private final float[] cosRadius;

    private final float centerX;
    private final float centerY;
    private final float centerZ;
    private final float cosCenter;
    private final float cosCone;

    private HexHoverCache(HexComponent hex, HexLayoutState state) {
        this.state = state;
        this.pitch = hex.getRotation().x;
        this.yaw = hex.getRotation().y;
        this.offsetX = hex.getOffset().x;
        this.offsetY = hex.getOffset().y;
        this.offsetZ = hex.getOffset().z;
        this.hexScale = hex.getScale();

        var n = state.placements().size();
        ids = new String[n];
        scales = new float[n];
        dirs = new float[n * 3];
        cosRadius = new float[n];

        var hexPos = new Vector3d(offsetX, offsetY, offsetZ);
        var center = new Vector3d(hexPos).normalize();
        centerX = (float) center.x;
        centerY = (float) center.y;
        centerZ = (float) center.z;
        var centerRadius = GlyphMath.getSelectionRadius(hexScale);
        cosCenter = (float) Math.cos(centerRadius);

        // positions in the hex plane (x right, y up, z towards the viewer), then turned by the hex's facing
        var facing = new Rotation3f(pitch, yaw, 0f);
        var plane = new HashMap<String, double[]>();
        double cone = centerRadius;
        var i = 0;
        for (var placement : state.placements().values()) {
            var parent = placement.isRoot() ? null : plane.get(placement.parentId());
            var px = parent != null ? parent[0] : 0;
            var py = parent != null ? parent[1] : 0;
            var pz = parent != null ? parent[2] : 0;
            var parentSpin = parent != null ? parent[3] : 0;
            var cos = Math.cos(parentSpin);
            var sin = Math.sin(parentSpin);
            var o = placement.offset();
            var x = px + o.x * cos - o.y * sin;
            var y = py + o.x * sin + o.y * cos;
            var z = pz + o.z;
            plane.put(placement.glyph().getId(), new double[] { x, y, z, parentSpin + placement.spin() });

            var dir = facing.transform(new Vector3d(x, y, z), new Vector3d()).add(hexPos).normalize();
            var radius = GlyphMath.getSelectionRadius(placement.scale());
            ids[i] = placement.glyph().getId();
            scales[i] = placement.scale();
            dirs[i * 3] = (float) dir.x;
            dirs[i * 3 + 1] = (float) dir.y;
            dirs[i * 3 + 2] = (float) dir.z;
            cosRadius[i] = (float) Math.cos(radius);
            cone = Math.max(cone, center.angle(dir) + radius);
            i++;
        }
        cosCone = (float) Math.cos(Math.min(cone, Math.PI));
    }

    @Nonnull
    public static HexHoverCache of(@Nonnull HexComponent hex, @Nonnull HexLayoutState state) {
        var cache = hex.getHoverCache();
        if (cache == null || !cache.matches(hex, state)) {
            cache = new HexHoverCache(hex, state);
            hex.setHoverCache(cache);
        }
        return cache;
    }

    private boolean matches(HexComponent hex, HexLayoutState current) {
        var o = hex.getOffset();
        return state == current && pitch == hex.getRotation().x && yaw == hex.getRotation().y
                && offsetX == o.x && offsetY == o.y && offsetZ == o.z && hexScale == hex.getScale();
    }

    public boolean hoversHex(float lookX, float lookY, float lookZ) {
        var centerDot = lookX * centerX + lookY * centerY + lookZ * centerZ;
        if (centerDot < cosCone) {
            return false;
        }
        if (centerDot >= cosCenter) {
            return true;
        }
        for (var i = 0; i < ids.length; i++) {
            if (lookX * dirs[i * 3] + lookY * dirs[i * 3 + 1] + lookZ * dirs[i * 3 + 2] >= cosRadius[i]) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public String hoveredGlyph(float lookX, float lookY, float lookZ) {
        if (lookX * centerX + lookY * centerY + lookZ * centerZ < cosCone) {
            return null;
        }
        String best = null;
        var bestScale = Float.MAX_VALUE;
        for (var i = 0; i < ids.length; i++) {
            if (scales[i] < bestScale
                    && lookX * dirs[i * 3] + lookY * dirs[i * 3 + 1] + lookZ * dirs[i * 3 + 2] >= cosRadius[i]) {
                best = ids[i];
                bestScale = scales[i];
            }
        }
        return best;
    }
}
