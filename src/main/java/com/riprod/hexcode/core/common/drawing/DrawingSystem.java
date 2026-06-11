package com.riprod.hexcode.core.common.drawing;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.component.HexcasterDrawingComponent;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.drawing.system.ShapeTemplateStore;
import com.riprod.hexcode.core.common.drawing.system.shapes.DollarOneFixedDetector;
import com.riprod.hexcode.core.common.drawing.system.shapes.ShapeDetector;
import com.riprod.hexcode.core.common.drawing.utils.ShapeComparator;
import com.riprod.hexcode.core.common.drawing.utils.StrokeCapture;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.component.NodeComponent;
import com.riprod.hexcode.core.state.crafting.entity.PedestalEntity;
import com.riprod.hexcode.core.state.crafting.handlers.node.Glyph.GlyphNodeHandler;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.state.HexcodeManager;
import com.riprod.hexcode.utils.VfxUtil;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DrawingSystem extends HexcodeManager {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private static ShapeDetector shapeDetector = new DollarOneFixedDetector();

  public static ShapeDetector getShapeDetector() {
    return shapeDetector;
  }

  public static void setShapeDetector(ShapeDetector detector) {
    shapeDetector = detector;
  }

  @Override
  public void firstTick(Ref<EntityStore> ref, HexcasterComponent hexcaster,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
      HexState previousState) {
    HexcasterDrawingComponent drawingComponent = new HexcasterDrawingComponent();
    buffer.putComponent(ref, HexcasterDrawingComponent.getComponentType(), drawingComponent);
  }

  @Override
  public void lastTick(Ref<EntityStore> ref, HexcasterComponent comp,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
      HexState nextState) {

    HexcasterDrawingComponent drawingComp = buffer.getComponent(ref, HexcasterDrawingComponent.getComponentType());
    PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(ref, buffer);
    if (pedestal == null) {
      return;
    }
    HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);

    List<DrawnShapeComponent> drawnShapes = drawingComp.getDrawnGlyphs();
    if (drawnShapes != null && !drawnShapes.isEmpty()) {
      GlyphCreationManager.NormalizeShapeSizes(drawnShapes);

      GlyphAsset matchedGlyph = GlyphCreationManager.MatchGlyph(drawnShapes);

      if (matchedGlyph == null) {
        LOGGER.atInfo().log("no matching glyph found for drawn shape");
      } else {
        float efficiency = ShapeComparator.calculateEfficiency(drawnShapes);
        float volatility = ShapeComparator.calculateVolatility(drawnShapes);
        LOGGER.atInfo().log("Efficiency: %f | Volatility: %f", efficiency, volatility);

        Glyph glyph = new Glyph(matchedGlyph, volatility, efficiency);

        if (pedestal != null) {
          ObeliskDispatcher.dispatchGlyphDrawn(buffer, pedestal, ref, glyph);
        }
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
            .dispatch((new GlyphDrawnEvent(ref, glyph, drawnShapes, matchedGlyph)));

        HeadRotation hRotation = buffer.getComponent(ref, HeadRotation.getComponentType());
        Vector3d spawnPos = calculateDrawCenter(drawnShapes);
        Transform transform = new Transform(spawnPos, hRotation.getRotation());

        if (spawnPos == null) {
          LOGGER.atInfo().log("cannot spawn drawn hex: missing pedestal or draw position");
        } else {
          Vector3d anchorPos = PedestalEntity.getAnchorPosition(session.getPedestalLocation());
          double maxRadius = pedestal.getMaxRadius();
          double distSq = spawnPos.distanceSquared(anchorPos);

          if (distSq > maxRadius * maxRadius) {
            LOGGER.atInfo().log("drawn hex outside pedestal radius");
            HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.DENIED_OUT_OF_RANGE, ref)
                    .pedestal(pedestal)
                    .message("Glyph drawn outside the pedestal's range.")
                    .build());
          } else {
            spawnDrawnGlyph(buffer, glyph, session, transform, ref);
          }
        }
      }
    }

    Ref<EntityStore> trailRef = drawingComp.getTrailRef();
    if (trailRef != null && trailRef.isValid()) {
      buffer.removeEntity(trailRef, RemoveReason.REMOVE);
    }

    drawingComp.clearDrawingState();
    drawingComp.clear(buffer);
  }

  public void tick0(Ref<EntityStore> ref, HexcasterComponent comp, float dt,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {

    HexcasterDrawingComponent drawingComp = buffer.getComponent(ref, HexcasterDrawingComponent.getComponentType());
    if (drawingComp == null) {
      return;
    }
    InterfaceManager.createIndicator(buffer, ref, drawingComp);
  }

  public void onPlayerJoin(Ref<EntityStore> playerRef, HexcasterComponent comp,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
  }

  public void onPlayerLeave(Ref<EntityStore> ref, HexcasterComponent comp,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    HexcasterDrawingComponent drawingComp = buffer.getComponent(ref, HexcasterDrawingComponent.getComponentType());
    if (drawingComp == null)
      return;
    drawingComp.clear(buffer);
  }

  public InteractionState enterInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
      HexcasterComponent comp) {

    HexcasterDrawingComponent drawingComp = accessor.getComponent(ref, HexcasterDrawingComponent.getComponentType());

    HeadRotation head = accessor.getComponent(ref, HeadRotation.getComponentType());
    if (head == null)
      return InteractionState.Failed;

    TimeResource timeResource = accessor.getResource(TimeResource.getResourceType());
    Long curTime = timeResource.getNow().toEpochMilli();
    drawingComp.setDrawStartTimeMillis(curTime);

    InterfaceManager.spawnTrails(accessor, ref, head);
    return InteractionState.NotFinished;
  }

  public InteractionState tickInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref, float dt,
      HexcasterComponent comp) {

    HexcasterDrawingComponent drawingComp = accessor.getComponent(ref, HexcasterDrawingComponent.getComponentType());
    if (drawingComp == null) {
      return InteractionState.Failed;
    }
    FloatArrayList points = drawingComp.getDrawnStrokes();
    if (points == null)
      return InteractionState.Finished;

    HeadRotation head = accessor.getComponent(ref, HeadRotation.getComponentType());
    if (head == null)
      return InteractionState.Failed;

    if (!StrokeCapture.appendHeadSample(points, head)) {
      return InteractionState.NotFinished;
    }

    InterfaceManager.positionTrail(accessor, ref, head);
    return InteractionState.NotFinished;
  }

  public InteractionState exitInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
      HexcasterComponent comp) {

    HexcasterDrawingComponent drawingComp = accessor.getComponent(ref, HexcasterDrawingComponent.getComponentType());
    if (drawingComp == null) {
      return InteractionState.Failed;
    }

    FloatArrayList points = drawingComp.getDrawnStrokes();
    if (points == null || points.size() < 3) {
      drawingComp.clearStrokes();
      return InteractionState.Finished;
    }

    String trainingId = comp.consumeTrainingShapeId();
    if (trainingId != null) {
      String overridePack = comp.consumeTrainingPackOverride();
      ShapeTemplateStore.Result result = ShapeTemplateStore.saveTemplate(trainingId, points, overridePack);
      if (result.success) {
        shapeDetector.clearCache();
        LOGGER.atInfo().log("recorded training template for '%s' (%d points) into pack '%s'",
            trainingId, points.size() / 2, result.packName);
      } else {
        LOGGER.atWarning().log("training template for '%s' failed: %s", trainingId, result.error);
      }
      InterfaceManager.removeTrails(accessor, ref);
      drawingComp.clearStrokes();
      return InteractionState.Finished;
    }

    TimeResource timeResource = accessor.getResource(TimeResource.getResourceType());
    long curTime = timeResource.getNow().toEpochMilli();
    long drawDuration = curTime - drawingComp.getDrawStartTimeMillis();

    DrawnShapeComponent result = StrokeCapture.recognizeStroke(accessor, ref, points, shapeDetector, drawDuration);
    InterfaceManager.removeTrails(accessor, ref);

    if (result == null) {
      drawingComp.clearStrokes();
      return InteractionState.Finished;
    }

    LOGGER.atInfo().log("%d ms (%f score) | S: %f | A: %f", drawDuration,
        result.getEfficiency(), result.getSize(), result.getVolatility());

    drawingComp.addDrawnGlyph(result);
    drawingComp.clearStrokes();
    drawingComp.setLastParticleSpawnMillis(0L);
    InterfaceManager.createIndicator(accessor, ref, drawingComp);

    return InteractionState.Finished;
  }

  private static Vector3d calculateDrawCenter(List<DrawnShapeComponent> drawnShapes) {
    double x = 0, y = 0, z = 0;
    int count = 0;
    for (DrawnShapeComponent shape : drawnShapes) {
      List<Vector3d> points = shape.getPoints();
      if (points == null) {
        continue;
      }
      for (Vector3d p : points) {
        x += p.x;
        y += p.y;
        z += p.z;
        count++;
      }
    }
    if (count == 0) {
      return null;
    }
    return new Vector3d(x / count, y / count, z / count);
  }

  private static void spawnDrawnGlyph(CommandBuffer<EntityStore> accessor, Glyph glyph,
      HexcodeSessionComponent session, Transform spawnPos, Ref<EntityStore> playerRef) {

    Ref<EntityStore> anchorRef = session.getAnchorNodeRef();
    if (anchorRef == null || !anchorRef.isValid()) {
      LOGGER.atWarning().log("cannot spawn drawn glyph: no active anchor entity ref on pedestal");
      HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
          .dispatch(CraftingEvent.builder(CraftingEvent.Reason.ERROR_INVALID_HEX, playerRef)
              .pedestalLocation(session.getPedestalLocation())
              .message("Pedestal has no active slot to attach this glyph to.")
              .build());
      return;
    }
    NodeComponent nodeComp = accessor.getComponent(anchorRef, NodeComponent.getComponentType());
    if (nodeComp == null) {
      LOGGER.atWarning().log("cannot spawn drawn glyph: no node component on anchor entity");
      return;
    }
    Ref<EntityStore> hexRef = nodeComp.getParentEntity();
    if (hexRef == null || !hexRef.isValid()) {
      LOGGER.atWarning().log("cannot spawn drawn glyph: no hex entity associated with anchor");
      return;
    }

    HexComponent hexComp = accessor.getComponent(hexRef, HexComponent.getComponentType());

    if (hexComp == null) {
      LOGGER.atWarning().log("cannot spawn drawn glyph: no hex component on hex entity");
      return;
    }

    GlyphComponent glyphComponent = new GlyphComponent(glyph);

    Rotation3f rotation = spawnPos.getRotation();
    glyphComponent.setRotation(rotation);
    glyph.setRotation(rotation);

    Vector3d worldPos = spawnPos.getPosition();

    TransformComponent hexTransform = accessor.getComponent(hexRef, TransformComponent.getComponentType());
    if (hexTransform != null) {
      Vector3d hexPos = hexTransform.getPosition();
      glyph.setPosition(new Vector3f(
          (float) (worldPos.x - hexPos.x),
          (float) (worldPos.y - hexPos.y),
          (float) (worldPos.z - hexPos.z)));
    }

    Ref<EntityStore> effectRef = GlyphNodeHandler.INSTANCE.spawnNode(accessor, hexRef, worldPos, playerRef,
        glyphComponent, hexRef);

    VfxUtil.sound("SFX_Eye_Void_Attack_Summon", worldPos, accessor);

    hexComp.addChildGlyphRef(glyph.getId(), effectRef);
    hexComp.getHex().put(glyph.getId(), glyph);
  }
}
