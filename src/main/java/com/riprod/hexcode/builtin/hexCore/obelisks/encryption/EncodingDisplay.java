package com.riprod.hexcode.builtin.hexCore.obelisks.encryption;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;
import com.riprod.hexcode.core.common.hexes.component.EncodingStroke;

public final class EncodingDisplay {

    private static final String BASE_MODEL = "Glyph_Template";
    private static final float DISPLAY_HEIGHT = 1.75f;
    private static final float DISPLAY_SCALE = 1.0f;

    private EncodingDisplay() {
    }

    public static void refresh(CommandBuffer<EntityStore> buffer, EncryptionSessionState state,
            Vector3i obeliskPos, @Nullable List<EncodingStroke> encoding) {
        despawn(buffer, state);
        if (encoding == null || encoding.isEmpty()) return;

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(BASE_MODEL);
        if (modelAsset == null) return;
        ModelAttachment[] attachments = GlyphModelUtil.deriveFromStrokes(encoding);
        if (attachments.length == 0) return;

        Model scaled = Model.createScaledModel(modelAsset, DISPLAY_SCALE);
        Model model = GlyphModelUtil.rebuild(scaled, attachments, scaled.getBoundingBox());

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(
                new Vector3d(obeliskPos.x + 0.5, obeliskPos.y + DISPLAY_HEIGHT, obeliskPos.z + 0.5),
                new Rotation3f()));
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
        holder.addComponent(NetworkId.getComponentType(),
                new NetworkId(buffer.getExternalData().takeNextNetworkId()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

        state.setDisplayRef(buffer.addEntity(holder, AddReason.SPAWN));
    }

    public static void despawn(CommandBuffer<EntityStore> buffer, EncryptionSessionState state) {
        Ref<EntityStore> ref = state.getDisplayRef();
        if (ref != null && ref.isValid()) {
            buffer.tryRemoveEntity(ref, RemoveReason.REMOVE);
        }
        state.setDisplayRef(null);
    }
}
