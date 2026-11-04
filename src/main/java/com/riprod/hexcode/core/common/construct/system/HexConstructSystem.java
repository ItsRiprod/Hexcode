package com.riprod.hexcode.core.common.construct.system;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.utils.LogScopes;

public class HexConstructSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    @Override
    public Query<EntityStore> getQuery() {
        return HexEffectsComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            HexEffectsComponent construct = chunk.getComponent(index, HexEffectsComponent.getComponentType());
            Ref<EntityStore> entityRef = chunk.getReferenceTo(index);

            if (construct == null || construct.getEffects().isEmpty()) {
                buffer.tryRemoveComponent(entityRef, HexEffectsComponent.getComponentType());
                return;
            }

            // snapshot ids; onEnd/onAbort may chain glyphs that addEffect to this same map
            List<UUID> ids = new ArrayList<>(construct.getEffects().keySet());
            ConstructTickContext ctx = new ConstructTickContext(chunk, index, buffer, entityRef);
            List<Runnable> deferred = null;

            for (UUID effectId : ids) {
                HexStatus<?> status = construct.getEffects().get(effectId);
                if (status == null) continue;
                ConstructHandler<?> handler = ConstructRegistry.get(status.getHandlerId());

                if (handler == null) {
                    LOGGER.atSevere().log("no construct handler for: %s", status.getHandlerId());
                    construct.removeEffect(effectId);
                    final HexStatus<?> s = status;
                    if (deferred == null) deferred = new ArrayList<>(2);
                    deferred.add(() -> abort(null, s, ctx));
                    continue;
                }

                boolean ended = tickEffect(effectId, handler, status, ctx, dt);
                if (ended) {
                    construct.removeEffect(effectId);
                    final ConstructHandler<?> h = handler;
                    final HexStatus<?> s = status;
                    if (deferred == null) deferred = new ArrayList<>(2);
                    deferred.add(() -> end(h, s, ctx));
                    continue;
                }

                boolean killRequested = status.isKillRequested();
                boolean budgetDepleted = status.getHexContext() != null
                        && status.getHexContext().volatility().getCurrent() <= 0;
                if (killRequested || budgetDepleted) {
                    LOGGER.atFine().log("construct '%s' terminated (%s)",
                            status.getHandlerId(),
                            killRequested ? "kill requested" : "volatility depleted");
                    construct.removeEffect(effectId);
                    final ConstructHandler<?> h = handler;
                    final HexStatus<?> s = status;
                    if (deferred == null) deferred = new ArrayList<>(2);
                    deferred.add(() -> abort(h, s, ctx));
                }
            }

            if (deferred != null) {
                for (Runnable r : deferred) r.run();
            }

            if (construct.getEffects().isEmpty()) {
                buffer.tryRemoveComponent(entityRef, HexEffectsComponent.getComponentType());
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("HexConstructSystem failed: %s", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean tickEffect(UUID effectId, ConstructHandler<?> handler, HexStatus<?> status,
            ConstructTickContext ctx, float dt) {
        try {
            bindAccessor(status, ctx);
            ConstructHandler raw = handler;
            HexStatus rawStatus = status;
            var hexContext = status.getHexContext();

            if (!status.hasFiredImmediate()) {
                if (hexContext.attach(status.getConstructId(), ctx.getEntityRef())) {
                    HexComponentRegistry.dispatchAttach(ctx.getEntityRef(), hexContext, ctx.getBuffer());
                }
                Object initial = raw.createInitialState(rawStatus, ctx);
                if (initial instanceof ConstructState cs) {
                    rawStatus.setState(cs);
                }
                raw.onFirstTick(rawStatus, ctx);
                status.markImmediateFired();
                return false;
            }
            var ended = raw.onTick(dt, rawStatus, ctx);
            if (!ended && hexContext.isAttachedTo(status.getConstructId())) {
                HexComponentRegistry.dispatchTick(dt, ctx.getEntityRef(), hexContext, ctx.getBuffer());
            }
            return ended;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("error ticking construct effect %s: %s", effectId, e);
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void end(ConstructHandler<?> handler, HexStatus<?> status, ConstructTickContext ctx) {
        if (handler == null) return;
        var hexContext = status.getHexContext();
        var owned = status.getConstructId().equals(hexContext.getHolder());
        try {
            bindAccessor(status, ctx);
            release(status, ctx);
            ConstructHandler raw = handler;
            HexStatus rawStatus = status;
            raw.onEnd(rawStatus, ctx);
        } catch (Exception e) {
            LOGGER.atWarning().log("construct handler onEnd failed: %s", e.getMessage());
            if (owned) {
                HexExecuter.endBranch(hexContext);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void abort(ConstructHandler<?> handler, HexStatus<?> status, ConstructTickContext ctx) {
        var hexContext = status.getHexContext();
        var owned = status.getConstructId().equals(hexContext.getHolder());
        try {
            bindAccessor(status, ctx);
            release(status, ctx);
            if (handler == null) return;
            ConstructHandler raw = handler;
            HexStatus rawStatus = status;
            raw.onAbort(rawStatus, ctx);
        } catch (Exception e) {
            LOGGER.atWarning().log("construct handler onAbort failed: %s", e.getMessage());
        } finally {
            if (owned) {
                HexExecuter.endBranch(hexContext);
            }
        }
    }

    private static void release(HexStatus<?> status, ConstructTickContext ctx) {
        HexExecuter.release(status.getHexContext(), status.getConstructId(), ctx.getBuffer());
    }

    private static void bindAccessor(HexStatus<?> status, ConstructTickContext ctx) {
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
    }
}
