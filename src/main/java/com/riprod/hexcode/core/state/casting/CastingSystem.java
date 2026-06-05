package com.riprod.hexcode.core.state.casting;

import java.util.List;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.drawing.utils.ShapeComparator;
import com.riprod.hexcode.core.common.drawing.utils.StrokeCapture;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;
import com.riprod.hexcode.core.state.casting.component.HexcasterCastingComponent;
import com.riprod.hexcode.core.state.casting.component.HexcasterCastingComponent.DraftSubState;
import com.riprod.hexcode.core.state.casting.utils.DraftFeedback;
import com.riprod.hexcode.core.state.casting.utils.GlyphPositioner;
import com.riprod.hexcode.core.state.casting.utils.GlyphStyler;
import com.riprod.hexcode.core.state.casting.utils.HexSelector;
import com.riprod.hexcode.core.state.casting.utils.HexSpawner;
import com.riprod.hexcode.core.state.casting.utils.InAirHexFactory;
import com.riprod.hexcode.core.state.casting.utils.RootSpawner;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.state.HexcodeManager;
import com.riprod.hexcode.utils.CleanupUtils;
import com.riprod.hexcode.utils.GlyphMath;
import com.riprod.hexcode.utils.HexSlot;
import com.riprod.hexcode.utils.LatencyUtil;

public class CastingSystem extends HexcodeManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float FINALIZE_BASE_SECONDS = 0.35f;
    private static final float FINALIZE_PING_FACTOR = 2.0f;
    private static final float FINALIZE_MAX_SECONDS = 1.5f;

    @Override
    public void firstTick(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
            HexState previousState) {
        HexcasterCastingComponent castingComp = buffer.getComponent(ref, HexcasterCastingComponent.getComponentType());
        if (castingComp == null) {
            castingComp = new HexcasterCastingComponent();
            buffer.addComponent(ref, HexcasterCastingComponent.getComponentType(), castingComp);
        }
        castingComp.setDraftSubState(DraftSubState.Idle);
        castingComp.clearCurrentStroke();
        castingComp.clearPendingShapes();
        castingComp.setFinalizeTimer(0f);
        castingComp.setStrokeStartMillis(0L);

        HexStaffComponent staff = CasterInventory.getHexStaffComponent(buffer, ref);

        if (staff == null) {
            LOGGER.atSevere().log("Player is missing required HexStaffComponent to enter casting mode");
            comp.requestStateChange(HexState.IDLE);
            return;
        }

        List<Hex> hexes = CasterInventory.getHexesForCasting(buffer, ref);
        String style = staff.getStyleId();

        ItemStack mainHand = InventoryComponent.getItemInHand(buffer, ref);
        ItemStack offHand = PlayerUtils.getHandItem(buffer, ref, HexSlot.OffHand);
        HexStaffAsset staffAsset = CasterInventory.getHexStaffAsset(mainHand);
        HexBookAsset bookAsset = CasterInventory.getHexBookAsset(offHand);
        ModelParticle[] staffParticles = staffAsset != null ? staffAsset.getCastingAuraParticles() : null;
        ModelParticle[] bookParticles = bookAsset != null ? bookAsset.getCastingAuraParticles() : null;
        ModelParticle[] particles = mergeParticles(staffParticles, bookParticles);

        float eyeHeight = 0f;
        ModelComponent modelComp = buffer.getComponent(ref, ModelComponent.getComponentType());
        if (modelComp != null && modelComp.getModel() != null) {
            eyeHeight = modelComp.getModel().getEyeHeight(ref, buffer);
        }
        Ref<EntityStore> castingRootRef = RootSpawner.createCastingRoot(buffer, ref, eyeHeight, particles);
        castingComp.setCastingRootRef(castingRootRef);

        List<Ref<EntityStore>> spawnedHexes = HexSpawner.spawnHexes(buffer, ref, castingRootRef, hexes, style);
        castingComp.setActiveHexes(spawnedHexes);
    }

    @Override
    public void lastTick(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
            HexState nextState) {
        HexcasterCastingComponent castingComp = buffer.getComponent(ref, HexcasterCastingComponent.getComponentType());
        if (castingComp == null) {
            return;
        }
        HexcasterIdleComponent execComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());

        cleanupEntities(buffer, castingComp);

        HexStaffComponent staff = CasterInventory.getHexStaffComponent(buffer, ref);
        HexComponent rootGlyph = castingComp.getLastHoveredHex();

        boolean usedDraft = staff != null
                && tryFinalizeDraftAsActiveHex(buffer, ref, castingComp, execComp, staff);
        if (!usedDraft && rootGlyph != null && staff != null) {
            execComp.resetCastState();
            execComp.setActiveHex(rootGlyph.getHex());
            CasterInventory.saveHexStaffComponent(buffer, ref, staff);
        }

        castingComp.clearCastingState();
        castingComp.clear(buffer);
    }

    private boolean tryFinalizeDraftAsActiveHex(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp, HexcasterIdleComponent execComp, HexStaffComponent staff) {
        DraftSubState sub = castingComp.getDraftSubState();
        if (sub == DraftSubState.Idle) {
            return false;
        }

        if (sub == DraftSubState.Drawing) {
            long durationMs = nowMillis(buffer) - castingComp.getStrokeStartMillis();
            DrawnShapeComponent shape = StrokeCapture.recognizeStroke(buffer, ref,
                    castingComp.getCurrentStrokePoints(), null, durationMs);
            if (shape != null) {
                castingComp.addPendingShape(shape);
            }
            castingComp.clearCurrentStroke();
        }

        if (castingComp.getPendingShapes().isEmpty()) {
            return false;
        }

        try {
            GlyphCreationManager.NormalizeShapeSizes(castingComp.getPendingShapes());
            GlyphAsset matched = GlyphCreationManager.MatchGlyph(castingComp.getPendingShapes());
            if (matched == null) {
                DraftFeedback.playFailFeedback(buffer, ref);
                return false;
            }

            float efficiency = ShapeComparator.calculateEfficiency(castingComp.getPendingShapes());
            float volatility = ShapeComparator.calculateVolatility(castingComp.getPendingShapes());
            Hex hex = InAirHexFactory.wrap(matched, volatility, efficiency);
            if (hex == null) {
                return false;
            }

            emitGlyphDrawn(ref, matched, volatility, efficiency, castingComp.getPendingShapes());

            execComp.resetCastState();
            execComp.setActiveHex(hex);
            CasterInventory.saveHexStaffComponent(buffer, ref, staff);
            return true;
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Failed to finalize draft on casting exit; falling back to hovered hex");
            return false;
        }
    }

    @Override
    public void tick0(Ref<EntityStore> ref, HexcasterComponent comp, float dt,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {

        HexcasterCastingComponent castingComp = buffer.getComponent(ref, HexcasterCastingComponent.getComponentType());
        if (castingComp == null) {
            comp.requestStateChange(HexState.IDLE);
            return;
        }

        Ref<EntityStore> headAnchor = castingComp.getHeadAnchorRef();
        if (castingComp.getDraggingHex() == null && headAnchor != null && headAnchor.isValid()) {
            buffer.tryRemoveComponent(headAnchor, MountedComponent.getComponentType());
            buffer.tryRemoveEntity(headAnchor, RemoveReason.REMOVE);
            castingComp.setHeadAnchorRef(null);
        }

        Ref<EntityStore> castingRootRef = castingComp.getCastingRootRef();

        TransformComponent transform = buffer.getComponent(ref, TransformComponent.getComponentType());
        HeadRotation headRotation = buffer.getComponent(ref, HeadRotation.getComponentType());
        if (transform == null || headRotation == null) {
            return;
        }
        Vector3d ownerPos = transform.getPosition();

        List<Ref<EntityStore>> activeHexes = castingComp.getActiveHexes();

        if (activeHexes == null || castingRootRef == null || !castingRootRef.isValid()) {
            return;
        }

        GlyphPositioner.PositionGlyphs(buffer, ref, ownerPos, castingRootRef);

        HexComponent hoveredHex = HexSelector.findHoveredHex(buffer, headRotation.getRotation(), activeHexes);

        GlyphStyler.hoverHex(buffer, hoveredHex, castingComp);

        tickDraftFinalize(buffer, ref, castingComp, dt);
    }

    @Override
    public void onPlayerJoin(Ref<EntityStore> playerRef, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onPlayerLeave(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        HexcasterCastingComponent castingComp = buffer.getComponent(ref, HexcasterCastingComponent.getComponentType());
        if (castingComp == null)
            return;

        CleanupUtils.safeRemoveEntity(buffer, castingComp.getHeadAnchorRef());
        CleanupUtils.safeRemoveEntity(buffer, castingComp.getDrawTrailRef());

        List<Ref<EntityStore>> activeHexes = castingComp.getActiveHexes();
        if (activeHexes != null) {
            for (Ref<EntityStore> hexRef : activeHexes) {
                if (hexRef == null || !hexRef.isValid())
                    continue;
                HexComponent hexComp = buffer.getComponent(hexRef, HexComponent.getComponentType());
                if (hexComp != null) {
                    CleanupUtils.safeRemoveEntities(buffer, hexComp.getChildGlyphRefsList());
                }
                CleanupUtils.safeRemoveEntity(buffer, hexRef);
            }
        }

        CleanupUtils.safeRemoveEntity(buffer, castingComp.getCastingRootRef());
    }

    @Override
    public InteractionState enterAbility(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterComponent comp, InteractionType inputType) {
        if (inputType != InteractionType.Ability2) {
            return InteractionState.Finished;
        }
        HexcasterCastingComponent castingComp = buffer.getComponent(ref,
                HexcasterCastingComponent.getComponentType());
        if (castingComp == null || castingComp.getDraftSubState() == DraftSubState.Idle) {
            return InteractionState.Finished;
        }
        // close an in-flight stroke first so its shape is captured, then commit immediately
        if (castingComp.getDraftSubState() == DraftSubState.Drawing) {
            endDraftStroke(buffer, ref, castingComp);
        }
        finalizeNow(buffer, ref, castingComp);
        return InteractionState.Finished;
    }

    @Override
    public InteractionState enterInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterComponent comp) {

        HexcasterCastingComponent castingComp = accessor.getComponent(ref,
                HexcasterCastingComponent.getComponentType());
        if (castingComp == null) {
            return InteractionState.Failed;
        }

        HexComponent hoveredHex = castingComp.getHoveredHex();
        if (hoveredHex == null) {
            return beginDraftStroke(accessor, ref, castingComp);
        }

        float eyeHeight = 0f;
        ModelComponent modelComp = accessor.getComponent(ref, ModelComponent.getComponentType());
        if (modelComp != null && modelComp.getModel() != null) {
            eyeHeight = modelComp.getModel().getEyeHeight(ref, accessor);
        }

        Ref<EntityStore> oldHeadAnchor = castingComp.getHeadAnchorRef();
        if (oldHeadAnchor != null && oldHeadAnchor.isValid()) {
            accessor.tryRemoveComponent(oldHeadAnchor, MountedComponent.getComponentType());
            accessor.tryRemoveEntity(oldHeadAnchor, RemoveReason.REMOVE);
        }

        Ref<EntityStore> headRootRef = CreateGlyph.createHeadAnchor(accessor, ref, eyeHeight);

        castingComp.setHeadAnchorRef(headRootRef);

        GlyphStyler.hoverHex(accessor, hoveredHex, castingComp);

        Ref<EntityStore> glyphRef = hoveredHex.getSelfRef();
        if (glyphRef != null && glyphRef.isValid()) {
            accessor.tryRemoveComponent(glyphRef, MountedComponent.getComponentType());

            float distance = hoveredHex.getDistance();
            accessor.addComponent(glyphRef, MountedComponent.getComponentType(),
                    new MountedComponent(headRootRef, new Rotation3f(0, 0, -distance), MountController.Minecart));
        }

        castingComp.setDraggingHex(hoveredHex);

        return InteractionState.NotFinished;
    }

    @Override
    public InteractionState tickInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref, float dt,
            HexcasterComponent comp) {

        HexcasterCastingComponent castingComp = accessor.getComponent(ref,
                HexcasterCastingComponent.getComponentType());
        if (castingComp == null) {
            return InteractionState.Finished;
        }

        if (castingComp.getDraftSubState() == DraftSubState.Drawing) {
            return tickDraftStroke(accessor, ref, castingComp, dt);
        }

        if (castingComp.getDraggingHex() == null) {
            return InteractionState.Finished;
        }

        Ref<EntityStore> headAnchor = castingComp.getHeadAnchorRef();
        if (headAnchor != null && headAnchor.isValid()) {
            HeadRotation headRot = accessor.getComponent(ref, HeadRotation.getComponentType());
            if (headRot != null) {
                TransformComponent headTransform = accessor.getComponent(headAnchor,
                        TransformComponent.getComponentType());
                headTransform.getRotation().set(headRot.getRotation().x, headRot.getRotation().y, 0f);
            }
        }

        HexSelector.DragGlyph(accessor, ref, castingComp.getDraggingHex());

        HeadRotation headRot2 = accessor.getComponent(ref, HeadRotation.getComponentType());
        if (headRot2 != null) {
            HexComponent targetHex = castingComp.getHoveredHex();
            GlyphComponent targetGlyph = null;
            if (targetHex != null && targetHex != castingComp.getDraggingHex()) {
                targetGlyph = HexSelector.findHoveredGlyph(accessor, headRot2.getRotation(), targetHex);
                if (targetGlyph != null) {
                    GlyphComponent outputChild = HexSelector.findOutputChild(accessor, targetHex, targetGlyph);
                    if (outputChild != null) {
                        targetGlyph = outputChild;
                    }
                }
            }
            GlyphStyler.hoverGlyph(accessor, targetGlyph, castingComp);
        }

        return InteractionState.NotFinished;
    }

    @Override
    public InteractionState exitInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterComponent comp) {

        HexcasterCastingComponent castingComp = accessor.getComponent(ref,
                HexcasterCastingComponent.getComponentType());
        if (castingComp == null) {
            return InteractionState.Finished;
        }

        if (castingComp.getDraftSubState() == DraftSubState.Drawing) {
            return endDraftStroke(accessor, ref, castingComp);
        }

        HexComponent draggedHex = castingComp.getDraggingHex();
        if (draggedHex == null) {
            return InteractionState.Finished;
        }

        GlyphComponent hoveredGlyph = castingComp.getHoveredGlyph();
        if (hoveredGlyph != null) {
            try {
                float eyeHeight = 0f;
                ModelComponent modelComp = accessor.getComponent(ref, ModelComponent.getComponentType());
                eyeHeight = modelComp.getModel().getEyeHeight(ref, accessor);
                HexSpawner.MergeGlyphs(accessor, hoveredGlyph, draggedHex, eyeHeight);
                castingComp.getActiveHexes().remove(draggedHex.getSelfRef());
                castingComp.setDraggingHex(null);
                Ref<EntityStore> mergeHeadAnchor = castingComp.getHeadAnchorRef();
                if (mergeHeadAnchor != null && mergeHeadAnchor.isValid()) {
                    accessor.tryRemoveComponent(mergeHeadAnchor, MountedComponent.getComponentType());
                    accessor.tryRemoveEntity(mergeHeadAnchor, RemoveReason.REMOVE);
                }
                castingComp.setHeadAnchorRef(null);
                GlyphStyler.hoverGlyph(accessor, null, castingComp);
                return InteractionState.Finished;
            } catch (Exception e) {
                LOGGER.atWarning().withCause(e).log("Error merging glyphs, dropping on ground instead");
            }
        }

        castingComp.setDraggingHex(null);
        Ref<EntityStore> dropHeadAnchor = castingComp.getHeadAnchorRef();
        if (dropHeadAnchor != null && dropHeadAnchor.isValid()) {
            accessor.tryRemoveComponent(dropHeadAnchor, MountedComponent.getComponentType());
            accessor.tryRemoveEntity(dropHeadAnchor, RemoveReason.REMOVE);
        }
        castingComp.setHeadAnchorRef(null);

        GlyphStyler.hoverHex(accessor, null, castingComp);
        GlyphStyler.hoverGlyph(accessor, null, castingComp);

        Rotation3f dhr = draggedHex.getRotation();
        Vector3d dropPos = GlyphMath.sphericalToCartesian(dhr);
        Vector3f dropOffset = new Vector3f((float) dropPos.x, (float) dropPos.y, (float) dropPos.z);
        draggedHex.setOffset(dropOffset);

        Vector3f doff = draggedHex.getOffset();
        accessor.putComponent(draggedHex.getSelfRef(), MountedComponent.getComponentType(),
                new MountedComponent(draggedHex.getRootRef(),
                        new Rotation3f(doff.x, doff.y, doff.z),
                        MountController.Minecart));

        return InteractionState.Finished;
    }

    private InteractionState beginDraftStroke(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp) {
        HeadRotation head = accessor.getComponent(ref, HeadRotation.getComponentType());
        if (head == null) {
            return InteractionState.Failed;
        }

        Ref<EntityStore> oldTrail = castingComp.getDrawTrailRef();
        if (oldTrail != null && oldTrail.isValid()) {
            InterfaceManager.removeTrailEntity(accessor, oldTrail);
            castingComp.setDrawTrailRef(null);
        }

        castingComp.clearCurrentStroke();
        castingComp.setFinalizeTimer(0f);
        castingComp.setStrokeStartMillis(nowMillis(accessor));
        castingComp.setDraftSubState(DraftSubState.Drawing);

        Ref<EntityStore> trailRef = InterfaceManager.spawnTrailEntity(accessor, ref, head);
        castingComp.setDrawTrailRef(trailRef);

        return InteractionState.NotFinished;
    }

    private InteractionState tickDraftStroke(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp, float dt) {
        HeadRotation head = accessor.getComponent(ref, HeadRotation.getComponentType());
        if (head == null) {
            return InteractionState.Failed;
        }
        StrokeCapture.appendHeadSample(castingComp.getCurrentStrokePoints(), head);
        InterfaceManager.positionTrailEntity(accessor, ref, castingComp.getDrawTrailRef(), head);
        return InteractionState.NotFinished;
    }

    private InteractionState endDraftStroke(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp) {
        Ref<EntityStore> trailRef = castingComp.getDrawTrailRef();
        if (trailRef != null) {
            InterfaceManager.removeTrailEntity(accessor, trailRef);
            castingComp.setDrawTrailRef(null);
        }

        long drawDuration = nowMillis(accessor) - castingComp.getStrokeStartMillis();

        DrawnShapeComponent shape = StrokeCapture.recognizeStroke(accessor, ref,
                castingComp.getCurrentStrokePoints(), null, drawDuration);
        castingComp.clearCurrentStroke();

        if (shape == null) {
            if (castingComp.getPendingShapes().isEmpty()) {
                castingComp.setDraftSubState(DraftSubState.Idle);
            } else {
                openFinalizeWindow(accessor, ref, castingComp);
            }
            return InteractionState.Finished;
        }

        castingComp.addPendingShape(shape);
        openFinalizeWindow(accessor, ref, castingComp);
        return InteractionState.Finished;
    }

    // snapshot a ping-scaled finalize window once so jitter can't wobble the auto-commit timer
    private void openFinalizeWindow(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp) {
        float pingS = LatencyUtil.pingMillis(accessor, ref) / 1000f;
        float delay = Math.min(FINALIZE_MAX_SECONDS, FINALIZE_BASE_SECONDS + pingS * FINALIZE_PING_FACTOR);
        castingComp.setFinalizeDelaySeconds(delay);
        castingComp.setFinalizeTimer(0f);
        castingComp.setDraftSubState(DraftSubState.AwaitingFinalize);
    }

    private static long nowMillis(CommandBuffer<EntityStore> buffer) {
        return buffer.getResource(TimeResource.getResourceType()).getNow().toEpochMilli();
    }

    private void tickDraftFinalize(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp, float dt) {
        if (castingComp.getDraftSubState() != DraftSubState.AwaitingFinalize) {
            return;
        }
        float timer = castingComp.getFinalizeTimer() + dt;
        if (timer < castingComp.getFinalizeDelaySeconds()) {
            castingComp.setFinalizeTimer(timer);
            return;
        }

        finalizeNow(buffer, ref, castingComp);
    }

    // match pending shapes into an in-air hex; shared by the auto-timer and the Ability2 force-commit
    private void finalizeNow(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp) {
        List<DrawnShapeComponent> pending = castingComp.getPendingShapes();
        if (pending.isEmpty()) {
            castingComp.setDraftSubState(DraftSubState.Idle);
            castingComp.setFinalizeTimer(0f);
            return;
        }

        try {
            GlyphCreationManager.NormalizeShapeSizes(pending);
            GlyphAsset matched = GlyphCreationManager.MatchGlyph(pending);
            if (matched == null) {
                DraftFeedback.playFailFeedback(buffer, ref);
            } else {
                spawnInAirHex(buffer, ref, castingComp, matched, pending);
            }
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Failed to finalize in-air glyph; treating as fizzle");
            DraftFeedback.playFailFeedback(buffer, ref);
        } finally {
            castingComp.clearPendingShapes();
            castingComp.setFinalizeTimer(0f);
            castingComp.setDraftSubState(DraftSubState.Idle);
        }
    }

    private void spawnInAirHex(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterCastingComponent castingComp, GlyphAsset matched, List<DrawnShapeComponent> pending) {
        HexStaffComponent staff = CasterInventory.getHexStaffComponent(buffer, ref);
        if (staff == null) {
            DraftFeedback.playFailFeedback(buffer, ref);
            return;
        }
        Ref<EntityStore> castingRootRef = castingComp.getCastingRootRef();
        if (castingRootRef == null || !castingRootRef.isValid()) {
            return;
        }

        float efficiency = ShapeComparator.calculateEfficiency(pending);
        float volatility = ShapeComparator.calculateVolatility(pending);
        Hex hex = InAirHexFactory.wrap(matched, volatility, efficiency);
        if (hex == null) {
            DraftFeedback.playFailFeedback(buffer, ref);
            return;
        }

        emitGlyphDrawn(ref, matched, volatility, efficiency, pending);

        List<Ref<EntityStore>> activeHexes = castingComp.getActiveHexes();
        Ref<EntityStore> hexRef = HexSpawner.spawnSingleHex(buffer, ref, castingRootRef, hex);
        if (hexRef == null) {
            DraftFeedback.playFailFeedback(buffer, ref);
            return;
        }
        activeHexes.add(hexRef);
    }

    private static void emitGlyphDrawn(Ref<EntityStore> ref, GlyphAsset matched,
            float volatility, float efficiency, List<DrawnShapeComponent> shapes) {
        Glyph glyph = new Glyph(matched, volatility, efficiency);
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
                .dispatch(new GlyphDrawnEvent(ref, glyph, shapes, matched));
    }

    private static ModelParticle[] mergeParticles(ModelParticle[] a, ModelParticle[] b) {
        if (a == null || a.length == 0)
            return b;
        if (b == null || b.length == 0)
            return a;
        ModelParticle[] merged = new ModelParticle[a.length + b.length];
        System.arraycopy(a, 0, merged, 0, a.length);
        System.arraycopy(b, 0, merged, a.length, b.length);
        return merged;
    }

    private void cleanupEntities(CommandBuffer<EntityStore> accessor, HexcasterCastingComponent comp) {
        List<Ref<EntityStore>> activeGlyphs = comp.getActiveHexes();
        for (Ref<EntityStore> hex : activeGlyphs) {
            try {
                HexComponent hexComp = accessor.getComponent(hex, HexComponent.getComponentType());
                List<Ref<EntityStore>> childGlyphRefs = hexComp.getChildGlyphRefsList();

                for (Ref<EntityStore> childGlyphRef : childGlyphRefs) {
                    try {
                        accessor.tryRemoveEntity(childGlyphRef, RemoveReason.REMOVE);
                    } catch (Exception e) {
                        LOGGER.atSevere().withCause(e)
                                .log("Failed to despawn child glyph entity with ref: " + childGlyphRef);
                    }
                }
                accessor.tryRemoveEntity(hex, RemoveReason.REMOVE);
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e)
                        .log("Failed to despawn hex entity");
            }
        }

        Ref<EntityStore> castingRootRef = comp.getCastingRootRef();
        if (castingRootRef != null) {
            try {
                accessor.tryRemoveEntity(castingRootRef, RemoveReason.REMOVE);
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to despawn casting root entity");
            }
        }

        Ref<EntityStore> headAnchor = comp.getHeadAnchorRef();
        if (headAnchor != null && headAnchor.isValid()) {
            try {
                accessor.tryRemoveComponent(headAnchor, MountedComponent.getComponentType());
                accessor.tryRemoveEntity(headAnchor, RemoveReason.REMOVE);
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to despawn head anchor entity");
            }
        }

        Ref<EntityStore> drawTrail = comp.getDrawTrailRef();
        if (drawTrail != null && drawTrail.isValid()) {
            try {
                accessor.tryRemoveEntity(drawTrail, RemoveReason.REMOVE);
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to despawn draw trail entity");
            }
        }
    }
}
