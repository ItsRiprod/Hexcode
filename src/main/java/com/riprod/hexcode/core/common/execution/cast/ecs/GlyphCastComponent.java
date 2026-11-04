package com.riprod.hexcode.core.common.execution.cast.ecs;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public interface GlyphCastComponent extends CastComponent {

    boolean onGlyph(@Nonnull Glyph glyph, @Nonnull HexContext ctx, @Nonnull CommandBuffer<EntityStore> buffer);
}
