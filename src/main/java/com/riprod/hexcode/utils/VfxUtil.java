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
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
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

  public static Color resolvePrimaryColorRaw(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
    Color c = resolveColor(overrides != null ? overrides.getPrimaryColor() : null,
        glyphStyleColor(glyphAsset, true));
    return c != null ? c : WHITE;
  }

  public static Vector3f resolvePrimaryColor(@Nullable HexContext ctx, @Nullable GlyphAsset glyphAsset) {
    return HexColors.toVector3f(resolvePrimaryColorRaw(ctx, glyphAsset));
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
    int index = SoundEvent.getAssetMap().getIndex(soundId);
    if (index >= 0) {
      SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, pos, accessor);
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
    spawnConfigured(glyphStyle.getPrimaryParticle(), pos, tint, accessor);
    if (glyphStyle.getPrimarySound() != null)
      sound(glyphStyle.getPrimarySound(), pos, accessor);
  }

  public static void spawnPrimaryDirected(@Nullable HexStyleAsset overrides, @Nullable GlyphAsset glyphAsset,
      Vector3d pos, Rotation3f rotation, ComponentAccessor<EntityStore> accessor) {
    HexStyleAsset glyphStyle = glyphAsset != null ? glyphAsset.getStyle() : null;
    if (glyphStyle == null)
      return;
    Color tint = resolveColor(overrides != null ? overrides.getPrimaryColor() : null, glyphStyle.getPrimaryColor());
    spawnConfiguredDirected(glyphStyle.getPrimaryParticle(), pos, rotation, tint, accessor);
    if (glyphStyle.getPrimarySound() != null)
      sound(glyphStyle.getPrimarySound(), pos, accessor);
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
    spawnConfigured(glyphStyle.getSecondaryParticle(), pos, tint, accessor, recipients);
    if (glyphStyle.getSecondarySound() != null)
      sound(glyphStyle.getSecondarySound(), pos, accessor);
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
    spawnConfigured(glyphStyle.getTertiaryParticle(), pos, tint, accessor, recipients);
    if (glyphStyle.getTertiarySound() != null)
      sound(glyphStyle.getTertiarySound(), pos, accessor);
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
    Color tint = resolveColor(
        overrides != null ? overrides.getPrimaryColor() : null,
        glyphStyle != null ? glyphStyle.getPrimaryColor() : null);
    spawnConfiguredDirected(particle, pos, rotation, tint, accessor);
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
      @Nullable Color tint, ComponentAccessor<EntityStore> accessor) {
    spawnConfigured(particle, pos, tint, accessor, null);
  }

  private static void spawnConfigured(@Nullable ModelParticle particle, Vector3d pos,
      @Nullable Color tint, ComponentAccessor<EntityStore> accessor,
      @Nullable List<Ref<EntityStore>> recipients) {
    if (particle == null || particle.getSystemId() == null)
      return;
    Color effective = tint != null ? tint : particle.getColor();
    if (effective == null) {
      ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos, accessor);
      return;
    }
    List<Ref<EntityStore>> playerRefs = recipients != null
        ? recipients
        : collectParticleRecipients(pos, 25.0, accessor);
    ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos, 0.0f, 0.0f, 0.0f, 1.0f, effective, playerRefs,
        accessor);
  }

  private static void spawnConfiguredDirected(@Nullable ModelParticle particle, Vector3d pos,
      Rotation3f rotation, @Nullable Color tint, ComponentAccessor<EntityStore> accessor) {
    if (particle == null || particle.getSystemId() == null)
      return;
    Color effective = tint != null ? tint : particle.getColor();
    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = accessor
        .getResource(EntityModule.get().getPlayerSpatialResourceType());
    List<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
    playerSpatialResource.getSpatialStructure().collect(pos, 25.0, playerRefs);
    if (effective == null) {
      ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos, rotation, playerRefs, accessor);
      return;
    }
    ParticleUtil.spawnParticleEffect(particle.getSystemId(), pos,
        rotation.y, rotation.x, rotation.roll(), 1.0f, effective, playerRefs, accessor);
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
    line(accessor, world, start, end, color, thickness, time, flags, null);
  }

  public static void line(ComponentAccessor<EntityStore> accessor, World world, Vector3d start, Vector3d end,
      Vector3f color,
      double thickness, float time, int flags, @Nullable Ref<EntityStore> ref) {
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
      DebugUtils.add(world, DebugShape.Cube, matrix, color, 0.7f, time, allFlags);
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
          time, (byte) allFlags, null, 0.7f);
      playerRef.getPacketHandler().write(packet);
    }
  }
}
