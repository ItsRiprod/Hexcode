package com.riprod.hexcode.core.common.hexes.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.hexes.codec.DecodeResult;
import com.riprod.hexcode.core.common.hexes.codec.HexCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class HexUtils {

    public static void validate(Hex hex) {
        if (hex.getGlyphs().isEmpty()) return;
        removeUnregisteredGlyphs(hex);
        cleanDanglingLinks(hex);
    }

    public static void repair(Hex hex) {
        if (hex.getGlyphs().isEmpty()) return;
        cleanDanglingLinks(hex);
    }

    public static void compress(Hex hex) {
        cleanDanglingLinks(hex);
        rekeyGlyphs(hex);
    }

    private static void rekeyGlyphs(Hex hex) {
        List<Glyph> glyphs = hex.getGlyphs();
        if (glyphs.isEmpty()) return;

        String prefix = String.format("%04x", ThreadLocalRandom.current().nextInt(0x10000));
        Map<String, String> idMap = new HashMap<>();
        int counter = 0;
        for (Glyph glyph : glyphs) {
            idMap.put(glyph.getId(), prefix + "-" + counter++);
        }

        String oldFirstId = hex.getFirstGlyphId();

        for (Glyph glyph : glyphs) {
            hex.remove(glyph.getId());
        }

        for (Glyph glyph : glyphs) {
            glyph.setId(idMap.get(glyph.getId()));

            for (Slot slot : glyph.getSlots().values()) {
                String[] oldLinks = slot.getLinks();
                List<String> remapped = new ArrayList<>(oldLinks.length);
                for (String oldId : oldLinks) {
                    String mapped = idMap.get(oldId);
                    if (mapped != null) remapped.add(mapped);
                }
                slot.setLinks(remapped.toArray(String[]::new));
            }

            hex.put(glyph.getId(), glyph);
        }

        if (oldFirstId != null) {
            hex.setFirstGlyphId(idMap.get(oldFirstId));
        }
    }

    public static String serialize(Hex hex) {
        return HexCodec.serialize(hex);
    }

    public static DecodeResult deserializeWithResult(String data) {
        return HexCodec.deserialize(data);
    }

    public static Hex deserialize(String data) {
        return HexCodec.deserialize(data).getHex();
    }

    private static void removeUnregisteredGlyphs(Hex hex) {
        Set<String> toRemove = new HashSet<>();
        for (Glyph glyph : hex.getGlyphs()) {
            GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
            if (asset == null || GlyphRegistry.get(asset.getHandler()) == null) {
                toRemove.add(glyph.getId());
            }
        }
        for (String id : toRemove) {
            hex.removeGlyph(id);
        }
    }

    private static void cleanDanglingLinks(Hex hex) {
        Set<String> validIds = new HashSet<>();
        for (Glyph g : hex.getGlyphs()) {
            validIds.add(g.getId());
        }
        for (Glyph glyph : hex.getGlyphs()) {
            for (Slot slot : glyph.getSlots().values()) {
                String[] links = slot.getLinks();
                if (links.length == 0) continue;
                List<String> kept = new ArrayList<>(links.length);
                for (String linkId : links) {
                    if (validIds.contains(linkId)) kept.add(linkId);
                }
                slot.setLinks(kept.toArray(String[]::new));
            }
        }
    }
}
