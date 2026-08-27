package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.riprod.hexcode.builtin.hexCore.scene.GlyphStyler;
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
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.CreateHex;
import com.riprod.hexcode.core.common.casting.component.CastingStyle;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.utils.GlyphMath;

public class HexSpawner {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static List<Ref<EntityStore>> spawnHexes(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ownerRef,
            Ref<EntityStore> castingRootRef,
            List<Hex> hexes,
            String styleId) {

        List<Ref<EntityStore>> spawnedHexes = new ArrayList<>();

        TransformComponent ownerTransform = accessor.getComponent(ownerRef, TransformComponent.getComponentType());
        Vector3d ownerPos = ownerTransform.getPosition();
        HeadRotation headRotation = accessor.getComponent(ownerRef, HeadRotation.getComponentType());
        Rotation3f ownerRotation = headRotation.getRotation();

        CastingStyle style = CastingStyleRegistry.getOrDefault(styleId);
        List<Rotation3f> rotations = style.getInitialPositions(hexes.size(), ownerRotation.y,
                ownerRotation.x);

        for (int i = 0; i < hexes.size(); i++) {
            Hex hex = hexes.get(i);

            String firstGlyphId = hex.getFirstGlyphId();
            Glyph firstGlyph = firstGlyphId != null ? hex.get(firstGlyphId) : null;
            if (firstGlyph == null) {
                LOGGER.atWarning().log(
                        "skipping malformed hex (no entry point): hexId=%s firstGlyphId=%s glyphs=%d",
                        hex.getHexId(), firstGlyphId, hex.getGlyphs().size());
                continue;
            }

            HexComponent hexComponent = new HexComponent(hex);
            Rotation3f rot = rotations.get(i);
            Vector3d position = GlyphMath.sphericalToCartesian(rot);
            hexComponent.setRootRef(castingRootRef);
            hexComponent.setParentRef(castingRootRef);

            hexComponent.setOffset(new Vector3f((float) position.x, (float) position.y, (float) position.z));
            hexComponent.setRotation(rot);
            Ref<EntityStore> hexRef = CreateHex.createHexEntity(accessor, hexComponent, ownerPos);
            hexComponent.setSelfRef(hexRef);
            spawnedHexes.add(hexRef);

            int numGlyphs = (int) hex.getGlyphs().stream()
                    .filter(glyph -> glyph != null)
                    .count();
            float scaleMultiplier = 1 + (numGlyphs * GlyphStyler.SCALE_PER_GLYPH);

            GlyphComponent firstGlyphComponent = new GlyphComponent(firstGlyph);

            firstGlyphComponent.setHexRef(hexRef);
            firstGlyphComponent.setParentRef(hexRef);
            firstGlyphComponent.setOffset(new Vector3f());
            firstGlyphComponent.setRotation(new Rotation3f(rot.x, rot.y, rot.z));
            firstGlyphComponent.setScale(scaleMultiplier);
            hexComponent.setScale(scaleMultiplier);

            GlyphSpawner.spawnGlyphs(accessor, hexComponent, firstGlyphComponent, ownerPos, new Rotation3f(rot.x, rot.y, 0f));
        }
        return spawnedHexes;

    }

    private static final float IN_AIR_SPAWN_DISTANCE = 3.0f;

    public static Ref<EntityStore> spawnSingleHex(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ownerRef,
            Ref<EntityStore> castingRootRef, Hex hex) {

        String firstGlyphId = hex.getFirstGlyphId();
        Glyph firstGlyph = firstGlyphId != null ? hex.get(firstGlyphId) : null;
        if (firstGlyph == null) {
            LOGGER.atWarning().log("skipping malformed in-air hex (no entry point): hexId=%s", hex.getHexId());
            return null;
        }

        TransformComponent ownerTransform = accessor.getComponent(ownerRef, TransformComponent.getComponentType());
        Vector3d ownerPos = ownerTransform.getPosition();
        HeadRotation headRotation = accessor.getComponent(ownerRef, HeadRotation.getComponentType());
        Rotation3f ownerRotation = headRotation.getRotation();

        Rotation3f rot = new Rotation3f(ownerRotation.x, ownerRotation.y, IN_AIR_SPAWN_DISTANCE);
        Vector3d position = GlyphMath.sphericalToCartesian(rot);

        HexComponent hexComponent = new HexComponent(hex);
        hexComponent.setRootRef(castingRootRef);
        hexComponent.setParentRef(castingRootRef);
        hexComponent.setOffset(new Vector3f((float) position.x, (float) position.y, (float) position.z));
        hexComponent.setRotation(new Rotation3f(rot.x, rot.y, rot.z));
        Ref<EntityStore> hexRef = CreateHex.createHexEntity(accessor, hexComponent, ownerPos);
        hexComponent.setSelfRef(hexRef);

        int numGlyphs = (int) hex.getGlyphs().stream().filter(g -> g != null).count();
        float scaleMultiplier = 1 + (numGlyphs * GlyphStyler.SCALE_PER_GLYPH);

        GlyphComponent firstGlyphComponent = new GlyphComponent(firstGlyph);
        firstGlyphComponent.setHexRef(hexRef);
        firstGlyphComponent.setParentRef(hexRef);
        firstGlyphComponent.setOffset(new Vector3f());
        firstGlyphComponent.setRotation(new Rotation3f(rot.x, rot.y, rot.z));
        firstGlyphComponent.setScale(scaleMultiplier);
        hexComponent.setScale(scaleMultiplier);

        GlyphSpawner.spawnGlyphs(accessor, hexComponent, firstGlyphComponent, ownerPos, new Rotation3f(rot.x, rot.y, 0f));
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

        GlyphStyler.UpdateHexTree(accessor, droppedOnHex, rootGlyphComponent);
    }
}
