package com.riprod.hexcode.core.state.crafting.system;

import java.util.List;
import java.util.Objects;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.HoverChangeEvent;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hover.utils.HoverableUtils;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.handlers.CraftingDragHandler;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.core.state.crafting.utils.HoverStyleUtils;
import com.riprod.hexcode.core.state.crafting.utils.LinkRenderer;

public class CraftingStateSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String HOVER_PARTICLE = "Object_Hover";

    public static InteractionState enterInteraction(Ref<EntityStore> ref, HexcasterComponent comp,
            CommandBuffer<EntityStore> buffer) {
        HexcasterCraftingComponent craftingComp = buffer.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return InteractionState.Failed;

        Ref<EntityStore> hoveredRef = craftingComp.getHoveredRef();
        if (hoveredRef == null || !hoveredRef.isValid())
            return InteractionState.Failed;

        craftingComp.setDragTickCount(0);

        return NodeRouter.enter(buffer, hoveredRef, ref);
    }

    public static void tickInteraction(CommandBuffer<EntityStore> accessor, float dt, Ref<EntityStore> ref,
            PedestalBlockComponent pedestal) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        craftingComp.setDragTickCount(craftingComp.getDragTickCount() + 1);

        Ref<EntityStore> draggedRef = craftingComp.getDraggingRef();
        if (draggedRef == null || !draggedRef.isValid())
            return;

        NodeRouter.drag(accessor, draggedRef, ref);
    }

    public static InteractionState exitInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return InteractionState.Finished;

        boolean isClick = craftingComp.getDragTickCount() < 5;

        Ref<EntityStore> draggedRef = craftingComp.getDraggingRef();

        if (draggedRef == null || !draggedRef.isValid()) {
            craftingComp.setDraggingRef(null);
            craftingComp.setHeadAnchorRef(accessor, null);
            craftingComp.setDragTickCount(0);
            return InteractionState.Finished;
        }

        InteractionState result;
        if (isClick) {
            result = NodeRouter.click(accessor, draggedRef, ref);
        } else {
            result = NodeRouter.exit(accessor, draggedRef, ref);
        }

        CraftingDragHandler.endDrag(accessor, draggedRef, craftingComp.getHeadAnchorRef(), craftingComp);

        craftingComp.setDraggingRef(null);
        craftingComp.setHeadAnchorRef(accessor, null);
        craftingComp.setDragTickCount(0);
        return result;
    }

    public static void tickCrafting(CommandBuffer<EntityStore> accessor, float dt, Ref<EntityStore> ref,
            PedestalBlockComponent pedestal) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        TransformComponent playerTransform = accessor.getComponent(ref,
                TransformComponent.getComponentType());
        if (playerTransform == null)
            return;

        List<Ref<EntityStore>> nearby = HoverableUtils.getNearbyHoverables(accessor,
                playerTransform.getPosition(), 8.0);

        Ref<EntityStore> draggedRef = craftingComp.getDraggingRef();
        if (draggedRef != null && draggedRef.isValid()) {
            nearby.remove(draggedRef); // remove the dragged ref from the hovered list
        }

        Ref<EntityStore> targetRef = HoverableUtils.getSmallestTarget(accessor, ref, nearby);

        Ref<EntityStore> previousHovered = craftingComp.getHoveredRef();
        boolean changed = !Objects.equals(targetRef, previousHovered);

        if (changed) {

            HoverStyleUtils.unhover(accessor, previousHovered, ref);

            craftingComp.setHoveredRef(targetRef);
            pedestal.setTickLength(HOVER_PARTICLE, 1f);

            HoverStyleUtils.hover(accessor, targetRef, ref);
            HytaleServer.get().getEventBus().dispatchFor(HoverChangeEvent.class)
                    .dispatch(new HoverChangeEvent(ref, targetRef, previousHovered));

            PedestalBlockComponent ped = PedestalBlockUtil.resolvePedestal(ref, accessor);
            if (ped != null) {
                if (previousHovered != null)
                    ObeliskDispatcher.dispatchUnhover(accessor, ped, ref, previousHovered);
                if (targetRef != null)
                    ObeliskDispatcher.dispatchHover(accessor, ped, ref, targetRef);
            }
        }

        HoverStyleUtils.hoverParticles(accessor, craftingComp.getHoveredRef(), dt, pedestal);

        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, accessor);

        if (session != null) {
            LinkRenderer.renderLinks(accessor, session, pedestal, dt);
        }
    }

    public static InteractionState enterAbility(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            InteractionType inputType) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return InteractionState.Finished;

        Ref<EntityStore> hoveredRef = craftingComp.getHoveredRef();
        if (hoveredRef == null)
            return InteractionState.Finished;

        return NodeRouter.ability(accessor, hoveredRef, inputType, ref);
    }
}
