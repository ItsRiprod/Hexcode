package com.riprod.hexcode.core.common.glyphs.component;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public abstract class Slot {
    public static final CodecMapCodec<Slot> CODEC = new CodecMapCodec<>("Type", true, false);
    public static final BuilderCodec<Slot> BASE_CODEC = BuilderCodec.abstractBuilder(Slot.class)
            .append(new KeyedCodec<>("Links", Codec.STRING_ARRAY),
                    (s, v) -> s.setLinks(v),
                    s -> s.links)
            .add()
            .build();

    protected String[] links = new String[0];

    protected transient String key;
    protected transient String label;
    protected transient String description;
    protected transient Vector3f color;
    protected transient Vector3f offset;
    protected transient DebugShape shape;
    protected transient boolean unique;

    public static void registerType(String id, Class<? extends Slot> type,
            BuilderCodec<? extends Slot> codec) {
        registerType(Priority.NORMAL, id, type, codec);
    }

    public static void registerType(Priority priority, String id, Class<? extends Slot> type,
            BuilderCodec<? extends Slot> codec) {
        CODEC.register(priority, id, type, codec);
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

    public void hydrateFrom(SlotConfig config, String key, Vector3f resolvedOffset) {
        this.key = key;
        this.label = config.getLabel();
        this.description = config.getDescription();
        this.color = HexColors.toVector3f(config.getColor());
        this.offset = resolvedOffset;
        this.shape = config.getShape();
        this.unique = config.isUnique();
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

    public String displayLabel() {
        return this.label;
    }

    public String displayDescription() {
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
