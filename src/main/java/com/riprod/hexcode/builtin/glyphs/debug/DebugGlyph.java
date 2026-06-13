package com.riprod.hexcode.builtin.glyphs.debug;

import java.util.List;
import java.util.Map;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;

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
        Ref<EntityStore> casterRef = hexContext.getCasterRef();
        if (casterRef == null || !casterRef.isValid())
            return;
        if (hexContext.getAccessor() == null)
            return;
        PlayerRef pr = hexContext.getAccessor().getComponent(casterRef, PlayerRef.getComponentType());
        if (pr == null)
            return;

        StringBuilder sb = new StringBuilder();

        float mana = hexContext.getHexRoot().getCurrentMana(hexContext.getAccessor());
        float maxVol = hexContext.getVolatilityTracker().getStartingBudget();
        float curVol = hexContext.getVolatilityTracker().getRemainingBudget();
        sb.append(String.format("Mana: %.1f\n", mana));
        sb.append(String.format("Volatility: %.1f / %.1f\n", curVol, maxVol));

        var nextLinks = glyph.getNextLinks();
        if (!nextLinks.isEmpty()) {
            sb.append("Next:\n");
            for (String nextId : nextLinks) {
                Glyph next = hexContext.getGlyph(nextId);
                if (next == null) {
                    sb.append("  - [missing ").append(shortId(nextId)).append("]\n");
                    continue;
                }
                sb.append(String.format("  - %s (vol %.2f, eff %.2f)\n",
                        next.getGlyphId(), next.getVolatility(), next.getEfficiency()));
            }
        }

        // multi-read: dump every wired slot input
        List<HexVar> slotValues = glyph.readSlotAll(DebugGlyphSlots.SLOT, hexContext);
        if (!slotValues.isEmpty()) {
            if (slotValues.size() == 1) {
                sb.append("Slot: ").append(slotValues.get(0).describe()).append("\n");
            } else {
                sb.append("Slot:\n");
                for (int i = 0; i < slotValues.size(); i++) {
                    HexVar sv = slotValues.get(i);
                    sb.append("  [").append(i).append("] ").append(sv.describe()).append("\n");
                }
            }
        }

        HexVar defaultVar = hexContext.getDefaultVariable();
        sb.append("Default: ").append(defaultVar != null ? defaultVar.describe() : "[none]").append("\n");

        Map<String, HexVar> vars = hexContext.getVariables();
        if (vars.isEmpty()) {
            sb.append("Vars: [empty]");
        } else {
            sb.append("Vars:\n");
            for (Map.Entry<String, HexVar> entry : vars.entrySet()) {
                HexVar v = entry.getValue();
                if (v == null)
                    continue;
                sb.append("  ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(v.describe())
                        .append("\n");
            }
        }

        String msg = sb.toString();
        pr.sendMessage(Message.raw(msg));
        LOGGER.atInfo().log(msg);
    }

    private static String shortId(String id) {
        if (id == null || id.length() < 8)
            return id == null ? "?" : id;
        return id.substring(0, 8);
    }
}
