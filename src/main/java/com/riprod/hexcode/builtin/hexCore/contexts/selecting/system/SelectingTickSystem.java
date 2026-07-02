package com.riprod.hexcode.builtin.hexCore.contexts.selecting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.core.state.crafting.system.CraftingStateSystem;

public class SelectingTickSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return SelectingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (pedestal == null) {
                ContextTransitionService.exit(buffer, player, SelectingState.CONTEXT_ID);
                return;
            }
            HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
            if (session == null) {
                ContextTransitionService.exit(buffer, player, SelectingState.CONTEXT_ID);
                return;
            }

            // the container node flips the pedestal to crafting on slot select; the
            // context follows the session
            if (session.getState() == PedestalState.CRAFTING) {
                ContextTransitionService.transitionFrom(buffer, player,
                        SelectingState.CONTEXT_ID, CraftingState.CONTEXT_ID, CraftingState.PRIORITY);
                return;
            }

            CraftingStateSystem.tickCrafting(buffer, dt, player, pedestal);

            CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
            if (caster != null) {
                if (caster.consumePrimaryPressed()) {
                    CraftingStateSystem.enterInteraction(player, null, buffer);
                }
                caster.consumePrimaryReleased();
                caster.consumeAbilityPressed();
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] selecting tick failed");
        }
    }
}
