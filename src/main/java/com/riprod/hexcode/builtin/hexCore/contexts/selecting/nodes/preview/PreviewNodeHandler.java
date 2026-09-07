package com.riprod.hexcode.builtin.hexCore.contexts.selecting.nodes.preview;

import com.riprod.hexcode.core.common.node.BaseNodeHandler;

import java.util.List;
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
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.CreateHex;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.component.HoverableType;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.GlyphMath;
import com.riprod.hexcode.builtin.hexCore.scene.GlyphStyler;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.hypixel.hytale.logger.HytaleLogger;

/**
 * The Container Node is the preview node that appears around the pedestal in
 * selecting mode
 * ContainerNodeHandler
 */
public class PreviewNodeHandler extends BaseNodeHandler {
    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public static final PreviewNodeHandler INSTANCE = new PreviewNodeHandler();

    private static final float GLYPH_DISPLAY_DISTANCE = 1.0f;
    private static final float PEDESTAL_GLYPH_PITCH = (float) (-Math.PI / 2);
    private static final Box PREVIEW_BOUNDING_BOX = new Box(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25);
    private static final float GLYPH_HEIGHT_MULTIPLIER = 0.2f;

    public static Ref<EntityStore> spawnForSlot(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session, Ref<EntityStore> player, Vector3d anchorPos,
            Vector3f offset, String slotKey, PedestalSlot slotAsset, Hex hex) {
        Ref<EntityStore> anchorRef = session.getAnchorRef();
        if (anchorRef == null || !anchorRef.isValid()) {
            return null;
        }
        Ref<EntityStore> containerRef = INSTANCE.spawnContainer(buffer, hex, anchorRef, anchorPos,
                offset, player, slotAsset);
        if (containerRef == null) {
            return null;
        }
        buffer.addComponent(containerRef, SlotComponent.getComponentType(), new SlotComponent(slotKey));
        return containerRef;
    }

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
        holder.addComponent(NodeComponent.getComponentType(), new NodeComponent(anchorRef, PreviewNodeConfig.TYPE));
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

        spawnGlyphs(accessor, hexComponent, firstGlyphComponent, globalPos, glyphRot, playerRef);
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

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
        throw new UnsupportedOperationException("Unimplemented method 'despawn'");
    }

    private static void spawnGlyphs(CommandBuffer<EntityStore> accessor, HexComponent hex, GlyphComponent glyph,
            Vector3d parentPos, Rotation3f parentRot, Ref<EntityStore> playerRef) {

        Ref<EntityStore> glyphRef = CreateGlyph.createGlyph(accessor, glyph, parentPos, parentRot, playerRef);
        glyph.setSelfRef(glyphRef);
        hex.addChildGlyphRef(glyph.getId(), glyphRef);

        List<Glyph> children = hex.getGlyphs(glyph.getFlowLinks());

        List<Rotation3f> childRotations = GlyphMath.getChildRotations(children.size(), glyph.getScale(),
                glyph.getRotation().z());

        for (int i = 0; i < children.size(); i++) {
            Glyph childGlyph = children.get(i);
            Rotation3f childRotation = childRotations.get(i);
            if (hex.getChildGlyphRef(childGlyph.getId()) != null) {
                continue;
            }

            GlyphComponent childGlyphComponent = new GlyphComponent(childGlyph.clone());

            if (children.size() == 1) {
                childGlyphComponent.setScale(glyph.getScale() * GlyphStyler.SCALE_SINGLE_GLYPH);
            } else {
                childGlyphComponent.setScale(glyph.getScale() * GlyphStyler.SCALE_MULTIPLIER);
            }

            childGlyphComponent.setRotation(childRotation);
            Vector3f offset = GlyphMath.toMountOffset(childRotation, childGlyph.getRotation());
            float yOffset = childGlyphComponent.getScale() * GLYPH_HEIGHT_MULTIPLIER;
            Vector3f scaledOffset = offset.add(0, 0, yOffset);
            childGlyphComponent.setVisualOffset(scaledOffset);

            childGlyphComponent.setParentRef(glyph.getSelfRef());
            childGlyphComponent.setHexRef(hex.getSelfRef());

            spawnGlyphs(accessor, hex, childGlyphComponent, parentPos, parentRot, playerRef);
        }
    }
}
