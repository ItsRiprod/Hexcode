package com.riprod.hexcode.builtin.elementalism.elements.fire;

import org.joml.Vector3dc;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;

public class FireElement extends ElementAsset {
    public static final String TYPE = "Fire";
    public static final BuilderCodec<FireElement> CODEC;

    protected float start;
    protected float slope;

    @Override
    public float rate(float elapsedActive, int branches, Vector3dc velocity,
            Ref<EntityStore> host, ComponentAccessor<EntityStore> accessor) {
        return this.start - this.slope * elapsedActive;
    }

    static {
        CODEC = BuilderCodec.builder(FireElement.class, FireElement::new, ElementAsset.ABSTRACT_CODEC)
                .appendInherited(new KeyedCodec<>("Start", Codec.FLOAT),
                        (a, v) -> a.start = v, a -> a.start,
                        (a, p) -> a.start = p.start)
                .addValidator(Validators.min(0.0f))
                .documentation("Charge per second when the element first becomes active")
                .add()
                .appendInherited(new KeyedCodec<>("Slope", Codec.FLOAT),
                        (a, v) -> a.slope = v, a -> a.slope,
                        (a, p) -> a.slope = p.slope)
                .addValidator(Validators.min(0.0f))
                .documentation("Charge per second lost from the rate for every second the element stays active. Past Start / Slope seconds the rate turns negative and the charge burns out.")
                .add()
                .build();
    }
}
