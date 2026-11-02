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
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class CastOverlayMapCodec
        extends MapProvidedMapCodec<CastOverlay<?>, CastComponentType<?>> {

    @SuppressWarnings("unchecked")
    public CastOverlayMapCodec() {
        super(HexComponentRegistry.getOverlayIdView(),
                type -> (Codec<CastOverlay<?>>) (Codec<?>) HexComponentRegistry.getOverlayCodec(type),
                LinkedHashMap::new,
                false);
    }

    @Nullable
    @Override
    protected String getKeyForId(String id) {
        return HexComponentRegistry.getOverlayIdView().containsKey(id) ? id : null;
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
                + "'; registered: " + HexComponentRegistry.getOverlayIdView().keySet());
    }
}
