package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.utils.SpellMana;

public final class CastManaComponent implements CastComponent, CastOverlay<CastManaComponent> {

    private static final float UNSET = -1f;

    private float cost = UNSET;

    public CastManaComponent() {
    }

    @Override
    public void applyTo(@Nonnull CastManaComponent target) {
        if (this.cost >= 0f) {
            target.setCost(this.cost);
        }
    }

    public float getCost() {
        return Math.max(0f, cost);
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    @Nonnull
    @Override
    public CastManaComponent copy() {
        CastManaComponent copy = new CastManaComponent();
        copy.cost = this.cost;
        return copy;
    }

    @Nonnull
    @Override
    public CastManaComponent branch() {
        return this;
    }

    public static final BuilderCodec<CastManaComponent> CODEC = BuilderCodec
            .builder(CastManaComponent.class, CastManaComponent::new)
            .<Float>appendInherited(new KeyedCodec<>("Cost", Codec.FLOAT),
                    (c, v) -> c.cost = v,
                    c -> c.cost < 0f ? null : c.cost,
                    (c, p) -> c.cost = p.cost)
            .documentation("Replaces the mana cost computed from the hex.")
            .add()
            .build();

    public static final CastComponentType<CastManaComponent> TYPE = HexComponentRegistry.register(
            CastManaComponent.class, "Mana", CODEC, a -> {
                var mana = new CastManaComponent();
                mana.setCost(SpellMana.computeTotalMana(a.get(CastHexComponent.TYPE).getHex(), a.accessor()));
                return mana;
            });
}
