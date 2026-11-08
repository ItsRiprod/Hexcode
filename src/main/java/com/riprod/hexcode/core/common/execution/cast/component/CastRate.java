package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.MapKeyValidator;

public final class CastRate {

    private static final Map<String, CastRate> BY_ID = new LinkedHashMap<>();
    private static final Map<String, CastRate> VIEW = Collections.unmodifiableMap(BY_ID);
    private static final Codec<Value> SCALAR_CODEC = new FunctionCodec<>(Codec.FLOAT, Value::scalar, Value::all);

    public static final CastRate VOLATILITY = register("Volatility");
    public static final CastRate MANA = register("Mana");
    public static final CastRate BUDGET = register("Budget");

    private final String id;
    private final Codec<Value> codec;

    private CastRate(@Nonnull String id, @Nonnull Codec<Value> codec) {
        this.id = id;
        this.codec = codec;
    }

    @Nonnull
    public static CastRate register(@Nonnull String id) {
        return add(id, SCALAR_CODEC);
    }

    @Nonnull
    public static CastRate register(@Nonnull String id, @Nonnull Validator<String> keyValidator) {
        return add(id, keyedCodec(keyValidator));
    }

    @Nonnull
    private static synchronized CastRate add(@Nonnull String id, @Nonnull Codec<Value> codec) {
        if (BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("cast rate '" + id + "' already exists!");
        }
        var rate = new CastRate(id, codec);
        BY_ID.put(id, rate);
        return rate;
    }

    @Nullable
    public static CastRate get(@Nonnull String id) {
        return BY_ID.get(id);
    }

    @Nonnull
    public static Map<String, CastRate> view() {
        return VIEW;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Codec<Value> getCodec() {
        return codec;
    }

    @Nonnull
    private static Codec<Value> keyedCodec(@Nonnull Validator<String> keyValidator) {
        return BuilderCodec.builder(Value.class, Value::new)
                .<Float>appendInherited(new KeyedCodec<>("All", Codec.FLOAT),
                        (v, x) -> v.all = x,
                        v -> v.all,
                        (v, p) -> v.all = p.all)
                .documentation("Multiplier for every key on this rate. Compounds with earlier layers.")
                .add()
                .<Map<String, Float>>appendInherited(
                        new KeyedCodec<>("Keys", new MapCodec<Float, Map<String, Float>>(Codec.FLOAT, HashMap::new, false)),
                        (v, x) -> v.keys = x,
                        v -> v.keys,
                        (v, p) -> v.keys = p.keys)
                .addValidator(new MapKeyValidator<>(keyValidator))
                .documentation("Multiplier per key, applied on top of All. Compounds with earlier layers.")
                .add()
                .build();
    }

    public static final class Value {

        private float all = 1f;
        private Map<String, Float> keys = Map.of();

        public Value() {
        }

        @Nonnull
        private static Value scalar(@Nonnull Float all) {
            var value = new Value();
            value.all = all;
            return value;
        }

        public float all() {
            return all;
        }

        @Nonnull
        public Map<String, Float> keys() {
            return keys;
        }
    }
}
