package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.LinkSlot;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.named.NamedSlot;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean.TrileanSlot;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot.SlotGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot.SlotGlyphSlots;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.node.NodeConfig;

public final class ComponentPorts {

    public record Port(String name, int mode, Glyph slotGlyph) {

        public boolean isFlow() {
            return mode == SlotGlyph.MODE_NEXT;
        }
    }

    private ComponentPorts() {
    }

    public static List<Port> list(Hex payload) {
        var slotGlyphs = new ArrayList<Glyph>();
        for (Glyph glyph : payload.getGlyphs()) {
            if (SlotGlyph.isSlotGlyph(glyph)) slotGlyphs.add(glyph);
        }
        slotGlyphs.sort(Comparator.comparingDouble((Glyph g) -> g.getPosition().y)
                .thenComparing(Glyph::getId));

        var ports = new ArrayList<Port>(slotGlyphs.size());
        var used = new HashSet<String>();
        int nextCount = 0;
        int inputCount = 0;
        int toggleCount = 0;
        for (Glyph slotGlyph : slotGlyphs) {
            int mode = SlotGlyph.mode(slotGlyph);
            String base = portName(slotGlyph);
            if (base == null || base.isBlank()) {
                base = switch (mode) {
                    case SlotGlyph.MODE_INPUT -> "Input_" + ++inputCount;
                    case SlotGlyph.MODE_TRILEAN -> "Toggle_" + ++toggleCount;
                    default -> "Next_" + ++nextCount;
                };
            }
            String name = base;
            int suffix = 2;
            while (!used.add(name)) {
                name = base + "_" + suffix++;
            }
            ports.add(new Port(name, mode, slotGlyph));
        }
        return ports;
    }

    public static Map<String, SlotConfig> schema(Glyph instance) {
        Hex view = instance.payloadView(null);
        if (view == null) return Map.of();
        var schema = new LinkedHashMap<String, SlotConfig>();
        for (Port port : list(view)) {
            var config = nodeConfigFor(port.mode());
            if (config != null) schema.put(port.name(), config);
        }
        return schema;
    }

    private static SlotConfig nodeConfigFor(int mode) {
        var id = switch (mode) {
            case SlotGlyph.MODE_INPUT -> "Input";
            case SlotGlyph.MODE_TRILEAN -> "Boolean";
            default -> "Next";
        };
        return NodeConfig.getAssetMap().getAsset(id) instanceof SlotConfig config ? config : null;
    }

    @Nullable
    private static String portName(Glyph slotGlyph) {
        return slotGlyph.getSlot(SlotGlyphSlots.NAME) instanceof NamedSlot named
                ? named.getValue() : null;
    }

    public static Slot typedPort(Port port, @Nullable Slot persisted) {
        if (port.mode() == SlotGlyph.MODE_TRILEAN) {
            var typed = new TrileanSlot();
            copyInto(typed, persisted);
            return typed;
        }
        var typed = new LinkSlot();
        copyInto(typed, persisted);
        return typed;
    }

    private static void copyInto(Slot typed, @Nullable Slot persisted) {
        if (persisted == null) return;
        typed.setLinks(Arrays.copyOf(persisted.getLinks(), persisted.getLinks().length));
        var state = persisted.encodeState();
        if (state != null) typed.decodeState(state);
    }
}
