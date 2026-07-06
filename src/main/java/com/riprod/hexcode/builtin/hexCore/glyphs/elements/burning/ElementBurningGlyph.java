package com.riprod.hexcode.builtin.hexCore.glyphs.elements.burning;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class ElementBurningGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String ID = "ElementBurning";

    // poc: element identity is the raw element name; the Fire status carries the DoT + matrix
    private static final String STATUS_EFFECT_ID = "Fire";
    private static final float DURATION_SECONDS = 5.0f;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public float getComplexity(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar subject = hexContext.getDefaultVariable();
        EntityVar entityVar = subject != null ? HexVarUtil.resolveEntityVar(subject, hexContext) : null;
        if (entityVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "ElementBurning must target an entity");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            LOGGER.atWarning().log("ElementBurning: entity target ref invalid");
            return;
        }

        float power = hexContext.consumeComplexity();
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(STATUS_EFFECT_ID);
        if (effect == null) {
            LOGGER.atWarning().log("ElementBurning: %s status effect not found", STATUS_EFFECT_ID);
        } else {
            EffectControllerComponent controller = accessor.getComponent(
                    targetRef, EffectControllerComponent.getComponentType());
            if (controller != null) {
                controller.addEffect(targetRef, effect, DURATION_SECONDS, OverlapBehavior.OVERWRITE, accessor);
                LOGGER.atInfo().log("ElementBurning applied %s status (power %.2f)", STATUS_EFFECT_ID, power);
            }
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
