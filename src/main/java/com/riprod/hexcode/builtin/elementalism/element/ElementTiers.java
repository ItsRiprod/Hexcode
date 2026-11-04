package com.riprod.hexcode.builtin.elementalism.element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BsonValue;

import com.hypixel.hytale.assetstore.map.CaseInsensitiveHashStrategy;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.codecs.map.Float2ObjectMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

public final class ElementTiers {
    public static final ElementTiers EMPTY = new ElementTiers(new String[0], new Entry[0]);
    public static final Codec<ElementTiers> CODEC = new TiersCodec();
    public static final Validator<ElementTiers> VALIDATOR = new TiersValidator();

    private final String[] keys;
    private final Entry[] entries;
    private final List<Entry> view;

    private ElementTiers(String[] keys, Entry[] entries) {
        this.keys = keys;
        this.entries = entries;
        this.view = List.of(entries);
    }

    public List<Entry> entries() {
        return this.view;
    }

    @Nullable
    public Entry tierFor(float charge) {
        for (var i = this.entries.length - 1; i >= 0; i--) {
            if (this.entries[i].threshold() <= charge) {
                return this.entries[i];
            }
        }
        return null;
    }

    @Nullable
    public ElementalTier byId(String tierId) {
        for (var entry : this.entries) {
            if (entry.tier().getId().equalsIgnoreCase(tierId)) {
                return entry.tier();
            }
        }
        return null;
    }

    private static ElementTiers of(Map<String, ElementalTier> raw) {
        var parsed = new ArrayList<Map.Entry<String, Entry>>(raw.size());
        for (var e : raw.entrySet()) {
            try {
                parsed.add(Map.entry(e.getKey(), new Entry(Float.parseFloat(e.getKey()), e.getValue())));
            } catch (NumberFormatException ex) {
                throw new CodecException("Tier threshold '" + e.getKey() + "' is not a number", ex);
            }
        }
        parsed.sort(Comparator.comparingDouble(p -> p.getValue().threshold()));
        return new ElementTiers(
                parsed.stream().map(Map.Entry::getKey).toArray(String[]::new),
                parsed.stream().map(Map.Entry::getValue).toArray(Entry[]::new));
    }

    private Map<String, ElementalTier> toMap() {
        var map = new LinkedHashMap<String, ElementalTier>(this.entries.length);
        for (var i = 0; i < this.entries.length; i++) {
            map.put(this.keys[i], this.entries[i].tier());
        }
        return map;
    }

    public record Entry(float threshold, ElementalTier tier) {
    }

    private static final class TiersCodec implements Codec<ElementTiers>, WrappedCodec<ElementalTier> {
        private final MapCodec<ElementalTier, LinkedHashMap<String, ElementalTier>> map =
                new MapCodec<>(ElementalTier.CODEC, LinkedHashMap::new, false);

        @Override
        public Codec<ElementalTier> getChildCodec() {
            return ElementalTier.CODEC;
        }

        @Override
        public ElementTiers decode(BsonValue bsonValue, ExtraInfo extraInfo) {
            return of(this.map.decode(bsonValue, extraInfo));
        }

        @Override
        public ElementTiers decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
            return of(this.map.decodeJson(reader, extraInfo));
        }

        @Override
        public BsonValue encode(ElementTiers tiers, ExtraInfo extraInfo) {
            return this.map.encode(tiers.toMap(), extraInfo);
        }

        @Nonnull
        @Override
        public Schema toSchema(@Nonnull SchemaContext context) {
            var schema = new ObjectSchema();
            var name = new StringSchema();
            name.setPattern(Float2ObjectMapCodec.FLOAT_PATTERN);
            name.setMarkdownDescription("Charge threshold at which the tier is reached");
            schema.setPropertyNames(name);
            schema.setAdditionalProperties(context.refDefinition(ElementalTier.CODEC));
            return schema;
        }
    }

    private static final class TiersValidator implements Validator<ElementTiers> {
        @Override
        public void accept(ElementTiers tiers, ValidationResults results) {
            if (tiers == null) {
                return;
            }
            var ids = new ObjectOpenCustomHashSet<String>(CaseInsensitiveHashStrategy.getInstance());
            for (var i = 0; i < tiers.entries.length; i++) {
                var threshold = tiers.entries[i].threshold();
                if (!(threshold >= 0f) || Float.isInfinite(threshold)) {
                    results.fail("Tier threshold '" + tiers.keys[i] + "' must be a finite non-negative number");
                }
                if (i > 0 && tiers.entries[i - 1].threshold() == threshold) {
                    results.fail("Tier thresholds '" + tiers.keys[i - 1] + "' and '" + tiers.keys[i] + "' are the same charge");
                }
                var id = tiers.entries[i].tier().getId();
                if (id != null && !ids.add(id)) {
                    results.fail("Tier Id '" + id + "' is used by more than one tier");
                }
            }
        }

        @Override
        public void updateSchema(SchemaContext context, Schema target) {
        }
    }
}
