package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;
import com.riprod.hexcode.core.state.casting.utils.HexSpawner;
import com.riprod.hexcode.core.state.casting.utils.RootSpawner;
import com.riprod.hexcode.utils.CleanupUtils;
import com.riprod.hexcode.utils.HexSlot;

public final class FlycastingScene {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private FlycastingScene() {
    }

    @Nullable
    public static FlycastingState spawn(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        HexStaffComponent staff = CasterInventory.getHexStaffComponent(buffer, player);
        if (staff == null) {
            return null;
        }

        DrawCaptureComponent capture = buffer.getComponent(player, DrawCaptureComponent.getComponentType());
        List<Hex> hexes = capture != null ? capture.getPalette()
                : CasterInventory.getHexesForCasting(buffer, player);

        ItemStack mainHand = InventoryComponent.getItemInHand(buffer, player);
        ItemStack offHand = PlayerUtils.getHandItem(buffer, player, HexSlot.OffHand);
        HexStaffAsset staffAsset = CasterInventory.getHexStaffAsset(mainHand);
        HexBookAsset bookAsset = CasterInventory.getHexBookAsset(offHand);
        ModelParticle[] staffParticles = staffAsset != null ? staffAsset.getCastingAuraParticles() : null;
        ModelParticle[] bookParticles = bookAsset != null ? bookAsset.getCastingAuraParticles() : null;
        ModelParticle[] particles = mergeParticles(staffParticles, bookParticles);

        float eyeHeight = resolveEyeHeight(buffer, player);
        Ref<EntityStore> castingRootRef = RootSpawner.createCastingRoot(buffer, player, eyeHeight, particles);

        FlycastingState state = new FlycastingState();
        state.setCastingRootRef(castingRootRef);
        state.setActiveHexes(HexSpawner.spawnHexes(buffer, player, castingRootRef, hexes, staff.getStyleId()));
        return state;
    }

    public static void teardown(CommandBuffer<EntityStore> buffer, FlycastingState state) {
        try {
            List<Ref<EntityStore>> activeHexes = state.getActiveHexes();
            for (Ref<EntityStore> hexRef : activeHexes) {
                if (hexRef == null || !hexRef.isValid()) {
                    continue;
                }
                HexComponent hexComp = buffer.getComponent(hexRef, HexComponent.getComponentType());
                if (hexComp != null) {
                    CleanupUtils.safeRemoveEntities(buffer, hexComp.getChildGlyphRefsList());
                }
                CleanupUtils.safeRemoveEntity(buffer, hexRef);
            }
            activeHexes.clear();

            FlycastingDragHandler.removeHeadAnchor(buffer, state);
            CleanupUtils.safeRemoveEntity(buffer, state.getCastingRootRef());
            state.setCastingRootRef(null);
            state.setHoveredHex(null);
            state.setHoveredGlyph(null);
            state.setLastHoveredHex(null);
            state.setDraggingHex(null);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] flycasting teardown failed");
        }
    }

    public static float resolveEyeHeight(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        ModelComponent modelComp = buffer.getComponent(player, ModelComponent.getComponentType());
        if (modelComp != null && modelComp.getModel() != null) {
            return modelComp.getModel().getEyeHeight(player, buffer);
        }
        return 0f;
    }

    private static ModelParticle[] mergeParticles(ModelParticle[] a, ModelParticle[] b) {
        if (a == null || a.length == 0)
            return b;
        if (b == null || b.length == 0)
            return a;
        ModelParticle[] merged = new ModelParticle[a.length + b.length];
        System.arraycopy(a, 0, merged, 0, a.length);
        System.arraycopy(b, 0, merged, a.length, b.length);
        return merged;
    }
}
