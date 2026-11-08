package com.riprod.hexcode.core.common.glyphs.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIButton;
import com.hypixel.hytale.codec.schema.metadata.ui.UICreateButtons;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorPreview;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.schema.metadata.ui.UISidebarButtons;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.icon.GlyphIconEditorButton;
import com.riprod.hexcode.core.common.glyphs.icon.GlyphIconStore;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphCostUtil;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.node.NodeConfig;

import lombok.Getter;

public abstract class GlyphAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, GlyphAsset>> {
    public static final AssetCodecMapCodec<String, GlyphAsset> CODEC;
    public static final BuilderCodec<GlyphAsset> ABSTRACT_CODEC;
    private static AssetStore<String, GlyphAsset, DefaultAssetMap<String, GlyphAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    protected static final float PASSIVE_FLOOR = 0.1f;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String modelPath = "Glyph_Template";
    protected String icon;
    protected String title;
    protected String description;
    protected String verboseDescription;
    protected float basePower = 1.0f;
    protected float manaConsumption = 1.0f;
    protected boolean isReversable = false;
    protected boolean isEnabled = true;
    protected boolean isDeprecated = false;
    protected VolatilityAsset volatility = new VolatilityAsset();
    @Getter
    protected Impact volatilityImpact = new Impact() {
        @Override
        public float compute(double input) {
            return 1.0f;
        }
    };
    protected ArrayList<String> shapes = new ArrayList<>();
    protected LinkedHashMap<String, String> slots = new LinkedHashMap<>();
    protected String effectsId;

    public static AssetStore<String, GlyphAsset, DefaultAssetMap<String, GlyphAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(GlyphAsset.class);
        }

        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, GlyphAsset> getAssetMap() {
        return (DefaultAssetMap<String, GlyphAsset>) getAssetStore().getAssetMap();
    }

    public GlyphAsset() {
    }

    /*
     * Invoked when the glyph is executed by another glyph or as the first glyph in
     * a chain
     */
    public abstract void execute(Glyph glyph, HexContext hexContext);

    /** Invoked whenever the value of the glyph is read */
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        return hexContext.getOwnVariable(glyph.getId());
    }

    @Nullable
    public static <T extends GlyphAsset> T get(String glyphId, Class<T> glyphClass) {
        var glyph = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (glyphClass.isInstance(glyph)) {
            return glyphClass.cast(glyph);
        }
        return null;
    }

    @Nullable
    public static <T extends GlyphAsset> T of(@Nullable Glyph glyph, Class<T> glyphClass) {
        return glyph == null ? null : get(glyph.getGlyphId(), glyphClass);
    }

    @Override
    public final String getId() {
        return this.id;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean isReversable() {
        return this.isReversable;
    }

    public boolean isDeprecated() {
        return this.isDeprecated;
    }

    public String getModelPath() {
        return this.modelPath;
    }

    public String getIcon() {
        if (this.icon != null && !this.icon.isEmpty()) {
            return this.icon;
        }
        return GlyphIconStore.derivedIconPath(this.id);
    }

    public float getBasePower() {
        return this.basePower;
    }

    public float getManaConsumption() {
        return this.manaConsumption;
    }

    public VolatilityAsset getVolatility() {
        return this.volatility;
    }

    public List<ShapeAsset> getShapes() {
        List<ShapeAsset> resolved = new ArrayList<>(this.shapes.size());
        for (String shapeId : this.shapes) {
            ShapeAsset shape = ShapeAsset.getAssetMap().getAsset(shapeId);
            if (shape != null)
                resolved.add(shape);
        }
        return Collections.unmodifiableList(resolved);
    }

    public Map<String, SlotConfig> getSlots() {
        LinkedHashMap<String, SlotConfig> resolved = new LinkedHashMap<>(this.slots.size());
        for (Map.Entry<String, String> entry : this.slots.entrySet()) {
            NodeConfig config = NodeConfig.getAssetMap().getAsset(entry.getValue());
            if (config instanceof SlotConfig slotConfig)
                resolved.put(entry.getKey(), slotConfig);
        }
        return resolved;
    }

    @Nullable
    public SlotConfig getSlot(String key) {
        String id = this.slots.get(key);
        NodeConfig config = id != null ? NodeConfig.getAssetMap().getAsset(id) : null;
        return config instanceof SlotConfig slotConfig ? slotConfig : null;
    }

    public Set<String> getSlotKeys() {
        return Collections.unmodifiableSet(this.slots.keySet());
    }

    public boolean hasSlot(String key) {
        return this.slots.containsKey(key);
    }

    public GlyphEffects getEffects() {
        if (this.effectsId == null)
            return null;
        return GlyphEffects.getAssetMap().getAsset(this.effectsId);
    }

    public int getSlotCount() {
        return this.slots.size();
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

    public boolean isPassive(Glyph glyph) {
        return false;
    }

    public float collectMana(Glyph glyph, GlyphAsset asset) {
        if (asset == null)
            return 0f;
        return asset.getManaConsumption()
                * ((1 - glyph.getEfficiency()) * 0.25f + 0.75f);
    }

    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return GlyphCostUtil.volatilityCost(glyph, hexContext, asset);
    }

    /** Builds a generic glyph with no config / custom codec */
    final public static <T extends GlyphAsset> void register(Class<T> glyph, @Nonnull String glyphId,
            Supplier<T> glyphSupplier) {
        CODEC.register(glyphId, glyph, BuilderCodec.builder(glyph, glyphSupplier, ABSTRACT_CODEC).build());
    }

    /** Builds a custom glyph with it's own codec */
    final public static <T extends GlyphAsset> void register(Class<T> glyph, @Nonnull String glyphId,
            BuilderCodec<T> glyphCodec) {
        CODEC.register(glyphId, glyph, glyphCodec);
    }

    static {
        CODEC = new AssetCodecMapCodec<>("Type", Codec.STRING,
                (a, s) -> a.id = s,
                a -> a.id,
                (a, data) -> a.data = data,
                a -> a.data);

        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(GlyphAsset::getAssetStore));
        ABSTRACT_CODEC = BuilderCodec.abstractBuilder(GlyphAsset.class)
                .metadata(new UIEditorPreview(UIEditorPreview.PreviewType.MODEL))
                .metadata(new UISidebarButtons(
                        new UIButton(GlyphIconEditorButton.BUTTON_TEXT_ID,
                                GlyphIconEditorButton.BUTTON_ID),
                        new UIButton("server.hexcode.assetEditor.buttons.spawnGlyph",
                                "HexcodeSpawnGlyph")))
                .metadata(new UICreateButtons(
                        new UIButton(GlyphIconEditorButton.BUTTON_TEXT_ID,
                                GlyphIconEditorButton.BUTTON_ID),
                        new UIButton("server.hexcode.assetEditor.buttons.spawnGlyph",
                                "HexcodeSpawnGlyph")))

                // documentation
                .appendInherited(new KeyedCodec<>("Title", Codec.STRING),
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
                .appendInherited(new KeyedCodec<>("Icon", Codec.STRING),
                        (a, v) -> a.icon = v, a -> a.icon,
                        (a, p) -> a.icon = p.icon)
                .addValidator(GlyphIconStore.ICON_VALIDATOR)
                .metadata(new UIEditor(new UIEditor.Icon(GlyphIconStore.ICON_PATH_TEMPLATE, 64, 64)))
                .documentation("Custom Icon Override")
                .add()

                .appendInherited(new KeyedCodec<>("IsEnabled", Codec.BOOLEAN),
                        (a, v) -> a.isEnabled = v, a -> a.isEnabled,
                        (a, p) -> a.isEnabled = p.isEnabled)
                .documentation("Whether or not the glyph is enabled")
                .add()
                .appendInherited(new KeyedCodec<>("IsDeprecated", Codec.BOOLEAN),
                        (a, v) -> a.isDeprecated = v, a -> a.isDeprecated,
                        (a, p) -> a.isDeprecated = p.isDeprecated)
                .documentation("Whether or not the glyph is deprecated")
                .add()
                .appendInherited(new KeyedCodec<>("BasePower", Codec.FLOAT),
                        (a, v) -> a.basePower = v, a -> a.basePower,
                        (a, p) -> a.basePower = p.basePower)
                .add()
                .appendInherited(new KeyedCodec<>("ManaConsumption", Codec.FLOAT),
                        (a, v) -> a.manaConsumption = v, a -> a.manaConsumption,
                        (a, p) -> a.manaConsumption = p.manaConsumption)
                .documentation("How much mana the glyph consumes when cast")
                .add()
                .appendInherited(new KeyedCodec<>("Volatility", VolatilityAsset.CODEC),
                        (a, v) -> a.volatility = v, a -> a.volatility,
                        (a, p) -> a.volatility = p.volatility)
                .documentation("The volatility config for the glyph")
                .add()
                .append(new KeyedCodec<>("VolatilityImpact", Impact.CODEC),
                        (c, v) -> c.volatilityImpact = v, c -> c.volatilityImpact)
                .add()

                // slots
                .appendInherited(
                        new KeyedCodec<>("Slots", new MapCodec<>(
                                NodeConfig.CHILD_ASSET_CODEC, LinkedHashMap::new, false)),
                        (a, v) -> {
                            a.slots = v != null ? new LinkedHashMap<>(v) : new LinkedHashMap<>();
                        },
                        a -> a.slots,
                        (a, p) -> {
                            a.slots = new LinkedHashMap<>(p.slots);
                        })
                .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
                .metadata(new UIEditorSectionStart("Slots"))
                .documentation("The slots available on the glyph - determines what can be connected to it")
                .add()

                // Drawing
                .appendInherited(
                        new KeyedCodec<>("ShapeStructure",
                                new ArrayCodec<>(ShapeAsset.CHILD_ASSET_CODEC, String[]::new)),
                        (c, v) -> {
                            if (v != null) {
                                c.shapes = new ArrayList<>(Arrays.asList(v));
                            } else {
                                c.shapes = new ArrayList<>();
                            }
                        },
                        c -> c.shapes.toArray(String[]::new),
                        (a, p) -> a.shapes = new ArrayList<>(p.shapes))
                .documentation("The shape of the glyph - determines what it takes to draw")
                .metadata(new UIEditorSectionStart("Drawing"))
                .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
                .add()
                .appendInherited(new KeyedCodec<>("IsReversable", Codec.BOOLEAN),
                        (a, v) -> a.isReversable = v, a -> a.isReversable,
                        (a, p) -> a.isReversable = p.isReversable)
                .documentation("Whether or not the shape is reversable (drawn in any order)")
                .add()
                .appendInherited(new KeyedCodec<>("ModelPath", Codec.STRING),
                        (a, v) -> a.modelPath = v, a -> a.modelPath,
                        (a, p) -> a.modelPath = p.modelPath)
                .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("Glyph Model Override")
                .add()

                // Cosmetics
                .appendInherited(new KeyedCodec<>("Effects", GlyphEffects.CHILD_ASSET_CODEC),
                        (a, v) -> a.effectsId = v,
                        a -> a.effectsId,
                        (a, p) -> a.effectsId = p.effectsId)
                .addValidatorLate(() -> GlyphEffects.VALIDATOR_CACHE.getValidator().late())
                .documentation("The effects of the glyph - palette, body model and particles and sounds per slot")
                .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
                .metadata(new UIEditorSectionStart("Effects"))
                .add()
                .build();
    }
}
