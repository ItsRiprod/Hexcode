package com.riprod.hexcode.core.common.construct.system;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
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
import com.riprod.hexcode.api.event.BranchEndEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.component.SustainState;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.cast.component.CastEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.utils.HexRefs;
import com.riprod.hexcode.utils.LogScopes;

public class HexConstructSpawner {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);
    private static final WeakHashMap<UUID, HexEffectsComponent> PENDING_APPLIES = new WeakHashMap<>();

    private HexConstructSpawner() {
    }

    @Nullable
    public static Holder<EntityStore> create(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId,
            @Nonnull Vector3d position) {
        return createWithState(buffer, hexContext, triggeringGlyph, handlerId, position, null);
    }

    @Nullable
    public static <S extends ConstructState> Holder<EntityStore> createWithState(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId,
            @Nonnull Vector3d position,
            @Nullable S initialState) {

        UUID constructId = UUID.randomUUID();
        if (!claim(hexContext, constructId, handlerId)) {
            return null;
        }
        HexStatus<S> construct = new HexStatus<>(
                handlerId, hexContext, constructId, triggeringGlyph, initialState, true);

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
            @Nullable GlyphAsset glyphAsset) {

        var own = ownBody(glyphAsset);
        var body = swappedBody(EffectRenderer.resolve(glyphAsset, hexContext), glyphAsset);
        return body != null ? GlyphModelUtil.withDefaultBox(body, own) : own;
    }

    @Nullable
    private static Model swappedBody(@Nonnull GlyphEffects effects, @Nullable GlyphAsset glyphAsset) {
        if (effects.getResolvedModel() != null) {
            return effects.getResolvedModel();
        }
        var id = effects.getModel();
        var own = glyphAsset != null && glyphAsset.getEffects() != null ? glyphAsset.getEffects().getModel() : null;
        var asset = id != null && !id.equals(own) ? ModelAsset.getAssetMap().getAsset(id) : null;
        return asset != null ? Model.createRandomScaleModel(asset) : null;
    }

    @Nullable
    public static Model rebuildModel(
            @Nonnull HexContext hexContext,
            @Nullable GlyphAsset glyphAsset,
            @Nonnull Model live,
            @Nonnull SustainState sustain) {

        var effects = EffectRenderer.resolve(glyphAsset, hexContext);
        var body = swappedBody(effects, glyphAsset);
        var own = ownBody(glyphAsset);
        var installed = sustain.getBody() != null && sustain.getBody().equals(live.getModelAssetId());
        var target = !installed ? null : body != null ? body : own;
        var swap = target != null && !target.getModelAssetId().equals(live.getModelAssetId());
        if (!swap && !sustain.hasInjected() && effects.get(EffectSlot.Sustain) == null) {
            return null;
        }
        var stripped = EffectRenderer.strip(live, sustain);
        var base = !swap ? stripped : body != null ? GlyphModelUtil.withDefaultBox(body, own) : own;
        if (installed) {
            sustain.setBody(base.getModelAssetId());
        }
        return EffectRenderer.inject(base, effects, sustain);
    }

    @Nullable
    private static Model ownBody(@Nullable GlyphAsset glyphAsset) {
        return glyphAsset != null ? GlyphModelUtil.assemble(glyphAsset, glyphAsset.getEffects()) : null;
    }

    public static void applyModel(
            @Nonnull Holder<EntityStore> holder,
            @Nonnull HexContext hexContext,
            @Nonnull Model model) {

        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

        // ModelReference cannot carry derived attachments, so persisting a shaped model would
        // let ModelSystems.ModelChange strip the shape on any later model swap
        if (hexContext.getEffects().getResolvedModel() == null) {
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        }
    }

    @Nullable
    public static Model attachModel(
            @Nonnull Holder<EntityStore> holder,
            @Nonnull HexContext hexContext,
            @Nullable GlyphAsset glyphAsset) {

        var model = resolveModel(hexContext, glyphAsset);
        if (model == null) {
            return null;
        }
        var owner = holder.getComponent(HexEffectsComponent.getComponentType());
        if (owner != null) {
            for (var status : owner.getEffects().values()) {
                if (status.getHexContext() == hexContext) {
                    model = EffectRenderer.inject(model, EffectRenderer.resolve(glyphAsset, hexContext),
                            status.getSustain());
                    status.getSustain().setVersion(hexContext.get(CastEffectsComponent.TYPE).getVersion());
                    status.getSustain().setBody(model.getModelAssetId());
                }
            }
        }
        applyModel(holder, hexContext, model);
        return model;
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

        UUID constructId = UUID.randomUUID();
        if (!claim(hexContext, constructId, handlerId)) {
            return null;
        }

        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(triggeringGlyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "construct target ref null/invalid");
            return null;
        }

        attach(buffer, targetRef, constructId, new HexStatus<>(
                handlerId, hexContext, constructId, triggeringGlyph, initialState, false));
        return constructId;
    }

    private static void attach(@Nonnull CommandBuffer<EntityStore> buffer, @Nonnull Ref<EntityStore> targetRef,
            @Nonnull UUID constructId, @Nonnull HexStatus<?> construct) {
        HexEffectsComponent existing = buffer.getComponent(targetRef, HexEffectsComponent.getComponentType());
        if (existing != null) {
            existing.addEffect(constructId, construct);
            return;
        }

        UUIDComponent uuidComponent = buffer.getComponent(targetRef, UUIDComponent.getComponentType());
        UUID targetUuid = uuidComponent != null ? uuidComponent.getUuid() : null;
        if (targetUuid == null) {
            HexEffectsComponent fresh = new HexEffectsComponent();
            fresh.addEffect(constructId, construct);
            buffer.addComponent(targetRef, HexEffectsComponent.getComponentType(), fresh);
            return;
        }

        HexEffectsComponent pending;
        synchronized (PENDING_APPLIES) {
            pending = PENDING_APPLIES.get(targetUuid);
            if (pending == null || pending.getEffects().isEmpty()) {
                pending = new HexEffectsComponent();
                pending.addEffect(constructId, construct);
                PENDING_APPLIES.put(targetUuid, pending);
                buffer.addComponent(targetRef, HexEffectsComponent.getComponentType(), pending);
                return;
            }
        }
        pending.addEffect(constructId, construct);
    }

    @Nullable
    private static HexEffectsComponent pendingOf(@Nonnull ComponentAccessor<EntityStore> accessor,
            @Nonnull Ref<EntityStore> targetRef) {
        var uuidComponent = accessor.getComponent(targetRef, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return null;
        }
        synchronized (PENDING_APPLIES) {
            return PENDING_APPLIES.get(uuidComponent.getUuid());
        }
    }

    private static boolean claim(@Nonnull HexContext hexContext, @Nonnull UUID constructId,
            @Nullable String handlerId) {
        if (hexContext.hold(constructId)) {
            return true;
        }
        LOGGER.atSevere().log("construct '%s' refused: branch %d of cast %s is already held by construct %s",
                handlerId, hexContext.getBranchId(), hexContext.getExecutionId(), hexContext.getHolder());
        return false;
    }

    public static int abortExisting(@Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull Ref<EntityStore> targetRef, @Nonnull String handlerId) {
        // Detach WITHOUT releasing: abort() must still see the construct as the branch holder,
        // otherwise it never ends the replaced cast's branch and that cast stays live forever.
        var aborted = new ArrayList<HexStatus<?>>();
        detach(buffer.getComponent(targetRef, HexEffectsComponent.getComponentType()), handlerId, aborted);
        detach(pendingOf(buffer, targetRef), handlerId, aborted);
        if (aborted.isEmpty()) return 0;

        var handler = ConstructRegistry.get(handlerId);
        var ctx = new ConstructTickContext(buffer, targetRef);
        for (var status : aborted) {
            HexConstructSystem.abort(handler, status, ctx, BranchEndEvent.Reason.INTERRUPTED);
        }
        return aborted.size();
    }

    @Nonnull
    public static List<HexStatus<?>> detachExisting(@Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull Ref<EntityStore> targetRef, @Nonnull String handlerId) {
        var detached = new ArrayList<HexStatus<?>>();
        detach(buffer.getComponent(targetRef, HexEffectsComponent.getComponentType()), handlerId, detached);
        detach(pendingOf(buffer, targetRef), handlerId, detached);
        for (var status : detached) {
            var hexContext = status.getHexContext();
            hexContext.updateRuntimeAccessors(buffer);
            HexExecuter.release(hexContext, status.getConstructId(), buffer);
        }
        return detached;
    }

    private static void detach(@Nullable HexEffectsComponent effects, @Nonnull String handlerId,
            @Nonnull List<HexStatus<?>> into) {
        if (effects == null) return;
        var it = effects.getEffects().values().iterator();
        while (it.hasNext()) {
            var status = it.next();
            if (handlerId.equals(status.getHandlerId()) && !into.contains(status)) {
                into.add(status);
                it.remove();
            }
        }
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

        HexEffectsComponent pending = pendingOf(accessor, ref);
        if (pending == null) return false;

        for (HexStatus<?> status : pending.getEffects().values()) {
            if (handlerId.equals(status.getHandlerId())) return true;
        }
        return false;
    }
}
