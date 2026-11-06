package com.riprod.hexcode.core.common.effects.component;

import javax.annotation.Nullable;

import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;

public class SustainState {
    @Nullable private ModelParticle[] particles;
    @Nullable private ModelTrail trail;
    @Nullable private ColorLight light;
    @Nullable private ColorLight replacedLight;
    private float cooldown;
    private int version = -1;
    @Nullable private String body;

    public void injected(@Nullable ModelParticle[] particles, @Nullable ModelTrail trail, @Nullable ColorLight light,
            @Nullable ColorLight replacedLight) {
        this.particles = particles;
        this.trail = trail;
        this.light = light;
        this.replacedLight = replacedLight;
    }

    public boolean hasInjected() {
        return this.particles != null || this.trail != null || this.light != null;
    }

    @Nullable
    public ModelParticle[] getParticles() {
        return this.particles;
    }

    @Nullable
    public ModelTrail getTrail() {
        return this.trail;
    }

    @Nullable
    public ColorLight getLight() {
        return this.light;
    }

    @Nullable
    public ColorLight getReplacedLight() {
        return this.replacedLight;
    }

    public boolean due(float dt, float interval) {
        this.cooldown -= dt;
        if (this.cooldown > 0f) {
            return false;
        }
        this.cooldown = interval;
        return true;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Nullable
    public String getBody() {
        return this.body;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }
}
