package com.riprod.hexcode.core.common.execution.cast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public final class HexComponentRegistry {

    private static final List<CastComponentType<?>> TYPES = new ArrayList<>();
    private static final List<BuilderCodec<?>> OVERLAY_CODECS = new ArrayList<>();
    private static final List<CastHydrator<?>> HYDRATORS = new ArrayList<>();
    private static final Map<String, CastComponentType<?>> BY_ID = new HashMap<>();
    private static final Object2ObjectLinkedOpenHashMap<String, CastComponentType<?>> OVERLAY_IDS =
            new Object2ObjectLinkedOpenHashMap<>();
    private static final Map<String, CastComponentType<?>> OVERLAY_ID_VIEW =
            Collections.unmodifiableMap(OVERLAY_IDS);
    private static boolean frozen;

    private HexComponentRegistry() {
    }

    @Nonnull
    public static <T extends CastComponent> CastComponentType<T> register(
            @Nonnull Class<? super T> typeClass, @Nonnull String id) {
        return add(typeClass, id, null, null);
    }

    @Nonnull
    public static <T extends CastComponent> CastComponentType<T> register(
            @Nonnull Class<? super T> typeClass, @Nonnull String id,
            @Nonnull BuilderCodec<? extends CastOverlay<T>> overlayCodec) {
        return add(typeClass, id, overlayCodec, null);
    }

    @Nonnull
    public static <T extends CastComponent> CastComponentType<T> register(
            @Nonnull Class<? super T> typeClass, @Nonnull String id, @Nonnull CastHydrator<T> hydrator) {
        return add(typeClass, id, null, hydrator);
    }

    @Nonnull
    public static <T extends CastComponent> CastComponentType<T> register(
            @Nonnull Class<? super T> typeClass, @Nonnull String id,
            @Nonnull BuilderCodec<? extends CastOverlay<T>> overlayCodec, @Nonnull CastHydrator<T> hydrator) {
        return add(typeClass, id, overlayCodec, hydrator);
    }

    @Nonnull
    private static <T extends CastComponent> CastComponentType<T> add(
            @Nonnull Class<? super T> typeClass, @Nonnull String id,
            @Nullable BuilderCodec<? extends CastOverlay<T>> overlayCodec,
            @Nullable CastHydrator<T> hydrator) {
        if (frozen) {
            throw new IllegalStateException("cast component " + typeClass.getName()
                    + " registered after the first cast; register during setup");
        }
        if (BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("cast component id '" + id + "' already exists!");
        }
        var type = new CastComponentType<T>(typeClass, TYPES.size(), id);
        TYPES.add(type);
        OVERLAY_CODECS.add(overlayCodec);
        HYDRATORS.add(hydrator);
        BY_ID.put(id, type);
        if (overlayCodec != null) {
            OVERLAY_IDS.put(id, type);
        }
        return type;
    }

    public static int size() {
        return TYPES.size();
    }

    @Nonnull
    public static CastComponentType<?> getType(int index) {
        return TYPES.get(index);
    }

    @Nullable
    public static CastComponentType<?> getType(@Nonnull String id) {
        return BY_ID.get(id);
    }

    @Nullable
    public static BuilderCodec<?> getOverlayCodec(@Nonnull CastComponentType<?> type) {
        return OVERLAY_CODECS.get(type.getIndex());
    }

    @Nullable
    public static CastHydrator<?> getHydrator(@Nonnull CastComponentType<?> type) {
        return HYDRATORS.get(type.getIndex());
    }

    @Nonnull
    public static Map<String, CastComponentType<?>> getOverlayIdView() {
        return OVERLAY_ID_VIEW;
    }

    public static void requireLoaded(@Nonnull CastComponentType<?>... types) {
        for (var i = 0; i < types.length; i++) {
            if (types[i] == null) {
                throw new IllegalStateException("cast component type at position " + i + " is not loaded");
            }
        }
    }

    public static void freeze() {
        frozen = true;
    }
}
