package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.utils;

import java.util.concurrent.atomic.AtomicLong;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.packets.player.AddOrUpdateTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.RemoveTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeDisplayEntry;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeShapeType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.IdentifyState.Glow;

public final class GlowUtil {

    private static final AtomicLong VOLUME_COUNTER = new AtomicLong();

    private GlowUtil() {
    }

    public static String nextVolumeId() {
        return "hxg_" + VOLUME_COUNTER.incrementAndGet();
    }

    public static void applyCasterEffect(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> casterRef, String effectId, float seconds) {
        if (casterRef == null || !casterRef.isValid() || effectId == null) return;
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
        if (effect == null) return;
        EffectControllerComponent controller = buffer.getComponent(
                casterRef, EffectControllerComponent.getComponentType());
        if (controller == null) return;
        controller.addEffect(casterRef, effect, seconds, OverlapBehavior.OVERWRITE, buffer);
    }

    public static void removeCasterEffect(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> casterRef, String effectId) {
        if (casterRef == null || !casterRef.isValid() || effectId == null) return;
        EffectControllerComponent controller = buffer.getComponent(
                casterRef, EffectControllerComponent.getComponentType());
        if (controller == null) return;
        int index = EntityEffect.getAssetMap().getIndex(effectId);
        if (index != Integer.MIN_VALUE) {
            controller.removeEffect(casterRef, index, buffer);
        }
    }

    // returns false when the glow can no longer be shown (viewer or target gone)
    public static boolean sendGlow(CommandBuffer<EntityStore> buffer, Glow glow) {
        Ref<EntityStore> viewer = glow.getViewer().getEntity(buffer);
        if (viewer == null || !viewer.isValid()) return false;
        PlayerRef playerRef = buffer.getComponent(viewer, PlayerRef.getComponentType());
        if (playerRef == null) return false;

        TriggerVolumeDisplayEntry entry = new TriggerVolumeDisplayEntry();
        entry.shapeType = TriggerVolumeShapeType.Box;
        entry.color = glow.getColor();
        entry.opacity = 0f;

        if (glow.isEntity()) {
            Ref<EntityStore> target = glow.getTarget().getEntity(buffer);
            if (target == null || !target.isValid()) return false;
            TransformComponent transform = buffer.getComponent(target, TransformComponent.getComponentType());
            if (transform == null) return false;
            Vector3d p = transform.getPosition();
            // display Box: position is the center, dimensions are half-extents (see TriggerVolumeManager)
            double cx = 0, cy = 0.9, cz = 0, hx = 0.5, hy = 0.9, hz = 0.5;
            BoundingBox bounds = buffer.getComponent(target, BoundingBox.getComponentType());
            Box box = bounds != null ? bounds.getBoundingBox() : null;
            if (box != null) {
                cx = (box.min.x + box.max.x) * 0.5;
                cy = (box.min.y + box.max.y) * 0.5;
                cz = (box.min.z + box.max.z) * 0.5;
                hx = (box.max.x - box.min.x) * 0.5;
                hy = (box.max.y - box.min.y) * 0.5;
                hz = (box.max.z - box.min.z) * 0.5;
            }
            entry.position = new Vector3f((float) (p.x + cx), (float) (p.y + cy), (float) (p.z + cz));
            entry.dimensions = new Vector3f((float) hx, (float) hy, (float) hz);
        } else {
            Vector3i b = glow.getBlockPos();
            entry.position = new Vector3f(b.x + 0.5f, b.y + 0.5f, b.z + 0.5f);
            entry.dimensions = new Vector3f(0.5f, 0.5f, 0.5f);
        }

        playerRef.getPacketHandler().write(new AddOrUpdateTriggerVolumeDisplay(glow.getVolumeId(), entry));
        return true;
    }

    public static void removeGlow(CommandBuffer<EntityStore> buffer, Glow glow) {
        Ref<EntityStore> viewer = glow.getViewer().getEntity(buffer);
        if (viewer == null || !viewer.isValid()) return;
        PlayerRef playerRef = buffer.getComponent(viewer, PlayerRef.getComponentType());
        if (playerRef != null) {
            playerRef.getPacketHandler().write(new RemoveTriggerVolumeDisplay(glow.getVolumeId()));
        }
    }
}
