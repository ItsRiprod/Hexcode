package com.riprod.hexcode.builtin.hextreme;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.builtin.hextreme.obelisk.PageLoadInteraction;
import com.riprod.hexcode.builtin.hextreme.obelisk.PageLoaderObelisk;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;

public class HextremePlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HextremePlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hexcode %s sub-plugin v%s initializing...",
                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
    }

    @Override
    public void setup() {
        RegisterConfigs();
        RegisterObelisks();
        RegisterInteractions();
        RegisterSystems();
        RegisterListeners();
    }

    private void RegisterConfigs() {
        HexConfigAsset.CODEC.register("PageConfig", PageConfig.class, PageConfig.CODEC);
    }

    private void RegisterObelisks() {
        ObeliskHandlerRegistry.register("page_load", new PageLoaderObelisk());
    }

    private void RegisterInteractions() {
        Interaction.CODEC.register("HexPageLoadInteraction", PageLoadInteraction.class, PageLoadInteraction.CODEC);
    }

    private void RegisterListeners() {
    }

    private void RegisterSystems() {
    }

}
