package com.riprod.hexcode.core.common.glyphs.registry;

import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.riprod.hexcode.core.common.node.NodeTypeId;

public final class StyleResolution {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Vector3f FALLBACK_COLOR = new Vector3f(0.6f, 0.6f, 0.6f);
    private static final DebugShape FALLBACK_SHAPE = DebugShape.Cube;

    public record ResolvedStyle(Vector3f color, DebugShape shape, NodeTypeId handlerId) {}

    private StyleResolution() {
    }

    public static ResolvedStyle resolve(SlotAsset slotAsset, String glyphId, String slotKey) {
        String styleId = slotAsset.getStyleId();
        if (styleId == null) {
            LOGGER.atSevere().log("glyph '%s' slot '%s': missing Style; using fallback",
                    glyphId, slotKey);
            return fallback();
        }

        SlotStyleAsset style = SlotStyleAsset.getAssetMap().getAsset(styleId);
        if (style == null) {
            LOGGER.atSevere().log("glyph '%s' slot '%s': style '%s' not found; using fallback",
                    glyphId, slotKey, styleId);
            return fallback();
        }

        NodeTypeId handler = "slot.standard".equals(style.getNodeHandlerId())
                ? NodeTypeId.SLOT_STANDARD
                : new NodeTypeId(style.getNodeHandlerId());
        return new ResolvedStyle(style.getColor(), style.getShape(), handler);
    }

    private static ResolvedStyle fallback() {
        return new ResolvedStyle(FALLBACK_COLOR, FALLBACK_SHAPE, NodeTypeId.SLOT_STANDARD);
    }
}
