package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class CastPolicyComponent implements CastComponent {

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

    @Nonnull
    @Override
    public CastPolicyComponent branch() {
        return this;
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
                .<Boolean>appendInherited(new KeyedCodec<>("RequireMagicCharges", Codec.BOOLEAN),
                        (c, v) -> c.requireMagicCharges = v,
                        c -> c.requireMagicCharges,
                        (c, p) -> c.requireMagicCharges = p.requireMagicCharges)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("ConsumeMana", Codec.BOOLEAN),
                        (c, v) -> c.consumeMana = v,
                        c -> c.consumeMana,
                        (c, p) -> c.consumeMana = p.consumeMana)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("ApplyVolatilityDecay", Codec.BOOLEAN),
                        (c, v) -> c.applyVolatilityDecay = v,
                        c -> c.applyVolatilityDecay,
                        (c, p) -> c.applyVolatilityDecay = p.applyVolatilityDecay)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("BypassVolatilityDepletion", Codec.BOOLEAN),
                        (c, v) -> c.bypassVolatilityDepletion = v,
                        c -> c.bypassVolatilityDepletion,
                        (c, p) -> c.bypassVolatilityDepletion = p.bypassVolatilityDepletion)
                .add()
                .<Float>appendInherited(new KeyedCodec<>("TierScale", Codec.FLOAT),
                        (c, v) -> c.tierScale = v,
                        c -> c.tierScale,
                        (c, p) -> c.tierScale = p.tierScale)
                .documentation("Scales the volatility budget granted by the caster's stats.")
                .add()
                .build();
    }

    public static final CastComponentType<CastPolicyComponent> TYPE = HexComponentRegistry.register(
            CastPolicyComponent.class, "Policy", Overlay.CODEC, a -> new CastPolicyComponent());
}
