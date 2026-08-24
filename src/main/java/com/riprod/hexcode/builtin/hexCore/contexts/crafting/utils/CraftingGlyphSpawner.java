package com.riprod.hexcode.builtin.hexCore.contexts.crafting.utils;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.builtin.hexCore.nodes.glyph.GlyphNodeHandler;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.utils.VfxUtil;

public final class CraftingGlyphSpawner {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private CraftingGlyphSpawner() {
    }

    @Nullable
    public static Vector3d calculateDrawCenter(List<DrawnShapeComponent> drawnShapes) {
        double x = 0, y = 0, z = 0;
        int count = 0;
        for (DrawnShapeComponent shape : drawnShapes) {
            List<Vector3d> points = shape.getPoints();
            if (points == null) {
                continue;
            }
            for (Vector3d p : points) {
                x += p.x;
                y += p.y;
                z += p.z;
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return new Vector3d(x / count, y / count, z / count);
    }

    public static void spawnDrawnGlyph(ComponentAccessor<EntityStore> buffer, Glyph glyph,
            HexcodeSessionComponent session, Vector3d worldPos, Rotation3f rotation,
            Ref<EntityStore> playerRef) {

        Ref<EntityStore> anchorRef = session.getAnchorNodeRef();
        if (anchorRef == null || !anchorRef.isValid()) {
            HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                    .dispatch(CraftingEvent.builder(CraftingEvent.Reason.ERROR_INVALID_HEX, playerRef)
                            .pedestalLocation(session.getPedestalLocation())
                            .message("Pedestal has no active slot to attach this glyph to.")
                            .build());
            return;
        }
        NodeComponent nodeComp = buffer.getComponent(anchorRef, NodeComponent.getComponentType());
        if (nodeComp == null) {
            return;
        }
        Ref<EntityStore> hexRef = nodeComp.getParentEntity();
        if (hexRef == null || !hexRef.isValid()) {
            return;
        }
        HexComponent hexComp = buffer.getComponent(hexRef, HexComponent.getComponentType());
        if (hexComp == null) {
            return;
        }

        GlyphComponent glyphComponent = new GlyphComponent(glyph);
        glyphComponent.setRotation(rotation);
        glyph.setRotation(rotation);

        TransformComponent hexTransform = buffer.getComponent(hexRef, TransformComponent.getComponentType());
        if (hexTransform != null) {
            Vector3d hexPos = hexTransform.getPosition();
            glyph.setPosition(new Vector3f(
                    (float) (worldPos.x - hexPos.x),
                    (float) (worldPos.y - hexPos.y),
                    (float) (worldPos.z - hexPos.z)));
        }

        Ref<EntityStore> effectRef = GlyphNodeHandler.INSTANCE.spawnNode(buffer, hexRef, worldPos, playerRef,
                glyphComponent, hexRef);

        VfxUtil.sound("SFX_Eye_Void_Attack_Summon", worldPos, buffer);

        hexComp.addChildGlyphRef(glyph.getId(), effectRef);
        hexComp.getHex().put(glyph.getId(), glyph);
    }
}
