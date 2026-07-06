package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure;

import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsConfig;

public class ConjurePhysicsConfig extends StandardPhysicsConfig {
    public static final ConjurePhysicsConfig INSTANCE = new ConjurePhysicsConfig();

    private ConjurePhysicsConfig() {
        this.gravity = 0;
        this.bounceCount = -1;
        this.bounciness = 0.7;
        this.sticksVertically = false;
        this.computeYaw = false;
        this.computePitch = false;
        this.densityAir = 0.0;
        this.terminalVelocityAir = 200.0;
        this.terminalVelocityWater = 200.0;
    }
}
