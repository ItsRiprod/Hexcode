package com.riprod.hexcode.core.common.drawing;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.utils.CleanupUtils;

public final class DrawAnchorUtils {

    private static final float DEFAULT_EYE_HEIGHT = 1.68f;

    private DrawAnchorUtils() {
    }

    public static Ref<EntityStore> ensureAnchor(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> player, DrawCaptureComponent capture) {
        Ref<EntityStore> existing = capture.getHeadAnchorRef();
        if (existing != null && existing.isValid()) {
            return existing;
        }
        Ref<EntityStore> anchor = CreateGlyph.createHeadAnchor(buffer, player, resolveEyeHeight(buffer, player));
        capture.setHeadAnchorRef(anchor);
        return anchor;
    }

    public static void rotateToHead(CommandBuffer<EntityStore> buffer, DrawCaptureComponent capture,
            HeadRotation head) {
        if (head == null) {
            return;
        }
        Ref<EntityStore> anchor = capture.getHeadAnchorRef();
        if (anchor == null || !anchor.isValid()) {
            return;
        }
        TransformComponent transform = buffer.getComponent(anchor, TransformComponent.getComponentType());
        if (transform != null) {
            transform.getRotation().set(head.getRotation().x, head.getRotation().y, 0f);
        }
    }

    public static void removeAnchor(CommandBuffer<EntityStore> buffer, DrawCaptureComponent capture) {
        Ref<EntityStore> anchor = capture.getHeadAnchorRef();
        if (anchor != null && anchor.isValid()) {
            buffer.tryRemoveComponent(anchor, MountedComponent.getComponentType());
            CleanupUtils.safeRemoveMountParent(buffer, anchor);
        }
        capture.setHeadAnchorRef(null);
    }

    private static float resolveEyeHeight(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        ModelComponent modelComp = buffer.getComponent(player, ModelComponent.getComponentType());
        if (modelComp != null && modelComp.getModel() != null) {
            return modelComp.getModel().getEyeHeight(player, buffer);
        }
        return DEFAULT_EYE_HEIGHT;
    }
}
