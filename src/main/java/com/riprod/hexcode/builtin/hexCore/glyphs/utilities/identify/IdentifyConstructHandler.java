package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify;

import java.util.Iterator;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.IdentifyState.Glow;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.utils.GlowUtil;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.component.HexRoot;

public class IdentifyConstructHandler implements ConstructHandler<IdentifyState> {

    @Override
    public boolean onTick(float dt, HexStatus<IdentifyState> status, ConstructTickContext ctx) {
        IdentifyState state = status.getState();
        if (state == null || state.getGlows().isEmpty()) return true;

        // caster entity is the tick owner; if it is dead/gone stop while it still ticks
        HexRoot root = status.getHexContext().getHexRoot();
        if (root == null || !root.isAlive()) return true;

        state.tick(dt);
        if (state.isExpired()) return true;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Iterator<Glow> it = state.getGlows().iterator();
        while (it.hasNext()) {
            Glow glow = it.next();
            if (!GlowUtil.sendGlow(buffer, glow)) {
                GlowUtil.removeGlow(buffer, glow);
                it.remove();
            }
        }
        if (state.getGlows().isEmpty()) return true;

        // volatility depletion / spell cancel ends the glow (caster owns the lifetime)
        return !drainSustain(dt, status);
    }

    @Override
    public void onCleanup(HexStatus<IdentifyState> status, ConstructTickContext ctx) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        IdentifyState state = status.getState();
        if (state != null) {
            for (Glow glow : state.getGlows()) {
                GlowUtil.removeGlow(buffer, glow);
            }
            state.getGlows().clear();
            GlowUtil.removeCasterEffect(buffer, ctx.getEntityRef(), state.getEffectId());
        }
    }
}
