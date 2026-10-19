package com.riprod.hexcode.core.common.drawing.system;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.utils.GlyphMath;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class InterfaceManager {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public static Ref<EntityStore> spawnTrailEntity(ComponentAccessor<EntityStore> accessor,
      Ref<EntityStore> playerRef, HeadRotation head) {
    TransformComponent transform = accessor.getComponent(playerRef, TransformComponent.getComponentType());
    ModelComponent playerModel = accessor.getComponent(playerRef, ModelComponent.getComponentType());
    if (head == null || transform == null || playerModel == null) {
      return null;
    }

    float eyeHeight = playerModel.getModel().getEyeHeight();
    Vector3d eyePos = new Vector3d(transform.getPosition()).add(0, eyeHeight, 0);
    Rotation3f rotation = head.getRotation();
    Vector3d position = GlyphMath.sphericalToCartesian(eyePos, head.getRotation().y,
        head.getRotation().x, 2.0f);

    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Trail_Anchor");
    Model model = Model.createUnitScaleModel(modelAsset);

    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
    holder.addComponent(TransformComponent.getComponentType(),
        new TransformComponent(position, rotation));
    holder.ensureComponent(UUIDComponent.getComponentType());
    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
    holder.addComponent(PersistentModel.getComponentType(),
        new PersistentModel(model.toReference()));
    holder.ensureComponent(PropComponent.getComponentType());
    holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

    return accessor.addEntity(holder, AddReason.SPAWN);
  }

  public static void positionTrailEntity(ComponentAccessor<EntityStore> accessor,
      Ref<EntityStore> playerRef, Ref<EntityStore> trailRef, HeadRotation head) {
    if (trailRef == null || !trailRef.isValid()) {
      return;
    }
    TransformComponent transform = accessor.getComponent(playerRef, TransformComponent.getComponentType());
    ModelComponent playerModel = accessor.getComponent(playerRef, ModelComponent.getComponentType());
    if (head == null || transform == null || playerModel == null) {
      return;
    }
    float eyeHeight = playerModel.getModel().getEyeHeight();
    Vector3d eyePos = new Vector3d(transform.getPosition()).add(0, eyeHeight, 0);
    Vector3d position = GlyphMath.sphericalToCartesian(eyePos, head.getRotation().y,
        head.getRotation().x, 2.0f);
    TransformComponent trailTransform = accessor.getComponent(trailRef, TransformComponent.getComponentType());
    if (trailTransform == null) return;
    trailTransform.setPosition(position);
  }

  public static void removeTrailEntity(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> trailRef) {
    if (trailRef == null || !trailRef.isValid()) {
      return;
    }
    Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
    accessor.removeEntity(trailRef, holder, RemoveReason.REMOVE);
  }

  public static List<Vector3d> getPositionsFromAngles(ComponentAccessor<EntityStore> accessor, FloatArrayList angles,
      Ref<EntityStore> playerRef, float radius) {
    TransformComponent transform = accessor.getComponent(playerRef, TransformComponent.getComponentType());
    ModelComponent playerModel = accessor.getComponent(playerRef, ModelComponent.getComponentType());
    List<Vector3d> outPositions = new ArrayList<>();

    if (transform == null || playerModel == null) {
      return outPositions; // skip
    }

    float eyeHeight = playerModel.getModel().getEyeHeight();
    Vector3d eyePos = new Vector3d(transform.getPosition()).add(0, eyeHeight, 0);

    int pointCount = angles.size() / 2;
    if (pointCount < 2)
      return outPositions;

    float[] cumDist = new float[pointCount];
    for (int i = 1; i < pointCount; i++) {
      float dy = angles.getFloat(i * 2) - angles.getFloat((i - 1) * 2);
      float dp = angles.getFloat(i * 2 + 1) - angles.getFloat((i - 1) * 2 + 1);
      cumDist[i] = cumDist[i - 1] + (float) Math.sqrt(dy * dy + dp * dp);
    }

    float totalDist = cumDist[pointCount - 1];
    int sampleCount = Math.min(pointCount, 12); // cap at 12 due to laser limit
    float stepDist = totalDist / (sampleCount - 1);

    int cursor = 0;
    for (int s = 0; s < sampleCount; s++) {
      float target = s * stepDist;
      while (cursor < pointCount - 1 && cumDist[cursor + 1] < target)
        cursor++;
      float yaw = angles.getFloat(cursor * 2);
      float pitch = angles.getFloat(cursor * 2 + 1);
      outPositions.add(
          GlyphMath.sphericalToCartesian(eyePos, yaw, pitch, radius));
    }

    return outPositions;
  }

  public static Color getColorFromQuality(float quality) {
    if (quality < 0.30f) {
      return new Color((byte) 255, (byte) 50, (byte) 50);
    } else if (quality < 0.40f) {
      return new Color((byte) 255, (byte) 140, (byte) 30);
    } else if (quality < 0.50f) {
      return new Color((byte) 255, (byte) 220, (byte) 30);
    } else if (quality < 0.60f) {
      return new Color((byte) 180, (byte) 255, (byte) 50); 
    } else if (quality < 0.70f) {
      return new Color((byte) 50, (byte) 220, (byte) 50); 
    } else if (quality < 0.85f) {
      return new Color((byte) 50, (byte) 180, (byte) 255); 
    } else {
      return new Color((byte) 180, (byte) 80, (byte) 255);
    }
  }
}
