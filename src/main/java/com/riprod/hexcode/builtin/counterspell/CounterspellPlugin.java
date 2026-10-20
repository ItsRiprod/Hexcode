package com.riprod.hexcode.builtin.counterspell;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.api.event.GlyphExecuteEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphDeprecatedNotificationListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphExecuteDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.ContextForceExitDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.DrawModeEnterDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.DrawModeExitDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphCommitDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.HexCastDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.HexContextChangeDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.ShapeDrawnDiagnosticListener;

public class CounterspellPlugin {

    public static void setup(JavaPlugin plugin) {
        RegisterSystems(plugin);
        RegisterListeners(plugin);
        plugin.getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                "Counterspell", plugin.getManifest().getVersion().toString());
    }

    private static void RegisterListeners(JavaPlugin plugin) {
        plugin.getEventRegistry().registerGlobal(GlyphFizzleEvent.class, new GlyphDiagnosticListener());
        plugin.getEventRegistry().registerGlobal(GlyphExecuteEvent.class, new GlyphExecuteDiagnosticListener());
        plugin.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphDeprecatedNotificationListener());
    }

    private static void RegisterSystems(JavaPlugin plugin) {
        var entityStoreRegistry = plugin.getEntityStoreRegistry();
        entityStoreRegistry.registerSystem(new HexCastDiagnosticListener());

        entityStoreRegistry.registerSystem(new DrawModeEnterDiagnosticListener());
        entityStoreRegistry.registerSystem(new DrawModeExitDiagnosticListener());
        entityStoreRegistry.registerSystem(new HexContextChangeDiagnosticListener());
        entityStoreRegistry.registerSystem(new ShapeDrawnDiagnosticListener());
        entityStoreRegistry.registerSystem(new GlyphCommitDiagnosticListener());
        entityStoreRegistry.registerSystem(new ContextForceExitDiagnosticListener());
    }

}
