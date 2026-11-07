package com.riprod.hexcode.builtin.elementalism.element.efficiency;

import org.joml.Vector3dc;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AsymptoticBranchEfficiency extends BranchEfficiency {
    public static final String TYPE = "Asymptotic";
    public static final BuilderCodec<AsymptoticBranchEfficiency> CODEC;

    protected float base = 1.0f;
    protected float limit = 1.0f;
    protected float rate = 0.5f;

    @Override
    public float efficiency(float elapsedActive, int branches, Vector3dc velocity,
            Ref<EntityStore> host, ComponentAccessor<EntityStore> accessor) {
        var extra = Math.max(0, branches - 1);
        return this.limit + (this.base - this.limit) * (float) Math.pow(this.rate, extra);
    }

    static {
        CODEC = BuilderCodec.builder(AsymptoticBranchEfficiency.class, AsymptoticBranchEfficiency::new)
                .appendInherited(new KeyedCodec<>("Base", Codec.FLOAT),
                        (e, v) -> e.base = v, e -> e.base,
                        (e, p) -> e.base = p.base)
                .addValidator(Validators.min(0.0f))
                .documentation("Charge rate multiplier while this is the only live branch charging the element")
                .add()
                .appendInherited(new KeyedCodec<>("Limit", Codec.FLOAT),
                        (e, v) -> e.limit = v, e -> e.limit,
                        (e, p) -> e.limit = p.limit)
                .addValidator(Validators.min(0.0f))
                .documentation("Multiplier approached as live branches grow. Below Base decays, above Base grows")
                .add()
                .appendInherited(new KeyedCodec<>("Rate", Codec.FLOAT),
                        (e, v) -> e.rate = v, e -> e.rate,
                        (e, p) -> e.rate = p.rate)
                .addValidator(Validators.range(0.0f, 1.0f))
                .documentation("Fraction of the remaining gap to Limit kept per extra branch. Lower reaches Limit sooner")
                .add()
                .build();
    }
}
