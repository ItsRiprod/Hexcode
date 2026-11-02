package com.riprod.hexcode.core.common.execution.context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.component.BranchVariablesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastDependenciesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastHexComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastManaComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastPolicyComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastSlotComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastStyleComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastVariablesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.DefaultVariableComponent;
import com.riprod.hexcode.core.common.execution.cast.component.ResourcePoolComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class HexContext {

    @Nonnull private final HexCast cast;
    private final CastComponent[] components;

    private transient CommandBuffer<EntityStore> accessor;
    @Nullable private transient Deque<String> resolutionStack;
    private final transient long branchId;

    public HexContext(@Nonnull HexCast cast, @Nonnull CastComponent[] components,
            CommandBuffer<EntityStore> accessor) {
        this.cast = cast;
        this.components = components;
        this.accessor = accessor;
        this.branchId = cast.openBranch();
    }

    private HexContext(@Nonnull HexContext parent) {
        this.cast = parent.cast;
        this.accessor = parent.accessor;
        this.components = new CastComponent[parent.components.length];
        for (int i = 0; i < components.length; i++) {
            var component = parent.components[i];
            components[i] = component != null ? component.branch() : null;
        }
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

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CastComponent> T get(@Nonnull CastComponentType<T> type) {
        var own = (T) components[type.getIndex()];
        return own != null ? own : cast.get(type);
    }

    public <T extends CastComponent> void put(@Nonnull CastComponentType<T> type, @Nonnull T component) {
        components[type.getIndex()] = component;
    }

    @Nonnull
    public Ref<EntityStore> getRootRef() {
        return cast.getRootRef();
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
        return get(CastPolicyComponent.TYPE);
    }

    public void addDependency(Ref<EntityStore> dependent) {
        if (dependent == null) return;
        get(CastDependenciesComponent.TYPE).add(dependent);
    }

    @Nullable
    public Hex getHex() {
        return get(CastHexComponent.TYPE).getHex();
    }

    public Glyph getGlyph(String id) {
        return getHex().get(id);
    }

    @Nonnull
    public Map<String, HexVar> getVariables() {
        return get(CastVariablesComponent.TYPE).all();
    }

    public HexVar getVariable(String slot) {
        return getVariables().get(slot);
    }

    public void setVariable(String slot, HexVar value) {
        get(CastVariablesComponent.TYPE).put(slot, value);
    }

    public void enterLocalScope() {
        get(BranchVariablesComponent.TYPE).enterScope();
    }

    @Nullable
    public HexVar getOwnVariable(String glyphId) {
        var local = get(BranchVariablesComponent.TYPE).get(glyphId);
        if (local != null) return local;
        return getVariables().get(glyphId);
    }

    public void setOwnVariable(String glyphId, HexVar value) {
        HexVar stored = value == null ? null : value.copy();
        get(BranchVariablesComponent.TYPE).put(glyphId, stored);
        get(CastVariablesComponent.TYPE).put(glyphId, stored);
    }

    @Nonnull
    public VolatilityComponent volatility() {
        return get(VolatilityComponent.TYPE);
    }

    @Nonnull
    public ResourcePoolComponent resources() {
        return get(ResourcePoolComponent.TYPE);
    }

    public float getResource(String id) {
        return resources().getResource(id);
    }

    public void addResource(String id, String source, float amount) {
        resources().addResource(id, source, amount);
    }

    public float consumeResource(String id, String spender, float cap) {
        return resources().consumeResource(id, spender, cap);
    }

    public Map<String, Float> getResources() {
        return resources().getResources();
    }

    @Nonnull
    private CastManaComponent mana() {
        return get(CastManaComponent.TYPE);
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

    @Nonnull
    public HexStyleAsset getStyle() {
        return get(CastStyleComponent.TYPE).get();
    }

    public void setStyle(@Nonnull HexStyleAsset style) {
        get(CastStyleComponent.TYPE).set(style);
    }

    public HexStyleAsset mutableStyle() {
        return get(CastStyleComponent.TYPE).mutable();
    }

    public HexColors getColors() {
        HexColors c = new HexColors();
        var style = getStyle();
        if (style.getPrimaryColor() != null) c.setPrimaryColor(style.getPrimaryColor().clone());
        if (style.getSecondaryColor() != null) c.setSecondaryColor(style.getSecondaryColor().clone());
        c.setPrimaryAlpha(style.getAlpha());
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
        return get(DefaultVariableComponent.TYPE).get();
    }

    public void setDefaultVariable(@Nullable HexVar defaultVariable) {
        get(DefaultVariableComponent.TYPE).set(defaultVariable);
    }

    @Nullable
    public String getCastSlotKey() {
        var slot = get(CastSlotComponent.TYPE);
        return slot != null ? slot.get() : null;
    }
}
