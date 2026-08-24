package com.riprod.hexcode.builtin.counterspell.eventListeners;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.utils.LogScopes;

public class HexCastDiagnosticListener extends WorldEventSystem<EntityStore, HexCastEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    public HexCastDiagnosticListener() {
        super(HexCastEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> buffer,
                       @Nonnull HexCastEvent event) {
        HexContext data = event.getContext();
        Hex hex = data != null ? data.getHex() : null;
        String firstGlyph = hex != null ? hex.get(hex.getFirstGlyphId()).getGlyphId() : "<null>";
        
        if (data == null) {
            LOGGER.atFine().log("HexCastEvent: context is null");
            return;
        }

        var policy = data.policy();
        LOGGER.atFine().log(
                "firstGlyph=%s mana=%s cancelled=%s reqCharges=%s consumeMana=%s decay=%s bypassVol=%s",
                firstGlyph,
                data != null ? data.getManaCost() : "<null>",
                event.isCancelled(),
                data != null && policy.isRequireMagicCharges(),
                data != null && policy.isConsumeMana(),
                data != null && policy.isApplyVolatilityDecay(),
                data != null && policy.isBypassVolatilityDepletion());
    }
}
