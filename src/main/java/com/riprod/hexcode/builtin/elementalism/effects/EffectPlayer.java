package com.riprod.hexcode.builtin.elementalism.effects;

import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.utils.VfxUtil;

public final class EffectPlayer {

    private EffectPlayer() {
    }

    public static void play(EffectEntry entry, Vector3d position, ComponentAccessor<EntityStore> accessor) {
        var particle = entry.getParticle();
        if (particle != null && VfxUtil.playersInRange(position, accessor)) {
            VfxUtil.particle(particle, position, entry.getColor(), accessor);
        }
        var sound = entry.getSound();
        if (sound != null) {
            VfxUtil.sound(sound, position, accessor);
        }
    }

    public static void playHit(ResolvedEffects effects, Vector3d position, boolean entity,
            ComponentAccessor<EntityStore> accessor) {
        play(effects.get(EffectSlot.IMPACT), position, accessor);
        play(effects.get(entity ? EffectSlot.ENTITY : EffectSlot.BLOCK), position, accessor);
    }

    public static Vector3d blockCenter(Vector3i block) {
        return new Vector3d(block.x + 0.5, block.y + 0.5, block.z + 0.5);
    }

    @Nullable
    public static Vector3d entityCenter(Ref<EntityStore> ref, ComponentAccessor<EntityStore> accessor) {
        if (!ref.isValid()) {
            return null;
        }
        var transform = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }
        var center = new Vector3d(transform.getPosition());
        var bounds = accessor.getComponent(ref, BoundingBox.getComponentType());
        if (bounds != null) {
            var box = bounds.getBoundingBox();
            center.y += (box.getMin().y + box.getMax().y) * 0.5;
        }
        return center;
    }
}
