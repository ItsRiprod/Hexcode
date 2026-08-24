package com.riprod.hexcode.core.common.execution.context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.component.CastDependenciesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastManaComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastPolicyComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastVariablesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.ResourcePoolComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.root.HexRoot;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class HexContext {

    @Nonnull private final HexCast cast;
    @Nullable private HexStyleAsset style;
    @Nullable private HexVar defaultVariable;

    private transient CommandBuffer<EntityStore> accessor;
    @Nullable private transient Map<String, HexVar> localVariables;
    @Nullable private transient Deque<String> resolutionStack;
    private final transient long branchId;

    public HexContext(Hex hex, float manaCost, HexRoot hexRoot, @Nullable HexStyleAsset style,
            @Nonnull HexCast cast) {
        this.cast = cast;
        this.style = style;
        cast.setHex(hex);
        cast.setRoot(hexRoot);
        cast.getOrCreate(CastManaComponent.getComponentType()).setCost(manaCost);
        this.branchId = cast.openBranch();
    }

    private HexContext(@Nonnull HexContext parent) {
        this.cast = parent.cast;
        this.style = parent.style;
        this.defaultVariable = parent.defaultVariable;
        this.accessor = parent.accessor;
        this.localVariables = parent.localVariables;
        this.branchId = parent.cast.openBranch();
    }

    public HexContext branch() {
        return new HexContext(this);
    }

    public long getBranchId() {
        return branchId;
    }

    public void endBranch() {
        cast.closeBranch(branchId);
    }

    @Nonnull
    public HexCast cast() {
        return cast;
    }

    @Nullable
    public UUID getExecutionId() {
        return cast.getExecutionId();
    }

    public CommandBuffer<EntityStore> getAccessor() {
        return accessor;
    }

    public void UpdateAccessor(CommandBuffer<EntityStore> newAccessor) {
        this.accessor = newAccessor;
    }

    public void updateRuntimeAccessors(CommandBuffer<EntityStore> buffer) {
        this.accessor = buffer;
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

    @Nonnull
    public CastPolicyComponent policy() {
        return cast.policy();
    }

    public void addDependency(Ref<EntityStore> dependent) {
        if (dependent == null) return;
        cast.getOrCreate(CastDependenciesComponent.getComponentType()).add(dependent);
    }

    @Nullable
    public HexRoot getHexRoot() {
        return cast.getRoot();
    }

    @Nullable
    public Ref<EntityStore> getCasterRef(ComponentAccessor<EntityStore> accessor) {
        HexRoot root = cast.getRoot();
        return root != null ? root.getSourceRef(accessor) : null;
    }

    @Nullable
    public Hex getHex() {
        return cast.getHex();
    }

    public Glyph getGlyph(String id) {
        return cast.getHex().get(id);
    }

    @Nonnull
    public Map<String, HexVar> getVariables() {
        return cast.getOrCreate(CastVariablesComponent.getComponentType()).all();
    }

    public HexVar getVariable(String slot) {
        return getVariables().get(slot);
    }

    public void setVariable(String slot, HexVar value) {
        cast.getOrCreate(CastVariablesComponent.getComponentType()).put(slot, value);
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
        return getVariables().get(glyphId);
    }

    public void setOwnVariable(String glyphId, HexVar value) {
        HexVar stored = value == null ? null : value.copy();
        if (this.localVariables != null) this.localVariables.put(glyphId, stored);
        cast.getOrCreate(CastVariablesComponent.getComponentType()).put(glyphId, stored);
    }

    @Nonnull
    public VolatilityComponent volatility() {
        return cast.volatility();
    }

    @Nullable
    public ResourcePoolComponent resources() {
        return cast.resources();
    }

    public float getResource(String id) {
        ResourcePoolComponent pools = resources();
        return pools != null ? pools.getResource(id) : 0f;
    }

    public void addResource(String id, String source, float amount) {
        cast.mutableResources().addResource(id, source, amount);
    }

    public float consumeResource(String id, String spender, float cap) {
        ResourcePoolComponent pools = resources();
        return pools != null ? pools.consumeResource(id, spender, cap) : 0f;
    }

    public Map<String, Float> getResources() {
        ResourcePoolComponent pools = resources();
        return pools != null ? pools.getResources() : Map.of();
    }

    @Nonnull
    private CastManaComponent mana() {
        return cast.getOrCreate(CastManaComponent.getComponentType());
    }

    public float getManaMultiplier() {
        return mana().getMultiplier();
    }

    public void setManaMultiplier(float manaCostMultiplier) {
        mana().setMultiplier(manaCostMultiplier);
    }

    public float getManaCost() {
        return mana().getTotal();
    }

    public void setManaCost(float manaCost) {
        mana().setCost(manaCost);
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
        return cast.getSlotKey();
    }

    public void setCastSlotKey(@Nullable String castSlotKey) {
        cast.setSlotKey(castSlotKey);
    }
}
