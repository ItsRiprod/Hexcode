package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.positioning.layout.HexLayout;
import com.riprod.hexcode.core.common.positioning.layout.LayoutParams;
import com.riprod.hexcode.core.common.positioning.registry.HexLayoutRegistry;
import com.riprod.hexcode.core.common.positioning.utils.HexEntities;
import com.riprod.hexcode.core.common.positioning.utils.HexRelayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.riprod.hexcode.utils.CleanupUtils;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.positioning.utils.HexEntityFactory;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.utils.GlyphMath;

public class HexSpawner {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static List<Ref<EntityStore>> spawnHexes(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ownerRef,
            Ref<EntityStore> castingRootRef, List<Hex> hexes, String styleId, @Nullable String layoutId) {
        var spawnedHexes = new ArrayList<Ref<EntityStore>>();
        var ownerPos = accessor.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition();
        var ownerRotation = accessor.getComponent(ownerRef, HeadRotation.getComponentType()).getRotation();

        var style = CastingStyleRegistry.getOrDefault(styleId);
        var rotations = style.getInitialPositions(hexes.size(), ownerRotation.y, ownerRotation.x);
        var layout = HexLayoutRegistry.getOrDefault(layoutId);

        for (var i = 0; i < hexes.size(); i++) {
            var hexRef = spawnHex(accessor, castingRootRef, hexes.get(i), rotations.get(i), ownerPos, layout);
            if (hexRef != null) {
                spawnedHexes.add(hexRef);
            }
        }
        return spawnedHexes;
    }

    private static final float IN_AIR_SPAWN_DISTANCE = 3.0f;

    @Nullable
    public static Ref<EntityStore> spawnSingleHex(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ownerRef,
            Ref<EntityStore> castingRootRef, Hex hex, @Nullable String layoutId) {
        var ownerPos = accessor.getComponent(ownerRef, TransformComponent.getComponentType()).getPosition();
        var ownerRotation = accessor.getComponent(ownerRef, HeadRotation.getComponentType()).getRotation();
        var rot = new Rotation3f(ownerRotation.x, ownerRotation.y, IN_AIR_SPAWN_DISTANCE);
        return spawnHex(accessor, castingRootRef, hex, rot, ownerPos, HexLayoutRegistry.getOrDefault(layoutId));
    }

    /** @param rot hex placement around the caster: x = pitch, y = yaw, z = distance */
    @Nullable
    private static Ref<EntityStore> spawnHex(CommandBuffer<EntityStore> accessor, Ref<EntityStore> castingRootRef,
            Hex hex, Rotation3f rot, Vector3d ownerPos, HexLayout layout) {
        var firstGlyphId = hex.getFirstGlyphId();
        var firstGlyph = firstGlyphId != null ? hex.get(firstGlyphId) : null;
        if (firstGlyph == null) {
            LOGGER.atWarning().log("skipping malformed hex (no entry point): hexId=%s firstGlyphId=%s glyphs=%d",
                    hex.getHexId(), firstGlyphId, hex.getGlyphs().size());
            return null;
        }

        var hexComponent = new HexComponent(hex);
        var position = GlyphMath.sphericalToCartesian(rot);
        hexComponent.setRootRef(castingRootRef);
        hexComponent.setParentRef(castingRootRef);
        hexComponent.setOffset(new Vector3f((float) position.x, (float) position.y, (float) position.z));
        hexComponent.setRotation(new Rotation3f(rot.x, rot.y, rot.z));
        var hexRef = HexEntityFactory.createHex(accessor, hexComponent, ownerPos);
        hexComponent.setSelfRef(hexRef);

        var rootScale = layout.rootScale(hex);
        hexComponent.setScale(rootScale);
        var root = new GlyphComponent(firstGlyph);
        root.setHexRef(hexRef);
        root.setParentRef(hexRef);

        HexEntities.spawnTree(accessor, hexComponent, root, layout, new LayoutParams(rootScale, rot.z, 0f),
                ownerPos, new Rotation3f(rot.x, rot.y, 0f), false);
        return hexRef;
    }

    public static void MergeGlyphs(CommandBuffer<EntityStore> accessor, GlyphComponent droppedOnGlyph,
            HexComponent draggedHex, float eyeHeight) {

        HexComponent droppedOnHex = accessor.getComponent(droppedOnGlyph.getHexRef(), HexComponent.getComponentType());
        Hex hex1 = droppedOnHex.getHex();

        hex1.absorb(draggedHex.getHex(), droppedOnGlyph.getId());

        Map<String, Ref<EntityStore>> droppedGlyphs = draggedHex.getChildGlyphRefs();
        droppedOnHex.addChildGlyphRefs(droppedGlyphs);
        String firstGlyphId = draggedHex.getHex().getFirstGlyphId();

        Ref<EntityStore> firstGlyphRef = droppedGlyphs.get(firstGlyphId);
        GlyphComponent firstChildGlyph = accessor.getComponent(firstGlyphRef, GlyphComponent.getComponentType());

        firstChildGlyph.setParentRef(droppedOnGlyph.getSelfRef());
        accessor.tryRemoveComponent(firstGlyphRef, MountedComponent.getComponentType());

        for (Ref<EntityStore> childRef : droppedGlyphs.values()) {
            if (childRef == null || !childRef.isValid()) {
                continue;
            }
            GlyphComponent childGlyph = accessor.getComponent(childRef, GlyphComponent.getComponentType());
            childGlyph.setHexRef(droppedOnGlyph.getHexRef());
        }

        MountedComponent mounted = new MountedComponent(droppedOnGlyph.getSelfRef(), new Vector3f(),
                MountController.Minecart);
        accessor.putComponent(firstGlyphRef, MountedComponent.getComponentType(), mounted);

        CleanupUtils.safeRemoveMountParent(accessor, draggedHex.getSelfRef());

        Ref<EntityStore> rootGlyph = droppedOnHex.getChildGlyphRef(hex1.getFirstGlyphId());
        GlyphComponent rootGlyphComponent = accessor.getComponent(rootGlyph, GlyphComponent.getComponentType());

        HexRelayout.relayout(accessor, droppedOnHex, rootGlyphComponent);
    }
}
