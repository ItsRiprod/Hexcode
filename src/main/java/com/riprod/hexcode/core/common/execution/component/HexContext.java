package com.riprod.hexcode.core.common.execution.component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.codec.HexFieldCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class HexContext {
    // === serialized fields ===
    @Nullable private Hex hex;
    @Nullable private HexRoot root;
    @Nullable private HexStats hexStats;
    private float manaCost = -1f;
    private float manaMultiplier = 1.0f;
    @Nullable private HexStyleAsset style;
    @Nullable private HexVar defaultVariable;
    @Nullable private String castSlotKey;
    private float currentComplexity = 0f;
    private Map<String, HexVar> variables = new HashMap<>();
    @Nullable
    private UUID executionId;

    // === transient fields ===
    private transient CommandBuffer<EntityStore> accessor;
    private transient Deque<String> resolutionStack = new ArrayDeque<>();
    private transient boolean requireMagicCharges = true;
    private transient boolean consumeMana = true;
    private transient boolean applyVolatilityDecay = true;
    private transient boolean bypassVolatilityDepletion = false;

    public HexContext() {
    }

    public HexContext(Hex hex, float manaCost, HexRoot hexRoot, @Nullable HexStyleAsset style,
            HexStats hexStats) {
        this.hex = hex;
        this.manaCost = manaCost;
        this.root = hexRoot;
        this.style = style;
        this.executionId = UUID.randomUUID();
        setHexStats(hexStats);
    }

    public HexContext applyNonDefaultsFrom(@Nullable HexContext other) {
        if (other == null) return this;
        if (other.hex != null) this.hex = other.hex;
        if (other.manaCost >= 0f) this.manaCost = other.manaCost;
        if (other.manaMultiplier != 1.0f) this.manaMultiplier *= other.manaMultiplier;
        if (other.style != null) this.style = other.style;
        if (other.hexStats != null && this.hexStats != null) {
            this.hexStats.applyOverridesFrom(other.hexStats);
        }
        return this;
    }

    public static HexContext cloneState(HexContext src) {
        if (src == null) return null;
        HexContext copy = new HexContext();
        copy.hex = src.hex != null ? src.hex.clone() : null;
        copy.root = src.root != null ? src.root.copy() : null;
        copy.hexStats = src.hexStats != null ? src.hexStats.copy() : null;
        copy.manaCost = src.manaCost;
        copy.manaMultiplier = src.manaMultiplier;
        copy.style = src.style != null ? src.style.clone() : null;
        copy.defaultVariable = src.defaultVariable;
        copy.castSlotKey = src.castSlotKey;
        copy.currentComplexity = src.currentComplexity;
        copy.variables = new HashMap<>(src.variables);
        copy.executionId = src.executionId;
        copy.requireMagicCharges = src.requireMagicCharges;
        copy.consumeMana = src.consumeMana;
        copy.applyVolatilityDecay = src.applyVolatilityDecay;
        copy.bypassVolatilityDepletion = src.bypassVolatilityDepletion;
        return copy;
    }

    public HexContext branch() {
        return branch(2);
    }

    public HexContext branch(int splitFactor) {
        HexContext branch = new HexContext();
        branch.root = this.root;
        branch.accessor = this.accessor;
        branch.hex = this.hex;
        branch.hexStats = this.hexStats;
        branch.executionId = this.executionId;
        branch.variables = this.variables;
        branch.style = this.style != null ? this.style.clone() : null;
        branch.manaCost = this.manaCost;
        branch.manaMultiplier = this.manaMultiplier;
        branch.defaultVariable = this.defaultVariable;
        branch.castSlotKey = this.castSlotKey;
        branch.currentComplexity = this.currentComplexity / Math.max(1, splitFactor);
        branch.requireMagicCharges = this.requireMagicCharges;
        branch.consumeMana = this.consumeMana;
        branch.applyVolatilityDecay = this.applyVolatilityDecay;
        branch.bypassVolatilityDepletion = this.bypassVolatilityDepletion;
        return branch;
    }

    public CommandBuffer<EntityStore> getAccessor() {
        return accessor;
    }

    public void UpdateAccessor(CommandBuffer<EntityStore> newAccessor) {
        this.accessor = newAccessor;
    }

    public boolean isResolving(String glyphId) {
        return resolutionStack.contains(glyphId);
    }

    public void pushResolving(String glyphId) {
        resolutionStack.push(glyphId);
    }

    public void popResolving() {
        resolutionStack.pop();
    }

    public int resolutionDepth() {
        return resolutionStack.size();
    }

    public void updateRuntimeAccessors(CommandBuffer<EntityStore> buffer) {
        this.accessor = buffer;
    }

    public boolean isRequireMagicCharges() {
        return requireMagicCharges;
    }

    public void setRequireMagicCharges(boolean requireMagicCharges) {
        this.requireMagicCharges = requireMagicCharges;
    }

    public boolean isConsumeMana() {
        return consumeMana;
    }

    public void setConsumeMana(boolean consumeMana) {
        this.consumeMana = consumeMana;
    }

    public boolean isApplyVolatilityDecay() {
        return applyVolatilityDecay;
    }

    public void setApplyVolatilityDecay(boolean applyVolatilityDecay) {
        this.applyVolatilityDecay = applyVolatilityDecay;
    }

    public boolean isBypassVolatilityDepletion() {
        return bypassVolatilityDepletion;
    }

    public void setBypassVolatilityDepletion(boolean bypassVolatilityDepletion) {
        this.bypassVolatilityDepletion = bypassVolatilityDepletion;
    }

    // === root + caster ref ===
    @Nullable
    public HexRoot getHexRoot() {
        return root;
    }

    public void setHexRoot(HexRoot hexRoot) {
        this.root = hexRoot;
    }

    @Nullable
    public Ref<EntityStore> getCasterRef(ComponentAccessor<EntityStore> accessor) {
        return root != null ? root.getSourceRef(accessor) : null;
    }

    @Nullable
    public Hex getHex() {
        return hex;
    }

    public void setHex(@Nullable Hex hex) {
        this.hex = hex;
    }

    public Glyph getGlyph(String id) {
        return hex.get(id);
    }

    public Map<String, HexVar> getVariables() {
        return variables;
    }

    public HexVar getVariable(String slot) {
        return variables.get(slot);
    }

    public void setVariables(Map<String, HexVar> variables) {
        this.variables = variables;
    }

    public void setVariable(String slot, HexVar value) {
        this.variables.put(slot, value == null ? null : value.copy());
    }

    @Nullable
    public HexStats getHexStats() {
        return hexStats;
    }

    public void setHexStats(HexStats hexStats) {
        this.hexStats = hexStats;
        if (hexStats != null) {
            hexStats.setExecutionId(this.executionId);
            this.currentComplexity = hexStats.getInitialComplexity();
        }
    }

    public float getComplexity() {
        return currentComplexity;
    }

    public void addComplexity(float amount) {
        this.currentComplexity = Math.max(0f, this.currentComplexity + amount);
    }

    public float consumeComplexity() {
        float pooled = this.currentComplexity;
        this.currentComplexity = 0f;
        return pooled;
    }

    public float getVolatilityOverride() {
        return this.hexStats != null ? this.hexStats.getInitialVolatility() : 0f;
    }

    public void setVolatilityOverride(float volatilityOverride) {
        if (this.hexStats == null) return;
        this.hexStats.setVolatility(volatilityOverride);
        this.hexStats.setInitialVolatility(volatilityOverride);
    }

    public float getVolatilityMultiplier() {
        return this.hexStats != null ? this.hexStats.getVolatilityMultiplier() : 1.0f;
    }

    public void setVolatilityMultiplier(float v) {
        if (this.hexStats != null) this.hexStats.setVolatilityMultiplier(v);
    }

    public float getPowerMultiplier() {
        return this.hexStats != null ? this.hexStats.getComplexityMultiplier() : 1.0f;
    }

    public void setPowerMultiplier(float v) {
        if (this.hexStats != null) this.hexStats.setComplexityMultiplier(v);
    }

    public float getMagicPowerMultiplier() {
        return getPowerMultiplier();
    }

    public float getManaMultiplier() {
        return manaMultiplier;
    }

    public void setManaMultiplier(float manaCostMultiplier) {
        this.manaMultiplier = manaCostMultiplier;
    }

    public float getManaCost() {
        return manaCost * manaMultiplier;
    }

    public void setManaCost(float manaCost) {
        this.manaCost = manaCost;
    }

    public float getBaseManaCost() {
        return manaCost;
    }

    @Nullable
    public HexStyleAsset getStyle() {
        return style;
    }

    public void setStyle(@Nullable HexStyleAsset style) {
        this.style = style;
    }

    public HexColors getColors() {
        HexColors c = new HexColors();
        if (style != null) {
            if (style.getPrimaryColor() != null) c.setPrimaryColor(style.getPrimaryColor().clone());
            if (style.getSecondaryColor() != null) c.setSecondaryColor(style.getSecondaryColor().clone());
            c.setPrimaryAlpha(style.getAlphaOrDefault());
        }
        return c;
    }

    public void setColors(@Nullable HexColors colors) {
        if (colors == null) return;
        if (style == null) style = HexStyleAsset.empty();
        style.setPrimaryColor(colors.getPrimaryColor() != null ? colors.getPrimaryColor().clone() : null);
        style.setSecondaryColor(colors.getSecondaryColor() != null ? colors.getSecondaryColor().clone() : null);
        style.setAlpha(colors.getPrimaryAlpha());
    }

    @Nullable
    public HexVar getDefaultVariable() {
        return defaultVariable;
    }

    public void setDefaultVariable(@Nullable HexVar defaultVariable) {
        this.defaultVariable = defaultVariable == null ? null : defaultVariable.copy();
    }

    @Nullable
    public String getCastSlotKey() {
        return castSlotKey;
    }

    public void setCastSlotKey(@Nullable String castSlotKey) {
        this.castSlotKey = castSlotKey;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public void toStringWalk(String id, StringBuilder sb, String prefix, boolean last, Set<String> visited) {
        Glyph node = hex.get(id);
        String connector = last ? "└── " : "├── ";
        String shortId = id.toString().substring(0, 8);

        if (node == null) {
            sb.append(prefix).append(connector).append(shortId).append(" [missing]\n");
            return;
        }

        sb.append(prefix).append(connector).append(node.getGlyphId())
                .append(" (").append(shortId).append(")")
                .append(" acc=").append(String.format("%.2f", node.getVolatility()))
                .append(" spd=").append(String.format("%.2f", node.getEfficiency()))
                .append(" slots=").append(node.getSlots().keySet());

        if (!visited.add(id)) {
            sb.append(" [cycle]\n");
            return;
        }

        sb.append("\n");
        String childPrefix = prefix + (last ? "    " : "│   ");
        List<String> nextLinks = node.getNextLinks();
        for (int i = 0; i < nextLinks.size(); i++) {
            toStringWalk(nextLinks.get(i), sb, childPrefix, i == nextLinks.size() - 1, visited);
        }

        visited.remove(id);
    }

    public static final BuilderCodec<HexContext> CODEC = BuilderCodec
            .builder(HexContext.class, HexContext::new)
            .append(new KeyedCodec<>("Hex", HexFieldCodec.IMBUE),
                    (c, v) -> c.hex = v,
                    c -> c.hex)
            .add()
            .append(new KeyedCodec<>("Root", HexRoot.CODEC),
                    (c, v) -> c.root = v,
                    c -> c.root)
            .add()
            .append(new KeyedCodec<>("ManaCost", Codec.FLOAT),
                    (c, v) -> c.manaCost = v,
                    c -> c.manaCost)
            .add()
            .append(new KeyedCodec<>("ManaMultiplier", Codec.FLOAT),
                    (c, v) -> c.manaMultiplier = v,
                    c -> c.manaMultiplier)
            .add()
            .append(new KeyedCodec<>("HexStats", HexStats.CODEC),
                    (c, v) -> c.hexStats = v,
                    c -> c.hexStats)
            .add()
            .append(new KeyedCodec<>("Style", HexStyleAsset.CODEC),
                    (c, v) -> c.style = v,
                    c -> c.style)
            .add()
            .append(new KeyedCodec<>("DefaultVariable", HexVar.CODEC),
                    (c, v) -> c.defaultVariable = v,
                    c -> c.defaultVariable)
            .add()
            .append(new KeyedCodec<>("CastSlotKey", Codec.STRING),
                    (c, v) -> c.castSlotKey = v,
                    c -> c.castSlotKey)
            .add()
            .append(new KeyedCodec<>("CurrentComplexity", Codec.FLOAT),
                    (c, v) -> c.currentComplexity = v,
                    c -> c.currentComplexity)
            .metadata(UIDisplayMode.HIDDEN)
            .add()
            .append(new KeyedCodec<>("Variables", new MapCodec<>(HexVar.CODEC, HashMap::new)),
                    (c, v) -> c.variables = v,
                    c -> c.variables)
            .metadata(UIDisplayMode.HIDDEN)
            .add()
            .append(new KeyedCodec<>("ExecutionId", Codec.UUID_STRING),
                    (c, v) -> c.executionId = v,
                    c -> c.executionId)
            .metadata(UIDisplayMode.HIDDEN)
            .add()
            .build();
}
