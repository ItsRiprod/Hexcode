package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.handler;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.components.RotationState;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.utils.RotationUtils;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;

public class RotationConstructHandler implements ConstructHandler<RotationState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<RotationState> status, ConstructTickContext ctx) {
        RotationState state = status.getState();
        if (state == null) return true;

        if (state.isExpired()) {
            Ref<EntityStore> ref = ctx.getEntityRef();
            if (ref == null || !ref.isValid()) return true;
            return !RotationUtils.teleportInFlight(ref, ctx.getBuffer());
        }

        state.tick(dt);
        return !drainSustain(dt, status);
    }

    @Override
    public void onCleanup(HexStatus<RotationState> status, ConstructTickContext ctx) {
        try {
            RotationState state = status.getState();
            if (state == null) return;

            Ref<EntityStore> ref = ctx.getEntityRef();
            if (ref == null || !ref.isValid()) return;

            CommandBuffer<EntityStore> buffer = ctx.getBuffer();
            HeadRotation hr = buffer.getComponent(ref, HeadRotation.getComponentType());
            if (hr == null) return;

            Rotation3f current = hr.getRotation();
            RotationUtils.applyExact(ref,
                    new Rotation3f(current.pitch(), current.yaw(), state.getPriorRoll()), buffer);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] RotationConstructHandler cleanup failed: %s", e.getMessage());
        }
    }
}
