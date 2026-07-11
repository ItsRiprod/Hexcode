package com.riprod.hexcode.builtin.hexCore.contexts.selecting.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.riprod.hexcode.builtin.hexCore.nodes.container.ContainerNodeHandler;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.builtin.hexCore.scene.RadialPositionUtil;

public final class SelectingScene {

    private SelectingScene() {
    }

    public static void spawnPreviews(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal, HexcodeSessionComponent session) {

        ImbuementProfileAsset profile = session.getProfile();
        if (profile == null) return;
        Map<String, SlotAsset> slots = profile.resolveSlots(session.getStoredItem());
        if (slots.isEmpty()) return;

        Ref<EntityStore> anchorRef = session.getAnchorRef();
        if (anchorRef == null || !anchorRef.isValid()) {
            return;
        }

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(pedestal.getLocation());
        List<Vector3f> offsets = RadialPositionUtil.calculateOffsets(slots.size(),
                PedestalSystem.PREVIEW_RADIUS, 0, PedestalSystem.HEX_SLOT_OFFSET);
        List<Ref<EntityStore>> spawnedRefs = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, SlotAsset> entry : slots.entrySet()) {
            Vector3f offset = offsets.get(i++);
            String slotKey = entry.getKey();
            SlotAsset slotAsset = entry.getValue();
            Hex hex = session.getHexAt(slotKey, buffer);
            Ref<EntityStore> hexRef = ContainerNodeHandler.INSTANCE.spawnContainer(buffer, hex, anchorRef,
                    anchorPos, offset, playerRef, slotAsset);
            if (hexRef != null) {
                buffer.addComponent(hexRef, SlotComponent.getComponentType(),
                        new SlotComponent(slotKey));
            }
            spawnedRefs.add(hexRef);
        }

        session.setHexPreviewRefs(spawnedRefs);
    }
}
