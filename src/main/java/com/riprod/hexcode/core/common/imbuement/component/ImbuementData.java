package com.riprod.hexcode.core.common.imbuement.component;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.hexes.codec.HexCodec;
import com.riprod.hexcode.core.common.hexes.codec.HexCodecException;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;

public class ImbuementData {

    @Nullable
    private String hexCompressedId;
    @Nullable
    private Hex hex;
    @Nullable
    private String hexAssetId;
    @Nullable
    private String overridesId;

    public ImbuementData() {
    }

    public ImbuementData copy() {
        ImbuementData copy = new ImbuementData();
        copy.hexCompressedId = this.hexCompressedId;
        copy.hex = this.hex;
        copy.hexAssetId = this.hexAssetId;
        copy.overridesId = this.overridesId;
        return copy;
    }

    @Override
    public ImbuementData clone() {
        return copy();
    }

    @Nullable
    public String getHexCompressedId() {
        return hexCompressedId;
    }

    public void setHexCompressedId(@Nullable String hexCompressedId) {
        this.hexCompressedId = hexCompressedId;
    }

    @Nullable
    public Hex getHex() {
        return hex;
    }

    public void setHexFromValue(@Nullable Hex hex) {
        this.hexCompressedId = encodeOrNull(hex);
        this.hex = null;
    }

    @Nullable
    private static String encodeOrNull(@Nullable Hex hex) {
        if (hex == null || hex.getGlyphs().isEmpty()) return null;
        try {
            return HexCodec.serialize(hex);
        } catch (HexCodecException e) {
            return null;
        }
    }

    @Nullable
    public String getHexAssetId() {
        return hexAssetId;
    }

    public void setHexAssetId(@Nullable String hexAssetId) {
        this.hexAssetId = hexAssetId;
    }

    @Nullable
    public HexConfigAsset getOverrides() {
        if (this.overridesId == null) return null;
        return HexConfigAsset.getAssetMap().getAsset(this.overridesId);
    }

    public static final BuilderCodec<ImbuementData> CODEC = BuilderCodec
            .builder(ImbuementData.class, ImbuementData::new)
            .versioned()
            .codecVersion(1)
            .append(new KeyedCodec<>("CompressedId", Codec.STRING),
                    (c, v) -> c.hexCompressedId = v,
                    c -> c.hexCompressedId != null ? c.hexCompressedId : encodeOrNull(c.hex))
            .add()
            .append(new KeyedCodec<>("Hex", Hex.CODEC),
                    (c, v) -> c.hex = v,
                    c -> c.hexCompressedId == null && encodeOrNull(c.hex) == null ? c.hex : null)
            .add()
            .append(new KeyedCodec<>("AssetId", Codec.STRING),
                    (c, v) -> c.hexAssetId = v,
                    c -> c.hexAssetId)
            .addValidatorLate(() -> SavedHexAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("Overrides", HexConfigAsset.CHILD_ASSET_CODEC),
                    (c, v) -> c.overridesId = v,
                    c -> c.overridesId)
            .addValidatorLate(() -> HexConfigAsset.VALIDATOR_CACHE.getValidator().late())
            .documentation("Optional cast overrides applied at fire time. Snapshot of cast-affecting values written at imbue time from the source item's stat modifiers.")
            .add()
            .build();
}
