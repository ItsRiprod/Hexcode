package com.riprod.hexcode.builtin.counterspell.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class GlyphDiagnosticListener implements Consumer<GlyphFizzleEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void accept(GlyphFizzleEvent event) {
        Glyph glyph = event.getGlyph();
        String glyphId = glyph != null ? glyph.getGlyphId() : "<null>";
        HexContext ctx = event.getCtx();
        Ref<EntityStore> caster = null;
        if (ctx != null && ctx.getHexRoot() != null) {
            caster = ctx.getHexRoot().getSourceRef(event.getCtx().getAccessor());
        }
        String casterStr = caster != null ? caster.toString() : "<null>";
        String detail = event.getDetail();
        Throwable cause = event.getCause();

        if (cause != null) {
            LOGGER.atSevere().withCause(cause).log(
                    "[fizzle] glyph=%s reason=%s caster=%s detail=%s",
                    glyphId, event.getReason(), casterStr,
                    detail != null ? detail : "<none>");
        } else {
            LOGGER.atWarning().log(
                    "[fizzle] glyph=%s reason=%s caster=%s detail=%s",
                    glyphId, event.getReason(), casterStr,
                    detail != null ? detail : "<none>");
        }
    }
}
