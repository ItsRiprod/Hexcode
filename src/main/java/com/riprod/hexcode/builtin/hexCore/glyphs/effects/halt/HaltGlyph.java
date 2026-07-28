package com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt.style.HaltStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VelocityUtil;

import java.util.Arrays;

public class HaltGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Halt";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(HaltConfig.class, HaltConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(HaltGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target ref unresolved");
            return;
        }

        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(accessor.getExternalData().getWorld(), caster, accessor, ref)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        HaltConfig config = getConfig(HaltConfig.class, asset);
        if (config == null) config = HaltConfig.DEFAULTS;

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(HaltGlyphSlots.DURATION, hexContext),
                asset.getSlot(HaltGlyphSlots.DURATION));

        try {
            if (VelocityUtil.isPhysicsTicked(ref, accessor)) {
                StandardPhysicsProvider physics = accessor.getComponent(ref,
                        StandardPhysicsProvider.getComponentType());
                physics.getForceProviderStandardState().nextTickVelocity.set(0d, 0d, 0d);
                if (duration > 0) {
                    physics.setState(StandardPhysicsProvider.STATE.INACTIVE);
                }
            } else {
                Velocity vel = accessor.getComponent(ref, Velocity.getComponentType());
                if (vel != null) {
                    vel.getInstructions().clear();
                    vel.addInstruction(new Vector3d(), null, ChangeVelocityType.Set);
                }
            }

            if (duration > 0) {
                HexConstructSpawner.applyWithState(accessor, ref, hexContext, glyph, HaltGlyph.ID,
                        new HaltState((float) duration, config.getEffectId(), glyph.getNextLinks()));

                Slot immediate = glyph.getSlot(HaltGlyphSlots.IMMEDIATE);
                if (immediate != null && immediate.getLinks().length > 0) {
                    HexContext immediateCtx = hexContext.branch();
                    immediateCtx.setDefaultVariable(entityVar);
                    HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
                }
            }

            if (duration > 0) {
                VfxUtil.applyBoundedEffect(hexContext, ref, glyph, config.getEffectId(),
                        (float) duration, OverlapBehavior.OVERWRITE);
            }

            TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
            if (tc != null) {
                HaltStyle.render(tc.getPosition(), hexContext, accessor);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("halt: could not halt entity: %s", e.getMessage());
        }

        if (duration <= 0) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }
}
