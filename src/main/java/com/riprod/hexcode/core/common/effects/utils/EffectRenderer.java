package com.riprod.hexcode.core.common.effects.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.component.GlyphEffect;
import com.riprod.hexcode.core.common.effects.component.SustainState;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.execution.cast.component.CastEffectsComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;
import com.riprod.hexcode.utils.VfxUtil;

public final class EffectRenderer {


    private EffectRenderer() {
    }

    public static void render(EffectSlot slot, @Nullable GlyphAsset glyph, @Nullable HexContext ctx,
            Vector3d position, @Nullable Vector3d facing, ComponentAccessor<EntityStore> accessor) {
        render(slot, resolve(glyph, ctx), position, facing, 1.0f, null, accessor);
    }

    public static void render(EffectSlot slot, @Nullable GlyphAsset glyph, @Nullable HexContext ctx,
            Vector3d position, ComponentAccessor<EntityStore> accessor) {
        render(slot, glyph, ctx, position, null, accessor);
    }

    public static void render(EffectSlot slot, GlyphEffects effects, Vector3d position, @Nullable Vector3d facing,
            float scale, @Nullable List<Ref<EntityStore>> recipients, ComponentAccessor<EntityStore> accessor) {
        var effect = effects.get(slot);
        if (effect == null) {
            return;
        }
        if (slot != EffectSlot.Sustain) {
            spawn(effect.getParticles(), effect, effects, position, facing, scale, recipients, accessor);
        }
        var sound = effect.getSound();
        if (sound != null) {
            VfxUtil.sound(sound, position, effects.getVolumeOrDefault(), accessor);
        }
    }

    public static GlyphEffects resolve(@Nullable GlyphAsset glyph, @Nullable HexContext ctx) {
        if (ctx != null) {
            return ctx.get(CastEffectsComponent.TYPE).resolve(glyph);
        }
        var own = glyph != null ? glyph.getEffects() : null;
        return own != null ? own : GlyphEffects.empty();
    }

    public static Model inject(Model model, GlyphEffects effects, @Nullable SustainState state) {
        var sustain = effects.get(EffectSlot.Sustain);
        if (sustain == null) {
            return model;
        }
        var color = color(effects, sustain);
        var particles = effects.getAlphaOrDefault() > 0f ? tinted(sustain.getParticles(), color, model.getScale())
                : null;
        var trail = sustain.getTrail() != null ? new ModelTrail(sustain.getTrail()) : null;
        var light = sustain.getLight() > 0f ? light(sustain.getLight(), color) : null;
        if (state != null) {
            state.injected(particles, trail, light, model.getLight());
        }
        return GlyphModelUtil.rebuild(model, model.getAttachments(), model.getBoundingBox(),
                light != null ? light : model.getLight(),
                concat(model.getParticles(), particles, ModelParticle[]::new),
                trail != null ? concat(model.getTrails(), new ModelTrail[] { trail }, ModelTrail[]::new)
                        : model.getTrails());
    }

    public static Model strip(Model live, SustainState state) {
        var light = state.getLight() != null && live.getLight() == state.getLight() ? state.getReplacedLight()
                : live.getLight();
        var trails = state.getTrail() != null ? new ModelTrail[] { state.getTrail() } : null;
        var stripped = GlyphModelUtil.rebuild(live, live.getAttachments(), live.getBoundingBox(), light,
                without(live.getParticles(), state.getParticles()), without(live.getTrails(), trails));
        state.injected(null, null, null, null);
        return stripped;
    }

    public static void pulse(SustainState state, GlyphEffects effects, Vector3d position, float dt,
            ComponentAccessor<EntityStore> accessor) {
        var sustain = effects.get(EffectSlot.Sustain);
        if (sustain == null || sustain.getPulse() == null || !state.due(dt, sustain.getInterval())) {
            return;
        }
        spawn(sustain.getPulse(), sustain, effects, position, null, 1.0f, null, accessor);
    }

    public static Vector3d blockCenter(Vector3i block) {
        return new Vector3d(block.x + 0.5, block.y + 0.5, block.z + 0.5);
    }

    @Nullable
    public static Vector3d entityCenter(Ref<EntityStore> ref, ComponentAccessor<EntityStore> accessor) {
        if (!ref.isValid()) {
            return null;
        }
        var transform = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }
        var center = new Vector3d(transform.getPosition());
        var bounds = accessor.getComponent(ref, BoundingBox.getComponentType());
        if (bounds != null) {
            var box = bounds.getBoundingBox();
            center.y += (box.getMin().y + box.getMax().y) * 0.5;
        }
        return center;
    }

    private static void spawn(@Nullable ModelParticle[] particles, GlyphEffect effect, GlyphEffects effects,
            Vector3d position, @Nullable Vector3d facing, float scale, @Nullable List<Ref<EntityStore>> recipients,
            ComponentAccessor<EntityStore> accessor) {
        if (particles == null || effects.getAlphaOrDefault() <= 0f || !VfxUtil.playersInRange(position, accessor)) {
            return;
        }
        var color = color(effects, effect);
        var rotation = facing != null ? Rotation3f.lookAt(facing) : null;
        for (var particle : particles) {
            var size = scale * particle.getScale();
            if (rotation != null) {
                VfxUtil.spawnConfiguredDirected(particle, position, rotation, color, size, accessor, recipients);
            } else {
                VfxUtil.spawnConfigured(particle, position, color, size, accessor, recipients);
            }
        }
    }

    @Nullable
    private static Color color(GlyphEffects effects, GlyphEffect effect) {
        return switch (effect.getTint()) {
            case Primary -> effects.getPrimaryColor();
            case Secondary -> effects.getSecondaryColor();
            case None -> null;
        };
    }

    private static ColorLight light(float level, @Nullable Color color) {
        if (color == null) {
            var full = (byte) Math.round(level);
            return new ColorLight(full, full, full, full);
        }
        var r = color.red & 0xFF;
        var g = color.green & 0xFF;
        var b = color.blue & 0xFF;
        var peak = Math.max(1, Math.max(r, Math.max(g, b)));
        return new ColorLight((byte) Math.round(level),
                (byte) Math.round(level * r / peak),
                (byte) Math.round(level * g / peak),
                (byte) Math.round(level * b / peak));
    }

    @Nullable
    private static ModelParticle[] tinted(@Nullable ModelParticle[] particles, @Nullable Color color, float scale) {
        if (particles == null) {
            return null;
        }
        var tinted = new ModelParticle[particles.length];
        for (var i = 0; i < particles.length; i++) {
            var p = particles[i];
            tinted[i] = new ModelParticle(p.getSystemId(), p.getTargetEntityPart(), p.getTargetNodeName(),
                    color != null ? color : p.getColor(), p.getScale(), p.getPositionOffset(), p.getRotationOffset(),
                    p.isDetachedFromModel(), p.isClearParticlesOnRemove()).scale(scale);
        }
        return tinted;
    }

    @Nullable
    private static <T> T[] concat(@Nullable T[] base, @Nullable T[] added,
            IntFunction<T[]> factory) {
        if (added == null || added.length == 0) {
            return base;
        }
        if (base == null || base.length == 0) {
            return added;
        }
        var merged = factory.apply(base.length + added.length);
        System.arraycopy(base, 0, merged, 0, base.length);
        System.arraycopy(added, 0, merged, base.length, added.length);
        return merged;
    }

    @Nullable
    private static <T> T[] without(@Nullable T[] live, @Nullable T[] injected) {
        if (live == null || injected == null || injected.length == 0) {
            return live;
        }
        var kept = Arrays.copyOf(live, live.length);
        var count = 0;
        for (var entry : live) {
            if (!contains(injected, entry)) {
                kept[count++] = entry;
            }
        }
        return Arrays.copyOf(kept, count);
    }

    private static boolean contains(Object[] entries, Object entry) {
        for (var candidate : entries) {
            if (candidate == entry) {
                return true;
            }
        }
        return false;
    }
}
