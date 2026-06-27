package com.riprod.hexcode.core.common.construct.system;

import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class HexConstructSpawner {

    // tick-local cache of pending HexEffectsComponent adds per target ref.
    // solves the check-then-add race: a second apply() in the same tick sees
    // the instance the first apply() queued (via weak ref, map cleared by GC after
    // ref becomes unreachable at tick boundary).
    private static final WeakHashMap<Ref<EntityStore>, HexEffectsComponent> PENDING_APPLIES = new WeakHashMap<>();

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
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        return holder;
    }

    public static void apply(
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nullable Ref<EntityStore> targetRef,
            @Nonnull HexContext hexContext,
            @Nullable Glyph triggeringGlyph,
            @Nullable String handlerId) {
        applyWithState(buffer, targetRef, hexContext, triggeringGlyph, handlerId, null);
    }

    public static <S extends ConstructState> void applyWithState(
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
            return;
        }

        UUID constructId = UUID.randomUUID();
        HexStatus<S> construct = new HexStatus<>(
                handlerId, hexContext, constructId, triggeringGlyph, initialState);

        HexEffectsComponent existing = buffer.getComponent(targetRef, HexEffectsComponent.getComponentType());
        if (existing != null) {
            existing.addEffect(constructId, construct);
            return;
        }

        HexEffectsComponent pending;
        synchronized (PENDING_APPLIES) {
            pending = PENDING_APPLIES.get(targetRef);
            if (pending == null || pending.getEffects().isEmpty()) {
                pending = new HexEffectsComponent();
                pending.addEffect(constructId, construct);
                PENDING_APPLIES.put(targetRef, pending);
                buffer.addComponent(targetRef, HexEffectsComponent.getComponentType(), pending);
                return;
            }
        }
        pending.addEffect(constructId, construct);
    }
}
