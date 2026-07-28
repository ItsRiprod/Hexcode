package com.riprod.hexcode.core.common.glyphs.utils;

import java.util.UUID;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
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
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class CreateGlyph {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  // compensates for the eliminated graph node debug sphere visual after the merge
  public static final float MERGED_HANDLER_SCALE_BUMP = 1.3f;

  public static Ref<EntityStore> createHeadAnchor(ComponentAccessor<EntityStore> accessor,
      Ref<EntityStore> playerRef, float eyeHeight) {
    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
    Vector3d playerPos = accessor.getComponent(playerRef, TransformComponent.getComponentType())
        .getPosition();

    holder.addComponent(TransformComponent.getComponentType(),
        new TransformComponent(playerPos, new Rotation3f()));
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
        new MountedComponent(playerRef, new Rotation3f(0, eyeHeight, 0),
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
        asset);

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
      MountedComponent mountComponent = new MountedComponent(hexRoot, new Rotation3f(goff.x, goff.y, goff.z),
          MountController.Minecart);
      holder.addComponent(MountedComponent.getComponentType(), mountComponent);
    }

    return holder;
  }

  private static Model withAttachments(Model scaled, GlyphAsset asset) {
    ModelAttachment[] existing = scaled.getAttachments();
    if (existing != null && existing.length > 0) {
      return scaled;
    }

    ModelAttachment[] derived = GlyphModelUtil.resolveAttachments(asset, existing);
    if (derived.length == 0) {
      return scaled;
    }

    return GlyphModelUtil.rebuild(scaled, derived, scaled.getBoundingBox());
  }

  public static Ref<EntityStore> createGlyph(CommandBuffer<EntityStore> accessor, GlyphComponent glyph,
      Vector3d parentPos, Rotation3f parentRot, Ref<EntityStore> playerRef) {

    Holder<EntityStore> holder = createGlyphHolder(accessor, glyph, parentPos, parentRot);
    Ref<EntityStore> ref = createEntity(accessor, holder);
    glyph.setSelfRef(ref);

    return ref;
  }

  public static Ref<EntityStore> createEntity(ComponentAccessor<EntityStore> buffer, Holder<EntityStore> holder) {
    Ref<EntityStore> ref = buffer.addEntity(holder, AddReason.SPAWN);
    return ref;
  }
}
