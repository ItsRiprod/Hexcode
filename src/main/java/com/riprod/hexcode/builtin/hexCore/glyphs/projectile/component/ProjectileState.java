package com.riprod.hexcode.builtin.hexCore.glyphs.projectile.component;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class ProjectileState implements Component<EntityStore> {

    private static ComponentType<EntityStore, ProjectileState> componentType;

    public static void setComponentType(ComponentType<EntityStore, ProjectileState> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, ProjectileState> getComponentType() {
        return componentType;
    }

    private HexContext hexContext;
    private String triggeringGlyphId;
    private List<String> nextLinks;

    public ProjectileState() {
    }

    public ProjectileState(@Nonnull HexContext hexContext, @Nonnull Glyph triggeringGlyph) {
        this.hexContext = hexContext;
        this.triggeringGlyphId = triggeringGlyph.getId();
        this.nextLinks = List.copyOf(triggeringGlyph.getNextLinks());
    }

    @Nullable
    public HexContext getHexContext() {
        return hexContext;
    }

    @Nullable
    public String getTriggeringGlyphId() {
        return triggeringGlyphId;
    }

    @Nullable
    public Glyph getTriggeringGlyph() {
        if (hexContext == null || triggeringGlyphId == null) return null;
        return hexContext.getGlyph(triggeringGlyphId);
    }

    @Nonnull
    public List<String> getNextLinks() {
        return nextLinks != null ? nextLinks : List.of();
    }

    @Nonnull
    @Override
    public ProjectileState clone() {
        ProjectileState copy = new ProjectileState();
        copy.hexContext = this.hexContext;
        copy.triggeringGlyphId = this.triggeringGlyphId;
        copy.nextLinks = this.nextLinks;
        return copy;
    }
}
