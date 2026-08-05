package com.riprod.hexcode.core.common.memories;

import com.hypixel.hytale.builtin.adventure.memories.memories.npc.NPCMemory;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.Message;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlyphMemory extends NPCMemory {

    @Nonnull
    public static final String ID = "Glyph";

    @Nonnull
    public static final BuilderCodec<GlyphMemory> CODEC = BuilderCodec.builder(GlyphMemory.class, GlyphMemory::new)
        .append(new KeyedCodec<>("GlyphId", Codec.STRING), (memory, s) -> memory.glyphId = s, memory -> memory.glyphId)
        .addValidator(Validators.nonNull())
        .add()
        .append(new KeyedCodec<>("CapturedTimestamp", Codec.LONG),
            (memory, l) -> memory.capturedTimestamp = l, memory -> memory.capturedTimestamp)
        .add()
        .build();

    private String glyphId;
    private long capturedTimestamp;

    private GlyphMemory() {
        super("", "");
    }

    public GlyphMemory(@Nonnull String glyphId) {
        super(glyphId, glyphId);
        this.glyphId = glyphId;
    }

    public void setCapturedTimestamp(long capturedTimestamp) {
        this.capturedTimestamp = capturedTimestamp;
    }

    @Override
    public String getId() {
        return this.glyphId;
    }

    @Nonnull
    @Override
    public String getTitle() {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(this.glyphId);
        if (asset != null && asset.getTitle() != null) {
            return asset.getTitle();
        }
        return this.glyphId;
    }

    @Nonnull
    @Override
    public Message getTooltipText() {
        return glyphDescriptionMessage(false);
    }

    private Message glyphDescriptionMessage(boolean allowVerbose) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(this.glyphId);
        if (asset == null) {
            return Message.raw("");
        }
        String key = allowVerbose && asset.getVerboseDescription() != null
                ? asset.getVerboseDescription() : asset.getDescription();
        return key != null ? Message.translation(key) : Message.raw("");
    }

    @Nonnull
    @Override
    public Message getUndiscoveredTooltipText() {
        return Message.translation("hexcode.memories.glyph.undiscovered.tooltipText");
    }

    @Nullable
    @Override
    public String getIconPath() {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(this.glyphId);
        return asset != null ? asset.getIcon() : null;
    }

    @Override
    public long getCapturedTimestamp() {
        return this.capturedTimestamp;
    }

    @Nonnull
    @Override
    public Message getLocationMessage() {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(this.glyphId);
        List<Message> entries = new ArrayList<>();
        if (asset != null) {
            for (Map.Entry<String, SlotConfig> entry : asset.getSlots().entrySet()) {
                SlotConfig slot = entry.getValue();
                if (slot.getLabel() == null) {
                    continue;
                }
                Message slotDescription = slot.getDescription() != null
                        ? Message.translation(slot.getDescription()) : Message.raw("");
                entries.add(Message.translation("hexcode.memories.glyph.slotEntry")
                        .param("slotName", Message.translation(slot.getLabel()))
                        .param("slotDescription", slotDescription));
            }
        }

        Message slotDefinitions = Message.raw("").insertAll(entries);
        return Message.translation("hexcode.memories.glyph.descriptionBody")
                .param("glyphDescription", glyphDescriptionMessage(true))
                .param("slotDefinitions", slotDefinitions);
    }

    @Nonnull
    public String getGlyphId() {
        return this.glyphId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        GlyphMemory other = (GlyphMemory) o;
        return Objects.equals(this.glyphId, other.glyphId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.glyphId);
    }

    @Nonnull
    @Override
    public String toString() {
        return "GlyphMemory{glyphId='" + this.glyphId + "'}";
    }
}
