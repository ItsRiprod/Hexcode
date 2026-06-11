package com.riprod.hexcode.core.state.crafting.handlers.node.Slot;

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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.glyphs.registry.StyleResolution;
import com.riprod.hexcode.core.common.glyphs.registry.StyleResolution.ResolvedStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.state.crafting.component.NodeComponent;
import com.riprod.hexcode.core.state.crafting.component.SlotComponent;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.utils.RadialPositionUtil;

public class SlotNodeHandler extends BaseSlotHandler {
    public static final SlotNodeHandler INSTANCE = new SlotNodeHandler();

    private static final double SLOT_SCALE = 0.2;
    private static final float SLOT_RESPAWN_INTERVAL = 0.5f;
    private static final float SLOT_RADIUS = 0.6f;

    public void spawnSlotsForGlyph(CommandBuffer<EntityStore> accessor, Ref<EntityStore> glyphRef,
            Ref<EntityStore> playerRef) {
        GlyphComponent glyphComp = accessor.getComponent(glyphRef, GlyphComponent.getComponentType());
        if (glyphComp == null) return;

        Glyph glyph = glyphComp.getGlyph();
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        if (asset == null) return;

        Map<String, SlotAsset> assetSlots = asset.getSlots();
        if (assetSlots.isEmpty()) return;

        List<Vector3f> radialFallbacks = computeRadialFallbacks(assetSlots);
        int radialIndex = 0;

        for (Map.Entry<String, SlotAsset> entry : assetSlots.entrySet()) {
            String key = entry.getKey();
            SlotAsset slotAsset = entry.getValue();
            Vector3f offset = slotAsset.getOffset();
            if (offset == null) {
                offset = radialFallbacks.get(radialIndex++);
            }

            Slot slot = glyph.getOrCreateSlot(key);
            slot.hydrateFrom(slotAsset, key, offset, glyph.getGlyphId());

            TransformComponent parentTransform = accessor.getComponent(glyphRef, TransformComponent.getComponentType());
            if (parentTransform == null) continue;

            Ref<EntityStore> slotRef = spawnSlotEntityAt(accessor, glyphRef, key, slotAsset, offset,
                    parentTransform.getPosition(), glyph.getGlyphId());
            if (slotRef != null) {
                glyphComp.getSlotEntityRefs().add(slotRef);
            }
        }
    }

    private List<Vector3f> computeRadialFallbacks(Map<String, SlotAsset> assetSlots) {
        int unsetCount = 0;
        for (SlotAsset s : assetSlots.values()) {
            if (s.getOffset() == null) unsetCount++;
        }
        if (unsetCount == 0) return new ArrayList<>();
        return RadialPositionUtil.calculateOffsets(unsetCount, SLOT_RADIUS, 0f, null);
    }

    private Ref<EntityStore> spawnSlotEntityAt(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> parentRef, String slotKey, SlotAsset asset, Vector3f offset,
            Vector3d parentWorldPos, String glyphId) {
        ResolvedStyle rs = StyleResolution.resolve(asset, glyphId, slotKey);

        Vector3d slotPos = new Vector3d(
                parentWorldPos.x + offset.x,
                parentWorldPos.y + offset.y,
                parentWorldPos.z + offset.z);

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(slotPos, new Rotation3f()));

        holder.addComponent(SlotComponent.getComponentType(), new SlotComponent(slotKey));

        holder.addComponent(DebugComponent.getComponentType(),
                new DebugComponent(rs.shape(), rs.color(), SLOT_SCALE, SLOT_RESPAWN_INTERVAL));

        Box slotBox = new Box(-SLOT_SCALE, -SLOT_SCALE, -SLOT_SCALE,
                SLOT_SCALE, SLOT_SCALE, SLOT_SCALE);
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(slotBox));

        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        int networkId = accessor.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

        HoverableComponent hoverable = new HoverableComponent(HoverableType.NODE);
        hoverable.setHintText("description", Message.translation(asset.getDescription()));
        holder.addComponent(HoverableComponent.getComponentType(), hoverable);
        holder.addComponent(DisplayNameComponent.getComponentType(),
                new DisplayNameComponent(Message.translation(asset.getLabel())));

        holder.addComponent(NodeComponent.getComponentType(),
                new NodeComponent(parentRef, rs.handlerId()));

        holder.addComponent(MountedComponent.getComponentType(),
                new MountedComponent(parentRef, new Rotation3f(offset.x, offset.y, offset.z), MountController.Minecart));

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
