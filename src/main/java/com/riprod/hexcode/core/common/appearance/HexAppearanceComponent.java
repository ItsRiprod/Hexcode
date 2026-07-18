package com.riprod.hexcode.core.common.appearance;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexAppearanceComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexAppearanceComponent> componentType;

    public static final BuilderCodec<HexAppearanceComponent> CODEC = BuilderCodec
            .builder(HexAppearanceComponent.class, HexAppearanceComponent::new)
            .append(new KeyedCodec<>("OriginalModel", Model.ModelReference.CODEC),
                    (c, v) -> c.originalModel = v,
                    c -> c.originalModel)
            .add()
            .build();

    public static void setComponentType(ComponentType<EntityStore, HexAppearanceComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexAppearanceComponent> getComponentType() {
        return componentType;
    }

    @Nullable
    private Model.ModelReference originalModel;

    @Nullable
    private transient PlayerSkin originalSkin;

    private final transient Map<String, AppearanceLayer> layers = new LinkedHashMap<>();

    public HexAppearanceComponent() {
    }

    public HexAppearanceComponent(@Nullable Model.ModelReference originalModel) {
        this.originalModel = originalModel;
    }

    @Nullable
    public Model.ModelReference getOriginalModel() {
        return originalModel;
    }

    @Nullable
    public PlayerSkin getOriginalSkin() {
        return originalSkin;
    }

    public void setOriginalSkin(@Nullable PlayerSkin originalSkin) {
        this.originalSkin = originalSkin;
    }

    public void putLayer(String layerId, AppearanceLayer layer) {
        layers.put(layerId, layer);
    }

    public void removeLayer(String layerId) {
        layers.remove(layerId);
    }

    public void clearLayers() {
        layers.clear();
    }

    public boolean hasLayers() {
        return !layers.isEmpty();
    }

    public Map<String, AppearanceLayer> getLayers() {
        return layers;
    }

    @Nonnull
    @Override
    public HexAppearanceComponent clone() {
        HexAppearanceComponent copy = new HexAppearanceComponent(originalModel);
        copy.originalSkin = originalSkin;
        copy.layers.putAll(layers);
        return copy;
    }
}
