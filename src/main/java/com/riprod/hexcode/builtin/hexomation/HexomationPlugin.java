package com.riprod.hexcode.builtin.hexomation;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class HexomationPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexomationPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hexcode %s sub-plugin v%s initializing...",
                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
    }

    @Override
    public void setup() {
        RegisterSystems();
        RegisterListeners();
    }

    private void RegisterListeners() {
    }

    private void RegisterSystems() {
    }

}
