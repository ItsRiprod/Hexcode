package com.riprod.hexcode.api.event;

import com.hypixel.hytale.event.IEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class GlyphExecuteEvent implements IEvent<Void> {

    private final String nodeId;
    private final Glyph glyph;
    private final HexContext ctx;

    public GlyphExecuteEvent(String nodeId, Glyph glyph, HexContext ctx) {
        this.nodeId = nodeId;
        this.glyph = glyph;
        this.ctx = ctx;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Glyph getGlyph() {
        return glyph;
    }

    public HexContext getCtx() {
        return ctx;
    }
}
