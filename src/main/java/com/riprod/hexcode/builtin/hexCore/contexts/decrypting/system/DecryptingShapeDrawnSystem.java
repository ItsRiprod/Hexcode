package com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component.DecryptingState;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionSessionState;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.drawing.utils.DraftFeedback;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;

public class DecryptingShapeDrawnSystem extends EntityEventSystem<EntityStore, ShapeDrawnEvent> {

    public DecryptingShapeDrawnSystem() {
        super(ShapeDrawnEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return DecryptingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ShapeDrawnEvent event) {
        event.setCancelled(true);

        Ref<EntityStore> player = chunk.getReferenceTo(index);
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        HexcodeSessionComponent session = pedestal != null
                ? SessionUtils.resolveSession(pedestal, buffer)
                : null;
        if (session == null) {
            return;
        }

        String slotKey = EncryptionObelisk.soleSlotKey(session);
        if (slotKey == null) {
            return;
        }
        Hex stored = session.getHexAt(slotKey, buffer);
        var encoding = stored != null ? stored.getEncoding() : null;
        if (encoding == null || encoding.isEmpty()) {
            return;
        }

        EncryptionSessionState state = session.obeliskState(EncryptionObelisk.HANDLER_ID,
                EncryptionSessionState::new);
        PlayerRef pr = buffer.getComponent(player, PlayerRef.getComponentType());
        float score = GlyphCreationManager.ScoreSequence(event.getStructure().getShapes(), encoding);
        if (score >= GlyphCreationManager.MATCH_THRESHOLD) {
            state.setUnlocked(true);
            if (pr != null) {
                NotificationUtil.sendNotification(pr.getPacketHandler(),
                        Message.translation("hexcode.components.decrypted.title"),
                        Message.translation("hexcode.components.decrypted.description"));
            }
        } else {
            DraftFeedback.playFailFeedback(buffer, player);
            if (pr != null) {
                NotificationUtil.sendNotification(pr.getPacketHandler(),
                        Message.translation("hexcode.components.encrypted.locked"),
                        Message.translation("hexcode.components.encrypted.wrong"));
            }
        }
    }
}
