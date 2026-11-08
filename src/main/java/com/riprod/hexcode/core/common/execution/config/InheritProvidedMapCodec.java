package com.riprod.hexcode.core.common.execution.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BsonDocument;
import org.bson.BsonValue;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.InheritCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.lookup.AMapProvidedMapCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;

public class InheritProvidedMapCodec<V, P, M extends Map<String, V>>
        extends AMapProvidedMapCodec<String, V, P, M>
        implements InheritCodec<M> {

    private final Supplier<M> supplier;

    public InheritProvidedMapCodec(@Nonnull Map<String, P> codecProvider, @Nonnull Function<P, Codec<V>> mapper,
            @Nonnull Supplier<M> supplier) {
        super(codecProvider, mapper, false);
        this.supplier = supplier;
    }

    @Override
    public M createMap() {
        return supplier.get();
    }

    @Override
    protected String getIdForKey(String key) {
        return key;
    }

    @Override
    protected String getKeyForId(String id) {
        return id;
    }

    @Nonnull
    @Override
    protected M emptyMap() {
        return createMap();
    }

    @Nonnull
    @Override
    protected M unmodifiableMap(@Nonnull M map) {
        return map;
    }

    @Override
    public M decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
        return decodeAndInherit(bsonValue.asDocument(), null, extraInfo);
    }

    @Override
    public M decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
        return decodeAndInheritJson(reader, null, extraInfo);
    }

    @Nonnull
    @Override
    public M decodeAndInherit(BsonDocument document, @Nullable M parent, ExtraInfo extraInfo) {
        var map = inherited(parent);
        decodeAndInherit(document, map, parent, extraInfo);
        return map;
    }

    @Override
    public void decodeAndInherit(BsonDocument document, M map, @Nullable M parent, ExtraInfo extraInfo) {
        for (var entry : document.entrySet()) {
            var id = entry.getKey();
            extraInfo.pushKey(id);
            try {
                var codec = codecFor(id);
                var parentValue = parent != null ? parent.get(id) : null;
                var value = entry.getValue();
                map.put(id, parentValue != null && value.isDocument()
                        && codec instanceof InheritCodec<V> inherit
                                ? inherit.decodeAndInherit(value.asDocument(), parentValue, extraInfo)
                                : codec.decode(value, extraInfo));
            } finally {
                extraInfo.popKey();
            }
        }
    }

    @Nonnull
    @Override
    public M decodeAndInheritJson(RawJsonReader reader, @Nullable M parent, ExtraInfo extraInfo)
            throws IOException {
        var map = inherited(parent);
        decodeAndInheritJson(reader, map, parent, extraInfo);
        return map;
    }

    @Override
    public void decodeAndInheritJson(RawJsonReader reader, M map, @Nullable M parent, ExtraInfo extraInfo)
            throws IOException {
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
                map.put(id, parentValue != null && codec instanceof InheritCodec<V> inherit
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
    private M inherited(@Nullable M parent) {
        var map = createMap();
        if (parent != null) {
            map.putAll(parent);
        }
        return map;
    }

    @Nonnull
    private Codec<V> codecFor(@Nonnull String id) {
        var provided = codecProvider.get(id);
        if (provided == null) {
            throw new IllegalArgumentException("unknown id '" + id + "'; registered: "
                    + Collections.unmodifiableSet(codecProvider.keySet()));
        }
        return mapper.apply(provided);
    }
}
