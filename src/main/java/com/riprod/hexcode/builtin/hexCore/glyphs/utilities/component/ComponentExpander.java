package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component;

import java.util.HashSet;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.CastTransform;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot.SlotGlyphSlots;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

public class ComponentExpander implements CastTransform {

    private static final int MAX_PASSES = 256;

    @Override
    public void apply(HexContext context, ComponentAccessor<EntityStore> accessor) {
        var hex = context.getHex();
        if (hex == null) return;

        var failed = new HashSet<String>();
        for (int pass = 0; pass < MAX_PASSES; pass++) {
            var expanded = false;
            for (Glyph instance : hex.getGlyphs()) {
                if (!instance.isComponentInstance() || failed.contains(instance.getId())) {
                    continue;
                }
                if (expandInstance(hex, instance, accessor)) {
                    expanded = true;
                } else {
                    failed.add(instance.getId());
                }
            }
            if (!expanded) return;
        }
    }

    private static boolean expandInstance(Hex host, Glyph instance,
            ComponentAccessor<EntityStore> accessor) {
        Hex view = instance.payloadView(accessor);
        if (view == null) return false;
        Hex payload = view.clone();

        HexUtils.validate(payload);
        if (payload.getGlyphs().isEmpty() || payload.getFirstGlyphId() == null
                || payload.get(payload.getFirstGlyphId()) == null) {
            return false;
        }

        HexUtils.rekeyPrefixed(payload, instance.getId() + "/");
        String entry = payload.getFirstGlyphId();

        for (var port : ComponentPorts.list(payload)) {
            var slotGlyph = port.slotGlyph();
            slotGlyph.setBoundaryOrigin(true);
            var hostPort = instance.getSlot(port.name());
            if (hostPort == null) continue;
            if (port.isFlow()) {
                var next = slotGlyph.getOrCreateSlot(Glyph.NEXT_SLOT);
                if (next != null) {
                    for (String link : hostPort.getLinks()) next.addLink(link);
                }
            } else {
                slotGlyph.getSlots().put(SlotGlyphSlots.PORT, ComponentPorts.typedPort(port, hostPort));
            }
        }

        for (Glyph glyph : host.getGlyphs()) {
            for (Slot slot : glyph.getSlots().values()) {
                slot.replaceLink(instance.getId(), entry);
            }
        }

        boolean wasFirst = instance.getId().equals(host.getFirstGlyphId());
        for (Glyph glyph : payload.getGlyphs()) {
            host.put(glyph.getId(), glyph);
        }
        host.removeGlyph(instance.getId());
        if (wasFirst) {
            host.setFirstGlyphId(entry);
        }
        return true;
    }
}
