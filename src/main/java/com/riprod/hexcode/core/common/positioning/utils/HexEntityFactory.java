package com.riprod.hexcode.core.common.positioning.utils;

import java.util.UUID;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;

/** Builds the entities a displayed hex is made of: hex entity, glyph entities, the head anchor. */
public final class HexEntityFactory {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  // compensates for the eliminated graph node debug sphere visual after the merge
  public static final float MERGED_HANDLER_SCALE_BUMP = 1.3f;

  public static Ref<EntityStore> createHeadAnchor(ComponentAccessor<EntityStore> accessor,
      Ref<EntityStore> playerRef, float eyeHeight) {
    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
    Vector3d playerPos = accessor.getComponent(playerRef, TransformComponent.getComponentType())
        .getPosition();
    var headRot = accessor.getComponent(playerRef, HeadRotation.getComponentType());
    var rotation = headRot != null
        ? new Rotation3f(headRot.getRotation().x, headRot.getRotation().y, 0f)
        : new Rotation3f();

    holder.addComponent(TransformComponent.getComponentType(),
        new TransformComponent(playerPos, rotation));
    holder.addComponent(UUIDComponent.getComponentType(),
        new UUIDComponent(UUID.randomUUID()));

    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Head_Anchor");
    Model model = Model.createUnitScaleModel(modelAsset);
    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
    holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
    holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

    int networkId = accessor.getExternalData().takeNextNetworkId();
    holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

    holder.addComponent(MountedComponent.getComponentType(),
        new MountedComponent(playerRef, new Vector3f(0, eyeHeight, 0),
            MountController.Minecart));

    return accessor.addEntity(holder, AddReason.SPAWN);
  }

  public static Holder<EntityStore> createGlyphHolder(ComponentAccessor<EntityStore> accessor, GlyphComponent glyph,
      Vector3d parentPos, Rotation3f parentRot) {
    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

    holder.addComponent(GlyphComponent.getComponentType(), glyph);

    TransformComponent glyphTransform = new TransformComponent(parentPos,
        parentRot);

    holder.addComponent(TransformComponent.getComponentType(),
        glyphTransform);

    GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
    if (asset == null) {
      LOGGER.atWarning().log("Unknown glyph ID: " + glyph.getGlyphId());
      return holder;
    }

    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(asset.getModelPath());
    if (modelAsset == null) {
      LOGGER.atWarning().log("Unknown model asset: " + asset.getModelPath());
      return holder;
    }

    Model model = withAttachments(
        Model.createScaledModel(modelAsset, glyph.getScale() * MERGED_HANDLER_SCALE_BUMP),
        asset, glyph.getGlyph(), accessor);

    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

    holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
    holder.addComponent(UUIDComponent.getComponentType(),
        new UUIDComponent(UUID.randomUUID()));
    holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
    holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
    holder.ensureComponent(EffectControllerComponent.getComponentType());
    int networkId = accessor.getExternalData().takeNextNetworkId();
    holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

    Ref<EntityStore> hexRoot = glyph.getParentRef();

    if (hexRoot != null) {
      Vector3f goff = glyph.getOffset();
      MountedComponent mountComponent = new MountedComponent(hexRoot, new Vector3f(goff.x, goff.y, goff.z),
          MountController.Minecart);
      holder.addComponent(MountedComponent.getComponentType(), mountComponent);
    }

    return holder;
  }

  private static Model withAttachments(Model scaled, GlyphAsset asset, Glyph data,
      ComponentAccessor<EntityStore> accessor) {
    ModelAttachment[] existing = scaled.getAttachments();
    if (existing != null && existing.length > 0) {
      return scaled;
    }

    ModelAttachment[] derived;
    if (data != null && data.isComponentInstance()) {
      Hex view = data.payloadView(accessor);
      derived = GlyphModelUtil.deriveFromStrokes(view != null ? view.getEncoding() : null);
    } else {
      derived = GlyphModelUtil.resolveAttachments(asset, existing);
    }
    if (derived.length == 0) {
      return scaled;
    }

    return GlyphModelUtil.rebuild(scaled, derived, scaled.getBoundingBox());
  }

  public static Holder<EntityStore> createHexHolder(ComponentAccessor<EntityStore> accessor, HexComponent hex,
      Vector3d parentPos) {
    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
    holder.addComponent(HexComponent.getComponentType(), hex);
    holder.addComponent(TransformComponent.getComponentType(),
        new TransformComponent(parentPos, new Rotation3f(hex.getRotation().x, hex.getRotation().y, 0)));
    holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
    holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
    holder.addComponent(NetworkId.getComponentType(), new NetworkId(accessor.getExternalData().takeNextNetworkId()));

    Ref<EntityStore> hexParent = hex.getParentRef();
    if (hexParent != null) {
      Vector3f hoff = hex.getOffset();
      holder.addComponent(MountedComponent.getComponentType(),
          new MountedComponent(hexParent, new Vector3f(hoff.x, hoff.y, hoff.z), MountController.Minecart));
    }
    return holder;
  }

  public static Ref<EntityStore> createHex(ComponentAccessor<EntityStore> accessor, HexComponent hex,
      Vector3d parentPos) {
    return accessor.addEntity(createHexHolder(accessor, hex, parentPos), AddReason.SPAWN);
  }
}
