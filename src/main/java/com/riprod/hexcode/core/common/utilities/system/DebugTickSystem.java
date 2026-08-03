package com.riprod.hexcode.core.common.utilities.system;

import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

public class DebugTickSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final double VIEW_DISTANCE = 75.0;

    @Override
    public Query<EntityStore> getQuery() {
        return DebugComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {

        try {
            DebugComponent debug = chunk.getComponent(index, DebugComponent.getComponentType());
            if (debug == null) {
                return;
            }

            debug.setTimer(debug.getTimer() - dt);
            if (debug.getTimer() > 0) {
                return;
            }

            debug.setTimer(debug.getRespawnInterval());

            if (debug.getOpacity() <= 0f) {
                return;
            }

            TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
            if (transform == null) {
                return;
            }

            Vector3d pos = transform.getPosition();

            MountedComponent mount = chunk.getComponent(index, MountedComponent.getComponentType());
            if (mount != null) {
                Ref<EntityStore> parentRef = mount.getMountedToEntity();
                if (parentRef != null && parentRef.isValid()) {
                    TransformComponent parentTransform = store.getComponent(parentRef,
                            TransformComponent.getComponentType());
                    if (parentTransform != null) {
                        Vector3d parentPos = parentTransform.getPosition();
                        Rotation3f offset = mount.getAttachmentOffset();
                        pos = new Vector3d(
                                parentPos.x + offset.x(),
                                parentPos.y + offset.y(),
                                parentPos.z + offset.z());
                    }
                }
            }

            Vector3d scale = debug.getScale();
            Matrix4d matrix = new Matrix4d();
            matrix.identity();
            matrix.translate(pos.x, pos.y, pos.z);
            matrix.scale(scale.x, scale.y, scale.z);

            Ref<EntityStore> targetRef = debug.getTargetRef();
            if (targetRef != null && targetRef.isValid()) {
                PlayerRef playerRef = store.getComponent(targetRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    playerRef.getPacketHandler().write(buildPacket(debug, matrix));
                }
                return;
            }

            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatial = store
                    .getResource(EntityModule.get().getPlayerSpatialResourceType());
            List<Ref<EntityStore>> viewers = SpatialResource.getThreadLocalReferenceList();
            playerSpatial.getSpatialStructure().collect(pos, VIEW_DISTANCE, viewers);
            if (viewers.isEmpty()) {
                return;
            }

            DisplayDebug packet = buildPacket(debug, matrix);
            for (int i = 0; i < viewers.size(); i++) {
                PlayerRef viewer = store.getComponent(viewers.get(i), PlayerRef.getComponentType());
                if (viewer == null) {
                    continue;
                }
                viewer.getPacketHandler().write(packet);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] DebugTickSystem failed: %s", e.getMessage());
        }
    }

    private static DisplayDebug buildPacket(DebugComponent debug, Matrix4d matrix) {
        return new DisplayDebug(
                debug.getShape(), matrixToFloatArray(matrix),
                new Vector3f(debug.getColor().x, debug.getColor().y, debug.getColor().z),
                debug.getFadeTime(), (byte) debug.getFlags(), null, debug.getOpacity());
    }

    private static float[] matrixToFloatArray(Matrix4d m) {
        float[] arr = new float[16];
        m.get(arr);
        return arr;
    }
}
