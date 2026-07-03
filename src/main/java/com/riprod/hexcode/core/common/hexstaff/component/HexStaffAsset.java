package com.riprod.hexcode.core.common.hexstaff.component;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleValidator;

import javax.annotation.Nullable;

public class HexStaffAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, HexStaffAsset>> {
  private static AssetStore<String, HexStaffAsset, DefaultAssetMap<String, HexStaffAsset>> ASSET_STORE;

  protected AssetExtraInfo.Data data;
  protected String id;
  protected String castStyleId;
  protected ModelParticle[] castingAuraParticles;
  protected ModelParticle[] craftingAuraParticles;
  protected float castDecayRate = 0.05f;
  protected String styleId;
  @Nullable
  protected HexContext defaults;

  public static AssetStore<String, HexStaffAsset, DefaultAssetMap<String, HexStaffAsset>> getAssetStore() {
    if (ASSET_STORE == null) {
      ASSET_STORE = AssetRegistry.getAssetStore(HexStaffAsset.class);
    }

    return ASSET_STORE;
  }

  public static DefaultAssetMap<String, HexStaffAsset> getAssetMap() {
    return (DefaultAssetMap<String, HexStaffAsset>) getAssetStore().getAssetMap();
  }

  private HexStaffAsset() {
  }

  @Override
  public String getId() {
    return this.id;
  }

  public String getCastStyleId() {
    return this.castStyleId;
  }

  public ModelParticle[] getCastingAuraParticles() {
    return this.castingAuraParticles;
  }

  public ModelParticle[] getCraftingAuraParticles() {
    return this.craftingAuraParticles;
  }

  public float getCastDecayRate() {
    return this.castDecayRate;
  }

  public String getStyleId() {
    return this.styleId;
  }

  public HexStyleAsset getStyle() {
    if (this.styleId == null) return null;
    return HexStyleAsset.getAssetMap().getAsset(this.styleId);
  }

  @Nullable
  public HexContext getDefaults() {
    return this.defaults;
  }

  public HexColors getColors() {
    HexStyleAsset style = getStyle();
    if (style == null) return null;
    HexColors c = new HexColors();
    if (style.getPrimaryColor() != null) c.setPrimaryColor(style.getPrimaryColor().clone());
    if (style.getSecondaryColor() != null) c.setSecondaryColor(style.getSecondaryColor().clone());
    c.setPrimaryAlpha(style.getAlphaOrDefault());
    return c;
  }

  public static final AssetBuilderCodec<String, HexStaffAsset> CODEC = AssetBuilderCodec.builder(
      HexStaffAsset.class,
      HexStaffAsset::new,
      Codec.STRING,
      (glyphAsset, s) -> glyphAsset.id = s,
      glyphAsset -> glyphAsset.id,
      (asset, data) -> asset.data = data,
      asset -> asset.data)
      .appendInherited(new KeyedCodec<>("CastStyleId", Codec.STRING),
          (a, v) -> a.castStyleId = v,
          a -> a.castStyleId,
          (a, p) -> a.castStyleId = p.castStyleId)
          .metadata(new UIEditor(new UIEditor.Dropdown("HexcodeCastingStyles")))
          .addValidatorLate(() -> CastingStyleValidator.INSTANCE.late())
      .add()
      .appendInherited(new KeyedCodec<>("CastingAuraParticles", ModelParticle.ARRAY_CODEC),
          (a, v) -> a.castingAuraParticles = v,
          a -> a.castingAuraParticles,
          (a, p) -> a.castingAuraParticles = p.castingAuraParticles)
      .add()
      .appendInherited(new KeyedCodec<>("CraftingAuraParticles", ModelParticle.ARRAY_CODEC),
          (a, v) -> a.craftingAuraParticles = v,
          a -> a.craftingAuraParticles,
          (a, p) -> a.craftingAuraParticles = p.craftingAuraParticles)
      .add()
      .appendInherited(new KeyedCodec<>("CastDecayRate", Codec.FLOAT),
          (a, v) -> a.castDecayRate = v,
          a -> a.castDecayRate,
          (a, p) -> a.castDecayRate = p.castDecayRate)
      .add()
      .appendInherited(new KeyedCodec<>("Style", HexStyleAsset.CHILD_ASSET_CODEC),
          (a, v) -> a.styleId = v,
          a -> a.styleId,
          (a, p) -> a.styleId = p.styleId)
      .addValidatorLate(() -> HexStyleAsset.VALIDATOR_CACHE.getValidator().late())
      .add()
      .appendInherited(new KeyedCodec<>("Defaults", HexContext.CODEC),
          (a, v) -> a.defaults = v,
          a -> a.defaults,
          (a, p) -> a.defaults = p.defaults)
      .documentation("Optional cast overrides applied when this staff is wielded. Same shape as ImbuementProfileAsset.Defaults.")
      .add()
      .build();

  public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(
      new AssetKeyValidator<>(HexStaffAsset::getAssetStore));
}
