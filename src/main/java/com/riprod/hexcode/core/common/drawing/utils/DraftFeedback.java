package com.riprod.hexcode.core.common.drawing.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.utils.GlyphMath;
import com.riprod.hexcode.utils.VfxUtil;

public final class DraftFeedback {
    private static final String FAIL_PARTICLE = "Hexcode_Drawing_Dot";
    private static final String FAIL_SOUND = "SFX_Hexcode_Node_Unlink";
    private static final float FORWARD_DISTANCE = 2.0f;

    private DraftFeedback() {
    }

    public static void playFailFeedback(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> playerRef) {
        Vector3d pos = forwardOfEye(accessor, playerRef);
        if (pos == null) {
            return;
        }
        VfxUtil.effect(FAIL_PARTICLE, FAIL_SOUND, pos, accessor);
    }

    public static Vector3d forwardOfEye(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> playerRef) {
        TransformComponent transform = accessor.getComponent(playerRef, TransformComponent.getComponentType());
        ModelComponent modelComp = accessor.getComponent(playerRef, ModelComponent.getComponentType());
        HeadRotation head = accessor.getComponent(playerRef, HeadRotation.getComponentType());
        if (transform == null || modelComp == null || modelComp.getModel() == null || head == null) {
            return null;
        }
        float eyeHeight = modelComp.getModel().getEyeHeight();
        Vector3d eye = new Vector3d(transform.getPosition()).add(0, eyeHeight, 0);
        return GlyphMath.sphericalToCartesian(eye, head.getRotation().y,
                head.getRotation().x, FORWARD_DISTANCE);
    }
}
