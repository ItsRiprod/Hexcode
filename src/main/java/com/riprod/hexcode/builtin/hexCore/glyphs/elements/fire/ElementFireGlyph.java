package com.riprod.hexcode.builtin.hexCore.glyphs.elements.fire;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
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

public class ElementFireGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String ID = "ElementFire";

    // poc: element identity is the raw element name, shared by cause/status/element-def
    private static final String DAMAGE_CAUSE_ID = "Fire";

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
                    "ElementFire must target an entity");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            LOGGER.atWarning().log("ElementFire: entity target ref invalid");
            return;
        }

        float power = hexContext.consumeComplexity();
        DamageCause cause = DamageCause.getAssetMap().getAsset(DAMAGE_CAUSE_ID);
        if (cause == null) {
            LOGGER.atWarning().log("ElementFire: %s damage cause not found", DAMAGE_CAUSE_ID);
        } else {
            Damage damage = new Damage(new Damage.EnvironmentSource("ums_element_fire"), cause, power);
            DamageSystems.executeDamage(targetRef, accessor, damage);
            LOGGER.atInfo().log("ElementFire dealt %.2f fire damage", power);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
