package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.LinkedHashMap;

import javax.annotation.Nonnull;

import com.hypixel.hytale.assetstore.map.CaseInsensitiveHashStrategy;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.config.InheritProvidedMapCodec;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public final class CastRatesComponent implements CastComponent {

    private final Reference2FloatOpenHashMap<CastRate> all = new Reference2FloatOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<CastRate, Object2FloatOpenCustomHashMap<String>> keys =
            new Reference2ObjectOpenHashMap<>();

    public CastRatesComponent() {
        all.defaultReturnValue(1f);
    }

    public float get(@Nonnull CastRate rate) {
        return all.getFloat(rate);
    }

    public float get(@Nonnull CastRate rate, @Nonnull String key) {
        var keyed = keys.get(rate);
        return keyed != null ? all.getFloat(rate) * keyed.getFloat(key) : all.getFloat(rate);
    }

    public void multiply(@Nonnull CastRate rate, float factor) {
        all.put(rate, all.getFloat(rate) * factor);
    }

    public void multiply(@Nonnull CastRate rate, @Nonnull String key, float factor) {
        var keyed = keys.computeIfAbsent(rate, r -> newKeyMap());
        keyed.put(key, keyed.getFloat(key) * factor);
    }

    @Nonnull
    private static Object2FloatOpenCustomHashMap<String> newKeyMap() {
        var map = new Object2FloatOpenCustomHashMap<String>(CaseInsensitiveHashStrategy.getInstance());
        map.defaultReturnValue(1f);
        return map;
    }

    @Nonnull
    @Override
    public CastRatesComponent copy() {
        var copy = new CastRatesComponent();
        copy.all.putAll(all);
        for (var entry : keys.entrySet()) {
            var keyed = newKeyMap();
            keyed.putAll(entry.getValue());
            copy.keys.put(entry.getKey(), keyed);
        }
        return copy;
    }

    @Nonnull
    @Override
    public CastRatesComponent branch() {
        return copy();
    }

    public static final class Overlay extends LinkedHashMap<String, CastRate.Value>
            implements CastOverlay<CastRatesComponent> {

        public Overlay() {
        }

        @Override
        public void applyTo(@Nonnull CastRatesComponent target) {
            for (var entry : entrySet()) {
                var rate = CastRate.get(entry.getKey());
                if (rate == null) {
                    continue;
                }
                var value = entry.getValue();
                target.multiply(rate, value.all());
                for (var key : value.keys().entrySet()) {
                    target.multiply(rate, key.getKey(), key.getValue());
                }
            }
        }
    }

    public static final InheritProvidedMapCodec<CastRate.Value, CastRate, Overlay> CODEC =
            new InheritProvidedMapCodec<>(CastRate.view(), CastRate::getCodec, Overlay::new);

    public static final CastComponentType<CastRatesComponent> TYPE = HexComponentRegistry.register(
            CastRatesComponent.class, "Rates", CODEC, a -> new CastRatesComponent());
}
