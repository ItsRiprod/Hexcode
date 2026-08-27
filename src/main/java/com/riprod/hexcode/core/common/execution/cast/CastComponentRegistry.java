package com.riprod.hexcode.core.common.execution.cast;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public final class CastComponentRegistry {

    private static final int INITIAL_CAPACITY = 8;

    private String[] ids = new String[INITIAL_CAPACITY];
    private BuilderCodec<?>[] overlayCodecs = new BuilderCodec<?>[INITIAL_CAPACITY];
    private Supplier<?>[] suppliers = new Supplier<?>[INITIAL_CAPACITY];
    private CastComponentType<?>[] types = new CastComponentType<?>[INITIAL_CAPACITY];

    private final Object2ObjectLinkedOpenHashMap<String, CastComponentType<?>> idToType =
            new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, CastComponentType<?>> idView = Collections.unmodifiableMap(idToType);
    private final BitSet indexReuse = new BitSet();
    private int size;

    @Nonnull
    public <T extends CastComponent> CastComponentType<T> registerComponent(
            @Nonnull Class<? super T> typeClass, @Nonnull Supplier<T> supplier) {
        return register(typeClass, null, supplier, null);
    }

    @Nonnull
    public <T extends CastComponent> CastComponentType<T> registerComponent(
            @Nonnull Class<? super T> typeClass, @Nonnull String id, @Nonnull Supplier<T> supplier,
            @Nonnull BuilderCodec<? extends CastOverlay<T>> overlayCodec) {
        return register(typeClass, id, supplier, overlayCodec);
    }

    @Nonnull
    private <T extends CastComponent> CastComponentType<T> register(
            @Nonnull Class<? super T> typeClass, @Nullable String id, @Nonnull Supplier<T> supplier,
            @Nullable BuilderCodec<? extends CastOverlay<T>> overlayCodec) {
        if (id != null && idToType.containsKey(id)) {
            throw new IllegalArgumentException("cast component id '" + id + "' already exists!");
        }

        int index;
        if (indexReuse.isEmpty()) {
            index = size++;
            if (index >= types.length) {
                grow(index + 1);
            }
        } else {
            index = indexReuse.nextSetBit(0);
            indexReuse.clear(index);
        }

        CastComponentType<T> type = new CastComponentType<>();
        type.init(this, typeClass, index);

        ids[index] = id;
        overlayCodecs[index] = overlayCodec;
        suppliers[index] = supplier;
        types[index] = type;
        if (id != null) {
            idToType.put(id, type);
        }
        return type;
    }

    public void unregisterComponent(@Nonnull CastComponentType<?> type) {
        int index = type.getIndex();
        if (index >= types.length || types[index] != type) {
            return;
        }
        String id = ids[index];
        if (id != null) {
            idToType.remove(id);
        }
        ids[index] = null;
        overlayCodecs[index] = null;
        suppliers[index] = null;
        types[index] = null;
        indexReuse.set(index);
        type.invalidate();
    }

    @Nullable
    public CastComponentType<?> getType(@Nonnull String id) {
        return idToType.get(id);
    }

    @Nullable
    public BuilderCodec<?> getOverlayCodec(@Nonnull CastComponentType<?> type) {
        int index = type.getIndex();
        return index < overlayCodecs.length ? overlayCodecs[index] : null;
    }

    @Nonnull
    public Map<String, CastComponentType<?>> getIdView() {
        return idView;
    }

    @Nonnull
    CastComponent create(int index) {
        return (CastComponent) suppliers[index].get();
    }

    private void grow(int minCapacity) {
        int newLength = Math.max(minCapacity, types.length * 2);
        ids = Arrays.copyOf(ids, newLength);
        overlayCodecs = Arrays.copyOf(overlayCodecs, newLength);
        suppliers = Arrays.copyOf(suppliers, newLength);
        types = Arrays.copyOf(types, newLength);
    }
}
