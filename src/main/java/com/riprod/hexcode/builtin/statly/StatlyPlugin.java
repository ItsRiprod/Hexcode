package com.riprod.hexcode.builtin.statly;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.statly.assets.BaseElementInteraction;
import com.riprod.hexcode.builtin.statly.assets.ElementAsset;
import com.riprod.hexcode.builtin.statly.interaction.GenericElementInteraction;
import com.riprod.hexcode.builtin.statly.systems.StatlyReactionSystem;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;

public class StatlyPlugin extends JavaPlugin {

    public StatlyPlugin(JavaPluginInit init) {
        super(init);
        getLogger().atFine().log("Unified Magic System sub-plugin initializing...");
    }

    @Override
    public void setup() {
        registerAssets();
        registerInteractions();
        registerSystems();
    }

    private void registerAssets() {
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(ElementAsset.class, new DefaultAssetMap<String, ElementAsset>())
                        .setPath("Statly/Elements")
                        .setCodec(ElementAsset.CODEC)
                        .setKeyFunction(ElementAsset::getId)
                        .loadsAfter(DamageCause.class)
                        .build());
    }

    private void registerInteractions() {
        BaseElementInteraction.CODEC.register(GenericElementInteraction.ID, GenericElementInteraction.class,
                GenericElementInteraction.CODEC);
    }

    private void registerSystems() {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
        entityStoreRegistry.registerSystem(new StatlyReactionSystem());
    }
}
