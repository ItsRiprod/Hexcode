package com.riprod.hexcode.core.common.hexes.component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexes.codec.HexFieldCodec;

public class HexComponent implements Component<EntityStore> {

    private enum HexFlags {
        Hovering,
        Dragging,
        Hiding,
        Selected
    }

    public static final BuilderCodec<HexComponent> CODEC = BuilderCodec
            .builder(HexComponent.class, HexComponent::new)
            .append(new KeyedCodec<>("Hex", HexFieldCodec.PLAYER),
                    (c, v) -> c.hex = v,
                    c -> c.hex)
            .add()
            .build();

    private static ComponentType<EntityStore, HexComponent> componentType;

    // persistent - from asset
    @Nonnull
    private Hex hex;

    // transient
    private Vector3f offset = new Vector3f(); // offset from parent
    private Rotation3f rotation = new Rotation3f(); // x = yaw, y = pitch, z = distance
    private float scale = 1f;
    private Map<String, Ref<EntityStore>> childGlyphRefs = new HashMap<>();
    private Ref<EntityStore> parentRef;
    private Ref<EntityStore> selfRef;
    private Ref<EntityStore> rootRef;
    private Set<HexFlags> flags = EnumSet.noneOf(HexFlags.class);

    // for the codec - do not use
    public HexComponent() {
    }

    public HexComponent(@Nonnull Hex hex) {
        this.hex = hex;
    }

    public static void setComponentType(ComponentType<EntityStore, HexComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexComponent> getComponentType() {
        return componentType;
    }

    @Nullable
    public Ref<EntityStore> getParentRef() {
        return parentRef;
    }

    public void setParentRef(@Nullable Ref<EntityStore> parentRef) {
        this.parentRef = parentRef;
    }

    @Nullable
    public Ref<EntityStore> getRootRef() {
        return rootRef;
    }

    public void setRootRef(@Nullable Ref<EntityStore> rootRef) {
        this.rootRef = rootRef;
    }

    @Nullable
    public Ref<EntityStore> getSelfRef() {
        return selfRef;
    }

    public void setSelfRef(@Nullable Ref<EntityStore> selfRef) {
        this.selfRef = selfRef;
    }

    public Hex getHex() {
        return hex;
    }

    public List<Glyph> getGlyphs() {
        return hex.getGlyphs();
    }

    public List<Glyph> getGlyphs(List<String> glyphIds) {
        return hex.getGlyphs(glyphIds);
    }

    public String getId() {
        return this.hex.getHexId();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Map<String, Ref<EntityStore>> getChildGlyphRefs() {
        return childGlyphRefs;
    }

    public Ref<EntityStore> getChildGlyphRef(String glyphId) {
        return childGlyphRefs.get(glyphId);
    }

    public List<Ref<EntityStore>> getChildGlyphRefs(List<String> glyphIds) {
        List<Ref<EntityStore>> refs = new ArrayList<>();
        for (String glyphId : glyphIds) {
            Ref<EntityStore> ref = childGlyphRefs.get(glyphId);
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    public List<Ref<EntityStore>> getChildGlyphRefsList() {
        return new ArrayList<>(childGlyphRefs.values());
    }

    public void addChildGlyphRef(String glyphId, Ref<EntityStore> childRef) {
        this.childGlyphRefs.put(glyphId, childRef);
    }

    public void addChildGlyphRefs(Map<String, Ref<EntityStore>> childGlyphRefs) {
        this.childGlyphRefs.putAll(childGlyphRefs);
    }

    public void setChildGlyphRefs(Map<String, Ref<EntityStore>> childGlyphRefs) {
        this.childGlyphRefs = childGlyphRefs;
    }

    public void removeChildGlyph(String glyphId) {
        this.childGlyphRefs.remove(glyphId);
    }

    public float yaw() {
        return this.rotation.y;
    }

    public void setYaw(float yaw) {
        this.rotation.y = yaw;
    }

    public float pitch() {
        return this.rotation.x;
    }

    public Rotation3f getRotation() {
        return this.rotation;
    }

    public void setPitch(float pitch) {
        this.rotation.x = pitch;
    }

    public float getDistance() {
        return this.rotation.z;
    }

    public void setDistance(float distance) {
        this.rotation.z = distance;
    }

    public void setRotation(Rotation3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getOffset() {
        return offset;
    }

    public void setOffset(Vector3f offset) {
        this.offset = offset;
    }

    public void setOffset(float x, float y, float z) {
        this.offset = new Vector3f(x, y, z);
    }

    public void setHoverState(boolean isHovered) {
        if (isHovered) {
            this.flags.add(HexFlags.Hovering);
        } else {
            this.flags.remove(HexFlags.Hovering);
        }
    }

    public boolean isHovered() {
        return this.flags.contains(HexFlags.Hovering);
    }

    public void setDragState(boolean isBeingDragged) {
        if (isBeingDragged) {
            this.flags.add(HexFlags.Dragging);
        } else {
            this.flags.remove(HexFlags.Dragging);
        }
    }

    public boolean isBeingDragged() {
        return this.flags.contains(HexFlags.Dragging);
    }

    @Nonnull
    @Override
    public HexComponent clone() {
        HexComponent copy = new HexComponent();
        copy.hex = this.hex.clone();
        copy.offset = new Vector3f(this.offset.x(), this.offset.y(), this.offset.z());
        copy.rotation = new Rotation3f(this.rotation.x(), this.rotation.y(), this.rotation.z());
        copy.scale = this.scale;
        copy.parentRef = this.parentRef;
        copy.selfRef = this.selfRef;
        copy.rootRef = this.rootRef;
        copy.childGlyphRefs = new HashMap<>(this.childGlyphRefs);
        copy.flags = this.flags.isEmpty() ? EnumSet.noneOf(HexFlags.class) : EnumSet.copyOf(this.flags);
        return copy;
    }
}
