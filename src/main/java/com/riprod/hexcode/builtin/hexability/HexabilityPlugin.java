package com.riprod.hexcode.builtin.hexability;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;

public class HexabilityPlugin {

    public static void setup(JavaPlugin plugin) {
        RegisterSystems(plugin);
        RegisterListeners(plugin);
        plugin.getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                "Hexability", plugin.getManifest().getVersion().toString());
    }

    private static void RegisterListeners(JavaPlugin plugin) {
    }

    private static void RegisterSystems(JavaPlugin plugin) {
    }

}
