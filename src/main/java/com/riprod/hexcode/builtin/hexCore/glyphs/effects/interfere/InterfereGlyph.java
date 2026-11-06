package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interfere;

import java.util.List;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.builtin.hexCore.glyphs.utils.ConstructSplicer;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.construct.system.HexConstructSystem;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class InterfereGlyph extends GlyphAsset {
    public static final String TYPE = "Interfere";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(InterfereGlyphSlots.TARGET, hexContext);
        if (targetVar == null) {
            HexExecuter.fail(hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        if (!(targetVar instanceof EntityVar entityVar)) {
            HexExecuter.fail(hexContext);
            return;
        }

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
                EffectRenderer.render(EffectSlot.Start, this, hexContext, pos, accessor);
            }
        }
        HexExecuter.endBranch(hexContext);
    }

    private int hijackConstruct(HexEffectsComponent construct, Ref<EntityStore> ref,
            Glyph glyph, HexContext hexContext, CommandBuffer<EntityStore> accessor) {

        List<HexStatus<?>> targets = ConstructSplicer.spliceable(construct);
        if (targets.isEmpty())
            return 0;

        ConstructTickContext ctx = new ConstructTickContext(accessor, ref);

        int hijacked = 0;
        for (HexStatus<?> target : targets) {
            ConstructHandler<?> handler = ConstructRegistry.get(target.getHandlerId());

            ConstructSplicer.splice(target, handler, hexContext, glyph,
                    ConstructSplicer.ChainMode.REPLACE,
                    ConstructSplicer.VariablePolicy.PREFER_CASTER,
                    0f);

            construct.removeEffect(target.getConstructId());
            HexConstructSystem.end(handler, target, ctx);
            hijacked++;
        }

        return hijacked;
    }

    private Vector3d resolvePosition(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        return tc != null ? tc.getPosition() : null;
    }
}
