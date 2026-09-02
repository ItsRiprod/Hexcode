package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.obelisks.seeker.HexNamePage;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;

public final class NamedSlotHandler extends BaseSlotHandler {
    public static final NamedSlotHandler INSTANCE = new NamedSlotHandler();

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        NodeComponent nodeComp = accessor.getComponent(node, NodeComponent.getComponentType());
        Ref<EntityStore> parentRef = nodeComp != null ? nodeComp.getParentEntity() : null;
        GlyphComponent parentGlyph = parentRef != null
                ? accessor.getComponent(parentRef, GlyphComponent.getComponentType()) : null;
        if (parentGlyph == null) return InteractionState.Failed;

        Player player = accessor.getComponent(playerRef, Player.getComponentType());
        PlayerRef pr = accessor.getComponent(playerRef, PlayerRef.getComponentType());
        if (player == null || pr == null) return InteractionState.Failed;

        Store<EntityStore> store = accessor.getExternalData().getWorld().getEntityStore().getStore();
        player.getPageManager().openCustomPage(playerRef, store,
                HexNamePage.forSlotGlyph(pr, parentGlyph.getGlyph().getId(), node));
        return InteractionState.Finished;
    }

    @Override
    public InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            craftingComp.setDraggingRef(null);
            craftingComp.setDragTickCount(0);
        }
        return InteractionState.Finished;
    }
}
