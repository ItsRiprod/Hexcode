package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.components.RotationState;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.utils.RotationUtils;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;

import com.riprod.hexcode.utils.BlockUtils;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VelocityUtil;

public class RotationValue implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String ID = "Rotation";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(RotationConfig.class, RotationConfig.CODEC);
    }

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar xVar = glyph.readSlot(RotationValueSlots.X, hexContext);
        HexVar yVar = glyph.readSlot(RotationValueSlots.Y, hexContext);
        HexVar zVar = glyph.readSlot(RotationValueSlots.Z, hexContext);

        var accessor = hexContext.getAccessor();
        return new RotationVar(new Rotation3f(
                (float) HexVarUtil.rotationAxis(xVar, 0, accessor),
                (float) HexVarUtil.rotationAxis(yVar, 1, accessor),
                (float) HexVarUtil.rotationAxis(zVar, 2, accessor)));
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getOwnVariable(glyph.getId());

        if (self != null) {
            return self;
        }

        return compute(glyph, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        if (result instanceof RotationVar rotVar && rotVar.getValue() != null) {
            HexVar target = glyph.readSlot(RotationValueSlots.TARGET, hexContext);
            EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
            if (entityVar != null) {
                applyToEntity(entityVar, rotVar.getValue(), glyph, hexContext);
            } else {
                BlockVar blockVar = HexVarUtil.resolveBlockVar(target, hexContext);
                if (blockVar != null && blockVar.getValue() != null) {
                    applyToBlock(blockVar.getValue(), rotVar.getValue(), hexContext);
                }
            }
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void applyToEntity(EntityVar entityVar, Rotation3f rotation, Glyph glyph,
            HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return;

        World world = accessor.getExternalData().getWorld();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(world, caster, accessor, ref)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            return;
        }

        if (VelocityUtil.isPhysicsTicked(ref, accessor)) {
            applyToProjectile(ref, rotation, hexContext);
            return;
        }

        try {
            HeadRotation hr = accessor.getComponent(ref, HeadRotation.getComponentType());
            float priorRoll = hr != null ? hr.getRotation().roll() : 0.0f;

            if (!RotationUtils.applyExact(ref, rotation, accessor))
                return;

            chargePlayerCost(ref, glyph, hexContext);
            trackRollRestore(ref, rotation, priorRoll, glyph, hexContext);
        } catch (Exception e) {
            LOGGER.atWarning().log("rotation glyph: could not rotate entity: %s", e.getMessage());
        }
    }

    private void chargePlayerCost(Ref<EntityStore> ref, Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor.getComponent(ref, Player.getComponentType()) == null)
            return;
        VolatilityComponent volatility = hexContext.volatility();
        if (volatility == null)
            return;
        volatility.consume(resolveConfig(glyph).getPlayerVolatilityCost());
    }

    private RotationConfig resolveConfig(Glyph glyph) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        RotationConfig config = getConfig(RotationConfig.class, asset);
        return config == null ? RotationConfig.DEFAULTS : config;
    }

    private void trackRollRestore(Ref<EntityStore> ref, Rotation3f applied, float priorRoll,
            Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor.getComponent(ref, PlayerRef.getComponentType()) == null)
            return;
        if (Math.abs(applied.roll()) <= RotationUtils.EPSILON_RADIANS)
            return;

        RotationConfig config = resolveConfig(glyph);

        RotationState existing = findRollRestore(ref, accessor);
        if (existing != null) {
            existing.setRemainingSeconds(config.getRollHoldSeconds());
            return;
        }

        if (HexConstructSpawner.hasPendingApply(accessor, ref, ID))
            return;

        HexConstructSpawner.applyWithState(accessor, ref, hexContext, glyph, ID,
                new RotationState(priorRoll, config.getRollHoldSeconds()));
    }

    private static RotationState findRollRestore(Ref<EntityStore> ref,
            CommandBuffer<EntityStore> accessor) {
        HexEffectsComponent effects = accessor.getComponent(ref,
                HexEffectsComponent.getComponentType());
        if (effects == null)
            return null;

        for (HexStatus<?> status : effects.getEffects().values()) {
            if (status != null && ID.equals(status.getHandlerId())
                    && status.getState() instanceof RotationState rotationState) {
                return rotationState;
            }
        }
        return null;
    }

    private void applyToProjectile(Ref<EntityStore> ref, Rotation3f rotation, HexContext hexContext) {
        Velocity vel = hexContext.getAccessor().getComponent(ref, Velocity.getComponentType());
        double speed = 0.0;
        if (vel != null) {
            Vector3d current = vel.getVelocity();
            if (current != null) {
                speed = Math.sqrt(current.x * current.x + current.y * current.y + current.z * current.z);
            }

        }
        if (speed <= 0.0001)
            speed = 1.0;

        Vector3d newVel = Vector3dUtil
                .setYawPitch(rotation.yaw(), rotation.pitch(), new Vector3d())
                .mul(speed);

        try {
            VelocityUtil.applyVelocity(ref, newVel, ChangeVelocityType.Set,
                    new VelocityConfig(), hexContext.getAccessor());
        } catch (Exception e) {
            LOGGER.atWarning().log("rotation glyph: could not re-aim projectile: %s", e.getMessage());
        }
    }

    private void applyToBlock(Vector3i pos, Rotation3f rotation, HexContext hexContext) {
        try {
            CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
            World world = accessor.getExternalData().getWorld();
            Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
            BlockUtils.rotateBlock(new Vector3i(pos), rotation, world, accessor, caster, getId());
        } catch (Exception e) {
            LOGGER.atWarning().log("rotation glyph: could not rotate block: %s", e.getMessage());
        }
    }
}
