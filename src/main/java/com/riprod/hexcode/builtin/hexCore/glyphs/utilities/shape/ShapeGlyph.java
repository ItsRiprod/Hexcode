package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.shape;

import javax.annotation.Nullable;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.HexVarUtil;

public class ShapeGlyph implements GlyphHandler {
    public static final String ID = "Shape";

    public static final float MIN_MODEL_SCALE = 0.1f;
    public static final float MAX_MODEL_SCALE = 4.0f;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset linked = resolveLinkedGlyph(glyph, hexContext);
        if (linked != null) {
            HexStyleAsset linkedStyle = linked.getStyle();
            if (linkedStyle != null) hexContext.mutableStyle().applyOverride(linkedStyle);

            Model assembled = GlyphModelUtil.assemble(linked, linkedStyle, resolveScale(glyph, hexContext));
            if (assembled != null) {
                hexContext.mutableStyle().setResolvedModel(assembled);
            } else {
                LOGGER.atWarning().log("shape: glyph '%s' produced no model attachments", linked.getId());
            }
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static float resolveScale(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        float scale = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ShapeGlyphSlots.SCALE, hexContext),
                asset != null ? asset.getSlot(ShapeGlyphSlots.SCALE) : null).floatValue();
        return Math.clamp(scale, MIN_MODEL_SCALE, MAX_MODEL_SCALE);
    }

    private static @Nullable GlyphAsset resolveLinkedGlyph(Glyph glyph, HexContext hexContext) {
        Slot shapeSlot = glyph.getSlots().get(ShapeGlyphSlots.SHAPE);
        if (shapeSlot == null) return null;
        String linkedId = shapeSlot.getFirstLink();
        if (linkedId == null) return null;
        Glyph linked = hexContext.getGlyph(linkedId);
        if (linked == null) return null;
        return GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
    }
}
