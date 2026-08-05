package com.riprod.hexcode.builtin.imbued;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.imbued.triggers.Ability1Trigger;
import com.riprod.hexcode.builtin.imbued.triggers.Ability2Trigger;
import com.riprod.hexcode.builtin.imbued.triggers.Ability3Trigger;
import com.riprod.hexcode.builtin.imbued.triggers.AttackedTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.BlockTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.InteractionTriggerSource;
import com.riprod.hexcode.builtin.imbued.triggers.OnAttackTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.OnShootTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.PrimaryTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.SecondaryTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.UseTrigger;
import com.riprod.hexcode.builtin.imbued.triggers.cast.CastTriggerSource;
import com.riprod.hexcode.builtin.imbued.triggers.death.DeathTriggerSource;
import com.riprod.hexcode.builtin.imbued.triggers.sources.EntityHitEventSource;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedArmorMarker;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedHotbarMarker;
import com.riprod.hexcode.core.common.imbuement.dispatch.ImbuementTriggerBootstrap;
import com.riprod.hexcode.core.common.imbuement.system.ImbuementMarkerSystem;
import com.riprod.hexcode.core.common.triggers.component.TriggerListenerComponent;
import com.riprod.hexcode.core.common.triggers.handler.TriggerConstructHandler;
import com.riprod.hexcode.core.common.triggers.registry.ManualTrigger;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.triggers.registry.TriggerRegistry;

public class ImbuedPlugin extends JavaPlugin {

    public ImbuedPlugin(JavaPluginInit init) {
        super(init);
        getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
    }

    @Override
    public void setup() {
        RegisterComponents();
        RegisterSystems();
        RegisterConstructs();

    }

    private void RegisterComponents() {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

        ComponentType<EntityStore, ImbuedHotbarMarker> hotbarMarkerType = entityStoreRegistry
                .registerComponent(ImbuedHotbarMarker.class, ImbuedHotbarMarker::new);
        ImbuedHotbarMarker.setComponentType(hotbarMarkerType);

        ComponentType<EntityStore, ImbuedArmorMarker> armorMarkerType = entityStoreRegistry
                .registerComponent(ImbuedArmorMarker.class, ImbuedArmorMarker::new);
        ImbuedArmorMarker.setComponentType(armorMarkerType);

        ComponentType<EntityStore, TriggerListenerComponent> triggerListenerType = entityStoreRegistry
                .registerComponent(TriggerListenerComponent.class, TriggerListenerComponent::new);
        TriggerListenerComponent.setComponentType(triggerListenerType);
    }

    private void RegisterSystems() {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

        entityStoreRegistry.registerSystem(new DeathTriggerSource());
        entityStoreRegistry.registerSystem(new CastTriggerSource());
        InteractionTriggerSource.register();

        entityStoreRegistry.registerSystem(new EntityHitEventSource.OnDamageDealtSystem());
        entityStoreRegistry.registerSystem(new EntityHitEventSource.OnDamageReceivedSystem());

        entityStoreRegistry.registerSystem(new ImbuementMarkerSystem());

        TriggerRegistry.register(new PrimaryTrigger());
        TriggerRegistry.register(new SecondaryTrigger());
        TriggerRegistry.register(new UseTrigger());
        TriggerRegistry.register(new Ability1Trigger());
        TriggerRegistry.register(new Ability2Trigger());
        TriggerRegistry.register(new Ability3Trigger());
        TriggerRegistry.register(new OnAttackTrigger());
        TriggerRegistry.register(new OnShootTrigger());
        TriggerRegistry.register(new BlockTrigger());
        TriggerRegistry.register(new AttackedTrigger());

        for (int i = 1; i <= 10; i++) {
            TriggerRegistry.register(new ManualTrigger(Integer.toString(i)));
        }
        TriggerRegistry.register(new ManualTrigger("Default"));

        TriggerListenerRegistry.registerBootstrap(ImbuementTriggerBootstrap::register);
    }

    private void RegisterConstructs() {
        ConstructRegistry.register(TriggerConstructHandler.HANDLER_ID, new TriggerConstructHandler());
    }
}
