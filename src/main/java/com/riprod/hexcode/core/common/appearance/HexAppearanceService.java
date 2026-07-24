package com.riprod.hexcode.core.common.appearance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class HexAppearanceService {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private HexAppearanceService() {
    }

    public static boolean addLayer(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            @Nonnull String layerId, @Nonnull AppearanceLayer layer) {
        if (!isUsable(buffer, ref)) return false;

        HexAppearanceComponent appearance = buffer.getComponent(ref, HexAppearanceComponent.getComponentType());
        if (appearance == null) {
            appearance = capture(buffer, ref);
            if (appearance == null) return false;
            buffer.putComponent(ref, HexAppearanceComponent.getComponentType(), appearance);
        }

        appearance.putLayer(layerId, layer.withSequence(SEQUENCE.incrementAndGet()));
        return apply(buffer, ref, appearance);
    }

    public static void removeLayer(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            @Nonnull String layerId) {
        if (!isUsable(buffer, ref)) return;

        HexAppearanceComponent appearance = buffer.getComponent(ref, HexAppearanceComponent.getComponentType());
        if (appearance == null) return;

        appearance.removeLayer(layerId);
        if (appearance.hasLayers()) {
            apply(buffer, ref, appearance);
            return;
        }

        apply(buffer, ref, appearance);
        buffer.tryRemoveComponent(ref, HexAppearanceComponent.getComponentType());
    }

    public static void restoreOriginal(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref) {
        if (!isUsable(buffer, ref)) return;

        HexAppearanceComponent appearance = buffer.getComponent(ref, HexAppearanceComponent.getComponentType());
        if (appearance == null) return;

        appearance.clearLayers();
        apply(buffer, ref, appearance);
        buffer.tryRemoveComponent(ref, HexAppearanceComponent.getComponentType());
    }

    @Nullable
    private static HexAppearanceComponent capture(@Nonnull ComponentAccessor<EntityStore> buffer,
            @Nonnull Ref<EntityStore> ref) {
        ModelComponent modelComponent = buffer.getComponent(ref, ModelComponent.getComponentType());
        if (modelComponent == null || modelComponent.getModel() == null) return null;

        Model model = modelComponent.getModel();
        if (model.getModelAssetId() == null) return null;

        Model.ModelReference reference = model.toReference();
        Map<String, String> attachments = reference.getRandomAttachmentIds();
        HexAppearanceComponent appearance = new HexAppearanceComponent(
                reference.getModelAssetId(), reference.getScale(), reference.isStaticModel(),
                attachments != null ? new LinkedHashMap<>(attachments) : null);

        PlayerSkinComponent skinComponent = buffer.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (skinComponent != null) {
            appearance.setOriginalSkin(skinComponent.getPlayerSkin());
        }

        Nameplate nameplateComponent = buffer.getComponent(ref, Nameplate.getComponentType());
        if (nameplateComponent != null) {
            appearance.setOriginalNameplate(nameplateComponent.getText());
        }

        return appearance;
    }

    private static boolean apply(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            @Nonnull HexAppearanceComponent appearance) {
        String modelAssetId = appearance.getOriginalModelAssetId();
        if (modelAssetId == null) return false;

        PlayerSkin disguiseSkin = null;
        Float overriddenBaseScale = null;
        long modelSequence = Long.MIN_VALUE;
        String nameplate = null;
        long nameplateSequence = Long.MIN_VALUE;
        float scaleProduct = 1.0f;

        for (AppearanceLayer layer : appearance.getLayers().values()) {
            if (layer.scaleMultiplier() != null) {
                scaleProduct *= layer.scaleMultiplier();
            }
            if (layer.modelAssetId() != null && layer.sequence() > modelSequence) {
                modelAssetId = layer.modelAssetId();
                disguiseSkin = layer.skin();
                overriddenBaseScale = layer.baseScale();
                modelSequence = layer.sequence();
            }
            if (layer.nameplate() != null && layer.sequence() > nameplateSequence) {
                nameplate = layer.nameplate();
                nameplateSequence = layer.sequence();
            }
        }

        ModelAsset asset = ModelAsset.getAssetMap().getAsset(modelAssetId);
        if (asset == null) {
            LOGGER.atWarning().log("[hexcode] appearance: unresolved model asset '%s'", modelAssetId);
            return false;
        }

        boolean overridden = modelSequence != Long.MIN_VALUE;
        float base = overridden
                ? (overriddenBaseScale != null ? overriddenBaseScale : 1.0f)
                : appearance.getOriginalScale();
        if (base <= 0f) {
            base = 1.0f;
        }
        float scale = base * scaleProduct;
        if (scale <= 0f) {
            scale = 1.0f;
        }

        Model model = overridden
                ? Model.createScaledModel(asset, scale, null)
                : Model.createScaledModel(asset, scale, appearance.getOriginalRandomAttachmentIds(),
                        null, appearance.isOriginalStatic());

        buffer.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
        buffer.putComponent(ref, EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale));

        applySkin(buffer, ref, overridden, disguiseSkin, appearance.getOriginalSkin());
        applyNameplate(buffer, ref, nameplate, appearance.getOriginalNameplate());
        return true;
    }

    private static void applyNameplate(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            @Nullable String nameplate, @Nullable String originalNameplate) {
        Nameplate current = buffer.getComponent(ref, Nameplate.getComponentType());

        if (nameplate != null) {
            if (current != null) {
                current.setText(nameplate);
            } else if (!nameplate.isEmpty()) {
                buffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(nameplate));
            }
            return;
        }

        if (originalNameplate != null) {
            setNameplate(buffer, ref, current, originalNameplate);
        } else if (current != null) {
            buffer.tryRemoveComponent(ref, Nameplate.getComponentType());
        }
    }

    private static void setNameplate(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            @Nullable Nameplate current, @Nonnull String text) {
        if (current != null) {
            current.setText(text);
        } else {
            buffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(text));
        }
    }

    private static void applySkin(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            boolean overridden, @Nullable PlayerSkin disguiseSkin, @Nullable PlayerSkin originalSkin) {
        PlayerSkinComponent current = buffer.getComponent(ref, PlayerSkinComponent.getComponentType());

        if (overridden && disguiseSkin != null) {
            putSkin(buffer, ref, current, disguiseSkin);
            return;
        }

        if (overridden) {
            if (current != null) {
                buffer.tryRemoveComponent(ref, PlayerSkinComponent.getComponentType());
            }
            return;
        }

        if (originalSkin != null) {
            putSkin(buffer, ref, current, originalSkin);
        }
    }

    private static void putSkin(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref,
            @Nullable PlayerSkinComponent current, @Nonnull PlayerSkin skin) {
        if (current != null && current.getPlayerSkin() == skin) {
            current.setNetworkOutdated();
            return;
        }
        buffer.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
    }

    private static boolean isUsable(@Nonnull ComponentAccessor<EntityStore> buffer, @Nonnull Ref<EntityStore> ref) {
        return ref.isValid() && ref.getStore() == buffer.getExternalData().getStore();
    }
}
