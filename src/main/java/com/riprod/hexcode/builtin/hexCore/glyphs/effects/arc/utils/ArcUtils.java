package com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ArcUtils {

    private ArcUtils() {
    }

    public static Ref<EntityStore> getNextArcTarget(
            Vector3d fromPosition, float maxDistance, Set<UUID> visited, CommandBuffer<EntityStore> buffer) {

        List<Ref<EntityStore>> candidates = new ArrayList<>();

        Selector.selectNearbyEntities(buffer, fromPosition, maxDistance, candidates::add, ref -> {
            UUIDComponent uuid = buffer.getComponent(ref, UUIDComponent.getComponentType());
            if (uuid == null) return false;
            if (visited.contains(uuid.getUuid())) return false;

            TransformComponent tc = buffer.getComponent(ref, TransformComponent.getComponentType());
            return tc != null;
        });

        if (candidates.isEmpty()) return null;

        Ref<EntityStore> best = null;
        double bestDistSq = Double.POSITIVE_INFINITY;
        for (Ref<EntityStore> candidate : candidates) {
            TransformComponent tc = buffer.getComponent(candidate, TransformComponent.getComponentType());
            if (tc == null) continue;
            Vector3d p = tc.getPosition();
            double dx = p.x - fromPosition.x;
            double dy = p.y - fromPosition.y;
            double dz = p.z - fromPosition.z;
            double dSq = dx * dx + dy * dy + dz * dz;
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                best = candidate;
            }
        }
        return best;
    }
}
