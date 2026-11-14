package com.riprod.hexcode.core.common.positioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.positioning.utils.HexHoverCache;

public final class GlyphSelector {

    private static final String OUTPUT_GLYPH_ID = "Glyph_Output";

    private GlyphSelector() {
    }

    @Nullable
    public static Ref<EntityStore> findHoveredHex(@Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Rotation3f headRotation, @Nonnull List<Ref<EntityStore>> hexes,
            @Nullable Ref<EntityStore> exclude) {
        var lookX = lookX(headRotation);
        var lookY = lookY(headRotation);
        var lookZ = lookZ(headRotation);
        for (var i = 0; i < hexes.size(); i++) {
            var hexRef = hexes.get(i);
            if (hexRef == null || !hexRef.isValid() || hexRef.equals(exclude)) {
                continue;
            }
            var hex = accessor.getComponent(hexRef, HexComponent.getComponentType());
            var state = hex != null ? hex.getLayoutState() : null;
            if (state != null && HexHoverCache.of(hex, state).hoversHex(lookX, lookY, lookZ)) {
                return hexRef;
            }
        }
        return null;
    }

    @Nullable
    public static Ref<EntityStore> findHoveredGlyph(@Nonnull Rotation3f headRotation, @Nonnull HexComponent hex) {
        var state = hex.getLayoutState();
        if (state == null) {
            return null;
        }
        var id = HexHoverCache.of(hex, state).hoveredGlyph(lookX(headRotation), lookY(headRotation),
                lookZ(headRotation));
        var ref = id != null ? hex.getChildGlyphRef(id) : null;
        return ref != null && ref.isValid() ? ref : null;
    }

    @Nullable
    public static Ref<EntityStore> findOutputChild(@Nonnull HexComponent hex, @Nonnull String glyphId) {
        var glyph = hex.getHex().get(glyphId);
        if (glyph == null) {
            return null;
        }
        var id = findOutputDepthFirst(hex.getHex(), glyph.getFlowLinks(), new HashSet<>());
        var ref = id != null ? hex.getChildGlyphRef(id) : null;
        return ref != null && ref.isValid() ? ref : null;
    }

    @Nullable
    private static String findOutputDepthFirst(Hex hex, List<String> glyphIds, Set<String> visited) {
        for (var id : glyphIds) {
            if (id == null || !visited.add(id)) {
                continue;
            }
            var glyph = hex.get(id);
            if (glyph == null) {
                continue;
            }
            if (OUTPUT_GLYPH_ID.equals(glyph.getGlyphId())) {
                return id;
            }
            var found = findOutputDepthFirst(hex, glyph.getFlowLinks(), visited);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static float lookX(Rotation3f head) {
        return (float) (-Math.sin(head.y) * Math.cos(head.x));
    }

    private static float lookY(Rotation3f head) {
        return (float) Math.sin(head.x);
    }

    private static float lookZ(Rotation3f head) {
        return (float) (-Math.cos(head.y) * Math.cos(head.x));
    }
}
