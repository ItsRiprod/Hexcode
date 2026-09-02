package com.riprod.hexcode.core.common.hexes.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;

public class Hex {

    private Map<String, Glyph> hexGraph;
    private String hexId;
    private String firstGlyphId;
    private String displayName;
    private List<EncodingStroke> encoding;

    public Hex() {
        this.hexGraph = new HashMap<>();
        this.hexId = UUID.randomUUID().toString();
    }

    public Hex(Map<String, Glyph> hexGraph, String hexId, String firstGlyphId) {
        this.hexGraph = hexGraph;
        this.hexId = hexId;
        this.firstGlyphId = firstGlyphId;
    }

    public Hex(Glyph... glyphs) {
        this.hexGraph = new HashMap<>();
        for (Glyph glyph : glyphs) {
            this.hexGraph.put(glyph.getId(), glyph);
        }
        this.firstGlyphId = glyphs.length > 0 ? glyphs[0].getId() : null;
        this.hexId = UUID.randomUUID().toString();
    }

    public void replaceWith(Hex other) {
        this.hexGraph.clear();
        this.hexGraph.putAll(other.hexGraph);
        this.hexId = other.hexId;
        this.firstGlyphId = other.firstGlyphId;
        this.displayName = other.displayName;
        this.encoding = copyEncoding(other.encoding);
    }

    public void absorb(Hex other, String insertLocation) {
        Glyph insertGlyph = hexGraph.get(insertLocation);
        if (insertGlyph == null) return;
        String otherFirst = other.getFirstGlyphId();
        if (otherFirst != null) {
            insertGlyph.addSlotLink(Glyph.NEXT_SLOT, otherFirst);
        }
        hexGraph.putAll(other.hexGraph);
    }

    public Glyph get(String id) {
        return hexGraph.get(id);
    }

    public List<Glyph> getGlyphs() {
        return new ArrayList<>(hexGraph.values());
    }

    public List<Glyph> getGlyphs(List<String> ids) {
        List<Glyph> glyphs = new ArrayList<>();
        for (String id : ids) {
            Glyph glyph = hexGraph.get(id);
            if (glyph != null) {
                glyphs.add(glyph);
            }
        }
        return glyphs;
    }

    public void put(String id, Glyph glyph) {
        hexGraph.put(id, glyph);
    }

    public void remove(String id) {
        hexGraph.remove(id);
    }

    public void removeGlyph(String id) {
        if (hexGraph.remove(id) == null) return;
        for (Glyph remaining : hexGraph.values()) {
            for (Slot slot : remaining.getSlots().values()) {
                slot.removeLink(id);
            }
        }
        if (id.equals(firstGlyphId)) {
            firstGlyphId = null;
        }
    }

    public String getHexId() {
        return hexId;
    }

    public String getFirstGlyphId() {
        return firstGlyphId;
    }

    public void setFirstGlyphId(String firstGlyphId) {
        this.firstGlyphId = firstGlyphId;
    }

    public void set(String hexId) {
        this.hexId = hexId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<EncodingStroke> getEncoding() {
        return encoding;
    }

    public void setEncoding(List<EncodingStroke> encoding) {
        this.encoding = encoding != null && encoding.isEmpty() ? null : encoding;
    }

    private static List<EncodingStroke> copyEncoding(List<EncodingStroke> source) {
        if (source == null || source.isEmpty()) return null;
        List<EncodingStroke> copy = new ArrayList<>(source.size());
        for (EncodingStroke stroke : source) copy.add(stroke.copy());
        return copy;
    }

    public static final BuilderCodec<Hex> CODEC = BuilderCodec
            .builder(Hex.class, Hex::new)
            .append(new KeyedCodec<>("HexGraph", new MapCodec<>(Glyph.CODEC, HashMap::new, false)),
                    (c, v) -> c.hexGraph = v,
                    c -> c.hexGraph)
            .add()
            .append(new KeyedCodec<>("HexId", Codec.STRING),
                    (c, v) -> c.hexId = v,
                    c -> c.hexId)
            .add()
            .append(new KeyedCodec<>("FirstGlyphId", Codec.STRING),
                    (c, v) -> c.firstGlyphId = v,
                    c -> c.firstGlyphId)
            .add()
            .append(new KeyedCodec<>("DisplayName", Codec.STRING),
                    (c, v) -> c.displayName = v,
                    c -> c.displayName)
            .add()
            .append(new KeyedCodec<>("Encoding", new ArrayCodec<>(EncodingStroke.CODEC, EncodingStroke[]::new)),
                    (c, v) -> c.encoding = v != null && v.length > 0 ? new ArrayList<>(List.of(v)) : null,
                    c -> c.encoding != null && !c.encoding.isEmpty()
                            ? c.encoding.toArray(EncodingStroke[]::new) : null)
            .add()
            .build();

    public Hex clone() {
        Hex newHex = new Hex();
        for (Map.Entry<String, Glyph> entry : hexGraph.entrySet()) {
            newHex.put(entry.getKey(), entry.getValue().clone());
        }
        newHex.setFirstGlyphId(this.firstGlyphId);
        newHex.set(this.hexId);
        newHex.setDisplayName(this.displayName);
        newHex.encoding = copyEncoding(this.encoding);
        return newHex;
    }

    @Override
    public String toString() {
        return "Hex{id=" + hexId + ", displayName=" + displayName + ", firstGlyphId=" + firstGlyphId
                + ", encoded=" + (encoding != null) + ", glyphs=" + hexGraph.values() + "}";
    }
}
