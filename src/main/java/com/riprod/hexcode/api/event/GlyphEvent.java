package com.riprod.hexcode.api.event;

import java.util.function.Consumer;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class GlyphEvent implements IEvent<Void>, ICancellable {

    private final Glyph glyph;
    private final GlyphAsset asset;
    private final HexContext context;
    private float costMultiplier = 1f;
    private float costFlat = 0f;
    private float resolvedCost = 0f;
    private boolean cancelled;

    public GlyphEvent(Glyph glyph, GlyphAsset asset, HexContext context) {
        this.glyph = glyph;
        this.asset = asset;
        this.context = context;
    }

    public Glyph getGlyph() {
        return glyph;
    }

    public GlyphAsset getAsset() {
        return asset;
    }

    public String getAssetId() {
        return glyph.getGlyphId();
    }

    public String getNodeId() {
        return glyph.getId();
    }

    public HexContext getContext() {
        return context;
    }

    public float getCostMultiplier() {
        return costMultiplier;
    }

    public void setCostMultiplier(float costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    public void multiplyCost(float factor) {
        this.costMultiplier *= factor;
    }

    public float getCostFlat() {
        return costFlat;
    }

    public void setCostFlat(float costFlat) {
        this.costFlat = costFlat;
    }

    public void addCost(float amount) {
        this.costFlat += amount;
    }

    public float getResolvedCost() {
        return resolvedCost;
    }

    public void setResolvedCost(float resolvedCost) {
        this.resolvedCost = resolvedCost;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static void register(EventRegistry registry, String assetId, Consumer<GlyphEvent> handler) {
        register(registry, EventPriority.NORMAL.getValue(), assetId, handler);
    }

    public static void register(EventRegistry registry, short priority, String assetId,
            Consumer<GlyphEvent> handler) {
        registry.register(priority, GlyphEvent.class, e -> {
            if (!e.isCancelled() && assetId.equals(e.getAssetId())) handler.accept(e);
        });
    }

    public static void registerAll(EventRegistry registry, Consumer<GlyphEvent> handler) {
        registerAll(registry, EventPriority.NORMAL.getValue(), handler);
    }

    public static void registerAll(EventRegistry registry, short priority, Consumer<GlyphEvent> handler) {
        registry.register(priority, GlyphEvent.class, e -> {
            if (!e.isCancelled()) handler.accept(e);
        });
    }

    public static void registerAllIncludingCancelled(EventRegistry registry, short priority,
            Consumer<GlyphEvent> handler) {
        registry.register(priority, GlyphEvent.class, handler);
    }
}
