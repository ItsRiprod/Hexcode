package com.riprod.hexcode.core.common.positioning.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.positioning.layout.HexLayout;
import com.riprod.hexcode.core.common.positioning.layout.HexLayoutState;
import com.riprod.hexcode.core.common.positioning.layout.LayoutParams;
import com.riprod.hexcode.core.common.positioning.layout.Placement;
import com.riprod.hexcode.utils.CleanupUtils;

public final class HexEntities {

    private HexEntities() {
    }

    @Nonnull
    public static Ref<EntityStore> reserve(@Nonnull CommandBuffer<EntityStore> accessor) {
        return new Ref<>(accessor.getStore());
    }

    public static void spawnTree(@Nonnull CommandBuffer<EntityStore> accessor, @Nonnull HexComponent hex,
            @Nonnull GlyphComponent root, @Nonnull HexLayout layout, @Nonnull LayoutParams params,
            @Nonnull Vector3d spawnPos, @Nonnull Rotation3f facing, boolean cloneGlyphs) {
        var placements = new LinkedHashMap<String, Placement>();
        for (var placement : layout.layout(hex.getHex(), params)) {
            placements.put(placement.glyph().getId(), placement);
            hex.addChildGlyphRef(placement.glyph().getId(), reserve(accessor));
        }
        var spins = accumulatedSpins(placements);
        for (var placement : placements.values()) {
            GlyphComponent comp;
            if (placement.isRoot()) {
                comp = root;
            } else {
                comp = new GlyphComponent(cloneGlyphs ? placement.glyph().clone() : placement.glyph());
                comp.setParentRef(hex.getChildGlyphRef(placement.parentId()));
                comp.setHexRef(hex.getSelfRef());
            }
            comp.setVisualOffset(new Vector3f(placement.offset()));
            comp.setScale(placement.scale());
            spawnGlyph(accessor, comp, spawnPos, worldRotation(facing, spins.get(comp.getId())),
                    hex.getChildGlyphRef(comp.getId()), null);
        }
        hex.setLayoutState(new HexLayoutState(layout, params, placements));
    }

    @Nonnull
    public static Map<String, Float> accumulatedSpins(@Nonnull Map<String, Placement> placements) {
        var spins = new HashMap<String, Float>();
        for (var placement : placements.values()) {
            var parent = placement.isRoot() ? null : spins.get(placement.parentId());
            spins.put(placement.glyph().getId(), (parent != null ? parent : 0f) + placement.spin());
        }
        return spins;
    }

    @Nonnull
    public static Rotation3f worldRotation(@Nonnull Rotation3f facing, @Nullable Float spin) {
        return new Rotation3f(facing.x, facing.y, facing.z + (spin != null ? spin : 0f));
    }

    @Nonnull
    public static Ref<EntityStore> spawnGlyph(@Nonnull CommandBuffer<EntityStore> accessor,
            @Nonnull GlyphComponent glyph, @Nonnull Vector3d position, @Nonnull Rotation3f rotation,
            @Nonnull Ref<EntityStore> reserved, @Nullable Consumer<Holder<EntityStore>> decorate) {
        var holder = HexEntityFactory.createGlyphHolder(accessor, glyph, position, rotation);
        if (decorate != null) {
            decorate.accept(holder);
        }
        accessor.addEntity(holder, reserved, AddReason.SPAWN);
        glyph.setSelfRef(reserved);
        return reserved;
    }

    public static void despawnGlyph(@Nonnull CommandBuffer<EntityStore> accessor, @Nullable Ref<EntityStore> glyphRef) {
        if (glyphRef == null || !glyphRef.isValid()) {
            return;
        }
        var glyph = accessor.getComponent(glyphRef, GlyphComponent.getComponentType());
        if (glyph != null) {
            CleanupUtils.safeRemoveEntities(accessor, glyph.getSlotEntityRefs());
            glyph.getSlotEntityRefs().clear();
        }
        accessor.tryRemoveComponent(glyphRef, MountedComponent.getComponentType());
        CleanupUtils.safeRemoveMountParent(accessor, glyphRef);
    }

    public static void despawnTree(@Nonnull CommandBuffer<EntityStore> accessor, @Nonnull HexComponent hex) {
        for (var glyphRef : hex.getChildGlyphRefsList()) {
            despawnGlyph(accessor, glyphRef);
        }
        hex.getChildGlyphRefs().clear();
        hex.setLayoutState(null);
        hex.setHoverCache(null);
    }

    public static void despawnHex(@Nonnull CommandBuffer<EntityStore> accessor, @Nullable Ref<EntityStore> hexRef) {
        if (hexRef == null || !hexRef.isValid()) {
            return;
        }
        var hex = accessor.getComponent(hexRef, HexComponent.getComponentType());
        if (hex != null) {
            despawnTree(accessor, hex);
        }
        accessor.tryRemoveComponent(hexRef, MountedComponent.getComponentType());
        CleanupUtils.safeRemoveMountParent(accessor, hexRef);
    }
}
