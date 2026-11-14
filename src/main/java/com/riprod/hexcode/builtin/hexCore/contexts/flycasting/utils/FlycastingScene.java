package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.positioning.utils.HexEntities;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.utils.CleanupUtils;
import com.riprod.hexcode.utils.HexSlot;

public final class FlycastingScene {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private FlycastingScene() {
    }

    @Nullable
    public static FlycastingState spawn(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        DrawCaptureComponent capture = buffer.getComponent(player, DrawCaptureComponent.getComponentType());
        List<Hex> hexes = capture != null ? capture.getPalette()
                : CasterInventory.getHexesForCasting(buffer, player);

        ItemStack offHand = PlayerUtils.getHandItem(buffer, player, HexSlot.OffHand);
        HexBookAsset bookAsset = CasterInventory.getHexBookAsset(offHand);
        var auras = new ArrayList<GlyphEffects>(2);
        var staffEffects = capture != null ? capture.getEffects() : null;
        if (staffEffects != null) auras.add(staffEffects);
        var bookEffects = bookAsset != null ? bookAsset.getEffects() : null;
        if (bookEffects != null) auras.add(bookEffects);

        float eyeHeight = resolveEyeHeight(buffer, player);
        Ref<EntityStore> castingRootRef = RootSpawner.createCastingRoot(buffer, player, eyeHeight, auras);

        String castStyleId = capture != null ? capture.getCastStyleId() : "ring";
        FlycastingState state = new FlycastingState();
        state.setCastingRootRef(castingRootRef);
        state.setLayoutId(capture != null ? capture.getLayoutId() : null);
        state.setActiveHexes(HexSpawner.spawnHexes(buffer, player, castingRootRef, hexes, castStyleId,
                state.getLayoutId()));
        return state;
    }

    public static void teardown(CommandBuffer<EntityStore> buffer, FlycastingState state) {
        try {
            List<Ref<EntityStore>> activeHexes = state.getActiveHexes();
            for (Ref<EntityStore> hexRef : activeHexes) {
                if (hexRef == null || !hexRef.isValid()) {
                    continue;
                }
                HexEntities.despawnHex(buffer, hexRef);
            }
            activeHexes.clear();

            CleanupUtils.safeRemoveMountParent(buffer, state.getCastingRootRef());
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
}
