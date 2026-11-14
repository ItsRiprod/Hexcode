package com.riprod.hexcode.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.configly.Configly;

import lombok.Getter;

import com.riprod.configly.Config;
import javax.annotation.Nonnull;

public final class HexcodeConfig extends Config {

    @Nonnull
    public static final String TYPE = "Hexcode";

    @Nonnull
    public static final HexcodeConfig DEFAULTS = new HexcodeConfig();

    @Nonnull
    public static final BuilderCodec<HexcodeConfig> CODEC = BuilderCodec
            .builder(HexcodeConfig.class, HexcodeConfig::new)
            .append(new KeyedCodec<>("RespectPvp", Codec.BOOLEAN),
                    (config, b) -> config.respectPvp = b,
                    config -> config.respectPvp)
            .metadata(new UIEditorSectionStart("Gameplay"))
            .documentation("Whether Hexcode utility/status spells honour the server PVP setting - damage spells will always respect the PVP config.")
            .add()
            .append(new KeyedCodec<>("RespectClaims", Codec.BOOLEAN),
                    (config, b) -> config.respectClaims = b,
                    config -> config.respectClaims)
            .documentation("Whether Hexcode spells honour block protection - world block break/place "
                    + "settings, non-modifiable environments, and Break/PlaceBlockEvent vetoes from "
                    + "land-claim plugins")
            .add()
            .append(new KeyedCodec<>("AllowNametagOverrides", Codec.BOOLEAN),
                    (config, b) -> config.allowNametagOverrides = b,
                    config -> config.allowNametagOverrides)
            .documentation("Whether or not glyphs can modify player's nametags")
            .add()
            .append(new KeyedCodec<>("MinEntityScale", Codec.FLOAT),
                    (config, f) -> config.minEntityScale = f,
                    config -> config.minEntityScale)
            .documentation("Smallest size any glyph can leave an entity at, after every Scale and "
                    + "Disguise layer on it is combined")
            .addValidator(Validators.greaterThan(0f))
            .add()
            .append(new KeyedCodec<>("MaxEntityScale", Codec.FLOAT),
                    (config, f) -> config.maxEntityScale = f,
                    config -> config.maxEntityScale)
            .documentation("Largest size any glyph can leave an entity at, after every Scale and "
                    + "Disguise layer on it is combined")
            .addValidator(Validators.greaterThan(0f))
            .add()
            .append(new KeyedCodec<>("MaxGlyphsPerTick", Codec.INTEGER),
                    (config, i) -> config.maxGlyphsPerTick = i,
                    config -> config.maxGlyphsPerTick)
            .metadata(new UIEditorSectionStart("Performance"))
            .documentation("Maximum glyphs every spell in a world may execute together in one tick. "
                    + "Work above this is deferred to a later tick, never dropped.")
            .addValidator(Validators.min(1))
            .add()
            .append(new KeyedCodec<>("MaxGlyphNanosPerTick", Codec.LONG),
                    (config, l) -> config.maxGlyphNanosPerTick = l,
                    config -> config.maxGlyphNanosPerTick)
            .documentation("Maximum wall-clock nanoseconds the glyph queue may spend in one tick. "
                    + "Work past this budget is deferred to a later tick, never dropped.")
            .addValidator(Validators.min(1L))
            .add()
            .append(new KeyedCodec<>("MaxGlyphsPerCast", Codec.INTEGER),
                    (config, i) -> config.maxGlyphsPerCast = i,
                    config -> config.maxGlyphsPerCast)
            .documentation("Maximum glyphs a single spell may execute in one tick, so one wide spell cannot "
                    + "monopolise the world budget")
            .addValidator(Validators.min(1))
            .add()
            .append(new KeyedCodec<>("MaxActiveCastsPerWorld", Codec.INTEGER),
                    (config, i) -> config.maxActiveCastsPerWorld = i,
                    config -> config.maxActiveCastsPerWorld)
            .documentation("Maximum spells that may be live in one world at once. Beyond this the oldest are "
                    + "ended first, so no single world can accumulate spells without bound.")
            .addValidator(Validators.min(1))
            .add()
            .append(new KeyedCodec<>("RootScalePerGlyph", Codec.FLOAT),
                    (config, f) -> config.rootScalePerGlyph = f,
                    config -> config.rootScalePerGlyph)
            .metadata(new UIEditorSectionStart("Advanced"))
            .documentation("Displayed hexes: how much the root glyph grows per glyph in the hex while the hex is small")
            .addValidator(Validators.min(0f))
            .add()
            .append(new KeyedCodec<>("RootScaleMaxGrowth", Codec.FLOAT),
                    (config, f) -> config.rootScaleMaxGrowth = f,
                    config -> config.rootScaleMaxGrowth)
            .documentation("Displayed hexes: the root glyph never grows past 1 + this, however many glyphs the hex has")
            .addValidator(Validators.min(0f))
            .add()
            .append(new KeyedCodec<>("ChildScale", Codec.FLOAT),
                    (config, f) -> config.childScale = f,
                    config -> config.childScale)
            .documentation("Radial layout: size of each of several child glyphs, relative to their parent")
            .addValidator(Validators.greaterThan(0f))
            .add()
            .append(new KeyedCodec<>("OnlyChildScale", Codec.FLOAT),
                    (config, f) -> config.onlyChildScale = f,
                    config -> config.onlyChildScale)
            .documentation("Radial layout: size of an only child glyph (stacked on its parent), relative to the parent")
            .addValidator(Validators.greaterThan(0f))
            .add()
            .append(new KeyedCodec<>("ChildSpread", Codec.FLOAT),
                    (config, f) -> config.childSpread = f,
                    config -> config.childSpread)
            .documentation("Radial layout: how far children sit from their parent, in multiples of the parent's hover radius")
            .addValidator(Validators.min(0f))
            .add()
            .append(new KeyedCodec<>("AuthoredRadius", Codec.FLOAT),
                    (config, f) -> config.authoredRadius = f,
                    config -> config.authoredRadius)
            .documentation("Authored layout: the crafted arrangement is scaled to this radius (blocks) times the root glyph's size")
            .addValidator(Validators.greaterThan(0f))
            .add()
            .append(new KeyedCodec<>("AuthoredDepthFalloff", Codec.FLOAT),
                    (config, f) -> config.authoredDepthFalloff = f,
                    config -> config.authoredDepthFalloff)
            .documentation("Authored layout: each step along the flow from the first glyph multiplies glyph size by this")
            .addValidator(Validators.greaterThan(0f))
            .add()
            .append(new KeyedCodec<>("AuthoredLayerSpacing", Codec.FLOAT),
                    (config, f) -> config.authoredLayerSpacing = f,
                    config -> config.authoredLayerSpacing)
            .documentation("Authored layout: gap between crafted heights, used only to decide which overlapping glyph draws on top")
            .addValidator(Validators.min(0f))
            .add()
            .afterDecode((config, extraInfo) -> {
                if (config.maxGlyphsPerCast > config.maxGlyphsPerTick) {
                    extraInfo.getValidationResults().warn("MaxGlyphsPerCast (" + config.maxGlyphsPerCast
                            + ") exceeds MaxGlyphsPerTick (" + config.maxGlyphsPerTick
                            + "); clamping MaxGlyphsPerCast to MaxGlyphsPerTick");
                    config.maxGlyphsPerCast = config.maxGlyphsPerTick;
                }
                if (config.minEntityScale > config.maxEntityScale) {
                    extraInfo.getValidationResults().warn("MinEntityScale (" + config.minEntityScale
                            + ") exceeds MaxEntityScale (" + config.maxEntityScale
                            + "); clamping MinEntityScale to MaxEntityScale");
                    config.minEntityScale = config.maxEntityScale;
                }
            })
            .build();

    private boolean respectPvp = true;
    private boolean respectClaims = true;
    private boolean allowNametagOverrides = true;
    @Getter 
    private float minEntityScale = 0.001f;
    @Getter 
    private float maxEntityScale = 100f;
    @Getter 
    private int maxGlyphsPerTick = 1024;
    @Getter 
    private long maxGlyphNanosPerTick = 5_000_000L;
    @Getter 
    private int maxGlyphsPerCast = 128;
    @Getter 
    private int maxCastLifetimeTicks = 12000;
    @Getter 
    private int maxActiveCastsPerWorld = 512;
    @Getter 
    private float rootScalePerGlyph = 0.05f;
    @Getter 
    private float rootScaleMaxGrowth = 1.0f;
    @Getter 
    private float childScale = 0.2f;
    @Getter 
    private float onlyChildScale = 0.45f;
    @Getter 
    private float childSpread = 1.2f;
    @Getter 
    private float authoredRadius = 0.6f;
    @Getter 
    private float authoredDepthFalloff = 0.7f;
    @Getter 
    private float authoredLayerSpacing = 0.02f;

    private HexcodeConfig() {
    }

    @Nonnull
    public static HexcodeConfig get() {
        return Configly.getOrElse(TYPE, HexcodeConfig.class, DEFAULTS);
    }

    public boolean respectsPvp() {
        return respectPvp;
    }

    public boolean respectsClaims() {
        return respectClaims;
    }

    public boolean allowsNametagOverrides() {
        return allowNametagOverrides;
    }
}
