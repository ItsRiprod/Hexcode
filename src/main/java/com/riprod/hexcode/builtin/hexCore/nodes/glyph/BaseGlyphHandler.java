package com.riprod.hexcode.builtin.hexCore.nodes.glyph;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.builtin.hexCore.scene.GlyphStyler;
import com.riprod.hexcode.core.common.node.BaseNodeHandler;

public abstract class BaseGlyphHandler extends BaseNodeHandler {

    @Override
    public void hover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        GlyphComponent glyphComp = accessor.getComponent(nodeRef, GlyphComponent.getComponentType());
        if (glyphComp == null) return;
        GlyphStyler.enterGlyphHover(accessor, glyphComp);
    }

    @Override
    public void unhover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        GlyphComponent glyphComp = accessor.getComponent(nodeRef, GlyphComponent.getComponentType());
        if (glyphComp == null) return;
        GlyphStyler.exitGlyphHover(accessor, glyphComp);
    }
}
