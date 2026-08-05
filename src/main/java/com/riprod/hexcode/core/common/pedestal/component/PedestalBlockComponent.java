package com.riprod.hexcode.core.common.pedestal.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset.AnimationSet;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3fUtil;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PedestalBlockComponent implements Component<ChunkStore> {

    public static final BuilderCodec<PedestalBlockComponent> CODEC = BuilderCodec
            .builder(PedestalBlockComponent.class, PedestalBlockComponent::new)
            .append(
                    new KeyedCodec<>("MaxObelisks", Codec.INTEGER),
                    (state, v) -> state.maxObelisks = v,
                    state -> state.maxObelisks)
            .documentation("The max amount of obelsisk that can be spawned")
            .add()
            .append(
                    new KeyedCodec<>("ObeliskRange", Codec.INTEGER),
                    (state, v) -> state.obeliskRange = v,
                    state -> state.obeliskRange)
            .documentation("The range obelisks are detected")
            .add()
            .append(
                    new KeyedCodec<>("PerPlayer", Codec.BOOLEAN),
                    (state, v) -> state.perPlayer = v,
                    state -> state.perPlayer)
            .documentation("Whether the pedestal should be activated per player or globally")
            .add()
            .append(
                    new KeyedCodec<>("MaxRadius", Codec.INTEGER),
                    (state, v) -> state.maxRadius = v,
                    state -> state.maxRadius)
            .documentation("The radius in which players are detected")
            .add()
            .appendInherited(new KeyedCodec<>("DisplayOffsetVector", Vector3fUtil.CODEC),
                    (a, v) -> a.displayOffset = v,
                    a -> a.displayOffset,
                    (a, p) -> a.displayOffset = p.displayOffset)
            .documentation("Imbued item display offset from the center")
            .add()
            .append(new KeyedCodec<>("AnimationSets", new MapCodec<>(ModelAsset.AnimationSet.CODEC, HashMap::new)),
                    (model, m) -> model.animationSetMap = m,
                    (model) -> model.animationSetMap)
            .add()
            .append(new KeyedCodec<>("HolderReference", Codec.STRING), (c, v) -> c.referenceHolder = v,
                    c -> c.referenceHolder)
            .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
            .documentation(
                    "A model that has the animations for the items (book/essence). Used for customizing the animation displays.")
            .add()
            .build();

    private static ComponentType<ChunkStore, PedestalBlockComponent> componentType;

    public static void setComponentType(ComponentType<ChunkStore, PedestalBlockComponent> type) {
        componentType = type;
    }

    public static ComponentType<ChunkStore, PedestalBlockComponent> getComponentType() {
        return componentType;
    }

    private int maxObelisks = 4;
    private int maxRadius = 30;
    private boolean perPlayer = false;
    private int obeliskRange = 30;
    private String referenceHolder = "Pedestal_Holder";
    private Map<String, AnimationSet> animationSetMap = Collections.emptyMap();
    protected Vector3f displayOffset = new Vector3f(0f, 0.3f, 0f);
    // transient runtime
    private Object2FloatOpenHashMap<String> lastTickMap = newTickMap();
    private List<Vector3i> obeliskLocations = new ArrayList<>();
    private Vector3i location;
    private Ref<EntityStore> sessionRef;
    private Ref<EntityStore> anchorRef;
    private String bookAssetId;

    @Nullable
    public Vector3i getLocation() {
        return location;
    }

    public void setLocation(Vector3i location) {
        this.location = location;
    }

    @Nullable
    public Ref<EntityStore> getSessionRef() {
        return sessionRef;
    }

    public void setSessionRef(@Nullable Ref<EntityStore> sessionRef) {
        this.sessionRef = sessionRef;
    }

    @Nullable
    public Ref<EntityStore> getAnchorRef() {
        return anchorRef;
    }

    public void setAnchorRef(@Nullable Ref<EntityStore> anchorRef) {
        this.anchorRef = anchorRef;
    }

    @Nullable
    public String getBookAssetId() {
        return bookAssetId;
    }

    public void setBookAssetId(@Nullable String bookAssetId) {
        this.bookAssetId = bookAssetId;
    }

    public int getMaxObelisks() {
        return maxObelisks;
    }

    public String getReferenceHolder() {
        return referenceHolder;
    }

    public int getObeliskRange() {
        return obeliskRange;
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    public int getActiveObeliskCount() {
        return obeliskLocations.size();
    }

    public List<Vector3i> getActiveObelisks() {
        return obeliskLocations;
    }

    public List<Vector3i> setActiveObelisks(List<Vector3i> obelisks) {
        List<Vector3i> removed = new ArrayList<>(obeliskLocations);
        removed.removeAll(obelisks);
        this.obeliskLocations = new ArrayList<>(obelisks);
        return removed;
    }

    public void addObelisk(Vector3i obeliskLoc) {
        this.obeliskLocations.add(obeliskLoc);
    }

    public boolean removeObelisk(Vector3i obeliskLoc) {
        return this.obeliskLocations.remove(obeliskLoc);
    }

    public Vector3f getDisplayOffset() {
        return this.displayOffset;
    }

    public Map<String, AnimationSet> getAnimationSetMap() {
        return animationSetMap;
    }

    private static Object2FloatOpenHashMap<String> newTickMap() {
        Object2FloatOpenHashMap<String> map = new Object2FloatOpenHashMap<>();
        map.defaultReturnValue(0f);
        return map;
    }

    public float getTickLength(String keyId) {
        return this.lastTickMap.getFloat(keyId);
    }

    public void setTickLength(String keyId, float value) {
        this.lastTickMap.put(keyId, value);
    }

    public void incrementTickLength(String keyId, float dt) {
        this.lastTickMap.addTo(keyId, dt);
    }

    public boolean isPerPlayer() {
        return perPlayer;
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        PedestalBlockComponent copy = new PedestalBlockComponent();
        copy.maxObelisks = this.maxObelisks;
        copy.maxRadius = this.maxRadius;
        copy.perPlayer = this.perPlayer;
        copy.obeliskRange = this.obeliskRange;
        copy.referenceHolder = this.referenceHolder;
        copy.displayOffset = this.displayOffset;
        copy.animationSetMap = this.animationSetMap;
        copy.lastTickMap = newTickMap();
        copy.lastTickMap.putAll(this.lastTickMap);
        copy.obeliskLocations = new ArrayList<>(this.obeliskLocations);
        copy.location = this.location;
        copy.sessionRef = this.sessionRef;
        copy.anchorRef = this.anchorRef;
        copy.bookAssetId = this.bookAssetId;
        return copy;
    }
}
