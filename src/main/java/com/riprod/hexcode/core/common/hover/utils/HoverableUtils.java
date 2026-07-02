package com.riprod.hexcode.core.common.hover.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector2d;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.hover.system.HoverableSpatialSystem;
import com.riprod.hexcode.core.common.node.component.NodeComponent;

public class HoverableUtils {

    public static List<Ref<EntityStore>> getNearbyHoverables(CommandBuffer<EntityStore> accessor, Vector3d position,
            double range) {
        SpatialResource<Ref<EntityStore>, EntityStore> spatial = accessor
                .getResource(HoverableSpatialSystem.getResourceType());

        List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        spatial.getSpatialStructure().collect(position, range, results);

        return results;
    }

    public static Ref<EntityStore> getSmallestTarget(CommandBuffer<EntityStore> accessor, Ref<EntityStore> playerRef,
            List<Ref<EntityStore>> targetRefs) {
        return getSmallestTarget(accessor, playerRef, targetRefs, null);
    }

    public static Ref<EntityStore> getSmallestTarget(CommandBuffer<EntityStore> accessor, Ref<EntityStore> playerRef,
            List<Ref<EntityStore>> targetRefs, @Nullable List<HoverableType> types) {
        Transform look = TargetUtil.getLook(playerRef, accessor);
        Vector3d rayStart = look.getPosition();
        Vector3d rayDir = look.getDirection();

        double firstTMax = Double.MAX_VALUE;
        double firstTMin = Double.MAX_VALUE;
        double bestVolume = Double.MAX_VALUE;

        Ref<EntityStore> best = null;

        for (int i = 0; i < targetRefs.size(); i++) {
            Ref<EntityStore> targetRef = targetRefs.get(i);
            if (targetRef == null || !targetRef.isValid())
                continue;

            if (types != null) {
                HoverableComponent comp = accessor.getComponent(targetRef, HoverableComponent.getComponentType());
                if (comp == null || !types.contains(comp.getType()))
                    continue;
            }

            Vector2d minMax = rayIntersect(accessor, targetRef, rayStart, rayDir);

            if (minMax == null)
                continue;

            if (minMax.x < 0)
                continue;

            if (minMax.x < firstTMin) { 
                firstTMin = minMax.x;
                firstTMax = minMax.y;
            }

            if (minMax.x > firstTMax) {
                continue;
            }

            double boxVolume = boundingVolume(accessor, targetRef);
            if (boxVolume < bestVolume) {
                bestVolume = boxVolume;
                best = targetRef;
            }
        }

        return best;
    }

    private static Vector2d rayIntersect(CommandBuffer<EntityStore> accessor, Ref<EntityStore> targetRef,
            Vector3d rayStart, Vector3d rayDir) {

        TransformComponent transform = accessor.getComponent(targetRef, TransformComponent.getComponentType());

        BoundingBox box = accessor.getComponent(targetRef, BoundingBox.getComponentType());

        if (box == null || transform == null)
            return null;

        Vector3d pos = transform.getPosition();
        Vector2d minMax = new Vector2d();
        boolean hit = CollisionMath.intersectRayAABB(rayStart, rayDir, pos.x, pos.y, pos.z, box.getBoundingBox(),
                minMax);

        return hit ? minMax : null;
    }

    private static double boundingVolume(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref) {
        BoundingBox bbox = accessor.getComponent(ref, BoundingBox.getComponentType());

        if (bbox == null) {
            return Double.MAX_VALUE;
        }

        Box box = bbox.getBoundingBox();
        Vector3d min = box.getMin();
        Vector3d max = box.getMax();
        return (max.x - min.x) * (max.y - min.y) * (max.z - min.z);
    }

    public static void ensureHoverable(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref, HoverableType type) {
        HoverableComponent comp = new HoverableComponent(type, ref);
        accessor.putComponent(ref, HoverableComponent.getComponentType(), comp);
    }

    public static void ensureHoverable(Holder<EntityStore> holder, HoverableType type) {
        holder.addComponent(HoverableComponent.getComponentType(), new HoverableComponent(type));
    }

    public static Ref<EntityStore> getGlyphFromHoverable(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> targetRef) {
        GlyphComponent glyphComp = buffer.getComponent(targetRef, GlyphComponent.getComponentType());
        if (glyphComp != null)
            return targetRef;

        NodeComponent nodeComp = buffer.getComponent(targetRef, NodeComponent.getComponentType());
        if (nodeComp != null)
            return nodeComp.getParentEntity();

        HexComponent hexComp = buffer.getComponent(targetRef, HexComponent.getComponentType());
        if (hexComp != null && hexComp.getHex() != null) {
            String firstId = hexComp.getHex().getFirstGlyphId();
            if (firstId != null) {
                return hexComp.getChildGlyphRef(firstId);
            }
        }

        return null;
    }
}
