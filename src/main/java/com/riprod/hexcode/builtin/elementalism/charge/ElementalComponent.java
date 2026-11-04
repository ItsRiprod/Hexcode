package com.riprod.hexcode.builtin.elementalism.charge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.riprod.hexcode.builtin.elementalism.effects.EffectPlayer;
import com.riprod.hexcode.builtin.elementalism.effects.EffectSlot;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;
import com.riprod.hexcode.builtin.elementalism.element.ElementTickInput;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.ecs.ConstructCastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.LifecycleCastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.TickingCastComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;

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
    private float constructCooldown;

    public ElementalComponent() {
    }

    public void activate(@Nonnull String elementId) {
        active = elementId;
        if (!charges.containsKey(elementId)) {
            charges.put(elementId, 0f);
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
    }

    @Override
    public void onAttach(@Nonnull Ref<EntityStore> host, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        var transform = buffer.getComponent(host, TransformComponent.getComponentType());
        lastPos = transform != null ? new Vector3d(transform.getPosition()) : null;
        var element = active != null ? ElementAsset.getAssetMap().getAsset(active) : null;
        var center = element != null ? EffectPlayer.entityCenter(host, buffer) : null;
        if (center != null) {
            EffectPlayer.play(element.effectsAt(charges.getFloat(active)).get(EffectSlot.LAUNCH), center, buffer);
        }
    }

    @Override
    public void onTick(float dt, @Nonnull Ref<EntityStore> host, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        if (active == null) {
            return;
        }
        var speed = trackSpeed(host, dt, buffer);
        var element = ElementAsset.getAssetMap().getAsset(active);
        if (element == null) {
            return;
        }
        var t = elapsed.getFloat(active);
        var rate = element.rate(new ElementTickInput(dt, t, speed, isLiving(host, buffer)));
        var gain = rate * dt * (1f + ctx.volatility().getSpent() / 100f);
        var charge = charges.addTo(active, gain) + gain;
        elapsed.put(active, t + dt);
        constructCooldown -= dt;
        if (constructCooldown <= 0f) {
            constructCooldown = element.getConstructInterval();
            var center = EffectPlayer.entityCenter(host, buffer);
            if (center != null) {
                EffectPlayer.play(element.effectsAt(charge).get(EffectSlot.CONSTRUCT), center, buffer);
            }
        }
    }

    @Override
    public void onBranchEnd(@Nonnull HexContext ctx, @Nonnull CommandBuffer<EntityStore> buffer) {
        if (charges.isEmpty()) {
            return;
        }
        ElementDischarge.discharge(charges, ctx, buffer);
        clear();
    }

    private float trackSpeed(@Nonnull Ref<EntityStore> host, float dt, @Nonnull CommandBuffer<EntityStore> buffer) {
        var transform = buffer.getComponent(host, TransformComponent.getComponentType());
        if (transform == null) {
            return 0f;
        }
        var pos = transform.getPosition();
        var speed = lastPos != null && dt > 0f ? (float) (lastPos.distance(pos) / dt) : 0f;
        if (lastPos == null) {
            lastPos = new Vector3d(pos);
        } else {
            lastPos.set(pos);
        }
        return speed;
    }

    private static boolean isLiving(@Nonnull Ref<EntityStore> host, @Nonnull CommandBuffer<EntityStore> buffer) {
        return buffer.getComponent(host, Player.getComponentType()) != null
                || buffer.getComponent(host, NPCEntity.getComponentType()) != null;
    }

    @Nonnull
    @Override
    public ElementalComponent copy() {
        var copy = new ElementalComponent();
        copy.charges.putAll(charges);
        copy.elapsed.putAll(elapsed);
        copy.active = active;
        copy.lastPos = lastPos != null ? new Vector3d(lastPos) : null;
        copy.constructCooldown = constructCooldown;
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

    public static final CastComponentType<ElementalComponent> TYPE = HexComponentRegistry.register(
            ElementalComponent.class, "Elemental", Overlay.CODEC, a -> new ElementalComponent());
}
