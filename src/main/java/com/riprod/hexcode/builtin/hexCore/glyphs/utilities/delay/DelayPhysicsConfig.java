package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay;

import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsConfig;

public class DelayPhysicsConfig extends StandardPhysicsConfig {

    public DelayPhysicsConfig(double gravity) {
        this.gravity = gravity;
        this.bounceCount = 0;
        this.bounciness = 0.3;
        this.sticksVertically = false;
        this.computeYaw = false;
        this.computePitch = false;
        this.terminalVelocityAir = 200.0;
        this.terminalVelocityWater = 100.0;
    }
}
