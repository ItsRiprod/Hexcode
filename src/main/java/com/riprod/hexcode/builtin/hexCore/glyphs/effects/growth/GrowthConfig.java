package com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class GrowthConfig extends GlyphConfig {

        public static final GrowthConfig DEFAULTS = new GrowthConfig();

        private double minAmount = 1.0;
        private double maxAmount = 20.0;
        private double stagesReferenceAmount = 5.0;
        private int bonemealRadius = 2;
        private float bonemealChance = 0.35f;
        private int attemptsFloor = 3;
        private double attemptsPerAmount = 1.5;
        private String growthEffectId = "Hexcode_Growth";
        private String[] vegetationBlocks = {
                        "Plant_Grass_Lush_Short",
                        "Plant_Grass_Lush",
                        "Plant_Grass_Lush_Tall",
                        "Plant_Flower_Common_Lime",
                        "Plant_Fern",
                        "Plant_Flower_Common_Blue",
                        "Plant_Flower_Common_Pink"
        };
        private String[] grassDirtPrefixes = { "Soil_Grass", "Soil_Dirt" };

        public double getMinAmount() {
                return minAmount;
        }

        public double getMaxAmount() {
                return maxAmount;
        }

        public double getStagesReferenceAmount() {
                return stagesReferenceAmount;
        }

        public int getBonemealRadius() {
                return bonemealRadius;
        }

        public float getBonemealChance() {
                return bonemealChance;
        }

        public int getAttemptsFloor() {
                return attemptsFloor;
        }

        public double getAttemptsPerAmount() {
                return attemptsPerAmount;
        }

        public String getGrowthEffectId() {
                return growthEffectId;
        }

        public String[] getVegetationBlocks() {
                return vegetationBlocks;
        }

        public String[] getGrassDirtPrefixes() {
                return grassDirtPrefixes;
        }

        public static final BuilderCodec<GrowthConfig> CODEC = BuilderCodec
                        .builder(GrowthConfig.class, GrowthConfig::new, GlyphConfig.BASE_CODEC)
                        .append(new KeyedCodec<>("MinAmount", Codec.DOUBLE, true),
                                        (c, v) -> c.minAmount = v, c -> c.minAmount)
                        .add()
                        .append(new KeyedCodec<>("MaxAmount", Codec.DOUBLE, true),
                                        (c, v) -> c.maxAmount = v, c -> c.maxAmount)
                        .add()
                        .append(new KeyedCodec<>("StagesReferenceAmount", Codec.DOUBLE, true),
                                        (c, v) -> c.stagesReferenceAmount = v, c -> c.stagesReferenceAmount)
                        .add()
                        .append(new KeyedCodec<>("BonemealRadius", Codec.INTEGER, true),
                                        (c, v) -> c.bonemealRadius = v, c -> c.bonemealRadius)
                        .add()
                        .append(new KeyedCodec<>("BonemealChance", Codec.FLOAT, true),
                                        (c, v) -> c.bonemealChance = v, c -> c.bonemealChance)
                        .add()
                        .append(new KeyedCodec<>("AttemptsFloor", Codec.INTEGER, true),
                                        (c, v) -> c.attemptsFloor = v, c -> c.attemptsFloor)
                        .add()
                        .append(new KeyedCodec<>("AttemptsPerAmount", Codec.DOUBLE, true),
                                        (c, v) -> c.attemptsPerAmount = v, c -> c.attemptsPerAmount)
                        .add()
                        .append(new KeyedCodec<>("GrowthEffect", Codec.STRING, true),
                                        (c, v) -> c.growthEffectId = v, c -> c.growthEffectId)
                        .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
                        .add()
                        .append(new KeyedCodec<>("VegetationBlocks", Codec.STRING_ARRAY, true),
                                        (c, v) -> c.vegetationBlocks = v, c -> c.vegetationBlocks)
                        .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getArrayValidator().late())
                        .add()
                        .append(new KeyedCodec<>("GrassDirtPrefixes", Codec.STRING_ARRAY, true),
                                        (c, v) -> c.grassDirtPrefixes = v, c -> c.grassDirtPrefixes)
                        .add()
                        .build();
}
