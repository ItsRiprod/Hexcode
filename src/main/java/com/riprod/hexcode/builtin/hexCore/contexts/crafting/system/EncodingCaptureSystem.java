package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.joml.Vector3i;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncodingDisplay;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionSessionState;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.hexes.component.EncodingStroke;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;

public class EncodingCaptureSystem extends EntityEventSystem<EntityStore, ShapeDrawnEvent> {

    public EncodingCaptureSystem() {
        super(ShapeDrawnEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency<>(Order.BEFORE, CraftingShapeDrawnSystem.class));
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ShapeDrawnEvent event) {
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        HexcodeSessionComponent session = pedestal != null
                ? SessionUtils.resolveSession(pedestal, buffer)
                : null;
        if (session == null) {
            return;
        }
        if (!(session.peekObeliskState(EncryptionObelisk.HANDLER_ID)
                instanceof EncryptionSessionState state) || !state.isCaptureArmed()) {
            return;
        }

        event.setCancelled(true);
        List<DrawnShapeComponent> drawn = event.getStructure().getShapes();
        PlayerRef pr = buffer.getComponent(player, PlayerRef.getComponentType());

        Ref<EntityStore> containerRef = session.getActiveContainerRef();
        HexComponent hexComp = containerRef != null && containerRef.isValid()
                ? buffer.getComponent(containerRef, HexComponent.getComponentType())
                : null;
        Hex hex = hexComp != null ? hexComp.getHex() : null;
        if (hex == null) {
            return;
        }

        List<EncodingStroke> strokes = new ArrayList<>(drawn.size());
        for (DrawnShapeComponent shape : drawn) {
            strokes.add(new EncodingStroke(shape.getShapeId(), shape.getRelativeSize()));
        }
        hex.setEncoding(strokes);
        state.setCaptureArmed(false);

        Vector3i obeliskPos = EncryptionObelisk.boundPosition(pedestal, buffer);
        if (obeliskPos != null) {
            EncodingDisplay.refresh(buffer, state, obeliskPos, strokes);
        }
        if (pr != null) {
            NotificationUtil.sendNotification(pr.getPacketHandler(),
                    Message.translation("hexcode.components.encode.set"),
                    Message.translation("hexcode.components.encode.setDesc"));
        }
    }
}
