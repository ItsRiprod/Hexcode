package com.riprod.hexcode.builtin.hexCore.glyphs.force;

import com.hypixel.hytale.protocol.VelocityThresholdStyle;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;

public class ForceVelocityConfig extends VelocityConfig {

    private static final float GROUND_RESISTANCE = 0.82f;
    private static final float AIR_RESISTANCE = 0.96f;

    @Override
    public com.hypixel.hytale.protocol.VelocityConfig toPacket() {
        return new com.hypixel.hytale.protocol.VelocityConfig(
                GROUND_RESISTANCE,
                GROUND_RESISTANCE,
                AIR_RESISTANCE,
                AIR_RESISTANCE,
                1.0f,
                VelocityThresholdStyle.Linear);
    }
}
