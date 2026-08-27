package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class RootSpawner {
    public static Ref<EntityStore> createCastingRoot(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> playerRef, float eyeHeight, ModelParticle[] particles) {

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        Vector3d playerPos = accessor.getComponent(playerRef, TransformComponent.getComponentType())
                .getPosition();
        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(playerPos, new Rotation3f()));
        holder.addComponent(MountedComponent.getComponentType(),
                new MountedComponent(playerRef, new Vector3f(0, eyeHeight, 0),
                        MountController.Minecart));

        holder.addComponent(UUIDComponent.getComponentType(),
                new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Casting_Anchor");
        Model model = new Model(
                modelAsset.getId(), 1.0f, (Map<String, String>) null, modelAsset.getAttachments(null),
                modelAsset.getBoundingBox(), modelAsset.getModel(), modelAsset.getTexture(),
                modelAsset.getGradientSet(), modelAsset.getGradientId(), modelAsset.getEyeHeight(),
                modelAsset.getCrouchOffset(), modelAsset.getSittingOffset(),
                modelAsset.getSleepingOffset(),
                modelAsset.getAnimationSetMap(), modelAsset.getCamera(),
                modelAsset.getLight(), particles, modelAsset.getTrails(), modelAsset.getPhysicsValues(),
                modelAsset.getDetailBoxes(), modelAsset.getPhobia(),
                modelAsset.getPhobiaModelAssetId());

        holder.addComponent(ModelComponent.getComponentType(),
                new ModelComponent(model));

        holder.addComponent(PersistentModel.getComponentType(),
                new PersistentModel(model.toReference()));

        int networkId = accessor.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

        Ref<EntityStore> ref = accessor.addEntity(holder, AddReason.SPAWN);

        return ref;
    }
}
