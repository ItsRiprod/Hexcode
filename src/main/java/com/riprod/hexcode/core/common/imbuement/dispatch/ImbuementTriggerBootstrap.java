package com.riprod.hexcode.core.common.imbuement.dispatch;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.component.TriggerSubscription;
import com.riprod.hexcode.core.common.triggers.handler.TriggerCallback;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.triggers.registry.TriggerRegistry;

public final class ImbuementTriggerBootstrap {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Map<Trigger.Source, CastRootDispatcher> DISPATCHERS;
    static {
        DISPATCHERS = new EnumMap<>(Trigger.Source.class);
        DISPATCHERS.put(Trigger.Source.ITEM_HELD, new ItemHeldCastDispatcher());
        DISPATCHERS.put(Trigger.Source.ITEM_EQUIPPED_ARMOR, new ItemEquippedArmorCastDispatcher());
        DISPATCHERS.put(Trigger.Source.ENTITY_SELF, new EntitySelfCastDispatcher());
    }

    private ImbuementTriggerBootstrap() {
    }

    public static void register(@Nonnull TriggerListenerRegistry registry) {
        for (Trigger trigger : TriggerRegistry.all()) {
            if (trigger.getSource() == Trigger.Source.MANUAL) continue;
            CastRootDispatcher dispatcher = DISPATCHERS.get(trigger.getSource());
            if (dispatcher == null) {
                LOGGER.atWarning().log("no dispatcher for trigger '%s' source %s", trigger.getId(), trigger.getSource());
                continue;
            }
            TriggerCallback cb = new DispatchCallback(trigger, dispatcher);
            registry.subscribe(TriggerSubscription.bootstrap(trigger.getId(), cb));
        }
    }

    private record DispatchCallback(Trigger trigger, CastRootDispatcher dispatcher) implements TriggerCallback {
        @Override
        public void onFire(@Nonnull CommandBuffer<EntityStore> buffer,
                @Nonnull TriggerSubscription sub, @Nonnull TriggerEvent event) {
            dispatcher.dispatch(trigger, event, buffer);
        }
    }
}
