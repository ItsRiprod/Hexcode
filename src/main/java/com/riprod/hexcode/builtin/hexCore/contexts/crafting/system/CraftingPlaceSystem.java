package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;

import com.riprod.hexcode.api.dispatch.GlyphPlaceEvent;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.utils.CraftingGlyphSpawner;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.utils.GlyphMath;
import com.riprod.hexcode.utils.LogScopes;

public class CraftingPlaceSystem extends EntityEventSystem<EntityStore, GlyphPlaceEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CRAFT);

    private static final float FALLBACK_SPAWN_DISTANCE = 4.0f;

    public CraftingPlaceSystem() {
        super(GlyphPlaceEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull GlyphPlaceEvent event) {
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        if (pedestal == null) {
            return;
        }
        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
        if (session == null) {
            return;
        }

        Glyph glyph = event.getGlyph();
        ObeliskDispatcher.dispatchGlyphDrawn(buffer, pedestal, player, glyph);
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
                .dispatch(new GlyphDrawnEvent(player, glyph,
                        event.getStructure() != null ? event.getStructure().getShapes() : List.of(),
                        event.getAsset()));

        HeadRotation headRotation = chunk.getComponent(index, HeadRotation.getComponentType());
        if (headRotation == null) {
            LOGGER.atFine().log("cannot spawn drawn hex: missing head rotation");
            return;
        }

        Vector3d spawnPos = event.getStructure() != null
                ? CraftingGlyphSpawner.calculateDrawCenter(event.getStructure().getShapes())
                : null;
        if (spawnPos == null) {
            Vector3d eyePos = PlayerUtils.getPlayerEyePosition(buffer, player);
            spawnPos = GlyphMath.sphericalToCartesian(eyePos, headRotation.getRotation().y,
                    headRotation.getRotation().x, FALLBACK_SPAWN_DISTANCE);
        }

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(session.getPedestalLocation());
        double maxRadius = pedestal.getMaxRadius();
        if (spawnPos.distanceSquared(anchorPos) > maxRadius * maxRadius) {
            HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                    .dispatch(CraftingEvent.builder(CraftingEvent.Reason.DENIED_OUT_OF_RANGE, player)
                            .pedestal(pedestal)
                            .message("Glyph drawn outside the pedestal's range.")
                            .build());
            return;
        }

        CraftingGlyphSpawner.spawnDrawnGlyph(buffer, glyph, session, spawnPos,
                headRotation.getRotation(), player);
    }
}
