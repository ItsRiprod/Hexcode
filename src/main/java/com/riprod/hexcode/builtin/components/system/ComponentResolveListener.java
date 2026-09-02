package com.riprod.hexcode.builtin.components.system;

import java.nio.charset.StandardCharsets;

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
import com.riprod.hexcode.api.dispatch.GlyphResolveEvent;
import com.riprod.hexcode.builtin.components.component.ComponentCacheEntry;
import com.riprod.hexcode.builtin.components.component.ComponentPasteCache;
import com.riprod.hexcode.builtin.components.utils.ComponentScan;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component.ComponentGlyph;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.codec.HexCodecV15;

public class ComponentResolveListener extends EntityEventSystem<EntityStore, GlyphResolveEvent> {

    public ComponentResolveListener() {
        super(GlyphResolveEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return ComponentPasteCache.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull GlyphResolveEvent event) {
        if (event.isResolved()) {
            return;
        }
        ComponentPasteCache cache = chunk.getComponent(index, ComponentPasteCache.getComponentType());
        if (cache == null || cache.getEntries().isEmpty()) {
            return;
        }

        var drawn = event.getStructure().getShapes();
        ComponentCacheEntry best = null;
        float bestScore = 0f;
        for (ComponentCacheEntry entry : cache.getEntries()) {
            float score = GlyphCreationManager.ScoreSequence(drawn, entry.encoding());
            if (score >= GlyphCreationManager.MATCH_THRESHOLD && score > bestScore) {
                bestScore = score;
                best = entry;
            }
        }
        if (best == null) {
            return;
        }

        Ref<EntityStore> player = chunk.getReferenceTo(index);
        if (!ComponentScan.stillHeld(buffer, player, best.sourceRaw())) {
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(ComponentGlyph.ID);
        if (asset == null) {
            return;
        }
        if (best.canonicalPayload().getBytes(StandardCharsets.UTF_8).length > HexCodecV15.MAX_SECTION_BYTES) {
            PlayerRef pr = buffer.getComponent(player, PlayerRef.getComponentType());
            if (pr != null) {
                NotificationUtil.sendNotification(pr.getPacketHandler(),
                        Message.translation("hexcode.components.paste.tooLarge"),
                        Message.translation("hexcode.components.paste.tooLargeDesc"));
            }
            return;
        }
        var glyph = new Glyph(asset, event.getStructure().getVolatility(),
                event.getStructure().getEfficiency());
        glyph.setPayload(best.canonicalPayload());
        event.setResolution(glyph, asset);
    }
}
