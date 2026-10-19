package com.riprod.hexcode.builtin.hextreme;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.builtin.hextreme.execution.system.CraftingExportSystem;
import com.riprod.hexcode.builtin.hextreme.imbuement.PageProfile;
import com.riprod.hexcode.builtin.hextreme.obelisk.PageLoadInteraction;
import com.riprod.hexcode.builtin.hextreme.obelisk.PageLoaderObelisk;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;

public class HextremePlugin {


    public static void setup(JavaPlugin plugin) {
        RegisterConfigs(plugin);
        RegisterProfiles(plugin);
        RegisterObelisks(plugin);
        RegisterInteractions(plugin);
        RegisterSystems(plugin);
        RegisterListeners(plugin);

        plugin.getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                "Hextreme", plugin.getManifest().getVersion().toString());
    }

    private static void RegisterConfigs(JavaPlugin plugin) {
        HexConfigAsset.CODEC.register("PageConfig", PageConfig.class, PageConfig.CODEC);
    }

    private static void RegisterProfiles(JavaPlugin plugin) {
        ImbuementProfileAsset.CODEC.register("Page", PageProfile.class, PageProfile.CODEC);
    }

    private static void RegisterObelisks(JavaPlugin plugin) {
        ObeliskHandlerRegistry.register("page_load", new PageLoaderObelisk());
    }

    private static void RegisterInteractions(JavaPlugin plugin) {
        Interaction.CODEC.register("HexPageLoadInteraction", PageLoadInteraction.class, PageLoadInteraction.CODEC);
    }

    private static void RegisterListeners(JavaPlugin plugin) {
    }

    private static void RegisterSystems(JavaPlugin plugin) {
        plugin.getEntityStoreRegistry().registerSystem(new CraftingExportSystem());
    }

}
