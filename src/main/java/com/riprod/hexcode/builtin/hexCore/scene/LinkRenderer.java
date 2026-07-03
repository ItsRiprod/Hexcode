package com.riprod.hexcode.builtin.hexCore.scene;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.utilities.OrientedDebugUtil;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.utils.VfxUtil;

public class LinkRenderer {

    private static final double LINK_THICKNESS = 0.05;
    private static final double CONE_DIAMETER_NORMAL = 0.06;
    private static final double CONE_DIAMETER_HIDDEN = 0.03;
    private static final float CONE_TIME = 0.6f;
    private static final float THROTTLE_INTERVAL = -0.5f;

    public static void renderLinks(CommandBuffer<EntityStore> accessor, HexcodeSessionComponent session,
            PedestalBlockComponent pedestal, float dt) {
        if (pedestal.getTickLength("LINK_RENDER") < 0) {
            pedestal.incrementTickLength("LINK_RENDER", dt);
            return;
        }
        pedestal.setTickLength("LINK_RENDER", THROTTLE_INTERVAL);

        Ref<EntityStore> anchorNodeRef = session.getAnchorNodeRef();
        if (anchorNodeRef == null || !anchorNodeRef.isValid()) return;

        NodeComponent anchorNode = accessor.getComponent(anchorNodeRef, NodeComponent.getComponentType());
        if (anchorNode == null) return;

        Ref<EntityStore> hexRootRef = anchorNode.getParentEntity();
        if (hexRootRef == null || !hexRootRef.isValid()) return;

        HexComponent hexComp = accessor.getComponent(hexRootRef, HexComponent.getComponentType());
        if (hexComp == null) return;

        World world = accessor.getExternalData().getWorld();
        Hex hex = hexComp.getHex();

        for (Glyph glyph : hex.getGlyphs()) {
            renderGlyphLinks(accessor, world, hexComp, glyph);
        }

        renderAnchorLink(accessor, world, hexComp, anchorNodeRef, hex);
    }

    public static void renderActiveLink(ComponentAccessor<EntityStore> accessor,
            World world, Vector3d source, Vector3d target, Vector3f color) {
        VfxUtil.line(accessor, world, source, target, color, LINK_THICKNESS, 0.04f, DebugUtils.FLAG_NONE);
    }

    private static void renderGlyphLinks(CommandBuffer<EntityStore> accessor, World world,
            HexComponent hexComp, Glyph glyph) {
        Ref<EntityStore> sourceRef = hexComp.getChildGlyphRef(glyph.getId());
        if (sourceRef == null || !sourceRef.isValid()) return;

        TransformComponent sourceTransform = accessor.getComponent(sourceRef, TransformComponent.getComponentType());
        if (sourceTransform == null) return;

        GlyphComponent sourceComp = accessor.getComponent(sourceRef, GlyphComponent.getComponentType());
        if (sourceComp == null) return;

        boolean slotsVisible = sourceComp.areSlotsVisible();
        Vector3d glyphCenter = sourceTransform.getPosition();

        for (Slot slot : glyph.getSlots().values()) {
            for (String linkId : slot.getLinks()) {
                renderSlotLink(accessor, world, hexComp, glyphCenter, slot, slotsVisible, linkId);
            }
        }
    }

    private static void renderSlotLink(CommandBuffer<EntityStore> accessor, World world,
            HexComponent hexComp, Vector3d glyphCenter, Slot slot, boolean slotsVisible, String linkId) {
        Ref<EntityStore> targetRef = hexComp.getChildGlyphRef(linkId);
        if (targetRef == null || !targetRef.isValid()) return;

        TransformComponent targetTransform = accessor.getComponent(targetRef, TransformComponent.getComponentType());
        if (targetTransform == null) return;

        Vector3d origin;
        double diameter;
        if (slotsVisible && slot.getOffset() != null) {
            Vector3f off = slot.getOffset();
            origin = new Vector3d(glyphCenter.x + off.x, glyphCenter.y + off.y, glyphCenter.z + off.z);
            diameter = CONE_DIAMETER_NORMAL;
        } else {
            origin = glyphCenter;
            diameter = CONE_DIAMETER_HIDDEN;
        }

        Vector3f color = slot.getColor() != null ? slot.getColor() : CraftingColors.GLYPH_LINK;
        OrientedDebugUtil.addCone(world, origin, targetTransform.getPosition(), color, diameter, CONE_TIME);
    }

    private static void renderAnchorLink(CommandBuffer<EntityStore> accessor, World world,
            HexComponent hexComp, Ref<EntityStore> anchorRef, Hex hex) {
        String firstId = hex.getFirstGlyphId();
        if (firstId == null) return;

        Ref<EntityStore> targetRef = hexComp.getChildGlyphRef(firstId);
        if (targetRef == null || !targetRef.isValid()) return;

        TransformComponent anchorTransform = accessor.getComponent(anchorRef, TransformComponent.getComponentType());
        TransformComponent targetTransform = accessor.getComponent(targetRef, TransformComponent.getComponentType());
        if (anchorTransform == null || targetTransform == null) return;

        OrientedDebugUtil.addCone(world, anchorTransform.getPosition(), targetTransform.getPosition(),
                CraftingColors.ANCHOR, CONE_DIAMETER_NORMAL, CONE_TIME);
    }
}
