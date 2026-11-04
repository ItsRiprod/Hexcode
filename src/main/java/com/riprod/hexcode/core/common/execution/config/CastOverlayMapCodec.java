package com.riprod.hexcode.core.common.execution.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BsonDocument;
import org.bson.BsonValue;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.InheritCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.lookup.MapProvidedMapCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class CastOverlayMapCodec
        extends MapProvidedMapCodec<CastOverlay<?>, CastComponentType<?>>
        implements InheritCodec<Map<String, CastOverlay<?>>> {

    @SuppressWarnings("unchecked")
    public CastOverlayMapCodec() {
        super(HexComponentRegistry.getOverlayIdView(),
                type -> (Codec<CastOverlay<?>>) (Codec<?>) HexComponentRegistry.getOverlayCodec(type),
                LinkedHashMap::new,
                false);
    }

    @Override
    public Map<String, CastOverlay<?>> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
        return decodeAndInherit(bsonValue.asDocument(), null, extraInfo);
    }

    @Override
    public Map<String, CastOverlay<?>> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo)
            throws IOException {
        return decodeAndInheritJson(reader, null, extraInfo);
    }

    @Nonnull
    @Override
    public Map<String, CastOverlay<?>> decodeAndInherit(BsonDocument document,
            @Nullable Map<String, CastOverlay<?>> parent, ExtraInfo extraInfo) {
        var map = inherited(parent);
        decodeAndInherit(document, map, parent, extraInfo);
        return map;
    }

    @Override
    public void decodeAndInherit(BsonDocument document, Map<String, CastOverlay<?>> map,
            @Nullable Map<String, CastOverlay<?>> parent, ExtraInfo extraInfo) {
        for (var entry : document.entrySet()) {
            var id = entry.getKey();
            extraInfo.pushKey(id);
            try {
                var codec = codecFor(id);
                var parentValue = parent != null ? parent.get(id) : null;
                var value = entry.getValue();
                map.put(id, parentValue != null && value.isDocument()
                        && codec instanceof InheritCodec<CastOverlay<?>> inherit
                                ? inherit.decodeAndInherit(value.asDocument(), parentValue, extraInfo)
                                : codec.decode(value, extraInfo));
            } finally {
                extraInfo.popKey();
            }
        }
    }

    @Nonnull
    @Override
    public Map<String, CastOverlay<?>> decodeAndInheritJson(RawJsonReader reader,
            @Nullable Map<String, CastOverlay<?>> parent, ExtraInfo extraInfo) throws IOException {
        var map = inherited(parent);
        decodeAndInheritJson(reader, map, parent, extraInfo);
        return map;
    }

    @Override
    public void decodeAndInheritJson(RawJsonReader reader, Map<String, CastOverlay<?>> map,
            @Nullable Map<String, CastOverlay<?>> parent, ExtraInfo extraInfo) throws IOException {
        reader.expect('{');
        reader.consumeWhiteSpace();
        if (reader.tryConsume('}')) {
            return;
        }
        while (true) {
            var id = reader.readString();
            reader.consumeWhiteSpace();
            reader.expect(':');
            reader.consumeWhiteSpace();
            extraInfo.pushKey(id, reader);
            try {
                var codec = codecFor(id);
                var parentValue = parent != null ? parent.get(id) : null;
                map.put(id, parentValue != null && codec instanceof InheritCodec<CastOverlay<?>> inherit
                        ? inherit.decodeAndInheritJson(reader, parentValue, extraInfo)
                        : codec.decodeJson(reader, extraInfo));
            } catch (Exception e) {
                throw new CodecException("Failed to decode", reader, extraInfo, e);
            } finally {
                extraInfo.popKey();
            }
            reader.consumeWhiteSpace();
            if (reader.tryConsumeOrExpect('}', ',')) {
                return;
            }
            reader.consumeWhiteSpace();
        }
    }

    @Nonnull
    private Map<String, CastOverlay<?>> inherited(@Nullable Map<String, CastOverlay<?>> parent) {
        var map = createMap();
        if (parent != null) {
            map.putAll(parent);
        }
        return map;
    }

    @Nonnull
    private Codec<CastOverlay<?>> codecFor(@Nonnull String id) {
        var type = HexComponentRegistry.getOverlayIdView().get(id);
        if (type == null) {
            throw new IllegalArgumentException("unknown cast component '" + id
                    + "'; registered: " + HexComponentRegistry.getOverlayIdView().keySet());
        }
        return mapper.apply(type);
    }
}
