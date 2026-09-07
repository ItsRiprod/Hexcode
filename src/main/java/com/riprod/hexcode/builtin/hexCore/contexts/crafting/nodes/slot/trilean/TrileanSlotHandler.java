package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.BaseSlotHandler;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;

public final class TrileanSlotHandler extends BaseSlotHandler {
    public static final TrileanSlotHandler INSTANCE = new TrileanSlotHandler();

    public static void styleMarker(DebugComponent debug, TrileanSlot slot) {
        debug.setShape(slot.currentShape());
        double s = SlotNodeHandler.SLOT_SCALE;
        debug.setScale(new Vector3d(s, slot.isInverted() ? -s : s, s));
    }

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        SlotComponent slotComp = accessor.getComponent(node, SlotComponent.getComponentType());
        NodeComponent nodeComp = accessor.getComponent(node, NodeComponent.getComponentType());
        if (slotComp == null || nodeComp == null) return InteractionState.Failed;

        Ref<EntityStore> parentRef = nodeComp.getParentEntity();
        if (parentRef == null) return InteractionState.Failed;

        GlyphComponent parentGlyph = accessor.getComponent(parentRef, GlyphComponent.getComponentType());
        if (parentGlyph == null) return InteractionState.Failed;

        Slot slot = parentGlyph.getGlyph().getSlot(slotComp.getSlotKey());
        if (!(slot instanceof TrileanSlot booleanSlot)) return InteractionState.Failed;

        booleanSlot.setState(booleanSlot.getState().cycle());

        DebugComponent debug = accessor.getComponent(node, DebugComponent.getComponentType());
        if (debug != null) styleMarker(debug, booleanSlot);

        accessor.putComponent(node, DisplayNameComponent.getComponentType(),
                new DisplayNameComponent(Message.translation(booleanSlot.displayLabel())));
        HoverableComponent hover = accessor.getComponent(node, HoverableComponent.getComponentType());
        if (hover != null) {
            hover.setHintText("description", Message.translation(booleanSlot.displayDescription()));
        }

        return InteractionState.Finished;
    }
}
