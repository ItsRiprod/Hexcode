package com.riprod.hexcode.core.common.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CasterComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, CasterComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, CasterComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, CasterComponent> getComponentType() {
        return componentType;
    }

    private String currentContext;
    private int currentPriority;

    private boolean primaryHeld;
    private boolean primaryPressed;
    private boolean primaryReleased;
    private int primaryHeldTicks;
    private InteractionType abilityPressed;

    public CasterComponent() {
    }

    @Nullable
    public String getCurrentContext() {
        return currentContext;
    }

    public int getCurrentPriority() {
        return currentPriority;
    }

    public void setContext(@Nullable String contextId, int priority) {
        this.currentContext = contextId;
        this.currentPriority = priority;
    }

    public boolean isPrimaryHeld() {
        return primaryHeld;
    }

    public void beginPrimary() {
        this.primaryHeld = true;
        this.primaryPressed = true;
        this.primaryHeldTicks = 0;
    }

    public void tickPrimary() {
        this.primaryHeldTicks++;
    }

    public void endPrimary() {
        this.primaryHeld = false;
        this.primaryReleased = true;
    }

    public boolean consumePrimaryPressed() {
        boolean pressed = this.primaryPressed;
        this.primaryPressed = false;
        return pressed;
    }

    public boolean consumePrimaryReleased() {
        boolean released = this.primaryReleased;
        this.primaryReleased = false;
        return released;
    }

    public int getPrimaryHeldTicks() {
        return primaryHeldTicks;
    }

    public void pressAbility(InteractionType type) {
        this.abilityPressed = type;
    }

    @Nullable
    public InteractionType consumeAbilityPressed() {
        InteractionType type = this.abilityPressed;
        this.abilityPressed = null;
        return type;
    }

    public void clearInput() {
        this.primaryHeld = false;
        this.primaryPressed = false;
        this.primaryReleased = false;
        this.primaryHeldTicks = 0;
        this.abilityPressed = null;
    }

    @Nonnull
    @Override
    public CasterComponent clone() {
        CasterComponent copy = new CasterComponent();
        copy.currentContext = this.currentContext;
        copy.currentPriority = this.currentPriority;
        copy.primaryHeld = this.primaryHeld;
        copy.primaryPressed = this.primaryPressed;
        copy.primaryReleased = this.primaryReleased;
        copy.primaryHeldTicks = this.primaryHeldTicks;
        copy.abilityPressed = this.abilityPressed;
        return copy;
    }
}
