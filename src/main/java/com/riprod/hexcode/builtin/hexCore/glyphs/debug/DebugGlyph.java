package com.riprod.hexcode.builtin.hexCore.glyphs.debug;

import java.util.Map;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class DebugGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String ID = "Debug";

    @Override
    public String getId() {
        return ID;
    };

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        dump(glyph, hexContext);

        return glyph.readSlot(DebugGlyphSlots.SLOT, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        dump(glyph, hexContext);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void dump(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> casterRef = hexContext.getCasterRef(hexContext.getAccessor());
        if (casterRef == null || !casterRef.isValid())
            return;
        if (hexContext.getAccessor() == null)
            return;
        PlayerRef pr = hexContext.getAccessor().getComponent(casterRef, PlayerRef.getComponentType());
        if (pr == null)
            return;

        String volatility = volatilityText(hexContext.getHexStats());
        String complexity = complexityText(hexContext);

        Slot slot = glyph.getSlot(DebugGlyphSlots.SLOT);
        boolean wired = slot != null && slot.getFirstLink() != null;

        Message msg = wired
                ? markup(Message.translation("hexcode.debugGlyph.slots")
                        .param("volatility", volatility)
                        .param("complexity", complexity)
                        .param("slots", buildSlotLines(slot, hexContext)))
                : markup(Message.translation("hexcode.debugGlyph.gis")
                        .param("volatility", volatility)
                        .param("complexity", complexity)
                        .param("gis", buildGisLines(hexContext)));

        pr.sendMessage(msg);
        LOGGER.atInfo().log(msg.getAnsiMessage());
    }

    private Message buildSlotLines(Slot slot, HexContext hexContext) {
        Message composite = Message.raw("");
        int index = 0;
        for (String linkId : slot.getLinks()) {
            Glyph linked = hexContext.getGlyph(linkId);
            if (linked == null)
                continue;

            HexVar value = resolveLink(linked, hexContext);

            Message line = markup(Message.translation("hexcode.debugGlyph.slots.line")
                    .param("index", index)
                    .param("value", valueText(value))
                    .param("glyph", linked.getGlyphId())
                    .param("accuracy", String.format("%.2f", linked.getVolatility()))
                    .param("speed", String.format("%.2f", linked.getEfficiency()))
                    .param("metadata", typeLabel(value)));

            if (index > 0)
                composite.insert("\n");
            composite.insert(line);
            index++;
        }
        return composite;
    }

    private Message buildGisLines(HexContext hexContext) {
        Message composite = Message.raw("");
        HexVar defaultVar = hexContext.getDefaultVariable();
        int index = 0;
        for (Map.Entry<String, HexVar> entry : hexContext.getVariables().entrySet()) {
            HexVar value = entry.getValue();
            if (value == null)
                continue;

            Glyph source = hexContext.getGlyph(entry.getKey());
            String glyph = source != null ? source.getGlyphId() : shortId(entry.getKey());

            Message line = markup(Message.translation("hexcode.debugGlyph.slots.gis")
                    .param("index", entry.getKey())
                    .param("value", valueText(value))
                    .param("glyph", glyph)
                    .param("metadata", gisMetadata(value, defaultVar)));

            if (index > 0)
                composite.insert("\n");
            composite.insert(line);
            index++;
        }
        return composite;
    }

    private static HexVar resolveLink(Glyph linked, HexContext hexContext) {
        GlyphAsset linkedAsset = GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
        GlyphHandler handler = linkedAsset != null ? GlyphRegistry.get(linkedAsset.getHandler()) : null;
        if (handler == null || hexContext.isResolving(linked.getId()))
            return null;
        hexContext.pushResolving(linked.getId());
        try {
            return handler.readValue(linked, hexContext);
        } finally {
            hexContext.popResolving();
        }
    }

    private static Message markup(Message message) {
        message.getFormattedMessage().markupEnabled = true;
        return message;
    }

    private static String volatilityText(HexStats tracker) {
        if (tracker == null)
            return "-";
        return String.format("%.1f / %.1f", tracker.getCurrentVolatility(), tracker.getInitialVolatility());
    }

    private static String complexityText(HexContext hexContext) {
        if (hexContext == null)
            return "-";
        return String.format("%.1f", hexContext.getComplexity());
    }

    private static String valueText(HexVar value) {
        if (value == null)
            return "[none]";
        String described = value.describe();
        int split = described.indexOf(": ");
        return split >= 0 ? described.substring(split + 2) : described;
    }

    private static String typeLabel(HexVar value) {
        if (value == null)
            return "[none]";
        return value.getClass().getSimpleName().replace("Var", "");
    }

    private static String gisMetadata(HexVar value, HexVar defaultVar) {
        String label = typeLabel(value);
        return value == defaultVar ? label + " *" : label;
    }

    private static String shortId(String id) {
        if (id == null || id.length() < 8)
            return id == null ? "?" : id;
        return id.substring(0, 8);
    }
}
