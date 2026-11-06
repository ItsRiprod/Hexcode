package com.riprod.hexcode.core.common.glyphs.component;

import static com.riprod.hexcode.api.execution.IllegalOperationException.requireFinite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.hexes.codec.HexCacheResource;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

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
    private String payload;
    private transient boolean boundaryOrigin;
    private transient Hex payloadView;

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

    @Nullable
    public String getPayload() {
        return payload;
    }

    public void setPayload(@Nullable String payload) {
        this.payload = payload;
        this.payloadView = null;
    }

    @Nullable
    public Hex payloadView(@Nullable ComponentAccessor<EntityStore> accessor) {
        if (payload == null)
            return null;
        if (payloadView == null) {
            try {
                var cache = accessor != null
                        ? accessor.getResource(HexCacheResource.getResourceType())
                        : null;
                payloadView = cache != null ? cache.getOrDecode(payload) : HexUtils.deserialize(payload);
            } catch (Exception e) {
                return null;
            }
        }
        return payloadView;
    }

    public boolean isComponentInstance() {
        return payload != null;
    }

    public String displayTitle(@Nullable ComponentAccessor<EntityStore> accessor) {
        if (isComponentInstance()) {
            Hex view = payloadView(accessor);
            String name = view != null ? view.getDisplayName() : null;
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (asset != null && asset.getTitle() != null) {
            return asset.getTitle();
        }
        return glyphId;
    }

    public boolean isBoundaryOrigin() {
        return boundaryOrigin;
    }

    public void setBoundaryOrigin(boolean boundaryOrigin) {
        this.boundaryOrigin = boundaryOrigin;
    }

    public Map<String, Slot> getSlots() {
        return slots;
    }

    @Nullable
    public Slot getSlot(String key) {
        return slots.get(key);
    }

    public boolean hasLinks(String key) {
        var slot = slots.get(key);
        return slot != null && slot.getLinks().length > 0;
    }

    public List<String> getLinks(String key) {
        var slot = slots.get(key);
        return slot == null || slot.getLinks().length == 0 ? List.of() : Arrays.asList(slot.getLinks());
    }

    @Nullable
    private static Function<Glyph, Map<String, SlotConfig>> portSchemaSynthesizer;

    public static void registerPortSchemaSynthesizer(Function<Glyph, Map<String, SlotConfig>> synthesizer) {
        portSchemaSynthesizer = synthesizer;
    }

    public Map<String, SlotConfig> effectiveSlots() {
        if (isComponentInstance()) {
            if (portSchemaSynthesizer == null)
                return Map.of();
            Map<String, SlotConfig> synthesized = portSchemaSynthesizer.apply(this);
            return synthesized != null ? synthesized : Map.of();
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(this.glyphId);
        return asset != null ? asset.getSlots() : Map.of();
    }

    public Slot getOrCreateSlot(String key) {
        Slot existing = slots.get(key);
        if (existing != null)
            return existing;
        SlotConfig config = effectiveSlots().get(key);
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
    public Vector3d read(String key, HexContext hexContext, Vector3d fallback) {
        return null;
    }

    @Nullable
    public Vector3d read(String key, HexContext hexContext, Rotation3fc fallback) {
        return null;

    }

    @Nullable
    public Glyph linkedGlyph(String key, HexContext hexContext) {
        var slot = slots.get(key);
        var firstLink = slot != null ? slot.getFirstLink() : null;
        return firstLink != null ? hexContext.getGlyph(firstLink) : null;
    }

    @Nullable
    public HexVar readSlot(String key, HexContext hexContext) {
        return readSlot(key, hexContext, () -> null);
    }

    @Nonnull
    public HexVar readSlot(String key, HexContext hexContext, @Nullable HexVar javaDefault) {
        return readSlot(key, hexContext, () -> javaDefault);
    }

    @Nonnull
    public HexVar readSlot(String key, HexContext hexContext, @Nullable Supplier<HexVar> defaultSupplier) {
        if (hexContext.resolutionDepth() >= MAX_RESOLVE_DEPTH)
            return null;

        Glyph linked = linkedGlyph(key, hexContext);
        if (linked == null)
            return resolveAssetDefault(key, hexContext, defaultSupplier);

        GlyphAsset linkedAsset = GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
        if (linkedAsset == null)
            return resolveAssetDefault(key, hexContext, defaultSupplier);


        if (hexContext.isResolving(linked.getId()))
            return null;

        hexContext.pushResolving(linked.getId());
        try {
            HexVar v = requireFinite(linkedAsset.readValue(linked, hexContext));
            return v != null ? v : resolveAssetDefault(key, hexContext, defaultSupplier);
        } finally {
            hexContext.popResolving();
        }
    }

    public void writeOutput(HexVar value, HexContext hexContext) {
        requireFinite(value);
        hexContext.setDefaultVariable(value);
        hexContext.setOwnVariable(this.id, value);
    }

    public void writeOutput(HexVar defaultSlotValue, HexVar selfValue, HexContext hexContext) {
        hexContext.setDefaultVariable(requireFinite(defaultSlotValue));
        hexContext.setOwnVariable(this.id, requireFinite(selfValue));
    }

    public void writeDefaultOutput(HexVar value, HexContext hexContext) {
        hexContext.setDefaultVariable(requireFinite(value));
    }

    public void writeSelfOutput(HexVar value, HexContext hexContext) {
        hexContext.setOwnVariable(this.id, requireFinite(value));
    }

    @Nonnull
    private HexVar resolveAssetDefault(String key, HexContext hexContext, @Nullable Supplier<HexVar> javaDefault) {
        Slot slot = slots.get(key);
        if (slot != null) {
            var inline = slot.inlineValue();
            if (inline != null)
                return inline;
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        SlotConfig slotConfig = asset != null ? asset.getSlot(key) : null;
        Double defaultNum = slotConfig != null ? slotConfig.getDefaultValue() : null;
        if (defaultNum != null)
            return new NumberVar(defaultNum);
        if (javaDefault != null) {
            var jDefault = javaDefault.get();
            if (jDefault != null)
                return jDefault;
        }

        // get the default slot - else return 0
        var slotZero = hexContext.getDefaultVariable();
        if (slotZero != null)
            return slotZero;
        return new NumberVar(0.0);
    }

    public List<String> getNextLinks() {
        return getLinks(NEXT_SLOT);
    }

    public List<String> getFlowLinks() {
        if (!isComponentInstance() && GlyphAsset.getAssetMap().getAsset(glyphId) == null)
            return getNextLinks();

        LinkedHashSet<String> flowLinks = new LinkedHashSet<>();
        for (Map.Entry<String, SlotConfig> entry : effectiveSlots().entrySet()) {
            if (!entry.getValue().isFlow())
                continue;
            Slot slot = slots.get(entry.getKey());
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

            if (hexContext.isResolving(linked.getId())) {
                resolved.add(new NumberVar(0.0));
                continue;
            }

            hexContext.pushResolving(linked.getId());
            try {
                HexVar value = requireFinite(linkedAsset.readValue(linked, hexContext));
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
                .append(new KeyedCodec<>("Payload", Codec.STRING),
                        (c, v) -> c.payload = v, c -> c.payload)
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
        clone.payload = this.payload;
        clone.boundaryOrigin = this.boundaryOrigin;
        return clone;
    }

    @Override
    public String toString() {
        return glyphId;
    }
}
