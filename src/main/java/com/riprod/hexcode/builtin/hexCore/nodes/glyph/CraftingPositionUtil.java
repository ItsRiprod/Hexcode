package com.riprod.hexcode.builtin.hexCore.nodes.glyph;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

public class CraftingPositionUtil {

    public static Vector3f lookToHexOffset(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> playerRef, Ref<EntityStore> hexRootRef, float distance) {
        Transform look = TargetUtil.getLook(playerRef, accessor);
        Vector3d rayStart = look.getPosition();
        Vector3d rayDir = look.getDirection();

        Vector3d worldPoint = new Vector3d(
            rayStart.x + rayDir.x * distance,
            rayStart.y + rayDir.y * distance,
            rayStart.z + rayDir.z * distance
        );

        TransformComponent rootTransform = accessor.getComponent(hexRootRef,
                TransformComponent.getComponentType());
        Vector3d rootPos = rootTransform.getPosition();

        return new Vector3f(
            (float)(worldPoint.x - rootPos.x),
            (float)(worldPoint.y - rootPos.y),
            (float)(worldPoint.z - rootPos.z)
        );
    }

    public static Vector3f worldToHexOffset(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> hexRootRef, Vector3d worldPos) {
        TransformComponent rootTransform = accessor.getComponent(hexRootRef,
                TransformComponent.getComponentType());
        Vector3d rootPos = rootTransform.getPosition();
        return new Vector3f(
            (float)(worldPos.x - rootPos.x),
            (float)(worldPos.y - rootPos.y),
            (float)(worldPos.z - rootPos.z));
    }

    public static Vector3d hexOffsetToWorld(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> hexRootRef, Vector3f offset) {
        TransformComponent rootTransform = accessor.getComponent(hexRootRef,
                TransformComponent.getComponentType());
        Vector3d rootPos = rootTransform.getPosition();

        return new Vector3d(
            rootPos.x + offset.x,
            rootPos.y + offset.y,
            rootPos.z + offset.z
        );
    }
}
