package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

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

import com.riprod.hexcode.api.dispatch.GlyphCommitEvent;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.utils.CraftingGlyphSpawner;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphResolver;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.utils.LogScopes;

public class CraftingShapeDrawnSystem extends EntityEventSystem<EntityStore, ShapeDrawnEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CRAFT);

    public CraftingShapeDrawnSystem() {
        super(ShapeDrawnEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ShapeDrawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        if (pedestal == null) {
            return;
        }
        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
        if (session == null) {
            return;
        }

        ShapeStructure structure = event.getStructure();
        GlyphAsset matched = GlyphResolver.resolve(structure);
        if (matched == null) {
            LOGGER.atFine().log("no matching glyph found for drawn shape");
            return;
        }

        Glyph glyph = new Glyph(matched, structure.getVolatility(), structure.getEfficiency());
        GlyphCommitEvent commit = new GlyphCommitEvent(player, glyph, matched, CraftingState.CONTEXT_ID);
        buffer.invoke(player, commit);
        if (commit.isCancelled()) {
            return;
        }

        ObeliskDispatcher.dispatchGlyphDrawn(buffer, pedestal, player, glyph);
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
                .dispatch(new GlyphDrawnEvent(player, glyph, structure.getShapes(), matched));

        HeadRotation headRotation = chunk.getComponent(index, HeadRotation.getComponentType());
        Vector3d spawnPos = CraftingGlyphSpawner.calculateDrawCenter(structure.getShapes());
        if (spawnPos == null || headRotation == null) {
            LOGGER.atFine().log("cannot spawn drawn hex: missing draw position");
            return;
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
