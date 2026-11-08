package com.riprod.hexcode.core.common.execution.cast.ecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public interface ConstructCastComponent extends CastComponent {

    default void onAttach(@Nonnull Ref<EntityStore> host, @Nullable Glyph holder, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    default void onRelease(@Nonnull Ref<EntityStore> host, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer) {
    }
}
