package com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt;

import java.util.Arrays;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt.style.HaltStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VelocityUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class HaltGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Halt";

    private String effectId = "Hexcode_Halt";

    public static final BuilderCodec<HaltGlyph> CODEC = BuilderCodec
            .builder(HaltGlyph.class, HaltGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(HaltGlyphSlots.TARGET, hexContext);
        if (!(targets instanceof EntityVar entityVar)) {

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

        Ref<EntityStore> caster = hexContext.getRootRef();
        if (!HexProtection.canAffectEntity(accessor.getExternalData().getWorld(), caster, accessor, ref)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(HaltGlyphSlots.DURATION, hexContext),
                getSlot(HaltGlyphSlots.DURATION));

        if (duration > 0) {
            HexConstructSpawner.abortExisting(accessor, ref, HaltGlyph.TYPE);
        }

        var spawned = false;
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
                var applied = VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId,
                        (float) duration, OverlapBehavior.OVERWRITE);

                spawned = HexConstructSpawner.applyWithState(accessor, ref, hexContext, glyph, HaltGlyph.TYPE,
                        new HaltState((float) duration, applied ? effectId : null, glyph.getNextLinks())) != null;
                if (!spawned) {
                    HaltConstructHandler.release(accessor, ref, applied ? effectId : null);
                    return;
                }

                Slot immediate = glyph.getSlot(HaltGlyphSlots.IMMEDIATE);
                if (immediate != null && immediate.getLinks().length > 0) {
                    HexContext immediateCtx = hexContext.branch();
                    immediateCtx.setDefaultVariable(entityVar);
                    HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
                }
            }

            TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
            if (tc != null) {
                HaltStyle.render(this, tc.getPosition(), hexContext, accessor);
            }
        } catch (Exception e) {
            if (spawned) {
                LOGGER.atWarning().log("halt: post-spawn step failed: %s", e.getMessage());
                return;
            }
            if (duration > 0) {
                HaltConstructHandler.release(accessor, ref, effectId);
            }
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Could not halt entity", e);
            return;
        }

        if (duration <= 0) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }
}
