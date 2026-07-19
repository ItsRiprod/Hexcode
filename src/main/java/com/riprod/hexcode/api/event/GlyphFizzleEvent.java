package com.riprod.hexcode.api.event;

import com.hypixel.hytale.event.IEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class GlyphFizzleEvent implements IEvent<Void> {

    public enum Reason {
        VOLATILITY_DEPLETED,
        INSUFFICIENT_MANA,
        HANDLER_FAILED,
        NOT_IMPLEMENTED,
        MANUALLY_CANCELLED,
        ERROR,
        GLYPH_DISABLED
    }

    private final Glyph glyph;
    private final Reason reason;
    private final HexContext ctx;
    private final String detail;
    private final Throwable cause;

    public GlyphFizzleEvent(Glyph glyph, Reason reason, HexContext ctx) {
        this(glyph, reason, ctx, null, null);
    }

    public GlyphFizzleEvent(Glyph glyph, Reason reason, HexContext ctx, String detail) {
        this(glyph, reason, ctx, detail, null);
    }

    public GlyphFizzleEvent(Glyph glyph, Reason reason, HexContext ctx, String detail,
            Throwable cause) {
        this.glyph = glyph;
        this.reason = reason;
        this.ctx = ctx;
        this.detail = detail;
        this.cause = cause;
    }

    public Glyph getGlyph() {
        return glyph;
    }

    public Reason getReason() {
        return reason;
    }

    public HexContext getCtx() {
        return ctx;
    }

    public String getDetail() {
        return detail;
    }

    public Throwable getCause() {
        return cause;
    }
}
