package com.riprod.hexcode.builtin.elementalism.charge;

import javax.annotation.Nonnull;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class ElementThreads implements CastComponent {

    private final Object2IntOpenHashMap<String> live = new Object2IntOpenHashMap<>();

    public ElementThreads() {
    }

    public void add(@Nonnull String elementId) {
        live.addTo(elementId, 1);
    }

    public void remove(@Nonnull String elementId) {
        var remaining = live.getInt(elementId) - 1;
        if (remaining <= 0) {
            live.removeInt(elementId);
        } else {
            live.put(elementId, remaining);
        }
    }

    public int count(@Nonnull String elementId) {
        return live.getInt(elementId);
    }

    @Nonnull
    @Override
    public ElementThreads copy() {
        var copy = new ElementThreads();
        copy.live.putAll(live);
        return copy;
    }

    @Nonnull
    @Override
    public ElementThreads branch() {
        return this;
    }

    public static final CastComponentType<ElementThreads> TYPE = HexComponentRegistry.register(
            ElementThreads.class, "ElementThreads", a -> new ElementThreads());
}
