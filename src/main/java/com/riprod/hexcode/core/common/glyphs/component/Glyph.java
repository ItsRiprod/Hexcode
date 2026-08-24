package com.riprod.hexcode.core.common.glyphs.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class Glyph {
    public static final String NEXT_SLOT = "Next";
    public static final String DEFAULT_SLOT = "0";

    private static final int MAX_RESOLVE_DEPTH = 64;

    private String glyphId;
    private String id;
    private float volatility;
    private float efficiency;
    private Map<String, Slot> slots;
    private Vector3f relPosition;
    private Rotation3f relRotation;

    public Glyph() {
        this.glyphId = "";
        this.id = "";
        this.volatility = 0;
        this.efficiency = 0;
        this.slots = new LinkedHashMap<>();
        this.relPosition = new Vector3f(0, 0, 0);
        this.relRotation = new Rotation3f(0, 0, 0);
    }

    public Glyph(GlyphAsset glyphAsset, float volatility, float efficiency) {
        this.glyphId = glyphAsset.getId();
        this.id = UUID.randomUUID().toString();
        this.volatility = volatility;
        this.efficiency = efficiency;
        this.slots = new LinkedHashMap<>();
        this.relPosition = new Vector3f(0, 0, 0);
        this.relRotation = new Rotation3f(0, 0, 0);
    }

    public String getGlyphId() {
        return glyphId;
    }

    public void setGlyphId(String glyphId) {
        this.glyphId = glyphId;
    }

    public float getVolatility() {
        return volatility;
    }

    public void setVolatility(float volatility) {
        this.volatility = volatility;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }

    public float computeBaseCost() {
        return computeBaseCost(GlyphAsset.getAssetMap().getAsset(glyphId));
    }

    public float computeDrawQuality() {
        return (1 - volatility) * 0.5f + 0.5f;
    }

    public float computeBaseCost(GlyphAsset asset) {
        if (asset == null)
            return 0f;
        return asset.getVolatility().getInstantCost() * computeDrawQuality();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vector3f getPosition() {
        return relPosition;
    }

    public void setPosition(Vector3f position) {
        this.relPosition = position;
    }

    public Rotation3f getRotation() {
        return relRotation;
    }

    public void setRotation(Rotation3f rotation) {
        this.relRotation = rotation;
    }

    public Map<String, Slot> getSlots() {
        return slots;
    }

    @Nullable
    public Slot getSlot(String key) {
        return slots.get(key);
    }

    @Nullable
    public Slot getOrCreateSlot(String key) {
        Slot existing = slots.get(key);
        if (existing != null)
            return existing;
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(this.glyphId);
        SlotConfig config = asset != null ? asset.getSlot(key) : null;
        if (config == null)
            return null;
        Slot created = config.create();
        slots.put(key, created);
        return created;
    }

    public void addSlotLink(String key, String linkedGlyphId) {
        Slot slot = getOrCreateSlot(key);
        if (slot != null)
            slot.addLink(linkedGlyphId);
    }

    public void removeSlotLink(String key, String linkedGlyphId) {
        Slot slot = slots.get(key);
        if (slot == null)
            return;
        slot.removeLink(linkedGlyphId);
    }

    public void clearSlot(String key) {
        Slot slot = slots.get(key);
        if (slot == null)
            return;
        slot.clearLinks();
    }

    public void clearAllSlots() {
        for (Slot slot : slots.values()) {
            slot.clearLinks();
        }
    }

    @Nullable
    public HexVar readSlot(String key, HexContext hexContext) {
        return readSlot(key, hexContext, null);
    }

    @Nullable
    public HexVar readSlot(String key, HexContext hexContext, @Nullable HexVar javaDefault) {
        if (hexContext.resolutionDepth() >= MAX_RESOLVE_DEPTH)
            return null;

        Slot slot = slots.get(key);
        String firstLink = slot != null ? slot.getFirstLink() : null;
        if (firstLink == null)
            return resolveAssetDefault(key, hexContext, javaDefault);

        Glyph linked = hexContext.getGlyph(firstLink);
        if (linked == null)
            return resolveAssetDefault(key, hexContext, javaDefault);

        GlyphAsset linkedAsset = GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
        if (linkedAsset == null)
            return resolveAssetDefault(key, hexContext, javaDefault);

        GlyphHandler handler = GlyphRegistry.get(linkedAsset.getHandler());
        if (handler == null)
            return resolveAssetDefault(key, hexContext, javaDefault);

        if (hexContext.isResolving(linked.getId()))
            return null;

        hexContext.pushResolving(linked.getId());
        try {
            HexVar v = handler.readValue(linked, hexContext);
            return v != null ? v : resolveAssetDefault(key, hexContext, javaDefault);
        } finally {
            hexContext.popResolving();
        }
    }

    public void writeOutput(HexVar value, HexContext hexContext) {
        hexContext.setDefaultVariable(value);
        hexContext.setOwnVariable(this.id, value);
    }

    public void writeOutput(HexVar defaultSlotValue, HexVar selfValue, HexContext hexContext) {
        hexContext.setDefaultVariable(defaultSlotValue);
        hexContext.setOwnVariable(this.id, selfValue);
    }

    public void writeDefaultOutput(HexVar value, HexContext hexContext) {
        hexContext.setDefaultVariable(value);
    }

    public void writeSelfOutput(HexVar value, HexContext hexContext) {
        hexContext.setOwnVariable(this.id, value);
    }

    @Nullable
    private HexVar resolveAssetDefault(String key, HexContext hexContext, @Nullable HexVar javaDefault) {
        Slot slot = slots.get(key);
        if (slot != null) {
            HexVar inline = slot.inlineValue();
            if (inline != null)
                return inline;
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        SlotConfig slotConfig = asset != null ? asset.getSlot(key) : null;
        Double defaultNum = slotConfig != null ? slotConfig.getDefaultValue() : null;
        if (defaultNum != null)
            return new NumberVar(defaultNum);
        if (javaDefault != null)
            return javaDefault;
        HexVar slotZero = hexContext.getDefaultVariable();
        if (slotZero != null)
            return slotZero;
        return new NumberVar(0.0);
    }

    public List<String> getNextLinks() {
        Slot slot = slots.get(NEXT_SLOT);
        if (slot == null)
            return List.of();
        String[] links = slot.getLinks();
        if (links.length == 0)
            return List.of();
        return Arrays.asList(links);
    }

    public List<String> getFlowLinks() {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (asset == null)
            return getNextLinks();

        LinkedHashSet<String> flowLinks = new LinkedHashSet<>();
        for (String key : asset.getSlotKeys()) {
            SlotConfig config = asset.getSlot(key);
            if (config == null || !config.isFlow())
                continue;
            Slot slot = slots.get(key);
            if (slot == null)
                continue;
            flowLinks.addAll(Arrays.asList(slot.getLinks()));
        }
        return flowLinks.isEmpty() ? List.of() : new ArrayList<>(flowLinks);
    }

    public List<HexVar> readSlotAll(String key, HexContext hexContext) {
        Slot slot = slots.get(key);
        if (slot == null)
            return List.of();

        String[] links = slot.getLinks();
        if (links.length == 0)
            return List.of();

        if (hexContext.resolutionDepth() >= MAX_RESOLVE_DEPTH)
            return List.of();

        List<HexVar> resolved = new ArrayList<>(links.length);
        for (String linkId : links) {
            Glyph linked = hexContext.getGlyph(linkId);
            if (linked == null)
                continue;
            GlyphAsset linkedAsset = GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
            if (linkedAsset == null)
                continue;
            GlyphHandler handler = GlyphRegistry.get(linkedAsset.getHandler());
            if (handler == null)
                continue;

            if (hexContext.isResolving(linked.getId())) {
                resolved.add(new NumberVar(0.0));
                continue;
            }

            hexContext.pushResolving(linked.getId());
            try {
                HexVar value = handler.readValue(linked, hexContext);
                if (value != null)
                    resolved.add(value);
            } finally {
                hexContext.popResolving();
            }
        }
        return resolved;
    }

    public static final BuilderCodec<Glyph> CODEC = buildCodec();

    @SuppressWarnings("unchecked")
    private static BuilderCodec<Glyph> buildCodec() {
        Codec<Map<String, Slot>> slotMapCodec = (Codec<Map<String, Slot>>) (Codec<?>) new MapCodec<>(Slot.CODEC,
                LinkedHashMap::new, false);
        return BuilderCodec
                .builder(Glyph.class, Glyph::new)
                .append(new KeyedCodec<>("GlyphId", Codec.STRING),
                        (n, v) -> n.glyphId = v, n -> n.glyphId)
                .add()
                .append(new KeyedCodec<>("Id", Codec.STRING),
                        (n, v) -> n.id = v, n -> n.id)
                .add()
                .append(new KeyedCodec<>("Accuracy", Codec.FLOAT),
                        (n, v) -> n.volatility = v, n -> n.volatility)
                .add()
                .append(new KeyedCodec<>("Speed", Codec.FLOAT),
                        (n, v) -> n.efficiency = v, n -> n.efficiency)
                .add()
                .<Map<String, Slot>>append(new KeyedCodec<>("Slots", slotMapCodec),
                        (n, v) -> n.slots = v != null ? new LinkedHashMap<>(v) : new LinkedHashMap<>(),
                        n -> n.slots)
                .add()
                .append(new KeyedCodec<>("RelativePosition", Codec.FLOAT_ARRAY),
                        (c, v) -> c.relPosition = new Vector3f(v[0], v[1], v[2]),
                        c -> new float[] { c.relPosition.x, c.relPosition.y, c.relPosition.z })
                .add()
                .append(new KeyedCodec<>("RelativeRotation", Codec.FLOAT_ARRAY),
                        (c, v) -> c.relRotation = new Rotation3f(v[0], v[1], v[2]),
                        c -> new float[] { c.relRotation.x, c.relRotation.y, c.relRotation.z })
                .add()
                .build();
    }

    public Glyph clone() {
        Glyph clone = new Glyph();
        clone.glyphId = this.glyphId;
        clone.id = this.id;
        clone.volatility = this.volatility;
        clone.efficiency = this.efficiency;
        clone.slots = new LinkedHashMap<>();
        for (Map.Entry<String, Slot> entry : this.slots.entrySet()) {
            clone.slots.put(entry.getKey(), entry.getValue().clone());
        }
        clone.relPosition = new Vector3f(this.relPosition.x, this.relPosition.y, this.relPosition.z);
        clone.relRotation = new Rotation3f(this.relRotation.x, this.relRotation.y, this.relRotation.z);
        return clone;
    }

    @Override
    public String toString() {
        return glyphId;
    }
}
