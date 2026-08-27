package com.riprod.hexcode.core.common.execution.cast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class CastRegistryProxy {

    private final CastComponentRegistry registry;
    private final List<CastComponentType<?>> registered = new ArrayList<>();

    public CastRegistryProxy(@Nonnull CastComponentRegistry registry) {
        this.registry = registry;
    }

    @Nonnull
    public <T extends CastComponent> CastComponentType<T> registerComponent(
            @Nonnull Class<? super T> typeClass, @Nonnull Supplier<T> supplier) {
        return track(registry.registerComponent(typeClass, supplier));
    }

    @Nonnull
    public <T extends CastComponent> CastComponentType<T> registerComponent(
            @Nonnull Class<? super T> typeClass, @Nonnull String id, @Nonnull Supplier<T> supplier,
            @Nonnull BuilderCodec<? extends CastOverlay<T>> overlayCodec) {
        return track(registry.registerComponent(typeClass, id, supplier, overlayCodec));
    }

    public void unregisterAll() {
        for (int i = registered.size() - 1; i >= 0; i--) {
            CastComponentType<?> type = registered.get(i);
            if (type.isValid()) {
                registry.unregisterComponent(type);
            }
        }
        registered.clear();
    }

    @Nonnull
    private <T extends CastComponent> CastComponentType<T> track(@Nonnull CastComponentType<T> type) {
        registered.add(type);
        return type;
    }
}
