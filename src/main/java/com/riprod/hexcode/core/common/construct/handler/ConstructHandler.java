package com.riprod.hexcode.core.common.construct.handler;

import java.util.List;

import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public interface ConstructHandler<S extends ConstructState> {

    default S createInitialState(HexStatus<S> status, ConstructTickContext ctx) {
        return null;
    }

    default void onFirstTick(HexStatus<S> status, ConstructTickContext ctx) {
    }

    default boolean onTick(float dt, HexStatus<S> status, ConstructTickContext ctx) {
        return !drainSustain(dt, status);
    }

    default void onCleanup(HexStatus<S> status, ConstructTickContext ctx) {
    }

    default void onEnd(HexStatus<S> status, ConstructTickContext ctx) {
        onCleanup(status, ctx);
    }

    default void onAbort(HexStatus<S> status, ConstructTickContext ctx) {
        onCleanup(status, ctx);
    }

    default List<String> getPendingNextGlyphIds(HexStatus<S> status) {
        return List.of();
    }

    default void setPendingNextGlyphIds(HexStatus<S> status, List<String> ids) {
    }

    default boolean drainSustain(float dt, HexStatus<S> status) {
        HexStats tracker = status.getHexContext().getVolatilityTracker();
        if (tracker == null) return true;
        float rate = resolveDrainRate(status);
        if (rate > 0f) {
            if (tracker.consumeVolatility(dt * rate) <= 0f) return false;
        }
        return tracker.getCurrentVolatility() > 0f;
    }

    static float resolveDrainRate(HexStatus<?> status) {
        Glyph trigger = status.getTriggeringGlyph();
        if (trigger == null) return 0f;
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(trigger.getGlyphId());
        if (asset == null) return 0f;
        return asset.getVolatility().getDrainPerSecond();
    }
}
