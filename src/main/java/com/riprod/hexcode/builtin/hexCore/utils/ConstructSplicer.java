package com.riprod.hexcode.builtin.hexCore.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.output.OutputGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot.SlotGlyph;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.utils.LogScopes;

public final class ConstructSplicer {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    public enum VariablePolicy { PREFER_TARGET, PREFER_CASTER }
    public enum ChainMode { APPEND_TAIL, REPLACE }

    private ConstructSplicer() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void splice(
            HexStatus<?> target,
            ConstructHandler<?> handler,
            HexContext caster,
            Glyph casterGlyph,
            ChainMode chainMode,
            VariablePolicy varPolicy,
            float donationAmount) {

        ConstructHandler raw = handler;
        HexStatus rawStatus = target;

        List<String> originalNext = new ArrayList<>(raw.getPendingNextGlyphIds(rawStatus));
        List<String> casterChildren = casterGlyph.getFlowLinks();

        HexContext targetCtx = target.getHexContext();
        Hex targetHex = targetCtx.getHex();
        Hex spliceSource = caster.getHex().clone();

        int outputsRewired = rewireOutputs(spliceSource, originalNext);

        int glyphsCopied = 0;
        for (Glyph g : spliceSource.getGlyphs()) {
            targetHex.put(g.getId(), g);
            glyphsCopied++;
        }

        mergeVariables(targetCtx, caster.getVariables(), varPolicy);

        VolatilityComponent targetTracker = targetCtx.volatility();
        if (targetTracker != null && donationAmount > 0f) {
            targetTracker.add(donationAmount);
        }

        List<String> newChain = computeNewChain(chainMode, originalNext, casterChildren);
        if (newChain != null) {
            raw.setPendingNextGlyphIds(rawStatus, newChain);
        }

        LOGGER.atFine().log(
                "splice: mode=%s policy=%s glyphs=%d outputs=%d originalNext=%d casterChildren=%d",
                chainMode, varPolicy, glyphsCopied, outputsRewired,
                originalNext.size(), casterChildren.size());
    }

    private static int rewireOutputs(Hex casterHex, List<String> originalNext) {
        int count = 0;
        for (Glyph g : casterHex.getGlyphs()) {
            if (!isContinuationMarker(g)) continue;
            g.clearSlot(Glyph.NEXT_SLOT);
            for (String id : originalNext) {
                g.addSlotLink(Glyph.NEXT_SLOT, id);
            }
            count++;
        }
        return count;
    }

    private static boolean isContinuationMarker(Glyph g) {
        if (OutputGlyph.ID.equals(g.getGlyphId())) return true;
        return !g.isBoundaryOrigin()
                && SlotGlyph.isSlotGlyph(g)
                && SlotGlyph.mode(g) == SlotGlyph.MODE_NEXT;
    }

    private static void mergeVariables(HexContext targetCtx,
            Map<String, HexVar> casterVars, VariablePolicy policy) {
        if (casterVars == null || casterVars.isEmpty()) return;
        Map<String, HexVar> targetVars = targetCtx.getVariables();
        for (Map.Entry<String, HexVar> e : casterVars.entrySet()) {
            if (policy == VariablePolicy.PREFER_TARGET) {
                targetVars.putIfAbsent(e.getKey(), e.getValue());
            } else {
                targetVars.put(e.getKey(), e.getValue());
            }
        }
    }

    private static List<String> computeNewChain(ChainMode mode,
            List<String> originalNext, List<String> casterChildren) {
        switch (mode) {
            case APPEND_TAIL: {
                if (casterChildren.isEmpty()) return null;
                List<String> combined = new ArrayList<>(originalNext.size() + casterChildren.size());
                combined.addAll(originalNext);
                combined.addAll(casterChildren);
                return combined;
            }
            case REPLACE:
            default:
                return new ArrayList<>(casterChildren);
        }
    }
}
