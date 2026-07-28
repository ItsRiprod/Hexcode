package com.riprod.hexcode.utils;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class VfxUtil {
  private VfxUtil() {
  }

  public static @Nullable String resolveModelId(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
    String id = overrides != null ? overrides.getPrimaryModel() : null;
    if (id != null)
      return id;
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    return glyphStyle != null ? glyphStyle.getPrimaryModel() : null;
  }

  private static final Color WHITE = new Color((byte) 255, (byte) 255, (byte) 255);

  private static final float DEFAULT_LINE_OPACITY = 0.7f;

  private static final float PARTICLE_SCALE = 1.0f;

  public static Color resolvePrimaryColorRaw(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
    Color c = resolveColor(overrides != null ? overrides.getPrimaryColor() : null,
        glyphStyleColor(glyphAsset, true));
    return c != null ? c : WHITE;
  }

  public static Vector3f resolvePrimaryColor(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    return HexColors.toVector3f(resolvePrimaryColorRaw(ctx, glyphAsset));
  }

  public static float resolveAlpha(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    return resolveAlpha(ctx != null ? ctx.getStyle() : null,
        glyphAsset != null ? glyphAsset.getStyle() : null);
  }

  private static float resolveAlpha(@Nullable HexStyleAsset overrides, @Nullable HexStyleAsset glyphStyle) {
    if (overrides != null && overrides.getAlpha() != null) {
      return overrides.getAlpha();
    }
    return glyphStyle != null ? glyphStyle.getAlphaOrDefault() : 1.0f;
  }

  public static float resolveVolume(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    return resolveVolume(ctx != null ? ctx.getStyle() : null,
        glyphAsset != null ? glyphAsset.getStyle() : null);
  }

  private static float resolveVolume(@Nullable HexStyleAsset overrides, @Nullable HexStyleAsset glyphStyle) {
    if (overrides != null && overrides.getVolume() != null) {
      float v = overrides.getVolume();
      return v < HexStyleAsset.MIN_VOLUME ? HexStyleAsset.MIN_VOLUME
          : Math.min(v, HexStyleAsset.MAX_VOLUME);
    }
    return glyphStyle != null ? glyphStyle.getVolumeOrDefault() : 1.0f;
  }

  private static @Nullable Color glyphStyleColor(@Nullable GlyphAsset glyphAsset, boolean primary) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (glyphStyle == null)
      return null;
    return primary ? glyphStyle.getPrimaryColor() : glyphStyle.getSecondaryColor();
  }

  public static void particle(String systemId, Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    ParticleUtil.spawnParticleEffect(systemId, pos, accessor);
  }

  public static void sound(String soundId, Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    sound(soundId, pos, 1.0f, accessor);
  }

  public static void sound(String soundId, Vector3d pos, float volumeModifier,
      ComponentAccessor<EntityStore> accessor) {
    int index = SoundEvent.getAssetMap().getIndex(soundId);
    if (index >= 0) {
      SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, pos.x, pos.y, pos.z,
          volumeModifier, 1.0f, accessor);
    }
  }

  public static void effect(String particleId, String soundId, Vector3d pos,
      ComponentAccessor<EntityStore> accessor) {
    particle(particleId, pos, accessor);
    sound(soundId, pos, accessor);
  }

  public static void spawnPrimary(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (glyphStyle == null)
      return;
    Color tint = resolveColor(overrides != null ? overrides.getPrimaryColor() : null, glyphStyle.getPrimaryColor());
    if (resolveAlpha(overrides, glyphStyle) > 0f)
      spawnConfigured(glyphStyle.getPrimaryParticle(), pos, tint, PARTICLE_SCALE, accessor);
    if (glyphStyle.getPrimarySound() != null)
      sound(glyphStyle.getPrimarySound(), pos, resolveVolume(overrides, glyphStyle), accessor);
  }

  public static void spawnPrimaryDirected(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, Rotation3f rotation, ComponentAccessor<EntityStore> accessor) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (glyphStyle == null)
      return;
    Color tint = resolveColor(overrides != null ? overrides.getPrimaryColor() : null, glyphStyle.getPrimaryColor());
    if (resolveAlpha(overrides, glyphStyle) > 0f)
      spawnConfiguredDirected(glyphStyle.getPrimaryParticle(), pos, rotation, tint,
          PARTICLE_SCALE, accessor);
    if (glyphStyle.getPrimarySound() != null)
      sound(glyphStyle.getPrimarySound(), pos, resolveVolume(overrides, glyphStyle), accessor);
  }

  public static void spawnSecondary(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    spawnSecondary(overrides, glyphAsset, pos, accessor, null);
  }

  public static void spawnSecondary(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor, @Nullable List<Ref<EntityStore>> recipients) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (glyphStyle == null)
      return;
    Color tint = resolveColor(overrides != null ? overrides.getSecondaryColor() : null, glyphStyle.getSecondaryColor());
    if (resolveAlpha(overrides, glyphStyle) > 0f)
      spawnConfigured(glyphStyle.getSecondaryParticle(), pos, tint, PARTICLE_SCALE,
          accessor, recipients);
    if (glyphStyle.getSecondarySound() != null)
      sound(glyphStyle.getSecondarySound(), pos, resolveVolume(overrides, glyphStyle), accessor);
  }

  public static void spawnTertiary(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    spawnTertiary(overrides, glyphAsset, pos, accessor, null);
  }

  public static void spawnTertiary(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor, @Nullable List<Ref<EntityStore>> recipients) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (glyphStyle == null)
      return;
    Color tint = resolveColor(overrides != null ? overrides.getSecondaryColor() : null, glyphStyle.getSecondaryColor());
    if (resolveAlpha(overrides, glyphStyle) > 0f)
      spawnConfigured(glyphStyle.getTertiaryParticle(), pos, tint, PARTICLE_SCALE,
          accessor, recipients);
    if (glyphStyle.getTertiarySound() != null)
      sound(glyphStyle.getTertiarySound(), pos, resolveVolume(overrides, glyphStyle), accessor);
  }

  public static void spawnFromConfig(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      @Nullable ModelParticle particle, @Nullable String soundId, Vector3d pos,
      ComponentAccessor<EntityStore> accessor) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (particle != null && resolveAlpha(overrides, glyphStyle) > 0f) {
      Color tint = resolveColor(overrides != null ? overrides.getPrimaryColor() : null,
          glyphStyle != null ? glyphStyle.getPrimaryColor() : null);
      spawnConfigured(particle, pos, tint, PARTICLE_SCALE, accessor);
    }
    if (soundId != null)
      sound(soundId, pos, resolveVolume(overrides, glyphStyle), accessor);
  }

  public static void spawnStyleParticle(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    spawnStyleParticleDirected(overrides, glyphAsset, pos, accessor, new Rotation3f((float) (-Math.PI / 2), 0, 0));
  }

  public static void spawnStyleParticleDirected(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor, Vector3d direction) {
    Rotation3f rotation = Rotation3f.lookAt(direction);
    spawnStyleParticleDirected(overrides, glyphAsset, pos, accessor, rotation);
  }

  public static void spawnStyleParticleDirected(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, ComponentAccessor<EntityStore> accessor, Rotation3f rotation) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    ModelParticle particle = overrides != null && overrides.getStyleParticle() != null
        ? overrides.getStyleParticle()
        : (glyphStyle != null ? glyphStyle.getStyleParticle() : null);
    if (particle == null)
      return;
    if (resolveAlpha(overrides, glyphStyle) <= 0f)
      return;
    Color tint = resolveColor(
        overrides != null ? overrides.getPrimaryColor() : null,
        glyphStyle != null ? glyphStyle.getPrimaryColor() : null);
    spawnConfiguredDirected(particle, pos, rotation, tint, PARTICLE_SCALE, accessor);
  }

  private static @Nullable Color resolveColor(@Nullable Color override, @Nullable Color fallback) {
    return override != null ? override : fallback;
  }

  public static List<Ref<EntityStore>> collectParticleRecipients(Vector3d pos, double radius,
      ComponentAccessor<EntityStore> accessor) {
    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = accessor
        .getResource(EntityModule.get().getPlayerSpatialResourceType());
    List<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
    playerRefs.clear();
    playerSpatialResource.getSpatialStructure().collect(pos, radius, playerRefs);
    return new ArrayList<>(playerRefs);
  }

  private static void spawnConfigured(@Nullable ModelParticle particle, Vector3d pos,
      @Nullable Color tint, float scale, ComponentAccessor<EntityStore> accessor) {
    spawnConfigured(particle, pos, tint, scale, accessor, null);
  }

  private static void spawnConfigured(@Nullable ModelParticle particle, Vector3d pos,
      @Nullable Color tint, float scale, ComponentAccessor<EntityStore> accessor,
      @Nullable List<Ref<EntityStore>> recipients) {
    if (particle == null || particle.getSystemId() == null)
      return;
    Color effective = tint != null ? tint : particle.getColor();
    if (effective == null) {
      // untinted spawn reaches the engine default distance rather than the 25 block
      // recipient sweep, so only leave that path when scale actually needs the tinted overload
      if (scale == 1.0f) {
        ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos, accessor);
        return;
      }
      effective = WHITE;
    }
    List<Ref<EntityStore>> playerRefs = recipients != null
        ? recipients
        : collectParticleRecipients(pos, 25.0, accessor);
    ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos, 0.0f, 0.0f, 0.0f, scale, effective, playerRefs,
        accessor);
  }

  private static void spawnConfiguredDirected(@Nullable ModelParticle particle, Vector3d pos,
      Rotation3f rotation, @Nullable Color tint, float scale, ComponentAccessor<EntityStore> accessor) {
    if (particle == null || particle.getSystemId() == null)
      return;
    Color effective = tint != null ? tint : particle.getColor();
    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = accessor
        .getResource(EntityModule.get().getPlayerSpatialResourceType());
    List<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
    playerSpatialResource.getSpatialStructure().collect(pos, 25.0, playerRefs);
    if (effective == null) {
      if (scale == 1.0f) {
        ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos, rotation, playerRefs, accessor);
        return;
      }
      effective = WHITE;
    }
    ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos,
        rotation.y, rotation.x, rotation.roll(), scale, effective, playerRefs, accessor);
  }

  private static int flowPhase = 0;

  public static void advanceFlowPhase() {
    flowPhase = (flowPhase + 1) % 4;
  }

  public static void particleAlongPath(String systemId, Vector3d source, Vector3d target,
      int count, ComponentAccessor<EntityStore> accessor) {
    if (count < 1)
      count = 1;
    double phaseOffset = (double) flowPhase / (count * 4);
    Vector3d point = new Vector3d();
    for (int i = 0; i < count; i++) {
      double t = (double) i / count + phaseOffset;
      if (t >= 1.0)
        t -= 1.0;
      point.x = source.x + (target.x - source.x) * t;
      point.y = source.y + (target.y - source.y) * t;
      point.z = source.z + (target.z - source.z) * t;
      ParticleUtil.spawnParticleEffect(systemId, point, accessor);
    }
  }

  public static void particleAlongPath(String systemId, Vector3d source, Vector3d target,
      int count, Color color, @Nullable Ref<EntityStore> playerRef,
      ComponentAccessor<EntityStore> accessor) {
    if (count < 1)
      count = 1;
    double phaseOffset = (double) flowPhase / (count * 4);

    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = accessor
        .getResource(EntityModule.get().getPlayerSpatialResourceType());
    List<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
    playerSpatialResource.getSpatialStructure().collect(source, (double) 25.0F, playerRefs);

    Vector3d point = new Vector3d();
    for (int i = 0; i < count; i++) {
      double t = (double) i / count + phaseOffset;
      if (t >= 1.0)
        t -= 1.0;
      point.x = source.x + (target.x - source.x) * t;
      point.y = source.y + (target.y - source.y) * t;
      point.z = source.z + (target.z - source.z) * t;

      ParticleUtil.spawnParticleEffect(systemId, point, 0.0f, 0.0f, 0.0f, 1.0f, color, playerRefs, accessor);
    }
  }

  public static void line(ComponentAccessor<EntityStore> accessor, World world, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags) {
    line(accessor, world, start, end, color, thickness, time, flags, DEFAULT_LINE_OPACITY, null);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, World world, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags, @Nullable Ref<EntityStore> ref) {
    line(accessor, world, start, end, color, thickness, time, flags, DEFAULT_LINE_OPACITY, ref);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, World world, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags, float opacity) {
    line(accessor, world, start, end, color, thickness, time, flags, opacity, null);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, World world, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags, float opacity, @Nullable Ref<EntityStore> ref) {
    if (opacity <= 0f)
      return;
    double dirX = end.x - start.x;
    double dirY = end.y - start.y;
    double dirZ = end.z - start.z;
    double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
    if (length < 0.001)
      return;
    Matrix4d matrix = new Matrix4d();
    matrix.identity();
    matrix.translate(start.x, start.y, start.z);
    double angleY = Math.atan2(dirZ, dirX);
    matrix.rotate(-(angleY + (Math.PI / 2)), 0.0, 1.0, 0.0);
    double angleX = Math.atan2(Math.sqrt(dirX * dirX + dirZ * dirZ), dirY);
    matrix.rotate(-angleX, 1.0, 0.0, 0.0);
    matrix.translate(0.0, length / 2.0, 0.0);
    matrix.scale(thickness, length, thickness);
    int allFlags = flags | DebugUtils.FLAG_NO_WIREFRAME;

    if (ref == null || !ref.isValid()) {
      DebugUtils.add(world, DebugShape.Cube, matrix, color, opacity, time, allFlags);
      return;
    }

    PlayerRef playerRef = accessor.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef != null) {
      float[] arr = new float[16];
      matrix.get(arr);
      DisplayDebug packet = new DisplayDebug(
          DebugShape.Cube, arr,
          new Vector3f(
              color.x, color.y, color.z),
          time, (byte) allFlags, null, opacity);
      playerRef.getPacketHandler().write(packet);
    }
  }

  public static boolean applyEffect(@Nullable HexContext hexContext, @Nullable Ref<EntityStore> target,
      @Nullable String effectId, float duration, OverlapBehavior overlap) {
    if (hexContext == null || target == null || !target.isValid() || effectId == null)
      return false;
    EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
    if (effect == null)
      return false;
    ComponentAccessor<EntityStore> accessor = hexContext.getAccessor();
    EffectControllerComponent controller = accessor.getComponent(
        target, EffectControllerComponent.getComponentType());
    if (controller == null)
      return false;
    controller.addEffect(target, effect, duration, overlap, accessor);
    return true;
  }

  public static boolean applyBoundedEffect(@Nullable HexContext hexContext, @Nullable Ref<EntityStore> target,
      @Nullable Glyph glyph, @Nullable String effectId, float requestedDuration, OverlapBehavior overlap) {
    return applyEffect(hexContext, target, effectId,
        volatilityBoundedDuration(hexContext, glyph, requestedDuration), overlap);
  }

  public static float volatilityBoundedDuration(@Nullable HexContext hexContext, @Nullable Glyph glyph,
      float requestedDuration) {
    float rate = drainRate(glyph);
    if (rate <= 0f)
      return requestedDuration;
    VolatilityComponent stats = hexContext != null ? hexContext.volatility() : null;
    if (stats == null)
      return requestedDuration;
    float multiplier = stats.getVolatilityMultiplier();
    float effectiveRate = rate * (multiplier > 0f ? multiplier : 1f);
    if (effectiveRate <= 0f)
      return requestedDuration;
    return Math.min(requestedDuration, stats.getCurrent() / effectiveRate);
  }

  private static float drainRate(@Nullable Glyph glyph) {
    if (glyph == null)
      return 0f;
    GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
    return asset != null ? asset.getVolatility().getDrainPerSecond() : 0f;
  }
}
