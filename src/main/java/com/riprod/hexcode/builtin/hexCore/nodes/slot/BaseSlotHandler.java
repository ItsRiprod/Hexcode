package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import java.util.List;

import com.hypixel.hytale.builtin.beam.AttachedBeam;
import com.hypixel.hytale.builtin.beam.BeamComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hover.utils.HoverableUtils;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.node.BaseNodeHandler;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public abstract class BaseSlotHandler extends BaseNodeHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CRAFT);
    private static final float DRAG_REACH = 2f;

    @Override
    public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        NodeComponent nodeComp = accessor.getComponent(node, NodeComponent.getComponentType());
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (nodeComp == null || craftingComp == null) return InteractionState.Failed;

        Ref<EntityStore> parentRef = nodeComp.getParentEntity();
        if (parentRef == null || !parentRef.isValid()) return InteractionState.Failed;

        var config = NodeConfig.getAssetMap().getAsset(nodeComp.getConfigId());
        var beams = accessor.getComponent(node, BeamComponent.getComponentType());
        if (config != null && beams != null && config.linkBeamIndex() >= 0) {
            beams.add(AttachedBeam.toEntity(config.linkBeamIndex(), config.getLinkBeamScale(), null,
                    new Vector3f(), playerRef, "Head", new Vector3f(0f, 0f, DRAG_REACH)));
        }

        craftingComp.setDraggingRef(node);
        return InteractionState.Finished;
    }

    @Override
    public InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null) return InteractionState.Failed;

        Ref<EntityStore> dropTargetRef = craftingComp.getHoveredRef();
        craftingComp.setDraggingRef(null);
        craftingComp.setDragTickCount(0);

        var result = link(accessor, nodeRef, dropTargetRef);
        syncBeams(accessor, nodeRef);
        return result;
    }

    private static InteractionState link(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> dropTargetRef) {
        if (dropTargetRef == null || !dropTargetRef.isValid()) return InteractionState.Finished;

        Ref<EntityStore> targetGlyphRef = HoverableUtils.getGlyphFromHoverable(accessor, dropTargetRef);
        if (targetGlyphRef == null || !targetGlyphRef.isValid()) return InteractionState.Finished;

        NodeComponent slotNode = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        SlotComponent slotComp = accessor.getComponent(nodeRef, SlotComponent.getComponentType());
        if (slotNode == null || slotComp == null) return InteractionState.Finished;

        Ref<EntityStore> sourceRef = slotNode.getParentEntity();
        if (sourceRef == null || sourceRef.equals(targetGlyphRef)) return InteractionState.Finished;

        GlyphComponent targetGlyph = accessor.getComponent(targetGlyphRef, GlyphComponent.getComponentType());
        if (targetGlyph == null) return InteractionState.Finished;

        GlyphComponent sourceGlyph = accessor.getComponent(sourceRef, GlyphComponent.getComponentType());
        if (sourceGlyph == null) return InteractionState.Finished;

        Slot slot = sourceGlyph.getGlyph().getSlot(slotComp.getSlotKey());
        if (slot != null && slot.isUnique() && slot.getLinks().length >= 1) {
            LOGGER.atFine().log("slot: rejected link to unique slot '%s' on %s (already has %d link(s))",
                    slotComp.getSlotKey(), sourceGlyph.getGlyphId(), slot.getLinks().length);
            return InteractionState.Failed;
        }
        sourceGlyph.getGlyph().addSlotLink(slotComp.getSlotKey(), targetGlyph.getId());
        LOGGER.atFine().log("slot: connected '%s' on %s to glyph %s",
                slotComp.getSlotKey(), sourceGlyph.getGlyphId(), targetGlyph.getId());
        return InteractionState.Finished;
    }

    @Override
    public InteractionState ability(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            InteractionType inputType, Ref<EntityStore> playerRef) {
        if (inputType != InteractionType.Ability3) return InteractionState.Failed;

        SlotComponent slotComp = accessor.getComponent(nodeRef, SlotComponent.getComponentType());
        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        if (slotComp == null || nodeComp == null) return InteractionState.Failed;

        Ref<EntityStore> parentRef = nodeComp.getParentEntity();
        if (parentRef == null) return InteractionState.Failed;

        GlyphComponent parentGlyph = accessor.getComponent(parentRef, GlyphComponent.getComponentType());
        if (parentGlyph == null) return InteractionState.Failed;

        parentGlyph.getGlyph().clearSlot(slotComp.getSlotKey());
        syncBeams(accessor, nodeRef);
        return InteractionState.Finished;
    }

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        syncBeams(accessor, node);
        return InteractionState.Finished;
    }

    private static void syncBeams(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef) {
        var beams = accessor.getComponent(nodeRef, BeamComponent.getComponentType());
        if (beams == null) return;

        SlotComponent slotComp = accessor.getComponent(nodeRef, SlotComponent.getComponentType());
        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        GlyphComponent parentGlyph = nodeComp != null && nodeComp.getParentEntity() != null
                ? accessor.getComponent(nodeComp.getParentEntity(), GlyphComponent.getComponentType())
                : null;
        HexComponent hexComp = parentGlyph != null && parentGlyph.getHexRef() != null
                ? accessor.getComponent(parentGlyph.getHexRef(), HexComponent.getComponentType())
                : null;
        if (slotComp == null || parentGlyph == null || hexComp == null) {
            beams.clear();
            return;
        }

        Slot slot = parentGlyph.getGlyph().getSlot(slotComp.getSlotKey());
        SlotConfig config = parentGlyph.getGlyph().effectiveSlots().get(slotComp.getSlotKey());
        beams.set(slot == null || config == null ? List.of() : VfxUtil.linkBeams(hexComp, slot, config));
    }

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        accessor.tryRemoveEntity(nodeRef, RemoveReason.REMOVE);
    }
}
