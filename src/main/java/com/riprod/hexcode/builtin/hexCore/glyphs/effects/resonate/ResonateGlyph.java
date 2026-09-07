package com.riprod.hexcode.builtin.hexCore.glyphs.effects.resonate;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.resonate.style.ResonateStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.utils.ConstructSplicer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class ResonateGlyph implements GlyphHandler {
    public static final String ID = "Resonate";

    @Override
    public String getId() { return ID; }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(ResonateGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targetVar, hexContext);
        if (entityVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be a creature");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
        }

        HexEffectsComponent construct = accessor.getComponent(ref, HexEffectsComponent.getComponentType());
        if (construct != null) {
            resonateConstruct(glyph, hexContext, construct, ref, accessor);
        } else {
            resonateEntity(glyph, hexContext, ref, accessor);
        }
    }

    private void resonateConstruct(Glyph glyph, HexContext hexContext,
            HexEffectsComponent construct, Ref<EntityStore> ref,
            CommandBuffer<EntityStore> accessor) {

        List<HexStatus<?>> targets = new ArrayList<>(construct.getEffects().values());
        if (targets.isEmpty()) {
            Vector3d pos = resolvePosition(ref, accessor);
            if (pos != null) ResonateStyle.renderNoSignal(pos, hexContext, accessor);
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target has no active effects");
            return;
        }

        for (HexStatus<?> target : targets) {
            ConstructHandler<?> handler = ConstructRegistry.get(target.getHandlerId());
            if (handler == null) continue;

            ConstructSplicer.splice(target, handler, hexContext, glyph,
                    ConstructSplicer.ChainMode.APPEND_TAIL,
                    ConstructSplicer.VariablePolicy.PREFER_TARGET,
                    0f);
        }

        Vector3d pos = resolvePosition(ref, accessor);
        if (pos != null) ResonateStyle.renderResonate(pos, hexContext, accessor);
    }

    private void resonateEntity(Glyph glyph, HexContext hexContext,
            Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {

        EntityStatMap targetStats = accessor.getComponent(ref, EntityStatMap.getComponentType());
        if (targetStats == null) {
            Vector3d pos = resolvePosition(ref, accessor);
            if (pos != null) ResonateStyle.renderNoSignal(pos, hexContext, accessor);
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Resonate: target has no mana pool");
            return;
        }

        Vector3d pos = resolvePosition(ref, accessor);
        if (pos != null) ResonateStyle.renderResonate(pos, hexContext, accessor);
    }

    private Vector3d resolvePosition(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        return tc != null ? tc.getPosition() : null;
    }
}
