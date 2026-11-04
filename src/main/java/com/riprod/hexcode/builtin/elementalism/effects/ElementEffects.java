package com.riprod.hexcode.builtin.elementalism.effects;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class ElementEffects {
    public static final BuilderCodec<ElementEffects> CODEC;

    private static final EffectSlot[] SLOTS = EffectSlot.values();

    private final EffectEntry[] entries = new EffectEntry[SLOTS.length];

    @Nullable
    public EffectEntry get(EffectSlot slot) {
        return this.entries[slot.ordinal()];
    }

    public static ResolvedEffects resolve(@Nullable ElementEffects overrides, @Nullable ElementEffects base,
            ElementEffects... fallbacks) {
        var chain = new ElementEffects[fallbacks.length + 1];
        chain[0] = base;
        System.arraycopy(fallbacks, 0, chain, 1, fallbacks.length);
        var resolved = new EffectEntry[SLOTS.length];
        for (var slot : SLOTS) {
            resolved[slot.ordinal()] = new EffectEntry(
                    pick(overrides, chain, slot, EffectEntry::getParticle),
                    pick(overrides, chain, slot, EffectEntry::getSound),
                    pick(overrides, chain, slot, EffectEntry::getColor));
        }
        return new ResolvedEffects(resolved);
    }

    public static ElementEffects stack(List<ElementEffects> layers) {
        var stacked = new ElementEffects();
        for (var slot : SLOTS) {
            var particle = first(layers, slot, EffectEntry::getParticle);
            var sound = first(layers, slot, EffectEntry::getSound);
            var color = first(layers, slot, EffectEntry::getColor);
            if (particle != null || sound != null || color != null) {
                stacked.entries[slot.ordinal()] = new EffectEntry(particle, sound, color);
            }
        }
        return stacked;
    }

    @Nullable
    private static <T> T first(List<ElementEffects> layers, EffectSlot slot, Function<EffectEntry, T> field) {
        for (var layer : layers) {
            var value = value(layer, slot, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    private static <T> T pick(@Nullable ElementEffects overrides, ElementEffects[] chain, EffectSlot slot,
            Function<EffectEntry, T> field) {
        var value = value(overrides, slot, field);
        for (var i = 0; value == null && i < chain.length; i++) {
            value = value(chain[i], slot, field);
            if (value == null && slot != EffectSlot.DEFAULT) {
                value = value(chain[i], EffectSlot.DEFAULT, field);
            }
        }
        return value;
    }

    @Nullable
    private static <T> T value(@Nullable ElementEffects layer, EffectSlot slot, Function<EffectEntry, T> field) {
        var entry = layer != null ? layer.get(slot) : null;
        return entry != null ? field.apply(entry) : null;
    }

    static {
        var builder = BuilderCodec.builder(ElementEffects.class, ElementEffects::new);
        for (var slot : SLOTS) {
            builder.append(new KeyedCodec<>(slot.getKey(), EffectEntry.CODEC),
                    (e, v) -> e.entries[slot.ordinal()] = v,
                    e -> e.entries[slot.ordinal()])
                    .documentation(slot.getDocumentation())
                    .add();
        }
        CODEC = builder.build();
    }
}
