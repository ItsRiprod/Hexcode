package com.riprod.hexcode.core.common.glyphs.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlyphComponent implements Component<EntityStore> {

    private enum GlyphFlags {
        Hovering,
        Dragging,
        SlotsVisible
    }

    public static final BuilderCodec<GlyphComponent> CODEC = BuilderCodec
            .builder(GlyphComponent.class, GlyphComponent::new)
            .append(new KeyedCodec<>("Glyph", Glyph.CODEC),
                    (c, v) -> c.glyph = v,
                    c -> c.glyph)
            .add()
            .build();

    private static ComponentType<EntityStore, GlyphComponent> componentType;

    // persistent - from asset
    @Nonnull
    private Glyph glyph = new Glyph();

    // transient
    private Ref<EntityStore> parentRef;
    private Ref<EntityStore> selfRef;
    private Ref<EntityStore> hexRef;
    private Set<GlyphFlags> flags = EnumSet.noneOf(GlyphFlags.class);
    private float scale = 1f;
    private Vector3f visualOffset;
    private List<Ref<EntityStore>> slotEntityRefs = new ArrayList<>();

    public GlyphComponent() {
    }

    public GlyphComponent(@Nonnull Glyph glyph) {
        this.glyph = glyph;
    }

    public static void setComponentType(ComponentType<EntityStore, GlyphComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, GlyphComponent> getComponentType() {
        return componentType;
    }

    @Nonnull
    public Glyph getGlyph() {
        return this.glyph;
    }

    @Nonnull
    public String getId() {
        return this.glyph.getId();
    }

    public List<String> getNext() {
        return this.glyph.getNextLinks();
    }

    @Nonnull
    public String getGlyphId() {
        return this.glyph.getGlyphId();
    }

    @Nullable
    public Ref<EntityStore> getParentRef() {
        return parentRef;
    }

    public void setParentRef(@Nullable Ref<EntityStore> parentRef) {
        this.parentRef = parentRef;
    }

    @Nullable
    public Ref<EntityStore> getSelfRef() {
        return selfRef;
    }

    public void setSelfRef(@Nullable Ref<EntityStore> selfRef) {
        this.selfRef = selfRef;
    }

    @Nullable
    public Ref<EntityStore> getHexRef() {
        return hexRef;
    }

    public void setHexRef(@Nullable Ref<EntityStore> hexRef) {
        this.hexRef = hexRef;
    }

    // legacy alias: under the merged GlyphNodeHandler the glyph entity IS the node entity,
    // so callers that previously despawned the separate node entity now despawn the glyph itself.
    @Nullable
    public Ref<EntityStore> getNodeRef() {
        return selfRef;
    }

    public void setNodeRef(@Nullable Ref<EntityStore> ref) {
        this.selfRef = ref;
    }

    public List<Ref<EntityStore>> getSlotEntityRefs() {
        return slotEntityRefs;
    }

    public float yaw() {
        return this.glyph.getRotation().y;
    }

    public void setYaw(float yaw) {
        this.glyph.getRotation().y = yaw;
    }

    public float pitch() {
        return this.glyph.getRotation().x;
    }

    public void setPitch(float pitch) {
        this.glyph.getRotation().setPitch(pitch);
    }

    public float getDistance() {
        return this.glyph.getRotation().z();
    }

    public void setDistance(float distance) {
        this.glyph.getRotation().setZ(distance);
    }

    public Rotation3f getRotation() {
        return this.glyph.getRotation();
    }

    public void setRotation(Rotation3f rotation) {
        this.glyph.setRotation(rotation);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getOffset() {
        return this.visualOffset != null ? this.visualOffset : this.glyph.getPosition();
    }

    public void setOffset(Vector3f offset) {
        this.glyph.setPosition(offset);
        this.visualOffset = offset;
    }

    public void setOffset(float x, float y, float z) {
        Vector3f v = new Vector3f(x, y, z);
        this.glyph.setPosition(v);
        this.visualOffset = v;
    }

    public void setVisualOffset(Vector3f offset) {
        this.visualOffset = offset;
    }

    public void setHoverState(boolean isHovered) {
        if (isHovered) {
            this.flags.add(GlyphFlags.Hovering);
        } else {
            this.flags.remove(GlyphFlags.Hovering);
        }
    }

    public boolean isHovered() {
        return this.flags.contains(GlyphFlags.Hovering);
    }

    public void setDragState(boolean isBeingDragged) {
        if (isBeingDragged) {
            this.flags.add(GlyphFlags.Dragging);
        } else {
            this.flags.remove(GlyphFlags.Dragging);
        }
    }

    public boolean isBeingDragged() {
        return this.flags.contains(GlyphFlags.Dragging);
    }

    public void setSlotsVisible(boolean visible) {
        if (visible) {
            this.flags.add(GlyphFlags.SlotsVisible);
        } else {
            this.flags.remove(GlyphFlags.SlotsVisible);
        }
    }

    public boolean areSlotsVisible() {
        return this.flags.contains(GlyphFlags.SlotsVisible);
    }

    public float getVolatility() {
        return this.glyph.getVolatility();
    }

    public float getEfficiency() {
        return this.glyph.getEfficiency();
    }

    @Nonnull
    @Override
    public GlyphComponent clone() {
        GlyphComponent copy = new GlyphComponent();
        copy.glyph = this.glyph.clone();
        copy.scale = this.scale;
        copy.parentRef = this.parentRef;
        copy.selfRef = this.selfRef;
        copy.hexRef = this.hexRef;
        copy.flags = this.flags.isEmpty() ? EnumSet.noneOf(GlyphFlags.class) : EnumSet.copyOf(this.flags);
        copy.visualOffset = this.visualOffset != null ? new Vector3f(this.visualOffset) : null;
        copy.slotEntityRefs = new ArrayList<>(this.slotEntityRefs);
        return copy;
    }

    @Override
    public String toString() {
        return String.format("GlyphComponent(Glyph=%s)", this.glyph);
    }
}
