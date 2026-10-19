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

public class StatlyPlugin {

    public static void setup(JavaPlugin plugin) {
        registerAssets(plugin);
        registerInteractions(plugin);
        registerSystems(plugin);
        plugin.getLogger().atFine().log("Unified Magic System sub-plugin initializing...");

    }

    private static void registerAssets(JavaPlugin plugin) {
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(ElementAsset.class, new DefaultAssetMap<String, ElementAsset>())
                        .setPath("Statly/Elements")
                        .setCodec(ElementAsset.CODEC)
                        .setKeyFunction(ElementAsset::getId)
                        .loadsAfter(DamageCause.class)
                        .build());
    }

    private static void registerInteractions(JavaPlugin plugin) {
        BaseElementInteraction.CODEC.register(GenericElementInteraction.ID, GenericElementInteraction.class,
                GenericElementInteraction.CODEC);
    }

    private static void registerSystems(JavaPlugin plugin) {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = plugin.getEntityStoreRegistry();
        entityStoreRegistry.registerSystem(new StatlyReactionSystem());
    }
}
