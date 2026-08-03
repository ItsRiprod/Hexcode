package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hover.utils.HoverableUtils;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.core.common.node.BaseNodeHandler;
import com.riprod.hexcode.builtin.hexCore.scene.LinkRenderer;

public abstract class BaseSlotHandler extends BaseNodeHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        NodeComponent nodeComp = accessor.getComponent(node, NodeComponent.getComponentType());
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (nodeComp == null || craftingComp == null) return InteractionState.Failed;

        Ref<EntityStore> parentRef = nodeComp.getParentEntity();
        if (parentRef == null || !parentRef.isValid()) return InteractionState.Failed;

        craftingComp.setDraggingRef(node);
        return InteractionState.Finished;
    }

    @Override
    public InteractionState tick(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null) return InteractionState.Failed;

        TransformComponent nodeTransform = accessor.getComponent(
                craftingComp.getDraggingRef(), TransformComponent.getComponentType());
        if (nodeTransform == null) return InteractionState.Finished;

        Transform look = TargetUtil.getLook(playerRef, accessor);
        Vector3d targetPoint = new Vector3d(
                look.getPosition().x + look.getDirection().x * 2,
                look.getPosition().y + look.getDirection().y * 2,
                look.getPosition().z + look.getDirection().z * 2);

        Vector3f color = resolveActiveLinkColor(accessor, node);
        LinkRenderer.renderActiveLink(accessor, accessor.getExternalData().getWorld(),
                nodeTransform.getPosition(), targetPoint, color);
        return InteractionState.Finished;
    }

    protected Vector3f resolveActiveLinkColor(CommandBuffer<EntityStore> accessor, Ref<EntityStore> slotRef) {
        SlotComponent slotComp = accessor.getComponent(slotRef, SlotComponent.getComponentType());
        NodeComponent nodeComp = accessor.getComponent(slotRef, NodeComponent.getComponentType());
        if (slotComp == null || nodeComp == null) return CraftingColors.GLYPH_LINK;

        Ref<EntityStore> parentRef = nodeComp.getParentEntity();
        if (parentRef == null) return CraftingColors.GLYPH_LINK;

        GlyphComponent parentGlyph = accessor.getComponent(parentRef, GlyphComponent.getComponentType());
        if (parentGlyph == null) return CraftingColors.ANCHOR;

        Slot slot = parentGlyph.getGlyph().getSlot(slotComp.getSlotKey());
        if (slot == null || slot.getColor() == null) return CraftingColors.GLYPH_LINK;
        return slot.getColor();
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
        return InteractionState.Finished;
    }

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        return InteractionState.Finished;
    }

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        accessor.tryRemoveEntity(nodeRef, RemoveReason.REMOVE);
    }
}
