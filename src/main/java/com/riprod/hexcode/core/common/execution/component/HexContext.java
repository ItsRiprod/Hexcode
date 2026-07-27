package com.riprod.hexcode.core.common.execution.component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.ResourcePoolComponent;
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class HexContext {
    // === serialized fields ===
    @Nullable private Hex hex;
    @Nullable private HexRoot root;
    @Nullable private HexCast cast;
    private float manaCost = -1f;
    private float manaMultiplier = 1.0f;
    @Nullable private HexStyleAsset style;
    @Nullable private HexVar defaultVariable;
    @Nullable private String castSlotKey;
    private Map<String, HexVar> variables = new HashMap<>();

    // === transient fields ===
    private transient CommandBuffer<EntityStore> accessor;
    @Nullable private transient Map<String, HexVar> localVariables;
    @Nullable private transient Deque<String> resolutionStack;
    private transient boolean requireMagicCharges = true;
    private transient boolean consumeMana = true;
    private transient boolean applyVolatilityDecay = true;
    private transient boolean bypassVolatilityDepletion = false;
    private transient float tierScale = 1.0f;
    private transient long branchId = -1L;

    public HexContext() {
    }

    public HexContext(Hex hex, float manaCost, HexRoot hexRoot, @Nullable HexStyleAsset style,
            HexCast cast) {
        this.hex = hex;
        this.manaCost = manaCost;
        this.root = hexRoot;
        this.style = style;
        this.cast = cast;
        this.branchId = cast != null ? cast.openBranch() : -1L;
    }

    public static HexContext cloneState(HexContext src) {
        if (src == null) return null;
        HexContext copy = new HexContext();
        copy.hex = src.hex != null ? src.hex.clone() : null;
        copy.root = src.root != null ? src.root.copy() : null;
        copy.cast = src.cast != null ? src.cast.copy() : null;
        copy.manaCost = src.manaCost;
        copy.manaMultiplier = src.manaMultiplier;
        copy.style = src.style != null ? src.style.clone() : null;
        copy.defaultVariable = src.defaultVariable;
        copy.castSlotKey = src.castSlotKey;
        copy.variables = new HashMap<>(src.variables);
        copy.localVariables = src.localVariables == null ? null : new HashMap<>(src.localVariables);
        copy.requireMagicCharges = src.requireMagicCharges;
        copy.consumeMana = src.consumeMana;
        copy.applyVolatilityDecay = src.applyVolatilityDecay;
        copy.bypassVolatilityDepletion = src.bypassVolatilityDepletion;
        copy.tierScale = src.tierScale;
        return copy;
    }

    public HexContext branch() {
        HexContext branch = new HexContext();
        branch.root = this.root;
        branch.accessor = this.accessor;
        branch.hex = this.hex;
        branch.cast = this.cast;
        branch.variables = this.variables;
        branch.localVariables = this.localVariables;
        branch.style = this.style;
        branch.manaCost = this.manaCost;
        branch.manaMultiplier = this.manaMultiplier;
        branch.defaultVariable = this.defaultVariable;
        branch.castSlotKey = this.castSlotKey;
        branch.requireMagicCharges = this.requireMagicCharges;
        branch.consumeMana = this.consumeMana;
        branch.applyVolatilityDecay = this.applyVolatilityDecay;
        branch.bypassVolatilityDepletion = this.bypassVolatilityDepletion;
        branch.tierScale = this.tierScale;
        branch.branchId = this.cast != null ? this.cast.openBranch() : -1L;
        return branch;
    }

    public long getBranchId() {
        return branchId;
    }

    public void beginRootBranch() {
        if (cast != null && branchId < 0L) {
            this.branchId = cast.openBranch();
        }
    }

    public void endBranch() {
        if (cast != null) {
            cast.closeBranch(branchId);
        }
    }

    public CommandBuffer<EntityStore> getAccessor() {
        return accessor;
    }

    public void UpdateAccessor(CommandBuffer<EntityStore> newAccessor) {
        this.accessor = newAccessor;
    }

    public boolean isResolving(String glyphId) {
        return resolutionStack != null && resolutionStack.contains(glyphId);
    }

    public void pushResolving(String glyphId) {
        if (resolutionStack == null) resolutionStack = new ArrayDeque<>(4);
        resolutionStack.push(glyphId);
    }

    public void popResolving() {
        if (resolutionStack != null) resolutionStack.pop();
    }

    public int resolutionDepth() {
        return resolutionStack == null ? 0 : resolutionStack.size();
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

    public float getTierScale() {
        return tierScale;
    }

    public void setTierScale(float tierScale) {
        this.tierScale = tierScale;
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

    public void enterLocalScope() {
        this.localVariables = this.localVariables == null
                ? new HashMap<>(4)
                : new HashMap<>(this.localVariables);
    }

    @Nullable
    public HexVar getOwnVariable(String glyphId) {
        if (this.localVariables != null) {
            HexVar local = this.localVariables.get(glyphId);
            if (local != null) return local;
        }
        return this.variables.get(glyphId);
    }

    public void setOwnVariable(String glyphId, HexVar value) {
        HexVar stored = value == null ? null : value.copy();
        if (this.localVariables != null) this.localVariables.put(glyphId, stored);
        this.variables.put(glyphId, stored);
    }

    @Nullable
    public HexCast cast() {
        return cast;
    }

    public void setCast(@Nullable HexCast cast) {
        this.cast = cast;
    }

    @Nullable
    public VolatilityComponent volatility() {
        return cast != null ? cast.volatility() : null;
    }

    @Nullable
    public ResourcePoolComponent resources() {
        return cast != null ? cast.resources() : null;
    }

    public float getResource(String id) {
        ResourcePoolComponent pools = resources();
        return pools != null ? pools.getResource(id) : 0f;
    }

    public void addResource(String id, String source, float amount) {
        if (cast != null) cast.mutableResources().addResource(id, source, amount);
    }

    public float consumeResource(String id, String spender, float cap) {
        ResourcePoolComponent pools = resources();
        return pools != null ? pools.consumeResource(id, spender, cap) : 0f;
    }

    public Map<String, Float> getResources() {
        ResourcePoolComponent pools = resources();
        return pools != null ? pools.getResources() : Map.of();
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

    public HexStyleAsset mutableStyle() {
        style = style == null ? HexStyleAsset.empty() : style.clone();
        return style;
    }

    public HexColors getColors() {
        HexColors c = new HexColors();
        if (style != null) {
            if (style.getPrimaryColor() != null) c.setPrimaryColor(style.getPrimaryColor().clone());
            if (style.getSecondaryColor() != null) c.setSecondaryColor(style.getSecondaryColor().clone());
            c.setPrimaryAlpha(style.getAlpha());
        }
        return c;
    }

    public void setColors(@Nullable HexColors colors) {
        if (colors == null) return;
        HexStyleAsset s = mutableStyle();
        s.setPrimaryColor(colors.getPrimaryColor() != null ? colors.getPrimaryColor().clone() : null);
        s.setSecondaryColor(colors.getSecondaryColor() != null ? colors.getSecondaryColor().clone() : null);
        if (colors.getPrimaryAlpha() != null) s.setAlpha(colors.getPrimaryAlpha());
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

    @Nullable
    public UUID getExecutionId() {
        return cast != null ? cast.getExecutionId() : null;
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
        List<String> flowLinks = node.getFlowLinks();
        for (int i = 0; i < flowLinks.size(); i++) {
            toStringWalk(flowLinks.get(i), sb, childPrefix, i == flowLinks.size() - 1, visited);
        }

        visited.remove(id);
    }

}
