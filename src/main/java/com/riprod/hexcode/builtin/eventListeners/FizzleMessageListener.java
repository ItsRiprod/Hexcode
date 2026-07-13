package com.riprod.hexcode.builtin.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class FizzleMessageListener implements Consumer<GlyphFizzleEvent> {

    @Override
    public void accept(GlyphFizzleEvent event) {
        HexContext ctx = event.getCtx();
        if (ctx == null || ctx.getAccessor() == null)
            return;
        var root = ctx.getHexRoot();
        Ref<EntityStore> caster = root.getSourceRef();
        if (caster == null || !caster.isValid())
            return;
        PlayerRef pr = ctx.getAccessor().getComponent(caster, PlayerRef.getComponentType());
        if (pr == null)
            return;
        NotificationUtil.sendNotification(pr.getPacketHandler(), resolveTitle(event.getGlyph()),
                Message.raw(resolveDescription(event)));
    }

    private static Message resolveTitle(Glyph glyph) {
        if (glyph == null)
            return Message.raw("Glyph Fizzled!");
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        if (asset != null && asset.getTitle() != null) {
            return Message.translation(asset.getTitle()).insert(" Fizzled!");
        }
        return Message.raw(glyph.getGlyphId() + " Fizzled!");
    }

    private static String resolveDescription(GlyphFizzleEvent reason) {
        return switch (reason.getReason()) {
            case INSUFFICIENT_MANA -> "Not enough mana!";
            case VOLATILITY_DEPLETED -> "Ran out of volatility!";
            case HANDLER_FAILED -> reason.getDetail();
            case NOT_IMPLEMENTED -> "Not Implemented!";
            case MANUALLY_CANCELLED -> "Dispelled by Caster.";
            case GLYPH_DISABLED -> "Glyph is disabled.";
            case ERROR -> "Glyph not found!";
            default -> "The spell fizzled.";
        };
    }
}
