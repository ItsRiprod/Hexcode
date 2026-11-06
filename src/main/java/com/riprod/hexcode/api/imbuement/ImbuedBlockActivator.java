package com.riprod.hexcode.api.imbuement;

import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.riprod.hexcode.utils.BlockAccess;
import com.hypixel.hytale.server.core.universe.world.World;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.cast.component.CastHexComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastOriginComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastPolicyComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastEffectsComponent;
import com.riprod.hexcode.core.common.execution.cast.component.DefaultVariableComponent;
import com.riprod.hexcode.core.common.execution.cast.component.ManaSourceComponent;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastHolder;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.ImbuementMetadata;
import com.riprod.hexcode.core.common.imbuement.asset.EssenceAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.block.BlockImbuementCapacity;
import com.riprod.hexcode.core.common.imbuement.block.EssenceRefill;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedBlockComponent;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementProfileUtils;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ImbuedBlockActivator {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public enum ActivationStatus {
        READY_FROM_SLOT,
        READY_FROM_ESSENCE,
        NO_HEX,
        NO_SLOT_NO_ESSENCE,
        EXECUTION_FAILED
    }

    private ImbuedBlockActivator() {
    }

    @Nonnull
    public static ActivationOutcome tryConsume(@Nonnull World world, @Nonnull Vector3i blockPos,
            @Nullable Ref<EntityStore> root, @Nonnull CommandBuffer<EntityStore> buffer) {
        if (root == null || !root.isValid()) return ActivationOutcome.noHex();

        ImbuedBlockComponent comp = BlockModule.getComponent(
                ImbuedBlockComponent.getComponentType(), world,
                blockPos.x, blockPos.y, blockPos.z);
        if (comp == null) return ActivationOutcome.noHex();

        ImbuementData base = comp.read(ImbuementMetadata.DEFAULT_SLOT);
        if (base == null) return ActivationOutcome.noHex();

        BlockType blockType = BlockAccess.blockType(world, blockPos);
        BlockImbuementCapacity.Capacity capacity = BlockImbuementCapacity.tryFor(blockType);
        if (capacity == null) return ActivationOutcome.noHex();

        EssenceAsset essence = null;
        ActivationStatus status;
        if (comp.getSlotsReady() > 0) {
            comp.setSlotsReady(comp.getSlotsReady() - 1);
            markDirty(world, blockPos);
            status = ActivationStatus.READY_FROM_SLOT;
        } else {
            essence = EssenceRefill.tryConsume(world, blockPos);
            if (essence == null) return ActivationOutcome.noSlotNoEssence();
            status = ActivationStatus.READY_FROM_ESSENCE;
        }

        Hex hex = ImbuementUtils.resolveHex(base, world.getEntityStore().getStore());
        if (hex == null) {
            LOGGER.atWarning().log("[hexcode] block at %s has unresolvable hex", blockPos);
            return new ActivationOutcome(ActivationStatus.EXECUTION_FAILED, base);
        }

        ImbuementData baseSnapshot = base.copy();
        var holder = new CastHolder(root);

        ImbuementProfileAsset profile = resolveProfile(blockType);
        HexConfigAsset profileDefaults = profile != null ? profile.getDefaults() : null;
        if (profileDefaults != null) profileDefaults.queueOnto(holder, buffer);

        var stats = new VolatilityComponent();
        stats.setInitial((float) capacity.getSlots() * capacity.getVolatilityPerSlot());
        holder.overlay(VolatilityComponent.TYPE, stats);

        if (essence != null) {
            float vm = essence.getVolatilityMultiplier();
            if (vm != 1.0f) {
                holder.overlay(VolatilityComponent.TYPE, target ->
                        target.setVolatilityMultiplier(target.getVolatilityMultiplier() * vm));
            }
            var effects = essence.getEffects();
            if (effects != null) {
                holder.overlay(CastEffectsComponent.TYPE, target -> target.mutable().compose(effects));
            }
        }

        HexConfigAsset overrides = base.getOverrides();
        if (overrides != null) overrides.queueOnto(holder, buffer);

        holder.put(CastHexComponent.TYPE, new CastHexComponent(hex));
        holder.put(ManaSourceComponent.TYPE, ManaSourceComponent.free());
        holder.put(CastOriginComponent.TYPE, new CastOriginComponent(
                new Vector3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5),
                new Rotation3f(), new BlockVar(blockPos)));
        holder.put(DefaultVariableComponent.TYPE, new DefaultVariableComponent(new BlockVar(blockPos)));

        holder.overlay(CastPolicyComponent.TYPE, target -> {
            target.setRequireMagicCharges(false);
            target.setApplyVolatilityDecay(false);
        });

        HexExecuter.cast(holder, buffer);

        return new ActivationOutcome(status, baseSnapshot);
    }

    private static void markDirty(@Nonnull World world, @Nonnull Vector3i pos) {
        BlockModule.BlockStateInfo info = BlockModule.getComponent(
                BlockModule.BlockStateInfo.getComponentType(), world, pos.x, pos.y, pos.z);
        if (info != null) info.markNeedsSaving();
    }

    @Nullable
    private static ImbuementProfileAsset resolveProfile(@Nullable BlockType blockType) {
        if (blockType == null) return null;
        Item item = blockType.getItem();
        if (item == null) return null;
        return ImbuementProfileUtils.first(item.getCategories());
    }

    public static final class ActivationOutcome {
        private final ActivationStatus status;
        @Nullable private final ImbuementData castData;

        ActivationOutcome(ActivationStatus status, @Nullable ImbuementData castData) {
            this.status = status;
            this.castData = castData;
        }

        @Nonnull public ActivationStatus getStatus() { return status; }
        @Nullable public ImbuementData getCastData() { return castData; }
        public boolean isReady() {
            return status == ActivationStatus.READY_FROM_SLOT
                    || status == ActivationStatus.READY_FROM_ESSENCE;
        }

        static ActivationOutcome noHex() {
            return new ActivationOutcome(ActivationStatus.NO_HEX, null);
        }
        static ActivationOutcome noSlotNoEssence() {
            return new ActivationOutcome(ActivationStatus.NO_SLOT_NO_ESSENCE, null);
        }
    }
}
