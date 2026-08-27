package com.riprod.hexcode.builtin.hexCore.scene;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.utils.CleanupUtils;

public class CraftingDragHandler {

    public static Ref<EntityStore> startDrag(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> playerRef, Ref<EntityStore> entityRef) {
        float eyeHeight = 0f;
        ModelComponent modelComp = accessor.getComponent(playerRef, ModelComponent.getComponentType());
        if (modelComp != null && modelComp.getModel() != null) {
            eyeHeight = modelComp.getModel().getEyeHeight(playerRef, accessor);
        }

        Ref<EntityStore> headAnchorRef = CreateGlyph.createHeadAnchor(accessor, playerRef, eyeHeight);

        accessor.putComponent(entityRef, MountedComponent.getComponentType(),
                new MountedComponent(headAnchorRef, new Vector3f(0, 0, -2f), MountController.Minecart));

        return headAnchorRef;
    }

    public static void updateDrag(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> headAnchorRef, Ref<EntityStore> playerRef) {
        HeadRotation headRot = accessor.getComponent(playerRef, HeadRotation.getComponentType());
        if (headRot == null || headAnchorRef == null || !headAnchorRef.isValid()) return;

        TransformComponent headTransform = accessor.getComponent(headAnchorRef,
                TransformComponent.getComponentType());
        headTransform.getRotation().set(
                headRot.getRotation().x,
                headRot.getRotation().y,
                0f);
    }

    public static void endDrag(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> entityRef, Ref<EntityStore> headAnchorRef,
            HexcasterCraftingComponent craftingComp) {
        if (entityRef != null && entityRef.isValid()) {
            accessor.tryRemoveComponent(entityRef, MountedComponent.getComponentType());
        }
        if (headAnchorRef != null) {
            if (headAnchorRef.isValid()) {
                accessor.tryRemoveComponent(headAnchorRef, MountedComponent.getComponentType());
                CleanupUtils.safeRemoveMountParent(accessor, headAnchorRef);
            } else if (craftingComp != null) {
                craftingComp.addPendingDespawn(headAnchorRef);
            }
        }
    }
}
