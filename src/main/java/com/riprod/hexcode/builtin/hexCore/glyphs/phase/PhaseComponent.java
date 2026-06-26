package com.riprod.hexcode.builtin.hexCore.glyphs.phase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PhaseComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, PhaseComponent> componentType;

    private List<PhasedBlock> phasedBlocks;
    private float duration;

    public PhaseComponent() {
    }

    public PhaseComponent(List<PhasedBlock> phasedBlocks, float duration) {
        this.phasedBlocks = phasedBlocks;
        this.duration = duration;
    }

    public static void setComponentType(ComponentType<EntityStore, PhaseComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, PhaseComponent> getComponentType() {
        return componentType;
    }

    public List<PhasedBlock> getPhasedBlocks() {
        return phasedBlocks;
    }

    public boolean decrementDuration(float delta) {
        duration -= delta;
        return duration <= 0;
    }

    @Nonnull
    @Override
    public PhaseComponent clone() {
        PhaseComponent copy = new PhaseComponent();
        copy.phasedBlocks = this.phasedBlocks != null ? new ArrayList<>(this.phasedBlocks) : null;
        copy.duration = this.duration;
        return copy;
    }
}
