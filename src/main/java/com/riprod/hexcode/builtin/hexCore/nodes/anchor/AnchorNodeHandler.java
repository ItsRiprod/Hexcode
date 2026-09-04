package com.riprod.hexcode.builtin.hexCore.nodes.anchor;
import com.riprod.hexcode.core.common.node.BaseNodeHandler;

import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.builtin.beam.AttachedBeam;
import com.hypixel.hytale.builtin.beam.BeamComponent;
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
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.hover.utils.HoverableUtils;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.builtin.hexCore.nodes.glyph.GlyphNodeHandler;

public class AnchorNodeHandler extends BaseNodeHandler {

    private static final double ROOT_NODE_SCALE = 0.2;
    private static final float DRAG_REACH = 5f;

    public static final AnchorNodeHandler INSTANCE = new AnchorNodeHandler();

    public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return InteractionState.Failed;

        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        var config = nodeComp != null ? NodeConfig.getAssetMap().getAsset(nodeComp.getConfigId()) : null;
        var beams = accessor.getComponent(nodeRef, BeamComponent.getComponentType());
        if (config != null && beams != null && config.linkBeamIndex() >= 0) {
            beams.add(AttachedBeam.toEntity(config.linkBeamIndex(), config.getLinkBeamScale(), null,
                    new Vector3f(), playerRef, "Head", new Vector3f(0f, 0f, DRAG_REACH)));
        }

        craftingComp.setDraggingRef(nodeRef);
        return InteractionState.NotFinished;
    }

    public InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        craftingComp.setDraggingRef(null);
        craftingComp.setDragTickCount(0);

        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        if (nodeComp == null)
            return InteractionState.Failed;

        HexComponent hexComp = accessor.getComponent(nodeComp.getParentEntity(),
                HexComponent.getComponentType());

        var result = linkFirstGlyph(accessor, hexComp, craftingComp.getHoveredRef());
        syncBeams(accessor, nodeRef, nodeComp, hexComp);
        return result;
    }

    private static InteractionState linkFirstGlyph(CommandBuffer<EntityStore> accessor,
            HexComponent hexComp, Ref<EntityStore> dropTargetRef) {
        if (dropTargetRef == null || !dropTargetRef.isValid())
            return InteractionState.Finished;

        Ref<EntityStore> targetGlyphRef = HoverableUtils.getGlyphFromHoverable(accessor, dropTargetRef);
        if (targetGlyphRef == null || !targetGlyphRef.isValid())
            return InteractionState.Failed;

        GlyphComponent targetEffect = accessor.getComponent(targetGlyphRef,
                GlyphComponent.getComponentType());
        if (hexComp != null && targetEffect != null) {
            hexComp.getHex().setFirstGlyphId(targetEffect.getId());
        }
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
            syncBeams(accessor, nodeRef, nodeComp, hexComp);
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
        syncBeams(accessor, nodeRef, nodeComp, hexComp);

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
        List<Glyph> children = hexComp != null ? hexComp.getHex().getGlyphs() : List.of();

        var store = accessor.getStore();
        for (Glyph glyph : children) {
            hexComp.addChildGlyphRef(glyph.getId(), new Ref<>(store));
        }

        var config = NodeConfig.getAssetMap().getAsset(node.getConfigId());
        holder.addComponent(BeamComponent.getComponentType(),
                new BeamComponent(firstGlyphBeams(hexComp, config).toArray(AttachedBeam[]::new)));

        Ref<EntityStore> nodeGlyph = accessor.addEntity(holder, AddReason.SPAWN);

        if (hexComp == null) {
            return nodeGlyph;
        }

        for (Glyph glyph : children) {

            Vector3f offset = glyph.getPosition();
            Vector3d worldPos = new Vector3d(
                    rootPos.x + offset.x,
                    rootPos.y + offset.y,
                    rootPos.z + offset.z);

            GlyphNodeHandler.INSTANCE.spawnNode(accessor, nodeGlyph, worldPos,
                    playerRef, new GlyphComponent(glyph), parentRef,
                    hexComp.getChildGlyphRef(glyph.getId()));
        }

        accessor.putComponent(parentRef, HexComponent.getComponentType(), hexComp);
        return nodeGlyph;
    }

    private static List<AttachedBeam> firstGlyphBeams(HexComponent hexComp, NodeConfig config) {
        if (hexComp == null || config == null || config.linkBeamIndex() < 0)
            return List.of();
        String firstId = hexComp.getHex().getFirstGlyphId();
        Ref<EntityStore> first = firstId != null ? hexComp.getChildGlyphRef(firstId) : null;
        if (first == null)
            return List.of();
        return List.of(AttachedBeam.toEntity(config.linkBeamIndex(), config.getLinkBeamScale(), null, first, null));
    }

    private static void syncBeams(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            NodeComponent nodeComp, HexComponent hexComp) {
        var beams = accessor.getComponent(nodeRef, BeamComponent.getComponentType());
        if (beams == null)
            return;
        beams.set(firstGlyphBeams(hexComp, NodeConfig.getAssetMap().getAsset(nodeComp.getConfigId())));
    }

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        craftingComp.setDraggingRef(null);
        craftingComp.setDragTickCount(0);

        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        if (nodeComp != null) {
            syncBeams(accessor, nodeRef, nodeComp,
                    accessor.getComponent(nodeComp.getParentEntity(), HexComponent.getComponentType()));
        }
        return InteractionState.Finished;
    }

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
        throw new UnsupportedOperationException("Unimplemented method 'despawn'");
    }
}
