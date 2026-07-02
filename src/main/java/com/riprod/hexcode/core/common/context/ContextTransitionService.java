package com.riprod.hexcode.core.common.context;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;

public final class ContextTransitionService {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ContextTransitionService() {
    }

    public static boolean attemptEnter(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            String contextId, int priority) {
        if (player == null || !player.isValid() || contextId == null) {
            return false;
        }

        CasterComponent caster = buffer.getComponent(player, CasterComponent.getComponentType());
        if (caster == null) {
            caster = new CasterComponent();
            caster.setContext(contextId, priority);
            buffer.putComponent(player, CasterComponent.getComponentType(), caster);
            announce(buffer, player, contextId);
            return true;
        }

        String current = caster.getCurrentContext();
        if (contextId.equals(current)) {
            return true;
        }
        if (current != null && priority <= caster.getCurrentPriority()) {
            LOGGER.atInfo().log("[hexcode] context enter rejected: %s(%d) vs active %s(%d)",
                    contextId, priority, current, caster.getCurrentPriority());
            return false;
        }

        if (current != null) {
            forfeitDrawCapture(buffer, player);
        }

        caster.setContext(contextId, priority);
        caster.clearInput();
        announce(buffer, player, contextId);
        return true;
    }

    public static boolean transitionFrom(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            String fromId, String toId, int toPriority) {
        if (player == null || !player.isValid() || fromId == null || toId == null) {
            return false;
        }

        CasterComponent caster = buffer.getComponent(player, CasterComponent.getComponentType());
        if (caster == null || !fromId.equals(caster.getCurrentContext())) {
            return false;
        }

        forfeitDrawCapture(buffer, player);
        caster.setContext(toId, toPriority);
        caster.clearInput();
        announce(buffer, player, toId);
        return true;
    }

    public static void setInContextStat(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            boolean inContext) {
        EntityStatMap statMap = buffer.getComponent(player, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }
        int index = HexcodeEntityStatTypes.getInContext();
        if (index == Integer.MIN_VALUE) {
            return;
        }
        statMap.setStatValue(index, inContext ? 1f : 0f);
    }

    public static void exit(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player, String contextId) {
        if (player == null || !player.isValid() || contextId == null) {
            return;
        }

        CasterComponent caster = buffer.getComponent(player, CasterComponent.getComponentType());
        if (caster == null || !contextId.equals(caster.getCurrentContext())) {
            return;
        }

        forfeitDrawCapture(buffer, player);
        caster.setContext(null, 0);
        caster.clearInput();
        announce(buffer, player, null);
    }

    // preemption and service-driven exits forfeit any in-flight draw; the blackboard is
    // cleared BEFORE removal so the lifecycle's exit-commit cannot emit forfeited strokes
    // into whichever context marker is still physically present
    private static void forfeitDrawCapture(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        DrawCaptureComponent capture = buffer.getComponent(player, DrawCaptureComponent.getComponentType());
        if (capture == null) {
            return;
        }
        capture.getStrokePoints().clear();
        capture.setStrokeActive(false);
        capture.getPendingShapes().clear();
        capture.setFinalizePending(false);
        buffer.tryRemoveComponent(player, DrawCaptureComponent.getComponentType());
    }

    private static void announce(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player, String newId) {
        LOGGER.atInfo().log("[hexcode] context -> %s", newId);
        buffer.invoke(new HexContextChangeEvent(player, newId));
    }
}
