package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.handlers.CraftingDragHandler;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;

public class CraftingPrimarySystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
            if (caster == null) {
                return;
            }
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (pedestal == null) {
                return;
            }
            HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
            if (session == null) {
                return;
            }
            boolean craftingActive = session.getState() == PedestalState.CRAFTING;

            if (caster.consumePrimaryPressed()) {
                enterInteraction(player, buffer);
            } else if (caster.isPrimaryHeld() && craftingActive) {
                tickInteraction(buffer, player);
            }
            if (caster.consumePrimaryReleased() && craftingActive) {
                exitInteraction(buffer, player);
            }
            InteractionType ability = caster.consumeAbilityPressed();
            if (ability != null && craftingActive) {
                enterAbility(buffer, player, ability);
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] crafting primary input failed");
        }
    }

    private static void enterInteraction(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer) {
        HexcasterCraftingComponent craftingComp = buffer.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        Ref<EntityStore> hoveredRef = craftingComp.getHoveredRef();
        if (hoveredRef == null || !hoveredRef.isValid())
            return;

        craftingComp.setDragTickCount(0);

        NodeRouter.enter(buffer, hoveredRef, ref);
    }

    private static void tickInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref) {
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

    private static void exitInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        boolean isClick = craftingComp.getDragTickCount() < 5;

        Ref<EntityStore> draggedRef = craftingComp.getDraggingRef();

        if (draggedRef == null || !draggedRef.isValid()) {
            craftingComp.setDraggingRef(null);
            craftingComp.setHeadAnchorRef(accessor, null);
            craftingComp.setDragTickCount(0);
            return;
        }

        if (isClick) {
            NodeRouter.click(accessor, draggedRef, ref);
        } else {
            NodeRouter.exit(accessor, draggedRef, ref);
        }

        CraftingDragHandler.endDrag(accessor, draggedRef, craftingComp.getHeadAnchorRef(), craftingComp);

        craftingComp.setDraggingRef(null);
        craftingComp.setHeadAnchorRef(accessor, null);
        craftingComp.setDragTickCount(0);
    }

    private static void enterAbility(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            InteractionType inputType) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        Ref<EntityStore> hoveredRef = craftingComp.getHoveredRef();
        if (hoveredRef == null)
            return;

        NodeRouter.ability(accessor, hoveredRef, inputType, ref);
    }
}
