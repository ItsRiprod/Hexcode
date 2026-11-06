package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.shape;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class ShapeGlyph extends GlyphAsset {
    public static final String TYPE = "Shape";

    public static final float MIN_MODEL_SCALE = 0.1f;
    public static final float MAX_MODEL_SCALE = 4.0f;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var linked = GlyphAsset.of(glyph.linkedGlyph(ShapeGlyphSlots.SHAPE, hexContext), GlyphAsset.class);
        if (linked != null) {
            var linkedEffects = linked.getEffects();
            if (linkedEffects != null)
                hexContext.mutableEffects().compose(linkedEffects);

            Model assembled = GlyphModelUtil.assemble(linked, linkedEffects, resolveScale(glyph, hexContext));
            if (assembled != null) {
                hexContext.mutableEffects().setResolvedModel(assembled);
            } else {
                LOGGER.atWarning().log("shape: glyph '%s' produced no model attachments", linked.getId());
            }
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private float resolveScale(Glyph glyph, HexContext hexContext) {
        float scale = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ShapeGlyphSlots.SCALE, hexContext),
                getSlot(ShapeGlyphSlots.SCALE)).floatValue();
        return Math.clamp(scale, MIN_MODEL_SCALE, MAX_MODEL_SCALE);
    }
}
