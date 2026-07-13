package com.riprod.hexcode.core.state.idle;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.component.VolatilityTracker;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.state.HexcodeManager;
import com.riprod.hexcode.utils.CleanupUtils;
import com.riprod.hexcode.utils.HexSlot;
import com.riprod.hexcode.utils.SpellMana;

public class IdleSystem extends HexcodeManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String HOLD_STALE_KEY = "idle_hold_stale";
    private static final float HOLD_STALE_THRESHOLD = 0.15f;

    @Override
    public void firstTick(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
            HexState previousState) {

        HexcasterCraftingComponent craftingComp = buffer.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            Ref<EntityStore> headAnchor = craftingComp.getHeadAnchorRef();
            if (headAnchor != null && headAnchor.isValid()) {
                CleanupUtils.safeRemoveEntity(buffer, headAnchor);
            }
            craftingComp.clear(buffer);
        }

        buffer.ensureComponent(ref, HexcasterIdleComponent.getComponentType());
        comp.setTickLength(HOLD_STALE_KEY, 0f);
    }

    @Override
    public void lastTick(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
            HexState nextState) {
        HexcasterIdleComponent idleComp = buffer.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return;
        idleComp.setHoldingPrimary(false);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
    }

    @Override
    public void tick0(Ref<EntityStore> ref, HexcasterComponent comp, float dt,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        HexcasterIdleComponent idleComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());
        if (idleComp.isHoldingPrimary()) {
            comp.incrementTickLength(HOLD_STALE_KEY, dt);
            if (comp.getTickLength(HOLD_STALE_KEY) > HOLD_STALE_THRESHOLD) {
                idleComp.setHoldingPrimary(false);
            }
        }
    }

    @Override
    public void onPlayerJoin(Ref<EntityStore> playerRef, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onPlayerLeave(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        HexcasterIdleComponent idleComp = buffer.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return;
        idleComp.cancelAll(ref);
    }

    @Override
    public InteractionState enterAbility(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterComponent comp, InteractionType inputType) {
        HexcasterIdleComponent idleComp = buffer.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (inputType == InteractionType.Ability1) {
            int count = idleComp != null ? idleComp.getActiveCount() : 0;
            if (idleComp != null) {
                idleComp.cancelAll(ref);
            }
            PlayerRef pr = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (pr != null && count > 0) {
                pr.sendMessage(Message.raw("dispelled " + count + " active spell(s)"));
            }
            return InteractionState.Finished;
        }
        return InteractionState.Finished;
    }

    @Override
    public InteractionState enterInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterComponent comp) {
        HexcasterIdleComponent idleComp = accessor.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (idleComp == null) {
            LOGGER.atWarning().log("no idle component on hexcaster, cannot execute");
            return InteractionState.Finished;
        }

        Hex activeHex = idleComp.getActiveHex();
        if (activeHex == null) {
            LOGGER.atFine().log("no active spell on staff, nothing to execute");
            return InteractionState.Finished;
        }

        Hex hexClone = activeHex.clone();
        HexUtils.validate(hexClone);

        PlayerHexRoot hexRoot = new PlayerHexRoot(ref, accessor);

        HexStaffComponent staff = CasterInventory.getHexStaffComponent(accessor, ref);
        float castDecayRate = staff != null ? staff.getCastDecayRate() : 0f;

        float volatilityMax = hexRoot.resolveVolatility(accessor);
        float baseMana = SpellMana.computeTotalMana(hexClone);
        float resolvedPower = hexRoot.resolveSpellPower(accessor);

        HexStaffAsset staffAsset = CasterInventory.getHexStaffAsset(
                PlayerUtils.getHandItem(accessor, ref, HexSlot.MainHand));
        HexBookAsset bookAsset = CasterInventory.getHexBookAsset(
                PlayerUtils.getHandItem(accessor, ref, HexSlot.OffHand));

        HexStyleAsset style = HexStyleAsset.empty();
        if (staffAsset != null && staffAsset.getStyle() != null) style.compose(staffAsset.getStyle());
        if (bookAsset != null && bookAsset.getStyle() != null
                && bookAsset.getStyle().getSecondaryColor() != null) {
            style.setSecondaryColor(bookAsset.getStyle().getSecondaryColor().clone());
        }

        idleComp.setHoldingPrimary(true);
        comp.setTickLength(HOLD_STALE_KEY, 0f);

        VolatilityTracker tracker = new VolatilityTracker(volatilityMax, 1.0f, resolvedPower);
        HexContext context = new HexContext(hexClone, baseMana, hexRoot, style, tracker);
        context.setCastDecayRate(castDecayRate);

        if (staffAsset != null) context.applyNonDefaultsFrom(staffAsset.getDefaults());
        if (bookAsset != null) context.applyNonDefaultsFrom(bookAsset.getDefaults());

        HexExecuter.cast(context, accessor);
        return InteractionState.NotFinished;
    }

    @Override
    public InteractionState tickInteraction(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref, float dt,
            HexcasterComponent comp) {
        HexcasterIdleComponent idleComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return InteractionState.Finished;

        idleComp.setHoldingPrimary(true);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
        return InteractionState.NotFinished;
    }

    @Override
    public InteractionState exitInteraction(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterComponent comp) {
        HexcasterIdleComponent idleComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return InteractionState.Finished;

        idleComp.setHoldingPrimary(false);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
        return InteractionState.Finished;
    }
}
