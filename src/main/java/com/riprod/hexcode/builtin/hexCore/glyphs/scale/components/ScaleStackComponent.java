package com.riprod.hexcode.builtin.hexCore.glyphs.scale.components;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ScaleStackComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ScaleStackComponent> componentType;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final BuilderCodec<ScaleStackComponent> CODEC = BuilderCodec
            .builder(ScaleStackComponent.class, ScaleStackComponent::new)
            .append(new KeyedCodec<>("BaseAssetId", Codec.STRING),
                    (c, v) -> c.baseAssetId = v,
                    c -> c.baseAssetId)
            .add()
            .append(new KeyedCodec<>("Contributions",
                    (Codec<Map<String, Float>>) (Codec<?>) new MapCodec<>(Codec.FLOAT, HashMap::new, false)),
                    (c, v) -> c.contributions = v,
                    c -> c.contributions)
            .add()
            .build();

    public static void setComponentType(ComponentType<EntityStore, ScaleStackComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, ScaleStackComponent> getComponentType() {
        return componentType;
    }

    @Nullable
    private String baseAssetId;
    private Map<String, Float> contributions = new HashMap<>();

    public ScaleStackComponent() {
    }

    public ScaleStackComponent(String baseAssetId) {
        this.baseAssetId = baseAssetId;
    }

    @Nullable
    public String getBaseAssetId() {
        return baseAssetId;
    }

    public Map<String, Float> getContributions() {
        return contributions;
    }

    public void put(UUID constructId, float magnitude) {
        contributions.put(constructId.toString(), magnitude);
    }

    public void remove(UUID constructId) {
        contributions.remove(constructId.toString());
    }

    public boolean isEmpty() {
        return contributions.isEmpty();
    }

    public float productOfContributions() {
        float product = 1.0f;
        for (Float v : contributions.values()) {
            if (v != null) product *= v;
        }
        return product;
    }

    @Nonnull
    @Override
    public ScaleStackComponent clone() {
        ScaleStackComponent copy = new ScaleStackComponent();
        copy.baseAssetId = this.baseAssetId;
        copy.contributions = new HashMap<>(this.contributions);
        return copy;
    }
}
