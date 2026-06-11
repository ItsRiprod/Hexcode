package com.riprod.hexcode.utils;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector2d;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TargetFilter {

    @Nullable
    public static Ref<EntityStore> getSmallestTarget(Ref<EntityStore> looker,
            List<Ref<EntityStore>> candidates, ComponentAccessor<EntityStore> accessor) {

        Transform look = TargetUtil.getLook(looker, accessor);
        return getSmallestTarget(look.getPosition(), look.getDirection(), candidates, accessor, Double.MAX_VALUE);
    }

    @Nullable
    public static Ref<EntityStore> getSmallestTarget(Vector3d rayStart, Vector3d rayDir,
            List<Ref<EntityStore>> candidates, ComponentAccessor<EntityStore> accessor, double maxRange) {

        double firstTMax = Double.MAX_VALUE;
        double firstTMin = Double.MAX_VALUE;

        for (int i = 0; i < candidates.size(); i++) {
            Ref<EntityStore> ref = candidates.get(i);
            if (ref == null || !ref.isValid()) {
                continue;
            }

            double tMin = rayEntryDistance(ref, rayStart, rayDir, accessor);
            if (tMin < 0 || tMin > maxRange) {
                continue;
            }

            if (tMin < firstTMin) {
                firstTMin = tMin;
                firstTMax = rayExitDistance(ref, rayStart, rayDir, accessor);
            }
        }

        if (firstTMin == Double.MAX_VALUE) {
            return null;
        }

        Ref<EntityStore> best = null;
        double bestVolume = Double.MAX_VALUE;

        for (int i = 0; i < candidates.size(); i++) {
            Ref<EntityStore> ref = candidates.get(i);
            if (ref == null || !ref.isValid()) {
                continue;
            }

            double tMin = rayEntryDistance(ref, rayStart, rayDir, accessor);
            if (tMin < 0 || tMin > firstTMax || tMin > maxRange) {
                continue;
            }

            double volume = boundingVolume(ref, accessor);
            if (volume < bestVolume) {
                bestVolume = volume;
                best = ref;
            }
        }

        return best;
    }

    private static double rayEntryDistance(Ref<EntityStore> ref, Vector3d rayStart,
            Vector3d rayDir, ComponentAccessor<EntityStore> accessor) {
        Vector2d minMax = rayIntersect(ref, rayStart, rayDir, accessor);
        return minMax != null ? minMax.x : -1;
    }

    private static double rayExitDistance(Ref<EntityStore> ref, Vector3d rayStart,
            Vector3d rayDir, ComponentAccessor<EntityStore> accessor) {
        Vector2d minMax = rayIntersect(ref, rayStart, rayDir, accessor);
        return minMax != null ? minMax.y : -1;
    }

    @Nullable
    private static Vector2d rayIntersect(Ref<EntityStore> ref, Vector3d rayStart,
            Vector3d rayDir, ComponentAccessor<EntityStore> accessor) {
        TransformComponent transform = accessor.getComponent(ref, TransformComponent.getComponentType());
        BoundingBox bb = accessor.getComponent(ref, BoundingBox.getComponentType());
        if (transform == null || bb == null) {
            return null;
        }

        Vector3d pos = transform.getPosition();
        Vector2d minMax = new Vector2d();
        boolean hit = CollisionMath.intersectRayAABB(
                rayStart, rayDir, pos.x, pos.y, pos.z, bb.getBoundingBox(), minMax);
        return hit ? minMax : null;
    }

    private static double boundingVolume(Ref<EntityStore> ref,
            ComponentAccessor<EntityStore> accessor) {
        BoundingBox bb = accessor.getComponent(ref, BoundingBox.getComponentType());
        if (bb == null) {
            return Double.MAX_VALUE;
        }
        Box box = bb.getBoundingBox();
        Vector3d min = box.getMin();
        Vector3d max = box.getMax();
        return (max.x - min.x) * (max.y - min.y) * (max.z - min.z);
    }
}
