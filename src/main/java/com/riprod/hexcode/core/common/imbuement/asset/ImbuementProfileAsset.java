package com.riprod.hexcode.core.common.imbuement.asset;

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
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public abstract class ImbuementProfileAsset
        implements JsonAssetWithMap<String, DefaultAssetMap<String, ImbuementProfileAsset>> {

    public static final AssetCodecMapCodec<String, ImbuementProfileAsset> CODEC;
    public static final BuilderCodec<ImbuementProfileAsset> BASE_CODEC;
    private static AssetStore<String, ImbuementProfileAsset, DefaultAssetMap<String, ImbuementProfileAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String categoryId = "";
    @Nullable
    protected ItemArmorSlot armorSlot;
    protected String[] excludedCategories = new String[0];
    @Nullable
    protected String defaultsId;
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

    @Override
    public String getId() {
        return this.id;
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
    public HexConfigAsset getDefaults() {
        if (this.defaultsId == null) return null;
        return HexConfigAsset.getAssetMap().getAsset(this.defaultsId);
    }

    @Nullable
    public String getDisplayModelOverride() {
        return displayModelOverride;
    }

    public Map<PedestalState, String> getStateAnimations() {
        return stateAnimations;
    }

    // per-type slot provider: books generate pages from the book asset, others return their JSON slots
    public abstract Map<String, PedestalSlot> resolveSlots(@Nullable ItemStack stored);

    @Nullable
    public PedestalSlot findSlot(@Nullable ItemStack stored, String key) {
        return resolveSlots(stored).get(key);
    }

    public boolean isSkipSelecting(@Nullable ItemStack stored) {
        return resolveSlots(stored).size() == 1;
    }

    // slot storage: default is the item's Imbuement metadata; a leaf may back its slots elsewhere

    public Map<String, ImbuementData> readAll(@Nullable ItemStack stored) {
        return ImbuementUtils.readAll(stored);
    }

    @Nullable
    public Hex readHex(@Nullable ItemStack stored, String slotKey, ComponentAccessor<EntityStore> accessor) {
        if (slotKey == null) return null;
        ImbuementData data = ImbuementUtils.read(stored, slotKey);
        return data != null ? ImbuementUtils.resolveHex(data, accessor) : null;
    }

    public ItemStack writeHex(ItemStack stored, String slotKey, @Nullable Hex hex) {
        if (hex == null || hex.getGlyphs().isEmpty()) {
            return ImbuementUtils.clear(stored, slotKey);
        }
        return ImbuementUtils.write(stored, slotKey, ImbuementUtils.fromHex(hex));
    }

    // context routing: null id lets the pedestal handler apply its own default (selecting)
    @Nullable
    public String getEntryContextId() {
        return null;
    }

    public int getEntryPriority() {
        return -1;
    }

    // whether writing metadata should also encode a BLOCK_HOLDER (block-backed imbuements)
    public boolean writesBlockHolder() {
        return false;
    }

    static {
        CODEC = new AssetCodecMapCodec<>("PedestalType", Codec.STRING,
                (a, s) -> a.id = s,
                a -> a.id,
                (a, data) -> a.data = data,
                a -> a.data);

        BASE_CODEC = BuilderCodec.abstractBuilder(ImbuementProfileAsset.class)
                .appendInherited(new KeyedCodec<>("CategoryId", Codec.STRING),
                        (a, v) -> { if (v != null) a.categoryId = v; },
                        a -> a.categoryId,
                        (a, p) -> a.categoryId = p.categoryId)
                .metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))
                .addValidatorLate(() -> ItemCategory.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("ArmorSlot", new EnumCodec<>(ItemArmorSlot.class)),
                        (a, v) -> a.armorSlot = v,
                        a -> a.armorSlot,
                        (a, p) -> a.armorSlot = p.armorSlot)
                .documentation("Optional. When set, this profile only matches armor items occupying the given slot (Head/Chest/Hands/Legs).")
                .add()
                .appendInherited(new KeyedCodec<>("ExcludedCategories",
                        new ArrayCodec<>(Codec.STRING, String[]::new)),
                        (a, v) -> { if (v != null) a.excludedCategories = v; },
                        a -> a.excludedCategories,
                        (a, p) -> a.excludedCategories = p.excludedCategories)
                .metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))
                .addValidatorLate(() -> new ArrayValidator<>(ItemCategory.VALIDATOR_CACHE.getValidator().late()).late())
                .documentation("Optional. If any of an item's Categories matches an entry in this list, the profile rejects the item.")
                .add()
                .appendInherited(new KeyedCodec<>("Defaults", HexConfigAsset.CHILD_ASSET_CODEC),
                        (a, v) -> a.defaultsId = v,
                        a -> a.defaultsId,
                        (a, p) -> a.defaultsId = p.defaultsId)
                .addValidatorLate(() -> HexConfigAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("Optional per-profile cast value defaults. Applied after player resolution and before per-imbuement Overrides.")
                .add()
                .appendInherited(new KeyedCodec<>("DisplayModelOverride", Codec.STRING),
                        (a, v) -> a.displayModelOverride = v,
                        a -> a.displayModelOverride,
                        (a, p) -> a.displayModelOverride = p.displayModelOverride)
                .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("Optional ModelAsset id to override the displayed item's default model.")
                .add()
                .appendInherited(new KeyedCodec<>("StateAnimations",
                        new EnumMapCodec<>(PedestalState.class, Codec.STRING,
                                () -> new EnumMap<>(PedestalState.class), false)),
                        (a, v) -> { if (v != null) a.stateAnimations = new EnumMap<>(v); },
                        a -> a.stateAnimations,
                        (a, p) -> a.stateAnimations = p.stateAnimations)
                .documentation("Per-PedestalState animation action name played on the imbued display.")
                .add()
                .build();

        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ImbuementProfileAsset::getAssetStore));
    }
}
