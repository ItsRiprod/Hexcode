package com.riprod.hexcode.builtin.hexCore.nodes.glyph;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.component.ComponentAccessor;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphStyleUtil;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.builtin.hexCore.scene.CraftingDragHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.utils.LogScopes;

public class GlyphNodeHandler extends BaseGlyphHandler {
  private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CRAFT);
  public static final GlyphNodeHandler INSTANCE = new GlyphNodeHandler();

  @Override
  public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
      Ref<EntityStore> playerRef) {
    HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
        HexcasterCraftingComponent.getComponentType());
    if (craftingComp == null)
      return InteractionState.Failed;

    Ref<EntityStore> headAnchor = CraftingDragHandler.startDrag(accessor, playerRef, node);
    craftingComp.setHeadAnchorRef(accessor, headAnchor);
    craftingComp.setDraggingRef(node);
    return InteractionState.Finished;
  }

  @Override
  public InteractionState tick(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
      Ref<EntityStore> playerRef) {
    HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
        HexcasterCraftingComponent.getComponentType());
    if (craftingComp == null)
      return InteractionState.Failed;

    CraftingDragHandler.updateDrag(accessor, craftingComp.getHeadAnchorRef(), playerRef);
    return InteractionState.Finished;
  }

  @Override
  public InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
      Ref<EntityStore> playerRef) {
    HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
        HexcasterCraftingComponent.getComponentType());
    if (craftingComp == null)
      return InteractionState.Finished;

    PedestalBlockComponent blockComp = PedestalBlockUtil.resolvePedestal(playerRef, accessor);
    if (blockComp == null)
      return InteractionState.Finished;

    HexcodeSessionComponent session = SessionUtils.resolveSession(blockComp, accessor);
    if (session == null)
      return InteractionState.Finished;

    Vector3f dropOffset = lookToHexOffset(accessor, playerRef,
        session.getAnchorNodeRef(), 2.0f);
    Vector3d dropWorldPos = hexOffsetToWorld(accessor,
        session.getAnchorNodeRef(), dropOffset);

    GlyphComponent glyphComp = accessor.getComponent(nodeRef, GlyphComponent.getComponentType());
    if (glyphComp == null)
      return InteractionState.Finished;

    Ref<EntityStore> headAnchorRef = craftingComp.getHeadAnchorRef();
    TransformComponent headTransform = accessor.getComponent(headAnchorRef, TransformComponent.getComponentType());
    if (headTransform == null)
      return InteractionState.Finished;

    Rotation3f playerRotation = headTransform.getRotation();
    TransformComponent nodeTransform = accessor.getComponent(nodeRef, TransformComponent.getComponentType());
    if (nodeTransform == null)
      return InteractionState.Finished;

    nodeTransform.getPosition().set(dropWorldPos);
    nodeTransform.getRotation().set(playerRotation);

    glyphComp.setOffset(dropOffset);
    glyphComp.setRotation(playerRotation);
    clearExpandedIfMatches(accessor, playerRef, nodeRef);
    glyphComp.setSlotsVisible(false);
    return InteractionState.Finished;
  }

  @Override
  public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
      Ref<EntityStore> playerRef) {
    GlyphComponent glyphComp = accessor.getComponent(nodeRef, GlyphComponent.getComponentType());
    if (glyphComp == null)
      return InteractionState.Failed;

    HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
        HexcasterCraftingComponent.getComponentType());

    Ref<EntityStore> previouslyExpanded = craftingComp != null ? craftingComp.getExpandedGlyphRef() : null;
    boolean clickedIsExpanded = previouslyExpanded != null && previouslyExpanded.equals(nodeRef);

    setExpandedGlyph(accessor, craftingComp, clickedIsExpanded ? null : nodeRef, playerRef);

    resetGlyphTransform(accessor, nodeRef, playerRef, glyphComp);
    return InteractionState.Finished;
  }

  private static void setExpandedGlyph(CommandBuffer<EntityStore> accessor,
      HexcasterCraftingComponent craftingComp, Ref<EntityStore> newRef,
      Ref<EntityStore> playerRef) {
    if (craftingComp == null)
      return;

    Ref<EntityStore> previous = craftingComp.getExpandedGlyphRef();
    if (previous != null && previous.isValid() && !previous.equals(newRef)) {
      GlyphComponent prevComp = accessor.getComponent(previous, GlyphComponent.getComponentType());
      if (prevComp != null) {
        SlotNodeHandler.INSTANCE.despawnSlotsForGlyph(accessor, previous);
        prevComp.setSlotsVisible(false);
      }
    }

    craftingComp.setExpandedGlyphRef(newRef);

    if (newRef != null && newRef.isValid()) {
      GlyphComponent newComp = accessor.getComponent(newRef, GlyphComponent.getComponentType());
      if (newComp != null && !newComp.areSlotsVisible()) {
        newComp.setSlotsVisible(true);
        SlotNodeHandler.INSTANCE.spawnSlotsForGlyph(accessor, newRef, playerRef);
      }
    }
  }

  private void resetGlyphTransform(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
      Ref<EntityStore> playerRef, GlyphComponent glyphComp) {
    PedestalBlockComponent blockComp = PedestalBlockUtil.resolvePedestal(playerRef, accessor);
    if (blockComp == null)
      return;
    HexcodeSessionComponent session = SessionUtils.resolveSession(blockComp, accessor);
    if (session == null)
      return;

    HeadRotation headRot = accessor.getComponent(playerRef, HeadRotation.getComponentType());
    if (headRot == null)
      return;

    Vector3d dropWorldPos = hexOffsetToWorld(accessor,
        session.getAnchorNodeRef(), glyphComp.getOffset());
    TransformComponent transform = accessor.getComponent(nodeRef, TransformComponent.getComponentType());
    if (transform == null)
      return;

    transform.getPosition().set(dropWorldPos);
    transform.getRotation().set(headRot.getRotation());
  }

  @Override
  public InteractionState ability(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
      InteractionType inputType, Ref<EntityStore> playerRef) {
    if (inputType != InteractionType.Ability3)
      return InteractionState.Failed;

    GlyphComponent glyphComp = accessor.getComponent(nodeRef, GlyphComponent.getComponentType());
    if (glyphComp == null)
      return InteractionState.Failed;

    Glyph glyph = glyphComp.getGlyph();
    boolean hasAnyLink = glyph.getSlots().values().stream()
        .anyMatch(s -> s.getLinks().length > 0);

    if (hasAnyLink) {
      glyph.clearAllSlots();
      return InteractionState.Finished;
    }

    return deleteGlyph(accessor, nodeRef, playerRef, glyphComp);
  }

  private InteractionState deleteGlyph(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
      Ref<EntityStore> playerRef, GlyphComponent glyphComp) {
    Ref<EntityStore> hexEntityRef = glyphComp.getHexRef();
    HexComponent hexComp = hexEntityRef != null
        ? accessor.getComponent(hexEntityRef, HexComponent.getComponentType())
        : null;

    String glyphId = glyphComp.getId();
    if (hexComp != null) {
      hexComp.getHex().removeGlyph(glyphId);
      hexComp.removeChildGlyph(glyphId);
    }

    clearExpandedIfMatches(accessor, playerRef, nodeRef);
    accessor.tryRemoveEntity(nodeRef, RemoveReason.REMOVE);
    LOGGER.atFine().log("glyph node: deleted glyph %s", glyphId);
    return InteractionState.Finished;
  }

  private static void clearExpandedIfMatches(CommandBuffer<EntityStore> accessor,
      Ref<EntityStore> playerRef, Ref<EntityStore> nodeRef) {
    if (playerRef == null)
      return;
    HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
        HexcasterCraftingComponent.getComponentType());
    if (craftingComp == null)
      return;
    Ref<EntityStore> expanded = craftingComp.getExpandedGlyphRef();
    if (expanded != null && expanded.equals(nodeRef)) {
      craftingComp.setExpandedGlyphRef(null);
      SlotNodeHandler.INSTANCE.despawnSlotsForGlyph(accessor, nodeRef);
    }
  }

  public Ref<EntityStore> spawnNode(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> parentRef,
      Vector3d position, Ref<EntityStore> playerRef, GlyphComponent glyphComp,
      Ref<EntityStore> hexEntityRef) {
    Glyph glyph = glyphComp.getGlyph();
    Rotation3f glyphRot = new Rotation3f(glyph.getRotation().x, glyph.getRotation().y, 0);
    Holder<EntityStore> glyphHolder = CreateGlyph.createGlyphHolder(accessor, glyphComp, position, glyphRot);

    HoverableComponent hoverComp = new HoverableComponent(HoverableType.NODE);
    Message displayName = null;
    try {
      GlyphAsset glyphAsset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
      if (glyphAsset != null) {
        displayName = Message.translation(glyph.displayTitle(accessor))
            .color(GlyphStyleUtil.getQualityColor(glyph.getVolatility(), glyph.getEfficiency()));
        hoverComp.setHintText("description", Message.translation(glyphAsset.getDescription()));
        hoverComp.setHintText("extra", Message.raw("V " + Math.round(glyph.getVolatility() * 100.0) / 100.0
            + " | E " + Math.round(glyph.getEfficiency() * 100.0) / 100.0));
      }
    } catch (Exception e) {
      LOGGER.atWarning().log("glyph node: failed to set hover hints: %s", e.getMessage());
    }

    glyphHolder.addComponent(HoverableComponent.getComponentType(), hoverComp);
    if (displayName != null) {
      glyphHolder.addComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(displayName));
    }
    glyphHolder.addComponent(NodeComponent.getComponentType(),
        new NodeComponent(hexEntityRef, GlyphNodeConfig.TYPE));

    Ref<EntityStore> glyphNodeRef = accessor.addEntity(glyphHolder, AddReason.SPAWN);
    glyphComp.setSelfRef(glyphNodeRef);
    glyphComp.setHexRef(hexEntityRef);
    glyphComp.setParentRef(hexEntityRef);

    HexComponent hexComp = accessor.getComponent(hexEntityRef, HexComponent.getComponentType());
    if (hexComp != null) {
      hexComp.addChildGlyphRef(glyph.getId(), glyphNodeRef);
    }

    return glyphNodeRef;
  }

  @Override
  public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
    GlyphComponent glyphComp = accessor.getComponent(nodeRef, GlyphComponent.getComponentType());
    if (glyphComp == null)
      return;

    Ref<EntityStore> hexEntityRef = glyphComp.getHexRef();
    if (hexEntityRef != null) {
      HexComponent hexComp = accessor.getComponent(hexEntityRef, HexComponent.getComponentType());
      if (hexComp != null) {
        hexComp.getHex().removeGlyph(glyphComp.getId());
        hexComp.removeChildGlyph(glyphComp.getId());
      }
    }

    clearExpandedIfMatches(accessor, playerRef, nodeRef);
    accessor.tryRemoveEntity(nodeRef, RemoveReason.REMOVE);
  }

  private static Vector3f lookToHexOffset(ComponentAccessor<EntityStore> accessor,
      Ref<EntityStore> playerRef, Ref<EntityStore> hexRootRef, float distance) {
    Transform look = TargetUtil.getLook(playerRef, accessor);
    Vector3d rayStart = look.getPosition();
    Vector3d rayDir = look.getDirection();
    Vector3d worldPoint = new Vector3d(
        rayStart.x + rayDir.x * distance,
        rayStart.y + rayDir.y * distance,
        rayStart.z + rayDir.z * distance);
    TransformComponent rootTransform = accessor.getComponent(hexRootRef, TransformComponent.getComponentType());
    Vector3d rootPos = rootTransform.getPosition();
    return new Vector3f(
        (float) (worldPoint.x - rootPos.x),
        (float) (worldPoint.y - rootPos.y),
        (float) (worldPoint.z - rootPos.z));
  }

  private static Vector3d hexOffsetToWorld(ComponentAccessor<EntityStore> accessor,
      Ref<EntityStore> hexRootRef, Vector3f offset) {
    TransformComponent rootTransform = accessor.getComponent(hexRootRef, TransformComponent.getComponentType());
    Vector3d rootPos = rootTransform.getPosition();
    return new Vector3d(rootPos.x + offset.x, rootPos.y + offset.y, rootPos.z + offset.z);
  }

}
