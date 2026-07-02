package com.riprod.hexcode.core.common.positioning;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class GlyphPositioner {

    private GlyphPositioner() {
    }

    public static void PositionGlyphs(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> ownerRef,
            Ref<EntityStore> rootGlyph) {
        if (rootGlyph == null || !rootGlyph.isValid()) {
            return;
        }
        TransformComponent rootTransform = accessor.getComponent(rootGlyph,
                TransformComponent.getComponentType());
        if (rootTransform == null) {
            return;
        }
        rootTransform.getRotation().set(0f, 0f, 0f);
    }
}
