package com.riprod.hexcode.core.common.glyphs.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotStyleAsset;
import com.riprod.hexcode.core.common.glyphs.registry.StyleResolution;
import com.riprod.hexcode.core.common.glyphs.registry.StyleResolution.ResolvedStyle;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public abstract class Slot {
    public static final CodecMapCodec<Slot> CODEC = new CodecMapCodec<>("Type", true, false);
    public static final BuilderCodec<Slot> BASE_CODEC = BuilderCodec.abstractBuilder(Slot.class)
            .append(new KeyedCodec<>("Links", Codec.STRING_ARRAY),
                    (s, v) -> s.setLinks(v),
                    s -> s.links)
            .add()
            .build();

    private static final Map<String, Supplier<? extends Slot>> FACTORIES = new HashMap<>();

    protected String[] links = new String[0];

    protected transient String key;
    protected transient String label;
    protected transient String description;
    protected transient Vector3f color;
    protected transient Vector3f offset;
    protected transient DebugShape shape;
    protected transient boolean unique;

    public static void registerType(String id, Class<? extends Slot> type,
            BuilderCodec<? extends Slot> codec, Supplier<? extends Slot> factory) {
        registerType(Priority.NORMAL, id, type, codec, factory);
    }

    public static void registerType(Priority priority, String id, Class<? extends Slot> type,
            BuilderCodec<? extends Slot> codec, Supplier<? extends Slot> factory) {
        CODEC.register(priority, id, type, codec);
        FACTORIES.put(id, factory);
    }

    public static Slot create(@Nullable String typeId) {
        Supplier<? extends Slot> factory = typeId != null ? FACTORIES.get(typeId) : null;
        return factory != null ? factory.get() : new LinkSlot();
    }

    public static Slot forAssetSlot(@Nullable String glyphId, String slotKey) {
        return create(resolveTypeId(glyphId, slotKey));
    }

    @Nullable
    private static String resolveTypeId(@Nullable String glyphId, String slotKey) {
        if (glyphId == null) return null;
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        SlotAsset slotAsset = asset != null ? asset.getSlot(slotKey) : null;
        String styleId = slotAsset != null ? slotAsset.getStyleId() : null;
        if (styleId == null) return null;
        SlotStyleAsset style = SlotStyleAsset.getAssetMap().getAsset(styleId);
        return style != null ? style.getSlotType() : null;
    }

    public String[] getLinks() {
        return this.links;
    }

    public void setLinks(String[] links) {
        this.links = links != null ? links : new String[0];
    }

    public void addLink(String glyphId) {
        if (glyphId == null) return;
        for (String existing : this.links) {
            if (existing.equals(glyphId)) return;
        }
        String[] grown = Arrays.copyOf(this.links, this.links.length + 1);
        grown[this.links.length] = glyphId;
        this.links = grown;
    }

    public void removeLink(String glyphId) {
        if (glyphId == null || this.links.length == 0) return;
        ArrayList<String> kept = new ArrayList<>(this.links.length);
        for (String existing : this.links) {
            if (!existing.equals(glyphId)) kept.add(existing);
        }
        this.links = kept.toArray(String[]::new);
    }

    public void clearLinks() {
        this.links = new String[0];
    }

    @Nullable
    public String getFirstLink() {
        return this.links.length > 0 ? this.links[0] : null;
    }

    public void hydrateFrom(SlotAsset asset, String key, Vector3f resolvedOffset, String glyphId) {
        ResolvedStyle rs = StyleResolution.resolve(asset, glyphId, key);
        this.key = key;
        this.label = asset.getLabel();
        this.description = asset.getDescription();
        this.color = rs.color();
        this.offset = resolvedOffset;
        this.shape = rs.shape();
        this.unique = asset.isUnique();
    }

    public boolean isUnique() {
        return this.unique;
    }

    public String getKey() {
        return this.key;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public Vector3f getColor() {
        return this.color;
    }

    public Vector3f getOffset() {
        return this.offset;
    }

    public DebugShape getShape() {
        return this.shape;
    }

    @Nullable
    public byte[] encodeState() {
        return null;
    }

    public void decodeState(byte[] state) {
    }

    @Nullable
    public HexVar inlineValue() {
        return null;
    }

    protected void copyBaseState(Slot copy) {
        copy.links = Arrays.copyOf(this.links, this.links.length);
        copy.key = this.key;
        copy.label = this.label;
        copy.description = this.description;
        copy.color = this.color;
        copy.offset = this.offset;
        copy.shape = this.shape;
        copy.unique = this.unique;
    }

    @Override
    public abstract Slot clone();

    @Override
    public String toString() {
        return this.key != null ? this.key : "Slot";
    }
}
