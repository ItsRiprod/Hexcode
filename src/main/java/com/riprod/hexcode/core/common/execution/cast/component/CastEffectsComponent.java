package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class CastEffectsComponent implements CastComponent {

    @Nonnull
    private GlyphEffects effects;
    @Nullable
    private GlyphEffects override;
    private int version;
    private final HashMap<String, GlyphEffects> resolved = new HashMap<>();

    public CastEffectsComponent(@Nonnull GlyphEffects effects) {
        this.effects = effects;
    }

    private CastEffectsComponent(@Nonnull GlyphEffects effects, @Nullable GlyphEffects override, int version) {
        this.effects = effects;
        this.override = override;
        this.version = version;
    }

    @Nonnull
    public GlyphEffects get() {
        return effects;
    }

    public void set(@Nonnull GlyphEffects effects) {
        this.effects = effects;
        changed();
    }

    @Nonnull
    public GlyphEffects mutable() {
        effects = effects.clone();
        changed();
        return effects;
    }

    public void setOverride(@Nullable GlyphEffects override) {
        this.override = override;
        changed();
    }

    public int getVersion() {
        return version;
    }

    @Nonnull
    public GlyphEffects resolve(@Nullable GlyphAsset glyph) {
        var key = glyph != null ? glyph.getId() : "";
        var cached = resolved.get(key);
        if (cached != null) {
            return cached;
        }
        var own = glyph != null ? glyph.getEffects() : null;
        var composed = (own != null ? own.clone() : GlyphEffects.empty()).compose(effects).compose(override);
        resolved.put(key, composed);
        return composed;
    }

    private void changed() {
        version++;
        resolved.clear();
    }

    @Nonnull
    @Override
    public CastEffectsComponent copy() {
        return new CastEffectsComponent(effects.clone(), override, version);
    }

    @Nonnull
    @Override
    public CastEffectsComponent branch() {
        return new CastEffectsComponent(effects, override, version);
    }

    public static final CastComponentType<CastEffectsComponent> TYPE = HexComponentRegistry.register(
            CastEffectsComponent.class, "Effects", a -> new CastEffectsComponent(GlyphEffects.empty()));
}
