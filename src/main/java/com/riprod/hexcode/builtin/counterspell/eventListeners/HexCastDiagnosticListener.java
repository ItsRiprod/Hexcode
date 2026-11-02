package com.riprod.hexcode.builtin.counterspell.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.utils.LogScopes;

public class HexCastDiagnosticListener implements Consumer<CastEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    @Override
    public void accept(CastEvent event) {
        HexContext data = event.getContext();
        Hex hex = data != null ? data.getHex() : null;
        String firstGlyph = hex != null ? hex.get(hex.getFirstGlyphId()).getGlyphId() : "<null>";

        if (data == null) {
            LOGGER.atFine().log("CastEvent: context is null");
            return;
        }

        var policy = data.policy();
        LOGGER.atFine().log(
                "firstGlyph=%s mana=%s cancelled=%s reqCharges=%s consumeMana=%s decay=%s bypassVol=%s",
                firstGlyph,
                data.getManaCost(),
                event.isCancelled(),
                policy.isRequireMagicCharges(),
                policy.isConsumeMana(),
                policy.isApplyVolatilityDecay(),
                policy.isBypassVolatilityDepletion());
    }
}
