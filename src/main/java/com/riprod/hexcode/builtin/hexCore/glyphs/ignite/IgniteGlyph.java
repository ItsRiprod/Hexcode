package com.riprod.hexcode.builtin.hexCore.glyphs.ignite;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.ignite.style.IgniteStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class IgniteGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() { return ID; }

    public static final String ID = "Ignite";

    private static final String BURN_EFFECT_ID = "Burn";
    private static final double DEFAULT_DURATION = 5.0;

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(IgniteGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        EntityEffect burnEffect = EntityEffect.getAssetMap().getAsset(BURN_EFFECT_ID);
        if (burnEffect == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Missing asset Burn");
            return;
        }

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double duration = HexVarUtil.numberOrDefault(
                glyph.readSlot(IgniteGlyphSlots.DURATION, hexContext), DEFAULT_DURATION);
        float durationSeconds = (float) duration;

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        try {
            EffectControllerComponent controller = accessor.getComponent(
                    ref, EffectControllerComponent.getComponentType());
            if (controller != null) {
                controller.addEffect(ref, burnEffect, durationSeconds,
                        OverlapBehavior.OVERWRITE, accessor);

                TransformComponent tc = accessor.getComponent(ref,
                        TransformComponent.getComponentType());
                if (tc != null) {
                    IgniteStyle.render(tc.getPosition(), hexContext, accessor);
                }
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("ignite: failed on entity: %s", e.getMessage());
        }

        IgniteState existing = ConstructStateUtil.findState(
                accessor, ref, IgniteGlyph.ID, IgniteState.class);
        if (existing != null) {
            existing.setRemainingDuration(durationSeconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
        } else {
            IgniteState state = new IgniteState(durationSeconds, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, IgniteGlyph.ID, state);
        }
    }
}
