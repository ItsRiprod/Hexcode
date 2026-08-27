package com.riprod.hexcode.core.common.execution.config;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BsonValue;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.lookup.MapProvidedMapCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.riprod.hexcode.core.common.execution.cast.CastComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;

public final class CastOverlayMapCodec
        extends MapProvidedMapCodec<CastOverlay<?>, CastComponentType<?>> {

    private final CastComponentRegistry registry;

    @SuppressWarnings("unchecked")
    public CastOverlayMapCodec(@Nonnull CastComponentRegistry registry) {
        super(registry.getIdView(),
                type -> (Codec<CastOverlay<?>>) (Codec<?>) registry.getOverlayCodec(type),
                LinkedHashMap::new,
                false);
        this.registry = registry;
    }

    @Nullable
    @Override
    protected String getKeyForId(String id) {
        return registry.getType(id) != null ? id : null;
    }

    @Override
    public void handleUnknown(Map<String, CastOverlay<?>> map, @Nonnull String key,
            BsonValue value, @Nonnull ExtraInfo extraInfo) {
        throw unknown(key);
    }

    @Override
    public void handleUnknown(Map<String, CastOverlay<?>> map, @Nonnull String key,
            @Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) {
        throw unknown(key);
    }

    private IllegalArgumentException unknown(String key) {
        return new IllegalArgumentException("unknown cast component '" + key
                + "'; registered: " + registry.getIdView().keySet());
    }
}
