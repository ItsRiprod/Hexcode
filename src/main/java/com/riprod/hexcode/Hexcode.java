package com.riprod.hexcode;

import com.riprod.configly.Configly;
import com.riprod.hexcode.core.HexCore;
import com.riprod.hexcode.builtin.counterspell.CounterspellPlugin;
import com.riprod.hexcode.builtin.hexCore.HexCorePlugin;
import com.riprod.hexcode.builtin.hextreme.HextremePlugin;
import com.riprod.hexcode.builtin.imbued.ImbuedPlugin;
import com.riprod.hexcode.builtin.statly.StatlyPlugin;
import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.patchly.PatchManager;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class Hexcode extends JavaPlugin {
    private final PatchManager patchManager;

    public Hexcode(JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this);
        getLogger().atFine().log("Hexcode v%s initializing...",
                this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        patchManager.install();
        Configly.register(HexcodeConfig.TYPE, HexcodeConfig.class, HexcodeConfig.CODEC);
        HexCore.setup(this);
        HexCorePlugin.setup(this);
        CounterspellPlugin.setup(this);
        HextremePlugin.setup(this);
        ImbuedPlugin.setup(this);
        StatlyPlugin.setup(this);
        
        getLogger().atFine().log("Hexcode %s setup complete!", this.getManifest().getVersion().toString());

    }

    
    @Override
    protected void shutdown() {
        patchManager.shutdown();
        HexCore.shutdown(this);

    }

    @Override 
    protected void start() {
        HexCore.start(this);
    }

}
