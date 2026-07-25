package com.riprod.hexcode.core.common.drawing.registry;

import javax.annotation.Nullable;

import org.joml.Vector2d;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.codec.Vector2dArrayCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public class ShapeAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ShapeAsset>> {
    public static final AssetBuilderCodec<String, ShapeAsset> CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    private static AssetStore<String, ShapeAsset, DefaultAssetMap<String, ShapeAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    public static final Impact IDENTITY_IMPACT = new Impact() {
        @Override
        public float compute(double input) {
            return (float) input;
        }
    };

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String templateId;
    protected Boolean canRotate;
    protected Boolean centerFilled;
    protected long expectedSpeed;
    protected float relativeSize = 1.0f;
    protected Vector2d relativePosition = new Vector2d();
    protected String statResource;
    protected float statContribution = 8.0f;
    protected Impact statResourceImpact = IDENTITY_IMPACT;
    protected String texture;
    protected String model;

    public static AssetStore<String, ShapeAsset, DefaultAssetMap<String, ShapeAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ShapeAsset.class);
        }

        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, ShapeAsset> getAssetMap() {
        return (DefaultAssetMap<String, ShapeAsset>) getAssetStore().getAssetMap();
    }

    private ShapeAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getBaseShapeId() {
        Object parentKey = this.data != null ? this.data.getParentKey() : null;
        return parentKey != null ? parentKey.toString() : this.id;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public Boolean getCanRotate() {
        return this.canRotate;
    }

    public Boolean getCenterFilled() {
        return this.centerFilled;
    }

    public long getExpectedSpeed() {
        return this.expectedSpeed;
    }

    public float getRelativeSize() {
        return this.relativeSize;
    }

    public Vector2d getRelativePosition() {
        return this.relativePosition;
    }

    @Nullable
    public String getStatResource() {
        return this.statResource;
    }

    public float getStatContribution() {
        return this.statContribution;
    }

    public Impact getStatResourceImpact() {
        return this.statResourceImpact;
    }

    @Nullable
    public String getTexture() {
        return this.texture;
    }

    @Nullable
    public String getModel() {
        return this.model;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(ShapeAsset.class, ShapeAsset::new, Codec.STRING, (asset, s) -> {
                    asset.id = s;
                }, (asset) -> {
                    return asset.id;
                }, (asset, data) -> {
                    asset.data = data;
                }, (asset) -> {
                    return asset.data;
                })
                .appendInherited(new KeyedCodec<>("TemplateId", Codec.STRING, true),
                        (a, v) -> a.templateId = v, a -> a.templateId,
                        (a, p) -> a.templateId = p.templateId)
                .addValidatorLate(() -> TemplateAsset.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("CanRotate", Codec.BOOLEAN, true),
                        (a, v) -> a.canRotate = v, a -> a.canRotate,
                        (a, p) -> a.canRotate = p.canRotate)
                .add()
                .appendInherited(new KeyedCodec<>("CenterFilled", Codec.BOOLEAN, true),
                        (a, v) -> a.centerFilled = v, a -> a.centerFilled,
                        (a, p) -> a.centerFilled = p.centerFilled)
                .add()
                .appendInherited(new KeyedCodec<>("ExpectedSpeed", Codec.LONG, true),
                        (a, v) -> a.expectedSpeed = v, a -> a.expectedSpeed,
                        (a, p) -> a.expectedSpeed = p.expectedSpeed)
                .add()
                .<Float>appendInherited(new KeyedCodec<>("RelativeSize", Codec.FLOAT, true),
                        (a, v) -> a.relativeSize = v, a -> a.relativeSize,
                        (a, p) -> a.relativeSize = p.relativeSize)
                .addValidator(Validators.range(0.0f, 1.0f))
                .add()
                .appendInherited(new KeyedCodec<>("RelativePosition", new Vector2dArrayCodec(), true),
                        (a, v) -> a.relativePosition = v, a -> a.relativePosition,
                        (a, p) -> a.relativePosition = p.relativePosition)
                .add()
                .appendInherited(new KeyedCodec<>("StatResource", Codec.STRING, true),
                        (a, v) -> a.statResource = v, a -> a.statResource,
                        (a, p) -> a.statResource = p.statResource)
                .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
                .add()
                .<Float>appendInherited(new KeyedCodec<>("StatContribution", Codec.FLOAT, true),
                        (a, v) -> a.statContribution = v, a -> a.statContribution,
                        (a, p) -> a.statContribution = p.statContribution)
                .addValidator(Validators.greaterThanOrEqual(0.0f))
                .add()
                .appendInherited(new KeyedCodec<>("StatResourceImpact", Impact.CODEC, true),
                        (a, v) -> a.statResourceImpact = v, a -> a.statResourceImpact,
                        (a, p) -> a.statResourceImpact = p.statResourceImpact)
                .add()
                .<String>appendInherited(new KeyedCodec<>("Texture", Codec.STRING, true),
                        (a, v) -> a.texture = v, a -> a.texture,
                        (a, p) -> a.texture = p.texture)
                .addValidator(CommonAssetValidator.TEXTURE_CHARACTER_ATTACHMENT)
                .add()
                .<String>appendInherited(new KeyedCodec<>("Model", Codec.STRING, true),
                        (a, v) -> a.model = v, a -> a.model,
                        (a, p) -> a.model = p.model)
                .addValidator(CommonAssetValidator.MODEL_CHARACTER_ATTACHMENT)
                .add()
                .build();
        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(ShapeAsset.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ShapeAsset::getAssetStore));
    }
}
