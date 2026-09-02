package com.riprod.hexcode.core.common.memories;

import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.builtin.adventure.memories.memories.MemoryProvider;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class GlyphMemoryProvider extends MemoryProvider<GlyphMemory> {

    @Nonnull
    public static final String CATEGORY = "Hexcode";
    public static final double DEFAULT_RADIUS = 0.0;

    public GlyphMemoryProvider() {
        super(GlyphMemory.ID, GlyphMemory.CODEC, DEFAULT_RADIUS);
    }

    @Nonnull
    @Override
    public Map<String, Set<Memory>> getAllMemories() {
        Map<String, Set<Memory>> allMemories = new Object2ObjectOpenHashMap<>();
        Set<Memory> glyphMemories = new HashSet<>();

        for (Map.Entry<String, GlyphAsset> entry : GlyphAsset.getAssetMap().getAssetMap().entrySet()) {
            GlyphAsset asset = entry.getValue();
            // skips Glyph_Template and any untitled helper asset
            if (asset == null || asset.getTitle() == null
                    || asset.getShapes() == null || asset.getShapes().isEmpty()) {
                continue;
            }
            glyphMemories.add(new GlyphMemory(entry.getKey()));
        }

        if (!glyphMemories.isEmpty()) {
            allMemories.put(CATEGORY, glyphMemories);
        }
        return allMemories;
    }
}
