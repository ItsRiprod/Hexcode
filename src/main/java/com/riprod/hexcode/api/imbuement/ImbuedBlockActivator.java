package com.riprod.hexcode.api.imbuement;

import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.component.BlockHexRoot;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.VolatilityTracker;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.imbuement.ImbuementMetadata;
import com.riprod.hexcode.core.common.imbuement.asset.EssenceAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementProfileRegistry;
import com.riprod.hexcode.core.common.imbuement.block.BlockImbuementCapacity;
import com.riprod.hexcode.core.common.imbuement.block.EssenceRefill;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedBlockComponent;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
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
    public static ActivationOutcome tryConsume(@Nonnull World world, @Nonnull Vector3i blockPos) {
        ImbuedBlockComponent comp = BlockModule.getComponent(
                ImbuedBlockComponent.getComponentType(), world,
                blockPos.x, blockPos.y, blockPos.z);
        if (comp == null) return ActivationOutcome.noHex();

        ImbuementData base = comp.read(ImbuementMetadata.DEFAULT_SLOT);
        if (base == null) return ActivationOutcome.noHex();

        int blockId = world.getBlock(blockPos.x, blockPos.y, blockPos.z);
        BlockType blockType = blockId == 0 ? null : BlockType.getAssetMap().getAsset(blockId);
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
        BlockHexRoot hexRoot = new BlockHexRoot(blockPos, capacity);
        VolatilityTracker tracker = new VolatilityTracker(hexRoot.resolveVolatility(), 1.0f, 1.0f);
        HexContext context = new HexContext(hex, 0f, hexRoot, null, tracker);

        ImbuementProfileAsset profile = resolveProfile(blockType);
        if (profile != null) context.applyNonDefaultsFrom(profile.getDefaults());
        if (essence != null) applyEssence(context, essence);
        context.applyNonDefaultsFrom(base.getOverrides());
        if (context.getStyle() == null) context.setStyle(HexStyleAsset.empty());
        context.setDefaultVariable(new BlockVar(blockPos));

        try {
            world.execute(() -> {
                try {
                    world.getEntityStore().getStore().invoke(new HexCastEvent(context));
                } catch (Exception e) {
                    LOGGER.atSevere().log(
                            "[hexcode] deferred block hex fire failed at %s: %s",
                            blockPos, e.getMessage());
                }
            });
        } catch (RuntimeException shuttingDown) {
            LOGGER.atWarning().log(
                    "[hexcode] block hex at %s dropped: world not accepting tasks",
                    blockPos);
            return new ActivationOutcome(ActivationStatus.EXECUTION_FAILED, baseSnapshot);
        }

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
        return ImbuementProfileRegistry.first(item.getCategories());
    }

    private static void applyEssence(@Nonnull HexContext ctx, @Nonnull EssenceAsset essence) {
        float vm = essence.getVolatilityMultiplier();
        VolatilityTracker tracker = ctx.getVolatilityTracker();
        if (vm != 1.0f && tracker != null) {
            tracker.setVolatilityMultiplier(tracker.getVolatilityMultiplier() * vm);
        }
        HexStyleAsset overlay = essence.getColors();
        if (overlay != null) {
            HexStyleAsset target = ctx.getStyle();
            if (target == null) {
                target = HexStyleAsset.empty();
                ctx.setStyle(target);
            }
            if (overlay.getPrimaryColor() != null) target.setPrimaryColor(overlay.getPrimaryColor().clone());
            if (overlay.getSecondaryColor() != null) target.setSecondaryColor(overlay.getSecondaryColor().clone());
            target.setAlpha(overlay.getAlphaOrDefault());
        }
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
