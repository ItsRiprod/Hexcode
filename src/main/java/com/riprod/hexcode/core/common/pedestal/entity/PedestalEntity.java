package com.riprod.hexcode.core.common.pedestal.entity;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.CustomModelTexture;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.hypixel.hytale.logger.HytaleLogger;

public class PedestalEntity {

    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public static Vector3d getAnchorPosition(Vector3i blockPos) {
        return new Vector3d(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5);
    }

    @Nullable
    public static Holder<EntityStore> buildAnchorHolder(Store<EntityStore> store, Vector3i blockPos) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        Vector3d anchorPos = getAnchorPosition(blockPos);

        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(anchorPos, new Rotation3f()));
        holder.addComponent(UUIDComponent.getComponentType(),
                new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        int networkId = store.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Pedestal_Holder");
        if (modelAsset == null) {
            logger.atWarning().log("pedestal buildAnchorHolder: no ModelAsset for id=Pedestal_Holder");
            return null;
        }

        Model model = Model.createScaledModel(modelAsset, 1.0f);
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));

        return holder;
    }

    public static Ref<EntityStore> spawnAnchorEntity(CommandBuffer<EntityStore> accessor,
            Vector3i blockPos) {

        Holder<EntityStore> holder = buildAnchorHolder(accessor.getStore(), blockPos);
        if (holder == null) {
            return null;
        }
        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    public static Ref<EntityStore> spawnBookDisplay(CommandBuffer<EntityStore> accessor,
            PedestalBlockComponent pedestal,
            HexcodeSessionComponent session, Vector3d anchorPos, ItemStack stack, Ref<EntityStore> playerRef) {

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(anchorPos, new Rotation3f()));

        holder.addComponent(UUIDComponent.getComponentType(),
                new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        int networkId = accessor.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

        addDisplayRendering(pedestal, session.getProfile(), holder, stack, pedestal.getReferenceHolder(), 1.0f);

        var posOff = pedestal.getDisplayOffset();

        holder.addComponent(MountedComponent.getComponentType(),
                new MountedComponent(session.getAnchorRef(), new Vector3f(posOff.x, posOff.y, posOff.z),
                        MountController.Minecart));

        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    private static void addDisplayRendering(PedestalBlockComponent pedestal,
            @Nullable ImbuementProfileAsset profile,
            Holder<EntityStore> holder, ItemStack stack, String anchorId, float scale) {

        Item item = stack.getItem();

        if (profile != null) {
            String override = profile.getDisplayModelOverride();
            if (override != null && !override.isEmpty()) {
                attachCustomModel(pedestal, holder, override, item.getTexture(), anchorId, scale);
                return;
            }
        }

        String itemModel = item.getModel();
        if (itemModel != null && !itemModel.isEmpty()) {
            attachCustomModel(pedestal, holder, itemModel, item.getTexture(), anchorId, scale);
            return;
        }

        if (item.hasBlockType()) {
            holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(stack));
            holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
            return;
        }

        BlockType blockType = BlockType.getAssetMap().getAsset(item.getBlockId());
        if (blockType != null) {
            String blockModel = blockType.getCustomModel();
            String blockTexture = null;
            CustomModelTexture[] textures = blockType.getCustomModelTexture();
            if (textures != null && textures.length > 0) {
                blockTexture = textures[0].getTexture();
            }
            if (blockModel != null && !blockModel.isEmpty()) {
                attachCustomModel(pedestal, holder, blockModel, blockTexture, anchorId, scale);
                return;
            }
        }

        logger.atWarning().log("pedestal: no renderable model for item id=%s", item.getId());
    }

    private static void attachCustomModel(PedestalBlockComponent pedestal, Holder<EntityStore> holder,
            String modelId, @Nullable String textureId, String anchorId, float scale) {

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(pedestal.getReferenceHolder());
        if (modelAsset == null) {
            logger.atWarning().log("pedestal attachCustomModel: no ModelAsset for id=%s", anchorId);
            return;
        }

        Model model = new Model(
                modelAsset.getId(), scale, (Map<String, String>) null, modelAsset.getAttachments(null),
                modelAsset.getBoundingBox(), modelId, textureId,
                modelAsset.getGradientSet(), modelAsset.getGradientId(), modelAsset.getEyeHeight(),
                modelAsset.getCrouchOffset(), modelAsset.getSittingOffset(),
                modelAsset.getSleepingOffset(),
                pedestal.getAnimationSetMap(), modelAsset.getCamera(),
                modelAsset.getLight(), modelAsset.getParticles(), modelAsset.getTrails(),
                modelAsset.getPhysicsValues(),
                modelAsset.getDetailBoxes(), modelAsset.getPhobia(),
                modelAsset.getPhobiaModelAssetId());

        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
    }

    @Nullable
    public static Map<String, ModelAsset.AnimationSet> getAnimationsFromModel(String modelAssetId) {
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelAssetId);
        if (modelAsset == null) {
            return null;
        }
        return modelAsset.getAnimationSetMap();
    }
}
