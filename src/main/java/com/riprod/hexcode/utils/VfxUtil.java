package com.riprod.hexcode.utils;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.builtin.beam.AttachedBeam;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
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
import com.riprod.hexcode.core.common.utilities.DebugEmitter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;

public class VfxUtil {
  private VfxUtil() {
  }

  public static List<AttachedBeam> linkBeams(HexComponent hexComp, Slot slot, SlotConfig config) {
    var index = config.linkBeamIndex();
    if (index < 0)
      return List.of();
    var beams = new ArrayList<AttachedBeam>();
    for (var linkId : slot.getLinks()) {
      var target = hexComp.getChildGlyphRef(linkId);
      if (target == null)
        continue;
      beams.add(AttachedBeam.toEntity(index, config.getLinkBeamScale(), null, target, null));
    }
    return beams;
  }

  public static List<AttachedBeam> glyphLinkBeams(HexComponent hexComp, Glyph glyph) {
    var beams = new ArrayList<AttachedBeam>();
    for (var entry : glyph.effectiveSlots().entrySet()) {
      var slot = glyph.getSlot(entry.getKey());
      if (slot != null)
        beams.addAll(linkBeams(hexComp, slot, entry.getValue()));
    }
    return beams;
  }

  private static final Color WHITE = new Color((byte) 255, (byte) 255, (byte) 255);

  private static final float DEFAULT_LINE_OPACITY = 0.7f;

  public static Color resolvePrimaryColorRaw(GlyphEffects effects) {
    var c = effects.getPrimaryColor();
    return c != null ? c : WHITE;
  }

  public static Vector3f resolvePrimaryColor(GlyphEffects effects) {
    return HexColors.toVector3f(resolvePrimaryColorRaw(effects));
  }

  public static float resolveAlpha(GlyphEffects effects) {
    return effects.getAlphaOrDefault();
  }

  public static boolean playersInRange(Vector3d pos, ComponentAccessor<EntityStore> accessor) {
    var playerSpatialResource = accessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
    var closest = playerSpatialResource.getSpatialStructure().closest(pos);
    if (closest == null)
      return false;
    var transform = accessor.getComponent(closest, TransformComponent.getComponentType());
    if (transform == null)
      return false;
    var range = ParticleUtil.DEFAULT_PARTICLE_DISTANCE;
    return transform.getPosition().distanceSquared(pos) <= range * range;
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

  public static List<Ref<EntityStore>> collectParticleRecipients(Vector3d pos, double radius,
      ComponentAccessor<EntityStore> accessor) {
    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = accessor
        .getResource(EntityModule.get().getPlayerSpatialResourceType());
    List<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
    playerRefs.clear();
    playerSpatialResource.getSpatialStructure().collect(pos, radius, playerRefs);
    return new ArrayList<>(playerRefs);
  }

  public static void spawnConfigured(@Nullable ModelParticle particle, Vector3d pos,
      @Nullable Color tint, float scale, ComponentAccessor<EntityStore> accessor,
      @Nullable List<Ref<EntityStore>> recipients) {
    if (particle == null || particle.getSystemId() == null)
      return;
    particle(particle.getSystemId(), pos, tint != null ? tint : particle.getColor(), scale, accessor, recipients);
  }

  private static void particle(String systemId, Vector3d pos, @Nullable Color tint, float scale,
      ComponentAccessor<EntityStore> accessor, @Nullable List<Ref<EntityStore>> recipients) {
    Color effective = tint;
    if (effective == null) {
      // untinted spawn reaches the engine default distance rather than the 25 block
      // recipient sweep, so only leave that path when scale actually needs the tinted overload
      if (scale == 1.0f) {
        ParticleUtil.spawnParticleEffect(systemId, pos, accessor);
        return;
      }
      effective = WHITE;
    }
    List<Ref<EntityStore>> playerRefs = recipients != null
        ? recipients
        : collectParticleRecipients(pos, 25.0, accessor);
    ParticleUtil.spawnParticleEffect(systemId, pos, 0.0f, 0.0f, 0.0f, scale, effective, playerRefs, accessor);
  }

  public static void spawnConfiguredDirected(@Nullable ModelParticle particle, Vector3d pos,
      Rotation3f rotation, @Nullable Color tint, float scale, ComponentAccessor<EntityStore> accessor,
      @Nullable List<Ref<EntityStore>> recipients) {
    if (particle == null || particle.getSystemId() == null)
      return;
    Color effective = tint != null ? tint : particle.getColor();
    var playerRefs = recipients != null ? recipients : collectParticleRecipients(pos, 25.0, accessor);
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

  public static void line(ComponentAccessor<EntityStore> accessor, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags) {
    line(accessor, start, end, color, thickness, time, flags, DEFAULT_LINE_OPACITY, null);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags, @Nullable Ref<EntityStore> ref) {
    line(accessor, start, end, color, thickness, time, flags, DEFAULT_LINE_OPACITY, ref);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags, float opacity) {
    line(accessor, start, end, color, thickness, time, flags, opacity, null);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, Vector3d start, Vector3d end,
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
      DebugEmitter.add(accessor, DebugShape.Cube, matrix, color, opacity, time, allFlags);
      return;
    }

    PlayerRef playerRef = accessor.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef != null) {
      playerRef.getPacketHandler().write(
          DebugEmitter.packet(DebugShape.Cube, matrix, color, opacity, time, allFlags));
    }
  }

  public static boolean applyEffect(@Nullable HexContext hexContext, @Nullable Ref<EntityStore> target,
      @Nullable String effectId, float duration, OverlapBehavior overlap) {
    return hexContext != null && applyEffect(hexContext.getAccessor(), target, effectId, duration, overlap, null);
  }

  public static boolean applyEffect(ComponentAccessor<EntityStore> accessor, @Nullable Ref<EntityStore> target,
      @Nullable String effectId, float duration, OverlapBehavior overlap, @Nullable Ref<EntityStore> owner) {
    if (target == null || !target.isValid() || effectId == null)
      return false;
    EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
    if (effect == null)
      return false;
    EffectControllerComponent controller = accessor.getComponent(
        target, EffectControllerComponent.getComponentType());
    if (controller == null)
      return false;
    return controller.addEffect(target, effect, duration, overlap, accessor, owner);
  }

  public static boolean applyBoundedEffect(@Nullable HexContext hexContext, @Nullable Ref<EntityStore> target,
      @Nullable Glyph glyph, @Nullable String effectId, float requestedDuration, OverlapBehavior overlap) {
    return applyEffect(hexContext, target, effectId,
        volatilityBoundedDuration(hexContext, glyph, requestedDuration), overlap);
  }

  public static boolean isEffectRemoved(ComponentAccessor<EntityStore> accessor, @Nullable Ref<EntityStore> target,
      @Nullable String effectId) {
    if (effectId == null || target == null || !target.isValid())
      return false;
    var effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
    if (effectIndex == Integer.MIN_VALUE)
      return false;
    var controller = accessor.getComponent(target, EffectControllerComponent.getComponentType());
    return controller != null && !controller.hasEffect(effectIndex);
  }

  public static void removeEffect(ComponentAccessor<EntityStore> accessor, @Nullable Ref<EntityStore> target,
      @Nullable String effectId) {
    if (effectId == null || target == null || !target.isValid())
      return;
    var effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
    if (effectIndex == Integer.MIN_VALUE)
      return;
    var controller = accessor.getComponent(target, EffectControllerComponent.getComponentType());
    if (controller != null)
      controller.removeEffect(target, effectIndex, accessor);
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
