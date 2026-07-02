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
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.core.state.crafting.system.CraftingStateSystem;

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
                CraftingStateSystem.enterInteraction(player, null, buffer);
            } else if (caster.isPrimaryHeld() && craftingActive) {
                CraftingStateSystem.tickInteraction(buffer, dt, player, pedestal);
            }
            if (caster.consumePrimaryReleased() && craftingActive) {
                CraftingStateSystem.exitInteraction(buffer, player);
            }
            InteractionType ability = caster.consumeAbilityPressed();
            if (ability != null && craftingActive) {
                CraftingStateSystem.enterAbility(buffer, player, ability);
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] crafting primary input failed");
        }
    }
}
