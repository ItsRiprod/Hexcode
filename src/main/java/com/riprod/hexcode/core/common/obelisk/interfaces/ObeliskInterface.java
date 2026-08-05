package com.riprod.hexcode.core.common.obelisk.interfaces;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import org.joml.Vector3i;

public interface ObeliskInterface {

    default void onStateChange(CommandBuffer<EntityStore> buffer, ObeliskBlockComponent obelisk,
            Vector3i obeliskPos, PedestalState previousState, PedestalState newState) {}

    default void onGlyphDrawn(ComponentAccessor<EntityStore> buffer, Ref<EntityStore> playerRef,
            Glyph glyph, ObeliskBlockComponent obelisk) {}

    default void onEnterCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            ObeliskBlockComponent obelisk) {}

    default void onExitCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            ObeliskBlockComponent obelisk) {}

    default void onHover(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            Ref<EntityStore> hoveredRef, ObeliskBlockComponent obelisk) {}

    default void onUnhover(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            Ref<EntityStore> unhoveredRef, ObeliskBlockComponent obelisk) {}
}
