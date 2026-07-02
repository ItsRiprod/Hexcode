package com.riprod.hexcode.core.state.crafting.entity;

import java.util.List;
import java.util.Map;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;

public class AnchorEntity {

    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();
    private static final float GLYPH_DISPLAY_DISTANCE = 1.0f;
    private static final float PEDESTAL_GLYPH_PITCH = (float) (-Math.PI / 2);
    private static final Box PREVIEW_BOUNDING_BOX = new Box(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25);

    private static void safeRemoveEntity(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) {
            return;
        }
        buffer.tryRemoveComponent(ref, MountedComponent.getComponentType());
        buffer.tryRemoveEntity(ref, RemoveReason.REMOVE);
    }

    public static void DespawnHexPreviews(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session) {
        List<Ref<EntityStore>> refs = session.getHexPreviewRefs();
        if (refs == null || refs.isEmpty()) {
            return;
        }

        for (int i = 0; i < refs.size(); i++) {
            Ref<EntityStore> hexRef = refs.get(i);
            if (hexRef == null || !hexRef.isValid()) {
                continue;
            }

            try {
                HexComponent hexComp = buffer.getComponent(hexRef, HexComponent.getComponentType());
                if (hexComp != null) {
                    Map<String, Ref<EntityStore>> childRefs = hexComp.getChildGlyphRefs();
                    if (childRefs != null) {
                        for (Ref<EntityStore> glyphRef : childRefs.values()) {
                            if (glyphRef == null || !glyphRef.isValid()) continue;
                            SlotNodeHandler.INSTANCE.despawnSlotsForGlyph(buffer, glyphRef);
                            safeRemoveEntity(buffer, glyphRef);
                        }
                    }
                }

                if (hexRef.isValid()) {
                    safeRemoveEntity(buffer, hexRef);
                }
            } catch (Exception e) {
                logger.atWarning().log("pedestal: failed to despawn hex preview %d: %s", i, e.getMessage());
            }
        }

        session.clearHexPreviewRefs();
    }
}
