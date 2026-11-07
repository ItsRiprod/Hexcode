package com.riprod.hexcode.builtin.elementalism.elements.water;

import org.joml.Vector3dc;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;

public class WaterElement extends ElementAsset {
    public static final String TYPE = "Water";
    public static final BuilderCodec<WaterElement> CODEC;

    protected float base;
    protected float fall;
    protected float rise;

    @Override
    public float rate(float elapsedActive, int branches, Vector3dc velocity,
            Ref<EntityStore> host, ComponentAccessor<EntityStore> accessor) {
        var vertical = (float) velocity.y();
        return this.base + this.fall * Math.max(0f, -vertical) - this.rise * Math.max(0f, vertical);
    }

    static {
        CODEC = BuilderCodec.builder(WaterElement.class, WaterElement::new, ElementAsset.ABSTRACT_CODEC)
                .appendInherited(new KeyedCodec<>("Base", Codec.FLOAT),
                        (a, v) -> a.base = v, a -> a.base,
                        (a, p) -> a.base = p.base)
                .addValidator(Validators.min(0.0f))
                .documentation("Charge per second while the host is level")
                .add()
                .appendInherited(new KeyedCodec<>("Fall", Codec.FLOAT),
                        (a, v) -> a.fall = v, a -> a.fall,
                        (a, p) -> a.fall = p.fall)
                .addValidator(Validators.min(0.0f))
                .documentation("Charge per second gained for each block per second the host falls")
                .add()
                .appendInherited(new KeyedCodec<>("Rise", Codec.FLOAT),
                        (a, v) -> a.rise = v, a -> a.rise,
                        (a, p) -> a.rise = p.rise)
                .addValidator(Validators.min(0.0f))
                .documentation("Charge per second lost for each block per second the host climbs")
                .add()
                .build();
    }
}
