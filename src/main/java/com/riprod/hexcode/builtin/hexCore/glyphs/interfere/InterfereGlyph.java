package com.riprod.hexcode.builtin.hexCore.glyphs.interfere;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.interfere.style.InterfereStyle;
import com.riprod.hexcode.builtin.hexCore.utils.ConstructSplicer;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class InterfereGlyph implements GlyphHandler {
    public static final String ID = "Interfere";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(InterfereGlyphSlots.TARGET, hexContext);
        if (targetVar == null) {
            HexExecuter.fail(hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targetVar, hexContext);
        if (entityVar == null)
            return;

        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.fail(hexContext);
            return;
        }

        Vector3d pos = resolvePosition(ref, accessor);

        HexEffectsComponent construct = accessor.getComponent(ref, HexEffectsComponent.getComponentType());
        if (construct != null) {
            int hijacked = hijackConstruct(construct, ref, glyph, hexContext, accessor);
            if (hijacked > 0 && pos != null) {
                InterfereStyle.renderHijack(pos, hexContext, accessor);
            }
        }
    }

    private int hijackConstruct(HexEffectsComponent construct, Ref<EntityStore> ref,
            Glyph glyph, HexContext hexContext, CommandBuffer<EntityStore> accessor) {

        List<HexStatus<?>> targets = new ArrayList<>(construct.getEffects().values());
        if (targets.isEmpty())
            return 0;

        HexStats tracker = hexContext.getVolatilityTracker();
        float donation = tracker != null ? tracker.getCurrentVolatility() : 0f;

        ConstructTickContext ctx = new ConstructTickContext(accessor, ref);

        int hijacked = 0;
        for (HexStatus<?> target : targets) {
            ConstructHandler<?> handler = ConstructRegistry.get(target.getHandlerId());
            if (handler == null)
                continue;

            ConstructSplicer.splice(target, handler, hexContext, glyph,
                    ConstructSplicer.ChainMode.REPLACE,
                    ConstructSplicer.VariablePolicy.PREFER_CASTER,
                    donation);

            try {
                invokeOnEnd(handler, target, ctx);
            } catch (Exception e) {
            }

            construct.removeEffect(target.getConstructId());
            hijacked++;
        }

        if (tracker != null && donation > 0f) {
            tracker.setVolatility(0f);
        }
        return hijacked;
    }

    private Vector3d resolvePosition(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        return tc != null ? tc.getPosition() : null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void invokeOnEnd(ConstructHandler<?> handler, HexStatus<?> status, ConstructTickContext ctx) {
        ConstructHandler raw = handler;
        HexStatus rawStatus = status;
        raw.onEnd(rawStatus, ctx);
    }
}
