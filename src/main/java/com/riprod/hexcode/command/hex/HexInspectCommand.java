package com.riprod.hexcode.command.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bson.BsonDocument;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;

public class HexInspectCommand extends AbstractPlayerCommand {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final FlagArg detailedFlag;

    public HexInspectCommand() {
        super("inspect", "Print the glyph tree of the active hex on the held staff");
        addAliases("i");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);

        this.detailedFlag = this.withFlagArg("detailed", "raw JSON dump of hex data");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        boolean detailed = detailedFlag.get(context);

        HexStaffComponent staff = CasterInventory.getHexStaffComponent(store, playerEntityRef);
        var execComp = store.getComponent(playerEntityRef, ExecutionComponent.getComponentType());
        if (staff == null) {
            send(playerRef, "You are not holding a hex staff");
            return;
        }
        if (execComp == null) {
            send(playerRef, "No execution component found on player");
            return;
        }

        Hex hex = execComp.getQueuedHex();
        if (hex == null) {
            send(playerRef, "No active hex on staff");
            return;
        }

        if (detailed) {
            printDetailed(playerRef, hex);
        } else {
            printFormatted(playerRef, hex, staff.getStyleId());
        }
    }

    private void printDetailed(PlayerRef playerRef, Hex hex) {
        ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
        BsonDocument doc = Hex.CODEC.encode(hex, extraInfo);
        String json = BsonUtil.toJson(doc);

        send(playerRef, "== Hex Raw Data ==");
        send(playerRef, json);
    }

    private void printFormatted(PlayerRef playerRef, Hex hex, String style) {
        Map<String, Integer> indexMap = new HashMap<>();
        List<Glyph> effectGlyphs = new ArrayList<>();
        Set<String> valueGlyphIds = new LinkedHashSet<>();
        int idx = 1;

        String currentId = hex.getFirstGlyphId();
        Set<String> walked = new HashSet<>();
        List<String> execQueue = new ArrayList<>();
        if (currentId != null) execQueue.add(currentId);

        while (!execQueue.isEmpty()) {
            String id = execQueue.removeFirst();
            if (id == null || walked.contains(id)) continue;
            walked.add(id);

            Glyph g = hex.get(id);
            if (g == null) continue;

            indexMap.put(id, idx++);
            effectGlyphs.add(g);
            for (Map.Entry<String, Slot> entry : g.getSlots().entrySet()) {
                if (isBranchSlot(g, entry.getKey())) {
                    for (String linkId : entry.getValue().getLinks()) {
                        execQueue.add(linkId);
                    }
                }
            }
        }

        for (Glyph g : hex.getGlyphs()) {
            if (!indexMap.containsKey(g.getId())) {
                indexMap.put(g.getId(), idx++);
                valueGlyphIds.add(g.getId());
            }
        }

        for (Glyph effect : effectGlyphs) {
            collectValues(hex, effect, valueGlyphIds, indexMap);
        }

        List<String> lines = new ArrayList<>();
        lines.add("== Hex Staff (style: " + style + ") ==");
        lines.add("");

        for (Glyph g : effectGlyphs) {
            int num = indexMap.get(g.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("  ").append(num).append(". ").append(formatGlyph(g));

            lines.add(sb.toString());

            for (Map.Entry<String, Slot> entry : g.getSlots().entrySet()) {
                String key = entry.getKey();
                String[] links = entry.getValue().getLinks();
                if (links.length == 0) continue;
                if (isBranchSlot(g, key)) continue;
                StringBuilder slotSb = new StringBuilder("     ").append(key).append(": ");
                for (int i = 0; i < links.length; i++) {
                    Glyph v = hex.get(links[i]);
                    if (v == null) continue;
                    Integer linkIdx = indexMap.get(v.getId());
                    slotSb.append("#").append(linkIdx != null ? linkIdx : "?")
                            .append(" ").append(shortName(v.getGlyphId()));
                    if (i < links.length - 1) slotSb.append(", ");
                }
                lines.add(slotSb.toString());
            }

            for (Map.Entry<String, Slot> entry : g.getSlots().entrySet()) {
                String key = entry.getKey();
                if (!isBranchSlot(g, key)) continue;
                String[] links = entry.getValue().getLinks();
                if (links.length == 0) continue;
                StringBuilder branchSb = new StringBuilder("     ")
                        .append(key.equals(Glyph.NEXT_SLOT) ? "next" : key).append(" -> [");
                for (int i = 0; i < links.length; i++) {
                    Integer linkIdx = indexMap.get(links[i]);
                    branchSb.append(linkIdx != null ? linkIdx : "?");
                    if (i < links.length - 1) branchSb.append(", ");
                }
                branchSb.append("]");
                lines.add(branchSb.toString());
            }
        }

        if (!valueGlyphIds.isEmpty()) {
            lines.add("");
            lines.add("  Values:");

            for (String vId : valueGlyphIds) {
                Glyph v = hex.get(vId);
                if (v == null) continue;

                StringBuilder sb = new StringBuilder();
                sb.append("  #").append(indexMap.get(vId)).append(" ").append(formatGlyph(v));

                List<String> parts = new ArrayList<>();
                for (Map.Entry<String, Slot> entry : v.getSlots().entrySet()) {
                    String key = entry.getKey();
                    if (isBranchSlot(v, key)) continue;
                    for (String linkId : entry.getValue().getLinks()) {
                        Glyph nested = hex.get(linkId);
                        if (nested == null) continue;
                        parts.add(key + " <- #" + indexMap.get(nested.getId()) + " " + shortName(nested.getGlyphId()));
                    }
                }
                if (!parts.isEmpty()) {
                    sb.append(" | ").append(String.join(", ", parts));
                }

                lines.add(sb.toString());
            }
        }

        for (String line : lines) {
            send(playerRef, line);
        }
    }

    private void collectValues(Hex hex, Glyph glyph, Set<String> valueGlyphIds, Map<String, Integer> indexMap) {
        for (Map.Entry<String, Slot> entry : glyph.getSlots().entrySet()) {
            if (isBranchSlot(glyph, entry.getKey())) continue;
            for (String refId : entry.getValue().getLinks()) {
                Glyph v = hex.get(refId);
                if (v != null && valueGlyphIds.add(v.getId())) {
                    collectValues(hex, v, valueGlyphIds, indexMap);
                }
            }
        }
    }

    private static boolean isBranchSlot(Glyph glyph, String slotKey) {
        if (Glyph.NEXT_SLOT.equals(slotKey)) return true;
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        if (asset == null) return false;
        SlotAsset slotAsset = asset.getSlot(slotKey);
        return slotAsset != null && "Next".equals(slotAsset.getStyleId());
    }

    private String formatGlyph(Glyph glyph) {
        StringBuilder sb = new StringBuilder();
        sb.append(shortName(glyph.getGlyphId()));

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        if (asset != null) {
            sb.append(" mana=").append(asset.getManaConsumption());
        }

        sb.append(" vol=").append(String.format("%.2f", glyph.getVolatility()));
        sb.append(" eff=").append(String.format("%.2f", glyph.getEfficiency()));
        return sb.toString();
    }

    private String shortName(String glyphId) {
        int colon = glyphId.indexOf(':');
        return colon >= 0 ? glyphId.substring(colon + 1) : glyphId;
    }

    private void send(PlayerRef playerRef, String message) {
        playerRef.sendMessage(Message.raw(message));
        LOGGER.atInfo().log(message);
    }
}
