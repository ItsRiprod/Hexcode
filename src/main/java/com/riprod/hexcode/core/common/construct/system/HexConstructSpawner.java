package com.riprod.hexcode.core.common.construct.system;

import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.utils.HexRefs;

public class HexConstructSpawner {

    private static final WeakHashMap<UUID, HexEffectsComponent> PENDING_APPLIES = new WeakHashMap<>();

    private HexConstructSpawner() {
    }

    public static Holder<EntityStore> create(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId,
            @Nonnull Vector3d position) {
        return createWithState(buffer, hexContext, triggeringGlyph, handlerId, position, null);
    }

    public static <S extends ConstructState> Holder<EntityStore> createWithState(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId,
            @Nonnull Vector3d position,
            @Nullable S initialState) {

        UUID constructId = UUID.randomUUID();
        HexStatus<S> construct = new HexStatus<>(
                handlerId, hexContext, constructId, triggeringGlyph, initialState);

        HexEffectsComponent component = new HexEffectsComponent();
        component.addEffect(constructId, construct);

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(position, new Rotation3f()));
        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.addComponent(NetworkId.getComponentType(),
                new NetworkId(buffer.getExternalData().takeNextNetworkId()));
        holder.addComponent(HexEffectsComponent.getComponentType(), component);
        holder.addComponent(HexcodeComponent.getComponentType(), new HexcodeComponent());
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        return holder;
    }

    @Nullable
    public static Model resolveModel(
            @Nonnull HexContext hexContext,
            @Nullable GlyphAsset glyphAsset,
            float defaultScale) {

        String modelId = VfxUtil.resolveModelId(hexContext, glyphAsset);
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;

        Model shaped = shapedModel(hexContext);
        if (shaped != null) {
            return GlyphModelUtil.withDefaultBox(shaped, modelAsset);
        }

        if (modelAsset == null && glyphAsset != null) {
            return GlyphModelUtil.assemble(glyphAsset, glyphAsset.getStyle(), defaultScale);
        }

        return modelAsset != null ? Model.createScaledModel(modelAsset, defaultScale) : null;
    }

    public static void applyModel(
            @Nonnull Holder<EntityStore> holder,
            @Nonnull HexContext hexContext,
            @Nonnull Model model) {

        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

        // ModelReference cannot carry derived attachments, so persisting a shaped model would
        // let ModelSystems.ModelChange strip the shape on any later model swap
        if (shapedModel(hexContext) == null) {
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        }
    }

    @Nullable
    public static Model attachModel(
            @Nonnull Holder<EntityStore> holder,
            @Nonnull HexContext hexContext,
            @Nullable GlyphAsset glyphAsset,
            float defaultScale) {

        Model model = resolveModel(hexContext, glyphAsset, defaultScale);
        if (model != null) {
            applyModel(holder, hexContext, model);
        }
        return model;
    }

    @Nullable
    private static Model shapedModel(@Nonnull HexContext hexContext) {
        HexStyleAsset style = hexContext.getStyle();
        return style != null ? style.getResolvedModel() : null;
    }

    @Nullable
    public static UUID apply(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nullable Ref<EntityStore> targetRef,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId) {
        return applyWithState(buffer, targetRef, hexContext, triggeringGlyph, handlerId, null);
    }

    @Nullable
    public static <S extends ConstructState> UUID applyWithState(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nullable Ref<EntityStore> targetRef,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId,
            @Nullable S initialState) {

        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(triggeringGlyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "construct target ref null/invalid");
            return null;
        }

        UUID constructId = UUID.randomUUID();
        HexStatus<S> construct = new HexStatus<>(
                handlerId, hexContext, constructId, triggeringGlyph, initialState);

        HexEffectsComponent existing = buffer.getComponent(targetRef, HexEffectsComponent.getComponentType());
        if (existing != null) {
            existing.addEffect(constructId, construct);
            return constructId;
        }

        UUIDComponent uuidComponent = buffer.getComponent(targetRef, UUIDComponent.getComponentType());
        UUID targetUuid = uuidComponent != null ? uuidComponent.getUuid() : null;
        if (targetUuid == null) {
            HexEffectsComponent fresh = new HexEffectsComponent();
            fresh.addEffect(constructId, construct);
            buffer.addComponent(targetRef, HexEffectsComponent.getComponentType(), fresh);
            return constructId;
        }

        HexEffectsComponent pending;
        synchronized (PENDING_APPLIES) {
            pending = PENDING_APPLIES.get(targetUuid);
            if (pending == null || pending.getEffects().isEmpty()) {
                pending = new HexEffectsComponent();
                pending.addEffect(constructId, construct);
                PENDING_APPLIES.put(targetUuid, pending);
                buffer.addComponent(targetRef, HexEffectsComponent.getComponentType(), pending);
                return constructId;
            }
        }
        pending.addEffect(constructId, construct);
        return constructId;
    }

    public static void clearPendingApply(@Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Ref<EntityStore> ref) {
        UUIDComponent uuidComponent = accessor.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;
        synchronized (PENDING_APPLIES) {
            PENDING_APPLIES.remove(uuidComponent.getUuid());
        }
    }

    public static boolean hasPendingApply(@Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Ref<EntityStore> ref, @Nonnull String handlerId) {
        if (!HexRefs.isLive(ref, accessor)) return false;

        UUIDComponent uuidComponent = accessor.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return false;
        UUID targetUuid = uuidComponent.getUuid();

        HexEffectsComponent pending;
        synchronized (PENDING_APPLIES) {
            pending = PENDING_APPLIES.get(targetUuid);
        }
        if (pending == null) return false;

        for (HexStatus<?> status : pending.getEffects().values()) {
            if (handlerId.equals(status.getHandlerId())) return true;
        }
        return false;
    }
}
