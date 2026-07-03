package com.riprod.hexcode.core.common.imbuement.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementSlotKeyValidator;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImbuementProfileAsset
        implements JsonAssetWithMap<String, DefaultAssetMap<String, ImbuementProfileAsset>> {

    public static final AssetBuilderCodec<String, ImbuementProfileAsset> CODEC;
    private static AssetStore<String, ImbuementProfileAsset, DefaultAssetMap<String, ImbuementProfileAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected ImbuementType type = ImbuementType.Item;
    protected String categoryId = "";
    @Nullable
    protected ItemArmorSlot armorSlot;
    protected String[] excludedCategories = new String[0];
    @Nullable
    protected HexContext defaults;
    protected Map<String, SlotAsset> slots = new LinkedHashMap<>();
    @Nullable
    protected String displayModelOverride;
    protected Map<PedestalState, String> stateAnimations = new EnumMap<>(PedestalState.class);

    public static AssetStore<String, ImbuementProfileAsset, DefaultAssetMap<String, ImbuementProfileAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ImbuementProfileAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, ImbuementProfileAsset> getAssetMap() {
        return (DefaultAssetMap<String, ImbuementProfileAsset>) getAssetStore().getAssetMap();
    }

    private ImbuementProfileAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public ImbuementType getType() {
        return type;
    }

    public String getCategoryId() {
        return categoryId;
    }

    @Nullable
    public ItemArmorSlot getArmorSlot() {
        return armorSlot;
    }

    public String[] getExcludedCategories() {
        return excludedCategories;
    }

    @Nullable
    public HexContext getDefaults() {
        return defaults;
    }

    public Map<String, SlotAsset> getSlots() {
        return slots;
    }

    public boolean isSkipSelecting() {
        return slots.size() == 1;
    }

    @Nullable
    public String getDisplayModelOverride() {
        return displayModelOverride;
    }

    public Map<PedestalState, String> getStateAnimations() {
        return stateAnimations;
    }

    @Nullable
    public SlotAsset findSlot(String key) {
        return slots.get(key);
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(ImbuementProfileAsset.class, ImbuementProfileAsset::new, Codec.STRING,
                        (asset, s) -> asset.id = s,
                        asset -> asset.id,
                        (asset, data) -> asset.data = data,
                        asset -> asset.data)
                .append(new KeyedCodec<>("Type", new EnumCodec<>(ImbuementType.class)),
                        (a, v) -> { if (v != null) a.type = v; },
                        a -> a.type)
                .add()
                .append(new KeyedCodec<>("CategoryId", Codec.STRING),
                        (a, v) -> { if (v != null) a.categoryId = v; },
                        a -> a.categoryId)
                .metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))
                .addValidatorLate(() -> ItemCategory.VALIDATOR_CACHE.getValidator().late())
                .add()
                .append(new KeyedCodec<>("ArmorSlot", new EnumCodec<>(ItemArmorSlot.class)),
                        (a, v) -> a.armorSlot = v,
                        a -> a.armorSlot)
                .documentation("Optional. When set, this profile only matches armor items occupying the given slot (Head/Chest/Hands/Legs).")
                .add()
                .append(new KeyedCodec<>("ExcludedCategories",
                        new ArrayCodec<>(Codec.STRING, String[]::new)),
                        (a, v) -> { if (v != null) a.excludedCategories = v; },
                        a -> a.excludedCategories)
                .metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))
                .addValidatorLate(() -> new ArrayValidator<>(ItemCategory.VALIDATOR_CACHE.getValidator().late()).late())
                .documentation("Optional. If any of an item's Categories matches an entry in this list, the profile rejects the item.")
                .add()
                .append(new KeyedCodec<>("Defaults", HexContext.CODEC),
                        (a, v) -> a.defaults = v,
                        a -> a.defaults)
                .documentation("Optional per-profile cast value defaults. Applied after player resolution and before per-imbuement Overrides.")
                .add()
                .append(new KeyedCodec<>("Slots",
                        new MapCodec<>(SlotAsset.CODEC, LinkedHashMap::new, false)),
                        (a, v) -> { if (v != null) a.slots = new LinkedHashMap<>(v); },
                        a -> a.slots)
                .documentation("Slot key → SlotAsset. Insertion order drives radial layout. SkipSelecting is implied when size==1. Keys must match a registered Trigger id (TriggerRegistry).")
                .addValidatorLate(() -> ImbuementSlotKeyValidator.INSTANCE.late())
                .add()
                .append(new KeyedCodec<>("DisplayModelOverride", Codec.STRING),
                        (a, v) -> a.displayModelOverride = v,
                        a -> a.displayModelOverride)
                .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("Optional ModelAsset id to override the displayed item's default model.")
                .add()
                .append(new KeyedCodec<>("StateAnimations",
                        new EnumMapCodec<>(PedestalState.class, Codec.STRING,
                                () -> new EnumMap<>(PedestalState.class), false)),
                        (a, v) -> { if (v != null) a.stateAnimations = new EnumMap<>(v); },
                        a -> a.stateAnimations)
                .documentation("Per-PedestalState animation action name played on the imbued display.")
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ImbuementProfileAsset::getAssetStore));
    }
}
