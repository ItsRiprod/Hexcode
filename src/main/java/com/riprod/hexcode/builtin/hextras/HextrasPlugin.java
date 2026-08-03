package com.riprod.hexcode.builtin.hextras;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class HextrasPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HextrasPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atFine().log("Hexcode %s sub-plugin v%s initializing...",
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
