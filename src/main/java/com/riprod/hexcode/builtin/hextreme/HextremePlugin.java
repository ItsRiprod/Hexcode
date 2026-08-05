package com.riprod.hexcode.builtin.hextreme;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.builtin.hextreme.execution.system.CraftingExportSystem;
import com.riprod.hexcode.builtin.hextreme.imbuement.PageProfile;
import com.riprod.hexcode.builtin.hextreme.obelisk.PageLoadInteraction;
import com.riprod.hexcode.builtin.hextreme.obelisk.PageLoaderObelisk;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;

public class HextremePlugin extends JavaPlugin {

    public HextremePlugin(JavaPluginInit init) {
        super(init);
        getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
    }

    @Override
    public void setup() {
        RegisterConfigs();
        RegisterProfiles();
        RegisterObelisks();
        RegisterInteractions();
        RegisterSystems();
        RegisterListeners();
    }

    private void RegisterConfigs() {
        HexConfigAsset.CODEC.register("PageConfig", PageConfig.class, PageConfig.CODEC);
    }

    private void RegisterProfiles() {
        ImbuementProfileAsset.CODEC.register("Page", PageProfile.class, PageProfile.CODEC);
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
        this.getEntityStoreRegistry().registerSystem(new CraftingExportSystem());
    }

}
