package com.riprod.hexcode.command.hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.BooleanSlot;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.BooleanSlotState;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.LinkSlot;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.NamedSlot;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.component.EncodingStroke;
import com.riprod.hexcode.core.common.hexes.codec.DecodeIssue;
import com.riprod.hexcode.core.common.hexes.codec.DecodeResult;
import com.riprod.hexcode.core.common.hexes.codec.HexCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.utils.LogScopes;

public class HexTestRoundtripCommand extends AbstractPlayerCommand {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CMD);

    private static final float ACC_TOL = 0.01f;
    private static final float SPEED_TOL = 0.01f;
    private static final float POS_TOL = 0.05f;

    public HexTestRoundtripCommand() {
        super("test-roundtrip", "encode+decode the active hex and verify structural equality");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        addAliases("tr");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        var execComp =
                store.getComponent(playerEntityRef, ExecutionComponent.getComponentType());
        if (execComp == null) {
            send(playerRef, "no execution component found on player");
            return;
        }

        Hex active = execComp.getQueuedHex();
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

        componentWireTest(playerRef, active);
        canonicalizeTest(playerRef, active);
    }

    private static void canonicalizeTest(PlayerRef playerRef, Hex active) {
        var source = HexCodec.serialize(active);
        String canon;
        try {
            canon = HexCodec.canonicalizeSnapshot(source);
        } catch (Exception e) {
            send(playerRef, "CANONICALIZE FAIL: " + e.getMessage());
            return;
        }
        if (!canon.equals(HexCodec.canonicalizeSnapshot(source))) {
            send(playerRef, "CANONICALIZE FAIL: not deterministic");
            return;
        }
        if (!canon.equals(HexCodec.canonicalizeSnapshot(canon))) {
            send(playerRef, "CANONICALIZE FAIL: not idempotent");
            return;
        }

        var decoded = HexCodec.deserialize(canon).getHex();
        if (decoded == null) {
            send(playerRef, "CANONICALIZE FAIL: canonical string does not decode");
            return;
        }
        var expectedY = new ArrayList<Float>();
        for (Glyph g : active.getGlyphs()) expectedY.add(g.getPosition().y);
        var actualY = new ArrayList<Float>();
        for (Glyph g : decoded.getGlyphs()) {
            Vector3f p = g.getPosition();
            if (Math.abs(p.x) > POS_TOL || Math.abs(p.z) > POS_TOL) {
                send(playerRef, "CANONICALIZE FAIL: x/z not stripped on " + g.getId());
                return;
            }
            var r = g.getRotation();
            if (Math.abs(r.x) > 0.03f || Math.abs(r.y) > 0.03f || Math.abs(r.z) > 0.03f) {
                send(playerRef, "CANONICALIZE FAIL: rotation not stripped on " + g.getId());
                return;
            }
            actualY.add(p.y);
        }
        expectedY.sort(Float::compare);
        actualY.sort(Float::compare);
        if (expectedY.size() != actualY.size()) {
            send(playerRef, "CANONICALIZE FAIL: glyph count changed");
            return;
        }
        for (int i = 0; i < expectedY.size(); i++) {
            if (Math.abs(expectedY.get(i) - actualY.get(i)) > POS_TOL) {
                send(playerRef, "CANONICALIZE FAIL: y heights not preserved");
                return;
            }
        }
        if (!Objects.equals(decoded.getDisplayName(), active.getDisplayName())) {
            send(playerRef, "CANONICALIZE FAIL: display name lost");
            return;
        }
        if (encodingDiff(active.getEncoding(), decoded.getEncoding()) != null) {
            send(playerRef, "CANONICALIZE FAIL: encoding lost");
            return;
        }
        send(playerRef, "CANONICALIZE OK (deterministic, idempotent, x/z+rotation stripped, y kept)");
    }

    private static void componentWireTest(PlayerRef playerRef, Hex active) {
        var payloadA = HexCodec.serialize(active);
        var variant = active.clone();
        variant.setDisplayName("component-wire-b");
        var payloadB = HexCodec.serialize(variant);
        if (payloadA.equals(payloadB)) {
            send(playerRef, "COMPONENT WIRE FAIL: variant payload not byte-distinct");
            return;
        }

        var carrier = GlyphAsset.getAssetMap().getAsset(active.getGlyphs().get(0).getGlyphId());
        if (carrier == null) {
            send(playerRef, "COMPONENT WIRE SKIP: carrier asset unresolved");
            return;
        }

        var host = new Hex();
        var instances = new ArrayList<Glyph>(3);
        for (int i = 0; i < 3; i++) {
            var instance = new Glyph(carrier, 0.9f, 0.8f);
            instance.setPayload(i < 2 ? payloadA : payloadB);
            instance.setPosition(new Vector3f(0, i, 0));
            host.put(instance.getId(), instance);
            instances.add(instance);
        }
        host.setFirstGlyphId(instances.get(0).getId());
        for (int i = 0; i < 3; i++) {
            var port = new LinkSlot();
            port.addLink(instances.get((i + 1) % 3).getId());
            instances.get(i).getSlots().put("Port_In", port);
            var toggle = new BooleanSlot();
            toggle.setState(BooleanSlotState.POSITIVE);
            instances.get(i).getSlots().put("Toggle", toggle);
            var label = new NamedSlot();
            label.setValue("Port_" + i);
            instances.get(i).getSlots().put("Label", label);
        }

        var result = HexCodec.deserialize(HexCodec.serialize(host));
        if (result.getHex() == null) {
            send(playerRef, "COMPONENT WIRE FAIL: decode returned null");
            return;
        }

        var reference = new BooleanSlot();
        reference.setState(BooleanSlotState.POSITIVE);
        byte[] expectedState = reference.encodeState();

        var payloads = new ArrayList<String>();
        var labelStates = new HashSet<String>();
        for (Glyph g : result.getHex().getGlyphs()) {
            payloads.add(g.getPayload());
            var portIn = g.getSlot("Port_In");
            if (portIn == null || portIn.getLinks().length != 1) {
                send(playerRef, "COMPONENT WIRE FAIL: dynamic Port_In dropped on " + g.getId());
                return;
            }
            var toggle = g.getSlot("Toggle");
            if (toggle == null || !Arrays.equals(toggle.encodeState(), expectedState)) {
                send(playerRef, "COMPONENT WIRE FAIL: Toggle state lost on " + g.getId());
                return;
            }
            var label = g.getSlot("Label");
            byte[] labelState = label == null ? null : label.encodeState();
            if (labelState == null) {
                send(playerRef, "COMPONENT WIRE FAIL: Label state lost on " + g.getId());
                return;
            }
            labelStates.add(new String(labelState, StandardCharsets.UTF_8));
        }
        if (payloads.contains(null) || new HashSet<>(payloads).size() != 2) {
            send(playerRef, "COMPONENT WIRE FAIL: payload dedup mapping broken: "
                    + new HashSet<>(payloads).size() + " distinct");
            return;
        }
        if (!new HashSet<>(payloads).equals(new HashSet<>(List.of(payloadA, payloadB)))) {
            send(playerRef, "COMPONENT WIRE FAIL: payload bytes not verbatim");
            return;
        }
        if (!labelStates.equals(new HashSet<>(List.of("Port_0", "Port_1", "Port_2")))) {
            send(playerRef, "COMPONENT WIRE FAIL: Label slot states lost: " + labelStates);
            return;
        }
        send(playerRef, "COMPONENT WIRE OK (dedup, verbatim payloads, dynamic ports, state, labels)");
    }

    @Nullable
    private static String structuralDiff(Hex a, Hex b) {
        if (a.getGlyphs().size() != b.getGlyphs().size()) {
            return "glyph count: " + a.getGlyphs().size() + " vs " + b.getGlyphs().size();
        }
        if (!Objects.equals(a.getDisplayName(), b.getDisplayName())) {
            return "displayName: " + a.getDisplayName() + " vs " + b.getDisplayName();
        }
        String encodingDiff = encodingDiff(a.getEncoding(), b.getEncoding());
        if (encodingDiff != null) return encodingDiff;

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
            if (!Objects.equals(ga.getPayload(), gb.getPayload())) {
                return "[" + i + "] Payload differs";
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

    @Nullable
    private static String encodingDiff(@Nullable List<EncodingStroke> a, @Nullable List<EncodingStroke> b) {
        int sizeA = a != null ? a.size() : 0;
        int sizeB = b != null ? b.size() : 0;
        if (sizeA != sizeB) return "encoding stroke count: " + sizeA + " vs " + sizeB;
        for (int i = 0; i < sizeA; i++) {
            EncodingStroke sa = a.get(i);
            EncodingStroke sb = b.get(i);
            if (!Objects.equals(sa.getShapeId(), sb.getShapeId())) {
                return "encoding[" + i + "] shape: " + sa.getShapeId() + " vs " + sb.getShapeId();
            }
            if (Math.abs(sa.getRelativeSize() - sb.getRelativeSize()) > ACC_TOL) {
                return "encoding[" + i + "] size: " + sa.getRelativeSize() + " vs " + sb.getRelativeSize();
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
        LOGGER.atFine().log(message);
    }
}
