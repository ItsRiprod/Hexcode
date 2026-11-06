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
import com.riprod.hexcode.core.common.execution.cast.component.CastEffectsComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastVariablesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.DefaultVariableComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;

public class HexContext {

    @Nonnull private final HexCast cast;
    private final CastComponent[] components;

    private transient CommandBuffer<EntityStore> accessor;
    @Nullable private transient Deque<String> resolutionStack;
    private final transient long branchId;
    @Nullable private transient UUID holder;
    @Nullable private transient Ref<EntityStore> host;

    public HexContext(@Nonnull HexCast cast, @Nonnull CastComponent[] components,
            CommandBuffer<EntityStore> accessor) {
        this.cast = cast;
        this.components = components;
        this.accessor = accessor;
        this.branchId = cast.openBranch(this);
    }

    private HexContext(@Nonnull HexContext parent) {
        this.cast = parent.cast;
        this.accessor = parent.accessor;
        this.components = new CastComponent[parent.components.length];
        for (int i = 0; i < components.length; i++) {
            var component = parent.components[i];
            components[i] = component != null ? component.branch() : null;
        }
        this.branchId = parent.cast.openBranch(this);
    }

    public HexContext branch() {
        return new HexContext(this);
    }

    public long getBranchId() {
        return branchId;
    }

    public boolean isLive() {
        return cast.isBranchLive(branchId);
    }

    @Nullable
    public UUID getHolder() {
        return holder;
    }

    public boolean isHeld() {
        return holder != null;
    }

    public boolean hold(@Nonnull UUID constructId) {
        if (holder != null) {
            return false;
        }
        holder = constructId;
        return true;
    }

    public boolean attach(@Nonnull UUID constructId, @Nonnull Ref<EntityStore> hostRef) {
        if (!constructId.equals(holder) || host != null || !isLive()) {
            return false;
        }
        host = hostRef;
        return true;
    }

    public boolean isAttachedTo(@Nonnull UUID constructId) {
        return host != null && constructId.equals(holder);
    }

    @Nullable
    public Ref<EntityStore> release() {
        var attached = host;
        holder = null;
        host = null;
        return attached;
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
    public GlyphEffects getEffects() {
        return get(CastEffectsComponent.TYPE).get();
    }

    public GlyphEffects mutableEffects() {
        return get(CastEffectsComponent.TYPE).mutable();
    }

    public HexColors getColors() {
        HexColors c = new HexColors();
        var effects = getEffects();
        if (effects.getPrimaryColor() != null) c.setPrimaryColor(effects.getPrimaryColor().clone());
        if (effects.getSecondaryColor() != null) c.setSecondaryColor(effects.getSecondaryColor().clone());
        c.setPrimaryAlpha(effects.getAlpha());
        return c;
    }

    public void setColors(@Nullable HexColors colors) {
        if (colors == null) return;
        var s = mutableEffects();
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
