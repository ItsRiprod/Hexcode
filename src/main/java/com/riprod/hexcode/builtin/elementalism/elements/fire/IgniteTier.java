package com.riprod.hexcode.builtin.elementalism.elements.fire;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.FireFluidTicker;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.status.ElementStatuses;
import com.riprod.hexcode.builtin.elementalism.tier.EffectElement;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.utils.BlockUtils;

public class IgniteTier extends EffectElement {
    public static final String TYPE = "Ignite";
    public static final BuilderCodec<IgniteTier> CODEC;

    private float duration = 10;
    private String fluid;
    private byte fluidLevel = 1;
    private String unlitState;

    @Override
    public boolean supportsEntity() {
        return true;
    }

    @Override
    public boolean supportsBlock() {
        return true;
    }

    @Override
    public boolean applyEntity(Ref<EntityStore> target, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var caster = ctx.getRootRef();
        if (!HexProtection.canAffectEntity(buffer.getExternalData().getWorld(), caster, buffer, target)) {
            return false;
        }
        return ElementStatuses.applyEntity(target, getStatusId(), this.duration, caster.isValid() ? caster : null,
                buffer);
    }

    @Override
    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var world = buffer.getExternalData().getWorld();
        var position = new Vector3i(block);
        var caster = ctx.getRootRef();
        var blockType = BlockAccess.blockType(world, position);
        if (blockType == null) {
            return false;
        }
        if (this.unlitState.equals(blockType.getCurrentInteractionState())
                && BlockUtils.useLeavesCurrentState(blockType)) {
            return BlockUtils.triggerUseInteraction(buffer, caster, world, position,
                    () -> ElementStatuses.applyBlock(world, position, getStatusId(), this.duration));
        }
        var fire = flammableFire(blockType);
        if (fire == null || !HexProtection.canModifyBlock(world, caster, buffer,
                new Vector3i(position), BlockAction.PLACE) || !placeFire(world, position, fire)) {
            return false;
        }
        ElementStatuses.applyBlock(world, position, getStatusId(), this.duration);
        return true;
    }

    @Nullable
    private Fluid flammableFire(BlockType blockType) {
        var fire = Fluid.getAssetMap().getAsset(this.fluid);
        var data = blockType.getData();
        if (fire == null || data == null || !(fire.getTicker() instanceof FireFluidTicker ticker)) {
            return null;
        }
        for (var flammability : ticker.getSortedFlammabilityConfigs()) {
            var pattern = flammability.getTagPattern();
            if (pattern != null && pattern.test(data.getTags())) {
                return fire;
            }
        }
        return null;
    }

    private boolean placeFire(World world, Vector3i position, Fluid fire) {
        var sectionRef = BlockAccess.sectionRef(world, position);
        if (sectionRef == null) {
            return false;
        }
        var store = world.getChunkStore().getStore();
        var fluids = store.getComponent(sectionRef, FluidSection.getComponentType());
        var blocks = store.getComponent(sectionRef, BlockSection.getComponentType());
        if (fluids == null || blocks == null
                || fluids.getFluidId(position.x, position.y, position.z) != Fluid.EMPTY_ID) {
            return false;
        }
        fluids.setFluid(position.x, position.y, position.z, fire, this.fluidLevel);
        blocks.setTicking(position.x, position.y, position.z, true);
        return true;
    }

    static {
        CODEC = BuilderCodec.builder(IgniteTier.class, IgniteTier::new, EffectElement.BASE_CODEC)
                .<Float>append(new KeyedCodec<>("Duration", Codec.FLOAT),
                        (t, v) -> t.duration = v, t -> t.duration)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Seconds of the status added to the entity or block that is ignited")
                .add()
                .append(new KeyedCodec<>("Fluid", Codec.STRING),
                        (t, v) -> t.fluid = v, t -> t.fluid)
                .addValidator(Validators.nonNull())
                .addValidator(Fluid.VALIDATOR_CACHE.getValidator())
                .documentation("Fire fluid placed on a flammable block. Its fire ticker decides which blocks are flammable")
                .add()
                .<Byte>append(new KeyedCodec<>("FluidLevel", Codec.BYTE),
                        (t, v) -> t.fluidLevel = v, t -> t.fluidLevel)
                .addValidator(Validators.range((byte) 1, (byte) 15))
                .documentation("Fluid level the fire is placed at")
                .add()
                .append(new KeyedCodec<>("UnlitState", Codec.STRING),
                        (t, v) -> t.unlitState = v, t -> t.unlitState)
                .addValidator(Validators.nonNull())
                .addValidator(Validators.nonEmptyString())
                .documentation("Block state of an unlit candle, torch or lantern. A block in this state runs its Use interaction to light")
                .add()
                .build();
    }
}
