package com.riprod.hexcode.builtin.elementalism.charge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;
import com.riprod.hexcode.api.event.BranchEndEvent;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.component.CastEffectsComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastRate;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.ecs.ConstructCastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.LifecycleCastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.TickingCastComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

public final class ElementalComponent
        implements TickingCastComponent, ConstructCastComponent, LifecycleCastComponent {

    private final Object2FloatLinkedOpenHashMap<String> charges = new Object2FloatLinkedOpenHashMap<>();
    private final Object2FloatOpenHashMap<String> elapsed = new Object2FloatOpenHashMap<>();
    @Nullable private String active;
    @Nullable private Vector3d lastPos;
    private final Vector3d velocity = new Vector3d();
    @Nullable private String vesselTier;
    @Nullable private String counted;
    private boolean open;

    public ElementalComponent() {
    }

    public void activate(@Nonnull String elementId, @Nonnull HexContext ctx) {
        activate(elementId);
        track(ctx);
        vessel(ctx, true);
    }

    private void track(@Nonnull HexContext ctx) {
        if (!open) {
            return;
        }
        var charging = active != null && ctx.rates().get(CHARGE, active) > 0f ? active : null;
        if (Objects.equals(counted, charging)) {
            return;
        }
        var threads = ctx.get(ElementThreads.TYPE);
        if (counted != null) {
            threads.remove(counted);
        }
        if (charging != null) {
            threads.add(charging);
        }
        counted = charging;
    }

    private void untrack(@Nonnull HexContext ctx) {
        if (counted != null) {
            ctx.get(ElementThreads.TYPE).remove(counted);
            counted = null;
        }
        open = false;
    }

    private void activate(@Nonnull String elementId) {
        active = elementId;
        if (!charges.containsKey(elementId)) {
            charges.put(elementId, 0f);
        }
    }

    public static void onCast(@Nonnull CastEvent event) {
        event.getContext().get(TYPE).vessel(event.getContext(), true);
    }

    private void vessel(@Nonnull HexContext ctx, boolean force) {
        var element = active != null ? ElementAsset.getAssetMap().getAsset(active) : null;
        if (element == null) {
            return;
        }
        var tier = element.vesselTier(charges.getFloat(active));
        var tierId = tier != null ? tier.getId() : null;
        if (force || !Objects.equals(tierId, vesselTier)) {
            vesselTier = tierId;
            ctx.get(CastEffectsComponent.TYPE).setOverride(element.vessel(tier));
        }
    }

    public float charge(@Nonnull String elementId) {
        return charges.getFloat(elementId);
    }

    @Nonnull
    public Object2FloatMap<String> charges() {
        return Object2FloatMaps.unmodifiable(charges);
    }

    public void clear() {
        charges.clear();
        elapsed.clear();
        active = null;
        vesselTier = null;
    }

    @Override
    public void onAttach(@Nonnull Ref<EntityStore> host, @Nullable Glyph holder, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        var transform = buffer.getComponent(host, TransformComponent.getComponentType());
        lastPos = transform != null ? new Vector3d(transform.getPosition()) : null;
    }

    @Override
    public void onTick(float dt, @Nonnull Ref<EntityStore> host, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        if (active == null) {
            return;
        }
        trackVelocity(host, dt, buffer);
        var element = ElementAsset.getAssetMap().getAsset(active);
        if (element == null) {
            return;
        }
        var t = elapsed.getFloat(active);
        var branches = ctx.get(ElementThreads.TYPE).count(active);
        var gain = element.rate(t, branches, velocity, host, buffer)
                * element.efficiency(t, branches, velocity, host, buffer) * dt
                * (1f + ctx.volatility().getSpent() / 100f)
                * ctx.rates().get(CHARGE, active);
        charges.put(active, Math.max(0f, charges.getFloat(active) + gain));
        elapsed.put(active, t + dt);
        vessel(ctx, false);
    }

    @Override
    public void onBranchOpen(@Nonnull HexContext ctx, @Nonnull CommandBuffer<EntityStore> buffer) {
        open = true;
        track(ctx);
    }

    @Override
    public void onBranchEnd(@Nonnull HexContext ctx, @Nonnull BranchEndEvent.Reason reason,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        untrack(ctx);
        if (charges.isEmpty()) {
            return;
        }
        if (reason != BranchEndEvent.Reason.ERROR) {
            ElementDischarge.discharge(charges, ctx, buffer);
        }
        clear();
        ctx.get(CastEffectsComponent.TYPE).setOverride(null);
    }

    private void trackVelocity(@Nonnull Ref<EntityStore> host, float dt, @Nonnull CommandBuffer<EntityStore> buffer) {
        var transform = buffer.getComponent(host, TransformComponent.getComponentType());
        if (transform == null) {
            velocity.zero();
            return;
        }
        var pos = transform.getPosition();
        if (lastPos != null && dt > 0f) {
            pos.sub(lastPos, velocity).div(dt);
            lastPos.set(pos);
        } else {
            velocity.zero();
            lastPos = new Vector3d(pos);
        }
    }

    @Nonnull
    @Override
    public ElementalComponent copy() {
        var copy = new ElementalComponent();
        copy.charges.putAll(charges);
        copy.elapsed.putAll(elapsed);
        copy.active = active;
        copy.vesselTier = vesselTier;
        copy.counted = counted;
        copy.open = open;
        copy.lastPos = lastPos != null ? new Vector3d(lastPos) : null;
        return copy;
    }

    @Nonnull
    @Override
    public ElementalComponent branch() {
        var child = new ElementalComponent();
        for (var entry : charges.object2FloatEntrySet()) {
            var half = entry.getFloatValue() / 2f;
            entry.setValue(half);
            child.charges.put(entry.getKey(), half);
        }
        child.elapsed.putAll(elapsed);
        child.active = active;
        child.vesselTier = vesselTier;
        return child;
    }

    public static final class Overlay implements CastOverlay<ElementalComponent> {

        @Nullable private String element;
        private float charge;

        public Overlay() {
        }

        @Override
        public void applyTo(@Nonnull ElementalComponent target) {
            if (element == null) {
                return;
            }
            target.activate(element);
            target.charges.addTo(element, charge);
        }

        public static final BuilderCodec<Overlay> CODEC = BuilderCodec
                .builder(Overlay.class, Overlay::new)
                .appendInherited(new KeyedCodec<>("Element", Codec.STRING),
                        (o, v) -> o.element = v,
                        o -> o.element,
                        (o, p) -> o.element = p.element)
                .addValidator(ElementAsset.VALIDATOR_CACHE.getValidator())
                .documentation("Element the cast starts charging. Sets the active element.")
                .add()
                .<Float>appendInherited(new KeyedCodec<>("Charge", Codec.FLOAT),
                        (o, v) -> o.charge = v,
                        o -> o.charge,
                        (o, p) -> o.charge = p.charge)
                .documentation("Charge added to the element before the first glyph. Adds to earlier layers.")
                .add()
                .build();
    }

    public static final CastRate CHARGE = CastRate.register("Charge", ElementAsset.VALIDATOR_CACHE.getValidator());

    public static final CastComponentType<ElementalComponent> TYPE = HexComponentRegistry.register(
            ElementalComponent.class, "Elemental", Overlay.CODEC, a -> new ElementalComponent());
}
