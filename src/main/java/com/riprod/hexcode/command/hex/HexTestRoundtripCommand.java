package com.riprod.hexcode.command.hex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.codec.DecodeIssue;
import com.riprod.hexcode.core.common.hexes.codec.DecodeResult;
import com.riprod.hexcode.core.common.hexes.codec.HexCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class HexTestRoundtripCommand extends AbstractPlayerCommand {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final float ACC_TOL = 0.01f;
    private static final float SPEED_TOL = 0.01f;
    private static final float POS_TOL = 0.05f;

    public HexTestRoundtripCommand() {
        super("test-roundtrip", "encode+decode the active hex and verify structural equality");
        addAliases("tr");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        HexcasterIdleComponent execComp =
                store.getComponent(playerEntityRef, HexcasterIdleComponent.getComponentType());
        if (execComp == null) {
            send(playerRef, "no execution component found on player");
            return;
        }

        Hex active = execComp.getActiveHex();
        if (active == null) {
            send(playerRef, "no hex selected on your staff");
            return;
        }

        Hex expected = active.clone();
        HexUtils.validate(expected);
        HexUtils.compress(expected);

        String serialized = HexCodec.serialize(active);
        send(playerRef, "serialized: " + serialized.length() + " chars");
        send(playerRef, serialized);

        DecodeResult result = HexCodec.deserialize(serialized);
        for (DecodeIssue issue : result.getIssues()) {
            send(playerRef, "  " + issue);
        }
        if (result.getHex() == null) {
            send(playerRef, "ROUNDTRIP FAIL: decode returned null");
            return;
        }

        String diff = structuralDiff(expected, result.getHex());
        if (diff != null) {
            send(playerRef, "ROUNDTRIP FAIL: " + diff);
        } else {
            send(playerRef, "ROUNDTRIP OK (" + result.getHex().getGlyphs().size() + " glyphs)");
        }
    }

    @Nullable
    private static String structuralDiff(Hex a, Hex b) {
        if (a.getGlyphs().size() != b.getGlyphs().size()) {
            return "glyph count: " + a.getGlyphs().size() + " vs " + b.getGlyphs().size();
        }

        List<Glyph> aOrder = canonicalOrder(a);
        List<Glyph> bOrder = canonicalOrder(b);

        Map<String, Integer> aPos = new HashMap<>(aOrder.size() * 2);
        Map<String, Integer> bPos = new HashMap<>(bOrder.size() * 2);
        for (int i = 0; i < aOrder.size(); i++) aPos.put(aOrder.get(i).getId(), i);
        for (int i = 0; i < bOrder.size(); i++) bPos.put(bOrder.get(i).getId(), i);

        Integer aFirst = aPos.get(a.getFirstGlyphId());
        Integer bFirst = bPos.get(b.getFirstGlyphId());
        if (aFirst == null || !aFirst.equals(bFirst)) {
            return "first glyph canonical index mismatch";
        }

        for (int i = 0; i < aOrder.size(); i++) {
            Glyph ga = aOrder.get(i);
            Glyph gb = bOrder.get(i);

            if (!ga.getGlyphId().equals(gb.getGlyphId())) {
                return "[" + i + "] GlyphId: " + ga.getGlyphId() + " vs " + gb.getGlyphId();
            }
            if (Math.abs(ga.getVolatility() - gb.getVolatility()) > ACC_TOL) {
                return "[" + i + "] Accuracy: " + ga.getVolatility() + " vs " + gb.getVolatility();
            }
            if (Math.abs(ga.getEfficiency() - gb.getEfficiency()) > SPEED_TOL) {
                return "[" + i + "] Speed: " + ga.getEfficiency() + " vs " + gb.getEfficiency();
            }
            Vector3f pa = ga.getPosition();
            Vector3f pb = gb.getPosition();
            if (Math.abs(pa.x - pb.x) > POS_TOL
                    || Math.abs(pa.y - pb.y) > POS_TOL
                    || Math.abs(pa.z - pb.z) > POS_TOL) {
                return "[" + i + "] RelativePosition: (" + pa.x + ", " + pa.y + ", " + pa.z + ") vs ("
                        + pb.x + ", " + pb.y + ", " + pb.z + ")";
            }

            Map<String, Slot> slotsA = ga.getSlots();
            Map<String, Slot> slotsB = gb.getSlots();
            if (slotsA.size() != slotsB.size()) {
                return "[" + i + "] slot count: " + slotsA.size() + " vs " + slotsB.size();
            }
            List<String> keysA = new ArrayList<>(slotsA.keySet());
            List<String> keysB = new ArrayList<>(slotsB.keySet());
            if (!keysA.equals(keysB)) {
                return "[" + i + "] slot key order: " + keysA + " vs " + keysB;
            }
            for (String name : keysA) {
                String[] linksA = slotsA.get(name).getLinks();
                String[] linksB = slotsB.get(name).getLinks();
                if (linksA.length != linksB.length) {
                    return "[" + i + "] slot '" + name + "' link count: "
                            + linksA.length + " vs " + linksB.length;
                }
                for (int k = 0; k < linksA.length; k++) {
                    Integer la = aPos.get(linksA[k]);
                    Integer lb = bPos.get(linksB[k]);
                    if (la == null || !la.equals(lb)) {
                        return "[" + i + "] slot '" + name + "' link[" + k + "] canonical index: "
                                + la + " vs " + lb;
                    }
                }
            }
        }
        return null;
    }

    private static List<Glyph> canonicalOrder(Hex hex) {
        List<Glyph> glyphs = hex.getGlyphs();
        glyphs.sort(Comparator.comparing(Glyph::getId));
        String firstId = hex.getFirstGlyphId();
        if (firstId != null) {
            for (int i = 0; i < glyphs.size(); i++) {
                if (firstId.equals(glyphs.get(i).getId())) {
                    if (i != 0) {
                        Glyph first = glyphs.remove(i);
                        glyphs.addFirst(first);
                    }
                    break;
                }
            }
        }
        return glyphs;
    }

    private static void send(PlayerRef playerRef, String message) {
        playerRef.sendMessage(Message.raw(message));
        LOGGER.atInfo().log(message);
    }
}
