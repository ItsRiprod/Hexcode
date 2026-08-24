package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;

public final class CastPolicyComponent implements CastComponent {

    private static CastComponentType<CastPolicyComponent> componentType;

    public static CastComponentType<CastPolicyComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<CastPolicyComponent> type) {
        componentType = type;
    }

    private boolean requireMagicCharges = true;
    private boolean consumeMana = true;
    private boolean applyVolatilityDecay = true;
    private boolean bypassVolatilityDepletion = false;
    private float tierScale = 1.0f;

    public CastPolicyComponent() {
    }

    public boolean isRequireMagicCharges() {
        return requireMagicCharges;
    }

    public void setRequireMagicCharges(boolean requireMagicCharges) {
        this.requireMagicCharges = requireMagicCharges;
    }

    public boolean isConsumeMana() {
        return consumeMana;
    }

    public void setConsumeMana(boolean consumeMana) {
        this.consumeMana = consumeMana;
    }

    public boolean isApplyVolatilityDecay() {
        return applyVolatilityDecay;
    }

    public void setApplyVolatilityDecay(boolean applyVolatilityDecay) {
        this.applyVolatilityDecay = applyVolatilityDecay;
    }

    public boolean isBypassVolatilityDepletion() {
        return bypassVolatilityDepletion;
    }

    public void setBypassVolatilityDepletion(boolean bypassVolatilityDepletion) {
        this.bypassVolatilityDepletion = bypassVolatilityDepletion;
    }

    public float getTierScale() {
        return tierScale;
    }

    public void setTierScale(float tierScale) {
        this.tierScale = tierScale;
    }

    @Nonnull
    @Override
    public CastPolicyComponent copy() {
        CastPolicyComponent copy = new CastPolicyComponent();
        copy.requireMagicCharges = this.requireMagicCharges;
        copy.consumeMana = this.consumeMana;
        copy.applyVolatilityDecay = this.applyVolatilityDecay;
        copy.bypassVolatilityDepletion = this.bypassVolatilityDepletion;
        copy.tierScale = this.tierScale;
        return copy;
    }

    public static final class Overlay implements CastOverlay<CastPolicyComponent> {

        @Nullable private Boolean requireMagicCharges;
        @Nullable private Boolean consumeMana;
        @Nullable private Boolean applyVolatilityDecay;
        @Nullable private Boolean bypassVolatilityDepletion;
        @Nullable private Float tierScale;

        public Overlay() {
        }

        @Override
        public void applyTo(@Nonnull CastPolicyComponent target) {
            if (requireMagicCharges != null) target.setRequireMagicCharges(requireMagicCharges);
            if (consumeMana != null) target.setConsumeMana(consumeMana);
            if (applyVolatilityDecay != null) target.setApplyVolatilityDecay(applyVolatilityDecay);
            if (bypassVolatilityDepletion != null) {
                target.setBypassVolatilityDepletion(bypassVolatilityDepletion);
            }
            if (tierScale != null) target.setTierScale(tierScale);
        }

        public static final BuilderCodec<Overlay> CODEC = BuilderCodec
                .builder(Overlay.class, Overlay::new)
                .append(new KeyedCodec<>("RequireMagicCharges", Codec.BOOLEAN),
                        (c, v) -> c.requireMagicCharges = v,
                        c -> c.requireMagicCharges)
                .add()
                .append(new KeyedCodec<>("ConsumeMana", Codec.BOOLEAN),
                        (c, v) -> c.consumeMana = v,
                        c -> c.consumeMana)
                .add()
                .append(new KeyedCodec<>("ApplyVolatilityDecay", Codec.BOOLEAN),
                        (c, v) -> c.applyVolatilityDecay = v,
                        c -> c.applyVolatilityDecay)
                .add()
                .append(new KeyedCodec<>("BypassVolatilityDepletion", Codec.BOOLEAN),
                        (c, v) -> c.bypassVolatilityDepletion = v,
                        c -> c.bypassVolatilityDepletion)
                .add()
                .append(new KeyedCodec<>("TierScale", Codec.FLOAT),
                        (c, v) -> c.tierScale = v,
                        c -> c.tierScale)
                .documentation("Scales the volatility budget granted by the caster's stats.")
                .add()
                .build();
    }
}
