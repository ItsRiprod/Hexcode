package com.riprod.hexcode.builtin.components;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.components.component.ComponentPasteCache;
import com.riprod.hexcode.builtin.components.imbuement.ComponentProfile;
import com.riprod.hexcode.builtin.components.system.ComponentCacheCleanupSystem;
import com.riprod.hexcode.builtin.components.system.ComponentCacheSystem;
import com.riprod.hexcode.builtin.components.system.ComponentResolveListener;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;

public class ComponentsPlugin extends JavaPlugin {

    public ComponentsPlugin(JavaPluginInit init) {
        super(init);
        getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
    }

    @Override
    public void setup() {
        RegisterComponents();
        RegisterProfiles();
        RegisterSystems();
    }

    private void RegisterComponents() {
        ComponentType<EntityStore, ComponentPasteCache> pasteCacheType = this.getEntityStoreRegistry()
                .registerComponent(ComponentPasteCache.class, ComponentPasteCache::new);
        ComponentPasteCache.setComponentType(pasteCacheType);
    }

    private void RegisterProfiles() {
        ImbuementProfileAsset.CODEC.register("Component", ComponentProfile.class, ComponentProfile.CODEC);
    }

    private void RegisterSystems() {
        this.getEntityStoreRegistry().registerSystem(new ComponentCacheSystem());
        this.getEntityStoreRegistry().registerSystem(new ComponentCacheCleanupSystem());
        this.getEntityStoreRegistry().registerSystem(new ComponentResolveListener());
    }
}
