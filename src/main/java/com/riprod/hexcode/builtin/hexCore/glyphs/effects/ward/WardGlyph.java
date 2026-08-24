package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward;

import java.util.Objects;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.style.WardStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.redirect.EntityRedirectSpawner;
import com.riprod.hexcode.utils.HexVarUtil;

public class WardGlyph implements GlyphHandler {

    public static final String ID = "Ward";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(WardConfig.class, WardConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> casterRef = hexContext.getCasterRef(accessor);
        if (casterRef == null || !casterRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Caster not found");
            return;
        }

        EntityVar targetVar = HexVarUtil.resolveEntityVar(
                glyph.readSlot(WardGlyphSlots.TARGET, hexContext), hexContext);
        EntityVar deferralVar = HexVarUtil.resolveEntityVar(
                glyph.readSlot(WardGlyphSlots.DEFERRAL, hexContext), hexContext);
        if (targetVar == null || deferralVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target and Deferral are both required");
            return;
        }

        Ref<EntityStore> targetRef = targetVar.getRawRef(accessor);
        Ref<EntityStore> deferralRef = deferralVar.getRawRef(accessor);
        if (targetRef == null || !targetRef.isValid()
                || deferralRef == null || !deferralRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target or Deferral could not be resolved");
            return;
        }

        PersistentRef target = new PersistentRef();
        target.setEntity(targetRef, accessor);
        PersistentRef deferral = new PersistentRef();
        deferral.setEntity(deferralRef, accessor);
        if (Objects.equals(target.getUuid(), deferral.getUuid())) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target and Deferral cannot be the same entity");
            return;
        }

        EntityRedirectSpawner.stamp(accessor, targetRef, casterRef, deferralRef);

        WardState state = new WardState(target, deferral, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(accessor, casterRef, hexContext, glyph, WardGlyph.ID, state);

        TransformComponent casterTransform = accessor.getComponent(
                casterRef, TransformComponent.getComponentType());
        if (casterTransform != null) {
            WardStyle.renderSpawn(casterTransform.getPosition(), hexContext, accessor);
        }
    }
}
