package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;

import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean.TrileanSlot;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean.TrileanSlotHandler;
import com.riprod.hexcode.builtin.hexCore.scene.RadialPositionUtil;

public class SlotNodeHandler extends BaseSlotHandler {
    public static final SlotNodeHandler INSTANCE = new SlotNodeHandler();

    public static final double SLOT_SCALE = 0.2;
    private static final float SLOT_RESPAWN_INTERVAL = 0.5f;
    private static final float SLOT_RADIUS = 0.6f;

    public void spawnSlotsForGlyph(CommandBuffer<EntityStore> accessor, Ref<EntityStore> glyphRef,
            Ref<EntityStore> playerRef) {
        GlyphComponent glyphComp = accessor.getComponent(glyphRef, GlyphComponent.getComponentType());
        if (glyphComp == null) return;

        Glyph glyph = glyphComp.getGlyph();
        Map<String, SlotConfig> assetSlots = glyph.effectiveSlots();
        if (assetSlots.isEmpty()) return;

        HeadRotation headRot = accessor.getComponent(playerRef, HeadRotation.getComponentType());
        float pitch = headRot != null ? headRot.getRotation().x : 0f;
        float yaw = headRot != null ? headRot.getRotation().y : 0f;
        float cp = (float) Math.cos(pitch);
        float sp = (float) Math.sin(pitch);
        float cy = (float) Math.cos(yaw);
        float sy = (float) Math.sin(yaw);
        Vector3f right = new Vector3f(-cy, 0f, sy);
        Vector3f up = new Vector3f(sp * sy, cp, sp * cy);

        List<Vector3f> radialFallbacks =
                RadialPositionUtil.calculateOffsets(assetSlots.size(), SLOT_RADIUS, 0f, right, up);
        int radialIndex = 0;

        for (Map.Entry<String, SlotConfig> entry : assetSlots.entrySet()) {
            String key = entry.getKey();
            SlotConfig config = entry.getValue();
            Vector3f offset = radialFallbacks.get(radialIndex++);

            Slot slot = glyph.getOrCreateSlot(key);
            if (slot == null) continue;
            if (glyph.isComponentInstance()) {
                slot = retypePort(glyph, key, config, slot);
                slot.setRawDisplayName(key);
            }
            slot.hydrateFrom(config, key, offset);

            TransformComponent parentTransform = accessor.getComponent(glyphRef, TransformComponent.getComponentType());
            if (parentTransform == null) continue;

            Ref<EntityStore> slotRef = spawnSlotEntityAt(accessor, glyphRef, key, config, offset,
                    parentTransform.getPosition(), slot);
            if (slotRef != null) {
                glyphComp.getSlotEntityRefs().add(slotRef);
            }
        }
    }

    private static Slot retypePort(Glyph glyph, String key, SlotConfig config, Slot slot) {
        Slot typed = config.create();
        if (typed == null || typed.getClass() == slot.getClass()) return slot;
        typed.setLinks(Arrays.copyOf(slot.getLinks(), slot.getLinks().length));
        byte[] state = slot.encodeState();
        if (state != null) typed.decodeState(state);
        glyph.getSlots().put(key, typed);
        return typed;
    }

    private Ref<EntityStore> spawnSlotEntityAt(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> parentRef, String slotKey, SlotConfig config, Vector3f offset,
            Vector3d parentWorldPos, Slot slot) {
        Vector3d slotPos = new Vector3d(
                parentWorldPos.x + offset.x,
                parentWorldPos.y + offset.y,
                parentWorldPos.z + offset.z);

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(slotPos, new Rotation3f()));

        holder.addComponent(SlotComponent.getComponentType(), new SlotComponent(slotKey));

        DebugComponent debug = new DebugComponent(
                slot.getShape(), slot.getColor(), SLOT_SCALE, SLOT_RESPAWN_INTERVAL);
        if (slot instanceof TrileanSlot booleanSlot) {
            TrileanSlotHandler.styleMarker(debug, booleanSlot);
        }
        holder.addComponent(DebugComponent.getComponentType(), debug);

        Box slotBox = new Box(-SLOT_SCALE, -SLOT_SCALE, -SLOT_SCALE,
                SLOT_SCALE, SLOT_SCALE, SLOT_SCALE);
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(slotBox));

        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        int networkId = accessor.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

        HoverableComponent hoverable = new HoverableComponent(HoverableType.NODE);
        hoverable.setHintText("description", Message.translation(slot.displayDescription()));
        holder.addComponent(HoverableComponent.getComponentType(), hoverable);
        holder.addComponent(DisplayNameComponent.getComponentType(),
                new DisplayNameComponent(slot.displayMessage()));

        holder.addComponent(NodeComponent.getComponentType(),
                new NodeComponent(parentRef, config.getId()));

        holder.addComponent(MountedComponent.getComponentType(),
                new MountedComponent(parentRef, new Vector3f(offset.x, offset.y, offset.z), MountController.Minecart));

        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    public void despawnSlotsForGlyph(CommandBuffer<EntityStore> accessor, Ref<EntityStore> glyphRef) {
        GlyphComponent glyphComp = accessor.getComponent(glyphRef, GlyphComponent.getComponentType());
        if (glyphComp == null) return;

        for (Ref<EntityStore> slotRef : glyphComp.getSlotEntityRefs()) {
            if (slotRef == null || !slotRef.isValid()) continue;
            accessor.tryRemoveEntity(slotRef, RemoveReason.REMOVE);
        }
        glyphComp.getSlotEntityRefs().clear();
    }

    public void despawn(CommandBuffer<EntityStore> accessor,
            HexcodeSessionComponent session) {
        // intentionally a no-op
    }
}
