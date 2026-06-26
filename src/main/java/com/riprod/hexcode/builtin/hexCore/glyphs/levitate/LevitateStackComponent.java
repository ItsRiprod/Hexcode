package com.riprod.hexcode.builtin.hexCore.glyphs.levitate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class LevitateStackComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, LevitateStackComponent> componentType;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final BuilderCodec<LevitateStackComponent> CODEC = BuilderCodec
            .builder(LevitateStackComponent.class, LevitateStackComponent::new)
            .append(new KeyedCodec<>("Contributions",
                    (Codec<Map<String, Float>>) (Codec<?>) new MapCodec<>(Codec.FLOAT, HashMap::new, false)),
                    (c, v) -> c.contributions = v,
                    c -> c.contributions)
            .add()
            .build();

    public static void setComponentType(ComponentType<EntityStore, LevitateStackComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, LevitateStackComponent> getComponentType() {
        return componentType;
    }

    private Map<String, Float> contributions = new HashMap<>();

    public LevitateStackComponent() {
    }

    public Map<String, Float> getContributions() {
        return contributions;
    }

    public void put(UUID constructId, float intensity) {
        contributions.put(constructId.toString(), intensity);
    }

    public void remove(UUID constructId) {
        contributions.remove(constructId.toString());
    }

    public boolean isEmpty() {
        return contributions.isEmpty();
    }

    @Nonnull
    @Override
    public LevitateStackComponent clone() {
        LevitateStackComponent copy = new LevitateStackComponent();
        copy.contributions = new HashMap<>(this.contributions);
        return copy;
    }
}
