package com.riprod.hexcode.core.common.hexes.codec;

import java.io.IOException;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class HexFieldCodec implements Codec<Hex> {

    public static final HexFieldCodec PLAYER = new HexFieldCodec(HexCodec::serialize);

    private final Function<Hex, String> encoder;

    private HexFieldCodec(Function<Hex, String> encoder) {
        this.encoder = encoder;
    }

    @Nullable
    @Override
    public Hex decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        if (bsonValue == null || bsonValue.isNull()) return null;
        if (bsonValue.isString()) {
            return decodeString(bsonValue.asString().getValue());
        }
        if (bsonValue.isDocument()) {
            // legacy structured form: a hex stored as a nested BSON document
            return Hex.CODEC.decode(bsonValue, extraInfo);
        }
        throw new CodecException("hex field expects STRING or DOCUMENT, got " + bsonValue.getBsonType());
    }

    @Override
    public BsonValue encode(Hex hex, ExtraInfo extraInfo) {
        // empty / null hexes (unfilled book pages, components without a spell)
        // serialize as BsonNull rather than failing — there's nothing to encode.
        if (hex == null || hex.getGlyphs().isEmpty()) {
            return BsonNull.VALUE;
        }
        return new BsonString(encoder.apply(hex));
    }

    @Nullable
    @Override
    public Hex decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        BsonValue bsonValue = RawJsonReader.readBsonValue(reader);
        return decode(bsonValue, extraInfo);
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context) {
        // schema describes the canonical (write-form) representation only.
        // legacy document-form reads remain functional but aren't advertised.
        return Codec.STRING.toSchema(context);
    }

    private static Hex decodeString(String s) {
        DecodeResult result = HexCodec.deserialize(s);
        Hex hex = result.getHex();
        if (hex == null) {
            throw new CodecException("hex string decode failed: " + result.getIssues());
        }
        return hex;
    }
}
