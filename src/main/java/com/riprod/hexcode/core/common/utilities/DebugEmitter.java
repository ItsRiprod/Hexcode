package com.riprod.hexcode.core.common.utilities;

import java.util.List;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.matrix.Matrix4dUtil;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3fc;

public final class DebugEmitter {

    public static final double DEFAULT_VIEW_DISTANCE = 75.0;

    private static final ThreadLocal<List<Ref<EntityStore>>> VIEWERS =
            ThreadLocal.withInitial(ObjectArrayList::new);

    private DebugEmitter() {
    }

    public static DisplayDebug packet(DebugShape shape, Matrix4d matrix, Vector3fc color,
            float opacity, float time, int flags) {
        return new DisplayDebug(shape, Matrix4dUtil.asFloatData(matrix), color, time,
                (byte) flags, null, opacity);
    }

    public static void add(ComponentAccessor<EntityStore> accessor, DebugShape shape, Matrix4d matrix,
            Vector3fc color, float opacity, float time, int flags) {
        add(accessor, shape, matrix, color, opacity, time, flags, DEFAULT_VIEW_DISTANCE);
    }

    public static void add(ComponentAccessor<EntityStore> accessor, DebugShape shape, Matrix4d matrix,
            Vector3fc color, float opacity, float time, int flags, double viewDistance) {
        send(accessor, new Vector3d(matrix.m30(), matrix.m31(), matrix.m32()),
                packet(shape, matrix, color, opacity, time, flags), viewDistance);
    }

    public static void send(ComponentAccessor<EntityStore> accessor, Vector3d origin, DisplayDebug packet) {
        send(accessor, origin, packet, DEFAULT_VIEW_DISTANCE);
    }

    public static void send(ComponentAccessor<EntityStore> accessor, Vector3d origin, DisplayDebug packet,
            double viewDistance) {
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatial = accessor
                .getResource(EntityModule.get().getPlayerSpatialResourceType());

        List<Ref<EntityStore>> viewers = VIEWERS.get();
        viewers.clear();
        playerSpatial.getSpatialStructure().collect(origin, viewDistance, viewers);

        for (int i = 0; i < viewers.size(); i++) {
            PlayerRef viewer = accessor.getComponent(viewers.get(i), PlayerRef.getComponentType());
            if (viewer == null) {
                continue;
            }
            viewer.getPacketHandler().write(packet);
        }

        viewers.clear();
    }
}
