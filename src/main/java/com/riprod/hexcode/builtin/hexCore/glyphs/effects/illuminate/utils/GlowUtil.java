package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.utils;

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
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.IlluminateState;
import com.riprod.hexcode.utils.BlockUtils;

public final class GlowUtil {

    private static final AtomicLong VOLUME_COUNTER = new AtomicLong();

    private GlowUtil() {
    }

    public static String nextVolumeId() {
        return "hxg_" + VOLUME_COUNTER.incrementAndGet();
    }

    // broadcasts a Style-colored box, sized to the owner's hitbox, to every player in the world
    public static void broadcastBox(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ownerRef, IlluminateState state) {
        if (ownerRef == null || !ownerRef.isValid()) return;
        World world = buffer.getExternalData().getWorld();

        double px, py, pz, hx, hy, hz;
        Vector3i blockPos = state.getBlockPos();
        if (blockPos != null) {
            // block-local 0..1 box, pre-rotated; world position = blockPos + box centre
            Box box = BlockUtils.resolveBlockDisplayBox(world, blockPos);
            if (box == null) box = new Box().setMinMax(0, 1);
            px = blockPos.x() + (box.min.x + box.max.x) * 0.5;
            py = blockPos.y() + (box.min.y + box.max.y) * 0.5;
            pz = blockPos.z() + (box.min.z + box.max.z) * 0.5;
            hx = (box.max.x - box.min.x) * 0.5;
            hy = (box.max.y - box.min.y) * 0.5;
            hz = (box.max.z - box.min.z) * 0.5;
        } else {
            TransformComponent transform = buffer.getComponent(ownerRef, TransformComponent.getComponentType());
            if (transform == null) return;
            Vector3d p = transform.getPosition();

            // display Box: position is the center, dimensions are half-extents (see TriggerVolumeManager)
            double cx = 0, cy = 0.5, cz = 0;
            hx = 0.5; hy = 0.5; hz = 0.5;
            BoundingBox bounds = buffer.getComponent(ownerRef, BoundingBox.getComponentType());
            Box box = bounds != null ? bounds.getBoundingBox() : null;
            if (box != null) {
                cx = (box.min.x + box.max.x) * 0.5;
                cy = (box.min.y + box.max.y) * 0.5;
                cz = (box.min.z + box.max.z) * 0.5;
                hx = (box.max.x - box.min.x) * 0.5;
                hy = (box.max.y - box.min.y) * 0.5;
                hz = (box.max.z - box.min.z) * 0.5;
            }
            px = p.x + cx;
            py = p.y + cy;
            pz = p.z + cz;
        }

        TriggerVolumeDisplayEntry entry = new TriggerVolumeDisplayEntry();
        entry.shapeType = TriggerVolumeShapeType.Box;
        entry.color = state.getBoxColor();
        entry.opacity = 0f;
        entry.position = new Vector3f((float) px, (float) py, (float) pz);
        entry.dimensions = new Vector3f((float) hx, (float) hy, (float) hz);

        AddOrUpdateTriggerVolumeDisplay packet = new AddOrUpdateTriggerVolumeDisplay(state.getVolumeId(), entry);
        for (PlayerRef player : world.getPlayerRefs()) {
            player.getPacketHandler().write(packet);
        }
    }

    public static void removeBox(CommandBuffer<EntityStore> buffer, String volumeId) {
        RemoveTriggerVolumeDisplay packet = new RemoveTriggerVolumeDisplay(volumeId);
        World world = buffer.getExternalData().getWorld();
        for (PlayerRef player : world.getPlayerRefs()) {
            player.getPacketHandler().write(packet);
        }
    }
}
