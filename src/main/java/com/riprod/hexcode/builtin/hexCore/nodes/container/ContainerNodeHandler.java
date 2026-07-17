package com.riprod.hexcode.builtin.hexCore.nodes.container;
import com.riprod.hexcode.core.common.node.BaseNodeHandler;

import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.SlotSelectedEvent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.CreateHex;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.builtin.hexCore.scene.GlyphStyler;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.builtin.hexCore.nodes.GlyphSpawner;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.hypixel.hytale.logger.HytaleLogger;

public class ContainerNodeHandler extends BaseNodeHandler {
    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public static final ContainerNodeHandler INSTANCE = new ContainerNodeHandler();

    private static final float GLYPH_DISPLAY_DISTANCE = 1.0f;
    private static final float PEDESTAL_GLYPH_PITCH = (float) (-Math.PI / 2);
    private static final Box PREVIEW_BOUNDING_BOX = new Box(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25);

    public Ref<EntityStore> spawnContainer(CommandBuffer<EntityStore> accessor, Hex hex,
            Ref<EntityStore> anchorRef, Vector3d anchorPos, Vector3f offset, Ref<EntityStore> playerRef,
            PedestalSlot slotAsset) {

        Vector3d globalPos = new Vector3d(anchorPos.x + offset.x, anchorPos.y + offset.y, anchorPos.z + offset.z);

        boolean isEmpty = hex == null;

        Holder<EntityStore> holder;
        HexComponent hexComponent = new HexComponent(hex);
        if (!isEmpty) {
            hexComponent.setRootRef(anchorRef);
            hexComponent.setParentRef(null);
            hexComponent.setOffset(offset);

            holder = CreateHex.createHexHolder(accessor, hexComponent, globalPos);
        } else {
            holder = EntityStore.REGISTRY.newHolder();
            holder.addComponent(TransformComponent.getComponentType(),
                    new TransformComponent(globalPos, new Rotation3f()));
            holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
            holder.addComponent(UUIDComponent.getComponentType(),
                    new UUIDComponent(UUID.randomUUID()));
            int networkId = accessor.getExternalData().takeNextNetworkId();
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(networkId));
        }

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Selection_Anchor");

        if (modelAsset == null) {
            return null;
        }

        Model model = Model.createScaledModel(modelAsset, 1.0f);

        holder.addComponent(ModelComponent.getComponentType(),
                new ModelComponent(model));

        holder.addComponent(PersistentModel.getComponentType(),
                new PersistentModel(model.toReference()));

        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(PREVIEW_BOUNDING_BOX));
        HoverableComponent hoverable = new HoverableComponent(HoverableType.NODE);
        if (slotAsset != null && slotAsset.getDescription() != null) {
            hoverable.setHintText("description", Message.translation(slotAsset.getDescription()));
        }
        holder.addComponent(HoverableComponent.getComponentType(), hoverable);
        if (slotAsset != null && slotAsset.getLabel() != null) {
            holder.addComponent(DisplayNameComponent.getComponentType(),
                    new DisplayNameComponent(Message.translation(slotAsset.getLabel())));
        }
        holder.addComponent(NodeComponent.getComponentType(), new NodeComponent(anchorRef, ContainerNodeConfig.TYPE));
        holder.addComponent(DebugComponent.getComponentType(),
                new DebugComponent(DebugShape.Sphere, isEmpty ? CraftingColors.EMPTY_SLOT : CraftingColors.FILLED_SLOT,
                        0.5, 2.0f));

        Ref<EntityStore> hexRef = CreateHex.createEntity(accessor, holder);
        if (isEmpty || hex == null) {
            return hexRef;
        }

        hexComponent.setSelfRef(hexRef);

        int numGlyphs = (int) hex.getGlyphs().stream()
                .filter(glyph -> glyph != null)
                .count();

        float scaleMultiplier = 1 + (numGlyphs * GlyphStyler.SCALE_PER_GLYPH);

        String firstGlyphId = hex.getFirstGlyphId();
        Glyph firstGlyph = firstGlyphId != null ? hex.get(firstGlyphId) : null;
        if (firstGlyph == null)
            return hexRef;
        GlyphComponent firstGlyphComponent = new GlyphComponent(firstGlyph.clone());

        Rotation3f glyphRot = new Rotation3f(PEDESTAL_GLYPH_PITCH, 0, GLYPH_DISPLAY_DISTANCE);
        firstGlyphComponent.setHexRef(hexRef);
        firstGlyphComponent.setParentRef(hexRef);
        firstGlyphComponent.setOffset(new Vector3f());
        firstGlyphComponent.setRotation(glyphRot);
        firstGlyphComponent.setScale(scaleMultiplier);
        hexComponent.setScale(scaleMultiplier);

        GlyphSpawner.spawnGlyphs(accessor, hexComponent, firstGlyphComponent, globalPos, glyphRot, playerRef);
        accessor.putComponent(hexRef, HexComponent.getComponentType(), hexComponent);
        return hexRef;
    }

    @Override
    public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(playerRef, accessor);
        if (pedestal == null) {
            logger.atWarning().log("container enter: pedestal is null");
            return InteractionState.Failed;
        }

        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, accessor);
        if (session == null) {
            logger.atWarning().log("container enter: session is null");
            return InteractionState.Failed;
        }

        if (!session.isOwner(playerRef)) {
            return InteractionState.Failed;
        }

        if (session.getState() != PedestalState.SELECTING) {
            logger.atWarning().log("container enter: wrong state=%s", session.getState());
            return InteractionState.Failed;
        }

        SlotComponent slotRef = accessor.getComponent(node,
                SlotComponent.getComponentType());
        String slotKey = slotRef != null ? slotRef.getSlotKey() : null;
        if (slotKey == null) {
            logger.atWarning().log("container enter: clicked preview has no slot key");
            return InteractionState.Failed;
        }

        // the handler only validates and announces selection intent; session writes,
        // the context transition, and the crafting scene handoff belong to the contexts
        SlotSelectedEvent event = new SlotSelectedEvent(playerRef, slotKey, node);
        accessor.invoke(playerRef, event);
        return event.isCancelled() ? InteractionState.Failed : InteractionState.Finished;
    }

    public Hex prepareForCrafting(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            HexcodeSessionComponent session, String slotKey) {
        HexComponent hexComp = accessor.getComponent(node, HexComponent.getComponentType());
        Hex storedHex = session.getHexAt(slotKey, accessor);

        if (hexComp != null) {
            Map<String, Ref<EntityStore>> oldChildren = hexComp.getChildGlyphRefs();
            if (oldChildren != null) {
                for (Ref<EntityStore> childRef : oldChildren.values()) {
                    if (childRef == null || !childRef.isValid())
                        continue;
                    SlotNodeHandler.INSTANCE.despawnSlotsForGlyph(accessor, childRef);
                    accessor.tryRemoveEntity(childRef, RemoveReason.REMOVE);
                }
                oldChildren.clear();
            }
        }

        Hex originalHex = storedHex != null ? storedHex.clone() : new Hex();
        HexComponent freshComp = new HexComponent(originalHex);
        freshComp.setSelfRef(node);
        if (hexComp != null) freshComp.setRootRef(hexComp.getRootRef());
        accessor.putComponent(node, HexComponent.getComponentType(), freshComp);
        accessor.removeComponent(node, DebugComponent.getComponentType());
        return originalHex;
    }

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
        throw new UnsupportedOperationException("Unimplemented method 'despawn'");
    }
}
