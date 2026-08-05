package com.riprod.hexcode.builtin.counterspell;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.api.event.GlyphExecuteEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.HexStateChangeEvent;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphDeprecatedNotificationListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphExecuteDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.ContextForceExitDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.DrawModeEnterDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.DrawModeExitDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphCommitDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.GlyphDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.HexCastDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.HexContextChangeDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.HexStateDiagnosticListener;
import com.riprod.hexcode.builtin.counterspell.eventListeners.ShapeDrawnDiagnosticListener;

public class CounterspellPlugin extends JavaPlugin {

    public CounterspellPlugin(JavaPluginInit init) {
        super(init);
        getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
    }

    @Override
    public void setup() {
        RegisterSystems();
        RegisterListeners();
    }

    private void RegisterListeners() {

        this.getEventRegistry().registerGlobal(GlyphFizzleEvent.class, new GlyphDiagnosticListener());
        this.getEventRegistry().registerGlobal(GlyphExecuteEvent.class, new GlyphExecuteDiagnosticListener());
        this.getEventRegistry().registerGlobal(HexStateChangeEvent.class, new HexStateDiagnosticListener());
        this.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphDeprecatedNotificationListener());
    }

    private void RegisterSystems() {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
        entityStoreRegistry.registerSystem(new HexCastDiagnosticListener());

        entityStoreRegistry.registerSystem(new DrawModeEnterDiagnosticListener());
        entityStoreRegistry.registerSystem(new DrawModeExitDiagnosticListener());
        entityStoreRegistry.registerSystem(new HexContextChangeDiagnosticListener());
        entityStoreRegistry.registerSystem(new ShapeDrawnDiagnosticListener());
        entityStoreRegistry.registerSystem(new GlyphCommitDiagnosticListener());
        entityStoreRegistry.registerSystem(new ContextForceExitDiagnosticListener());
    }

}
