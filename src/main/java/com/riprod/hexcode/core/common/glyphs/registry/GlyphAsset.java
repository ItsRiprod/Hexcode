package com.riprod.hexcode.core.common.glyphs.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIButton;
import com.hypixel.hytale.codec.schema.metadata.ui.UICreateButtons;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class GlyphAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, GlyphAsset>> {
    public static final AssetBuilderCodec<String, GlyphAsset> CODEC;
    private static AssetStore<String, GlyphAsset, DefaultAssetMap<String, GlyphAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String modelPath;
    protected String title;
    protected String description;
    protected String verboseDescription;
    protected float basePower = 1.0f;
    protected int manaConsumption = 10;
    protected boolean isReversable = false;
    protected boolean isEnabled = true;
    protected VolatilityAsset volatility = new VolatilityAsset();
    protected ArrayList<DrawnShapeComponent> shapes = new ArrayList<>();
    protected LinkedHashMap<String, SlotAsset> slots = new LinkedHashMap<>();
    protected String styleId;

    private transient Object2IntOpenHashMap<String> slotIndexCache;

    public static AssetStore<String, GlyphAsset, DefaultAssetMap<String, GlyphAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(GlyphAsset.class);
        }

        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, GlyphAsset> getAssetMap() {
        return (DefaultAssetMap<String, GlyphAsset>) getAssetStore().getAssetMap();
    }

    private GlyphAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean isReversable() {
        return this.isReversable;
    }

    public String getModelPath() {
        return this.modelPath;
    }

    public float getBasePower() {
        return this.basePower;
    }

    public int getManaConsumption() {
        return this.manaConsumption;
    }

    public VolatilityAsset getVolatility() {
        return this.volatility;
    }

    public List<DrawnShapeComponent> getShapes() {
        return Collections.unmodifiableList(this.shapes);
    }

    public Map<String, SlotAsset> getSlots() {
        return Collections.unmodifiableMap(this.slots);
    }

    public SlotAsset getSlot(String key) {
        return this.slots.get(key);
    }

    public Set<String> getSlotKeys() {
        return Collections.unmodifiableSet(this.slots.keySet());
    }

    public boolean hasSlot(String key) {
        return this.slots.containsKey(key);
    }

    public String getStyleId() {
        return this.styleId;
    }

    public HexStyleAsset getStyle() {
        if (this.styleId == null) return null;
        return HexStyleAsset.getAssetMap().getAsset(this.styleId);
    }

    public int getSlotCount() {
        return this.slots.size();
    }

    public int getSlotIndex(String key) {
        if (this.slotIndexCache == null) {
            this.slotIndexCache = buildSlotIndexCache();
        }
        return this.slotIndexCache.getOrDefault(key, -1);
    }

    private Object2IntOpenHashMap<String> buildSlotIndexCache() {
        Object2IntOpenHashMap<String> cache = new Object2IntOpenHashMap<>(this.slots.size());
        cache.defaultReturnValue(-1);
        int i = 0;
        for (String key : this.slots.keySet()) {
            cache.put(key, i++);
        }
        return cache;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getVerboseDescription() {
        return this.verboseDescription;
    }

    static {
        CODEC = buildCodec();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(GlyphAsset::getAssetStore));
    }

    @SuppressWarnings("unchecked")
    private static AssetBuilderCodec<String, GlyphAsset> buildCodec() {
        Codec<Map<String, SlotAsset>> slotMapCodec = (Codec<Map<String, SlotAsset>>) (Codec<?>) new MapCodec<>(
                SlotAsset.CODEC, LinkedHashMap::new, false);
        return AssetBuilderCodec
                .builder(GlyphAsset.class, GlyphAsset::new, Codec.STRING, (glyphAsset, s) -> {
                    glyphAsset.id = s;
                }, (glyphAsset) -> {
                    return glyphAsset.id;
                }, (asset, data) -> {
                    asset.data = data;
                }, (asset) -> {
                    return asset.data;
                })
                .appendInherited(new KeyedCodec<>("IsEnabled", Codec.BOOLEAN),
                        (a, v) -> a.isEnabled = v, a -> a.isEnabled,
                        (a, p) -> a.isEnabled = p.isEnabled)
                .documentation("Whether or not the glyph is enabled")
                .add()
                .<String>appendInherited(new KeyedCodec<>("ModelPath", Codec.STRING),
                        (a, v) -> a.modelPath = v, a -> a.modelPath,
                        (a, p) -> a.modelPath = p.modelPath)
                .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("The location of the glyph model")
                .add()
                .<Float>appendInherited(new KeyedCodec<>("BasePower", Codec.FLOAT),
                        (a, v) -> a.basePower = v, a -> a.basePower,
                        (a, p) -> a.basePower = p.basePower)
                .add()
                .<String>appendInherited(new KeyedCodec<>("Title", Codec.STRING),
                        (a, v) -> a.title = v, a -> a.title,
                        (a, p) -> a.title = p.title)
                .documentation("The human-readable name of the glyph")
                .add()
                .appendInherited(new KeyedCodec<>("Description", Codec.STRING),
                        (a, v) -> a.description = v, a -> a.description,
                        (a, p) -> a.description = p.description)
                .documentation("The human-readable description of the glyph")
                .add()
                .appendInherited(new KeyedCodec<>("VerboseDescription", Codec.STRING),
                        (a, v) -> a.verboseDescription = v, a -> a.verboseDescription,
                        (a, p) -> a.verboseDescription = p.verboseDescription)
                .documentation("Optional verbose description translation key for the glyph's memory entry")
                .add()
                .appendInherited(new KeyedCodec<>("ManaConsumption", Codec.INTEGER),
                        (a, v) -> a.manaConsumption = v, a -> a.manaConsumption,
                        (a, p) -> a.manaConsumption = p.manaConsumption)
                .documentation("How much mana the glyph consumes when cast")
                .add()
                .appendInherited(new KeyedCodec<>("Volatility", VolatilityAsset.CODEC),
                        (a, v) -> a.volatility = v, a -> a.volatility,
                        (a, p) -> a.volatility = p.volatility)
                .documentation("The volatility config for the glyph")
                .add()
                .appendInherited(
                        new KeyedCodec<>("ShapeStructure",
                                new ArrayCodec<>(DrawnShapeComponent.CODEC, DrawnShapeComponent[]::new)),
                        (c, v) -> {
                            if (v != null) {
                                c.shapes = new ArrayList<>(Arrays.asList(v));
                            } else {
                                c.shapes = new ArrayList<>();
                            }
                        },
                        c -> c.shapes.toArray(DrawnShapeComponent[]::new),
                        (a, p) -> a.shapes = new ArrayList<>(p.shapes))
                .documentation("The shape of the glyph - determines what it takes to draw")
                .add()
                .appendInherited(new KeyedCodec<>("IsReversable", Codec.BOOLEAN),
                        (a, v) -> a.isReversable = v, a -> a.isReversable,
                        (a, p) -> a.isReversable = p.isReversable)
                .documentation("Whether or not the shape is reversable (drawn in any order)")
                .add()
                .appendInherited(
                        new KeyedCodec<>("Slots", slotMapCodec),
                        (a, v) -> {
                            a.slots = v != null ? new LinkedHashMap<>(v) : new LinkedHashMap<>();
                            a.slotIndexCache = null;
                        },
                        a -> a.slots,
                        (a, p) -> {
                            a.slots = new LinkedHashMap<>(p.slots);
                            a.slotIndexCache = null;
                        })
                .add()
                .appendInherited(new KeyedCodec<>("Style", HexStyleAsset.CHILD_ASSET_CODEC),
                        (a, v) -> a.styleId = v,
                        a -> a.styleId,
                        (a, p) -> a.styleId = p.styleId)
                .addValidatorLate(() -> HexStyleAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("The visual style of the glyph - particles, sounds, default colors, model")
                .add()
                .build();
    }
}
