package com.riprod.hexcode.builtin.hexCore.pedestals;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;

public class HoverStyleUtils {

    private static final String HOVER_PARTICLE = "Object_Hover";

    public static void unhover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> unhoveredRef,
            Ref<EntityStore> playerRef) {

        if (unhoveredRef == null || !unhoveredRef.isValid())
            return;

        HoverableComponent hoveredComponent = accessor.getComponent(unhoveredRef,
                HoverableComponent.getComponentType());
        if (hoveredComponent == null)
            return;

        NodeRouter.unhover(accessor, unhoveredRef, playerRef);
    }

    public static void hover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> hovered,
            Ref<EntityStore> playerRef) {
        if (hovered == null || !hovered.isValid())
            return;

        HoverableComponent hoveredComponent = accessor.getComponent(hovered,
                HoverableComponent.getComponentType());
        if (hoveredComponent == null)
            return;

        NodeRouter.hover(accessor, hovered, playerRef);
    }

    public static void hoverParticles(CommandBuffer<EntityStore> accessor, Ref<EntityStore> hovered, float dt,
            PedestalBlockComponent pedestal) {
        if (hovered == null || !hovered.isValid())
            return;

        GlyphComponent glyph = accessor.getComponent(hovered, GlyphComponent.getComponentType());
        if (glyph == null)
            return;

        if (pedestal.getTickLength(HOVER_PARTICLE) > 0f) {
            pedestal.incrementTickLength(HOVER_PARTICLE, dt);
            return;
        }

        pedestal.setTickLength(HOVER_PARTICLE, -0.5f);
        TransformComponent transform = accessor.getComponent(hovered,
                TransformComponent.getComponentType());
        if (transform == null)
            return;
        Vector3d pos = transform.getPosition();
        ParticleUtil.spawnParticleEffect(HOVER_PARTICLE, pos, accessor);
    }
}
