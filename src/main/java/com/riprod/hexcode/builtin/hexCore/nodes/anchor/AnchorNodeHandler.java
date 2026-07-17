package com.riprod.hexcode.builtin.hexCore.nodes.anchor;
import com.riprod.hexcode.core.common.node.BaseNodeHandler;

import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.hover.utils.HoverableUtils;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.builtin.hexCore.nodes.glyph.GlyphNodeHandler;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.builtin.hexCore.scene.LinkRenderer;

public class AnchorNodeHandler extends BaseNodeHandler {

    private static final double ROOT_NODE_SCALE = 0.2;

    public static final AnchorNodeHandler INSTANCE = new AnchorNodeHandler();

    public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return InteractionState.Failed;
        craftingComp.setDraggingRef(nodeRef);
        return InteractionState.NotFinished;
    }

    public InteractionState tick(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        TransformComponent nodeTransform = accessor.getComponent(nodeRef, TransformComponent.getComponentType());

        Transform look = TargetUtil.getLook(playerRef, accessor);
        Vector3d targetPoint = new Vector3d(
                look.getPosition().x + look.getDirection().x * 5,
                look.getPosition().y + look.getDirection().y * 5,
                look.getPosition().z + look.getDirection().z * 5);

        if (nodeTransform != null) {
            LinkRenderer.renderActiveLink(accessor, accessor.getExternalData().getWorld(),
                    nodeTransform.getPosition(), targetPoint,
                    CraftingColors.ANCHOR);
        }
        return InteractionState.Finished;
    }

    public InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());

        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        if (nodeComp == null) {
            craftingComp.setDraggingRef(null);
            craftingComp.setDragTickCount(0);
            return InteractionState.Failed;
        }

        Ref<EntityStore> hexRootRef = nodeComp.getParentEntity();

        Ref<EntityStore> dropTargetRef = craftingComp.getHoveredRef();

        if (dropTargetRef == null || !dropTargetRef.isValid()) {
            craftingComp.setDraggingRef(null);
            craftingComp.setDragTickCount(0);
            return InteractionState.Finished;
        }

        Ref<EntityStore> targetGlyphRef = HoverableUtils.getGlyphFromHoverable(accessor, dropTargetRef);

        if (targetGlyphRef == null || !targetGlyphRef.isValid()) {
            craftingComp.setDraggingRef(null);
            craftingComp.setDragTickCount(0);
            return InteractionState.Failed;
        }

        HexComponent hexComp = accessor.getComponent(hexRootRef,
                HexComponent.getComponentType());
        GlyphComponent targetEffect = accessor.getComponent(targetGlyphRef,
                GlyphComponent.getComponentType());
        if (hexComp != null && targetEffect != null) {
            hexComp.getHex().setFirstGlyphId(targetEffect.getId());
        }

        craftingComp.setDraggingRef(null);
        craftingComp.setDragTickCount(0);
        return InteractionState.Finished;
    }

    public InteractionState ability(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            InteractionType type, Ref<EntityStore> playerRef) {

        if (type != InteractionType.Ability3)
            return InteractionState.Finished;

        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        if (nodeComp == null)
            return InteractionState.Failed;

        Ref<EntityStore> hexRef = nodeComp.getParentEntity();
        if (hexRef == null || !hexRef.isValid())
            return InteractionState.Finished;

        HexComponent hexComp = accessor.getComponent(hexRef, HexComponent.getComponentType());
        if (hexComp == null)
            return InteractionState.Failed;

        if (hexComp.getHex().getFirstGlyphId() != null) {
            hexComp.getHex().setFirstGlyphId(null);
            return InteractionState.Finished;
        }

        List<Ref<EntityStore>> children = hexComp.getChildGlyphRefsList();
        for (Ref<EntityStore> childRef : children) {
            if (childRef != null && childRef.isValid()) {
                GlyphNodeHandler.INSTANCE.despawn(accessor, childRef, playerRef);
            }
        }

        hexComp.getChildGlyphRefs().clear();
        hexComp.getHex().setFirstGlyphId(null);

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            craftingComp.setDraggingRef(null);
            craftingComp.setExpandedGlyphRef(null);
            craftingComp.setDragTickCount(0);
        }

        return InteractionState.Finished;
    }

    public Ref<EntityStore> spawnNode(CommandBuffer<EntityStore> accessor, Hex coreHex, Ref<EntityStore> parentRef,
            Vector3d rootPos,
            Ref<EntityStore> playerRef) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(rootPos, new Rotation3f()));

        NodeComponent node = new NodeComponent(parentRef, AnchorNodeConfig.TYPE);

        holder.addComponent(NodeComponent.getComponentType(), node);

        holder.addComponent(DisplayNameComponent.getComponentType(),
                new DisplayNameComponent(Message.raw("Entrypoint Node")));

        Box nodeBox = new Box(-ROOT_NODE_SCALE, -ROOT_NODE_SCALE, -ROOT_NODE_SCALE,
                ROOT_NODE_SCALE, ROOT_NODE_SCALE, ROOT_NODE_SCALE);
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(nodeBox));

        holder.addComponent(UUIDComponent.getComponentType(),
                new UUIDComponent(UUID.randomUUID()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        int networkId = accessor.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));

        holder.addComponent(DebugComponent.getComponentType(),
                new DebugComponent(DebugShape.Sphere, CraftingColors.ANCHOR,
                        ROOT_NODE_SCALE * 2.5, 2.0f));
        holder.addComponent(HoverableComponent.getComponentType(),
                new HoverableComponent(HoverableType.NODE));

        HexComponent hexComp = accessor.getComponent(parentRef, HexComponent.getComponentType());
        Ref<EntityStore> nodeGlyph = accessor.addEntity(holder, AddReason.SPAWN);

        if (hexComp == null) {
            return nodeGlyph;
        }

        List<Glyph> children = hexComp.getHex().getGlyphs();

        for (Glyph glyph : children) {

            Vector3f offset = glyph.getPosition();
            Vector3d worldPos = new Vector3d(
                    rootPos.x + offset.x,
                    rootPos.y + offset.y,
                    rootPos.z + offset.z);

            GlyphComponent glyphComp = new GlyphComponent(glyph);

            Ref<EntityStore> glyphRef = GlyphNodeHandler.INSTANCE.spawnNode(accessor, nodeGlyph, worldPos,
                    playerRef, glyphComp, parentRef);

            hexComp.addChildGlyphRef(glyph.getId(), glyphRef);
        }

        accessor.putComponent(parentRef, HexComponent.getComponentType(), hexComp);
        return nodeGlyph;
    }

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        craftingComp.setDraggingRef(null);
        craftingComp.setDragTickCount(0);
        return InteractionState.Finished;
    }

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
        throw new UnsupportedOperationException("Unimplemented method 'despawn'");
    }
}
