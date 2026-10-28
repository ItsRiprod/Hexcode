package com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.universe.world.World;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap.style.SwapStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.BlockUtils;

import lombok.Getter;

public class SwapGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Swap";

    @Getter
    private String effectId = "Teleportation";

    public static final BuilderCodec<SwapGlyph> CODEC = BuilderCodec
            .builder(SwapGlyph.class, SwapGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double distance = 0.0;
        HexVar varsA = glyph.readSlot(SwapGlyphSlots.A, hexContext);
        if (varsA == null)
            varsA = hexContext.getDefaultVariable();
        HexVar varsB = glyph.readSlot(SwapGlyphSlots.B, hexContext);
        if (varsB == null)
            varsB = hexContext.getDefaultVariable();
        if (varsA != null && varsB != null) {
            Vector3d posA = varsA.position(hexContext.getAccessor());
            Vector3d posB = varsB.position(hexContext.getAccessor());
            if (posA != null && posB != null) {
                double dx = posA.x - posB.x;
                double dy = posA.y - posB.y;
                double dz = posA.z - posB.z;
                distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            }
        }

        var impact = asset == null
                ? null
                : asset.getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, distance);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar varsA = glyph.readSlot(SwapGlyphSlots.A, hexContext);
        if (varsA == null)
            varsA = hexContext.getDefaultVariable();
        HexVar varsB = glyph.readSlot(SwapGlyphSlots.B, hexContext);
        if (varsB == null)
            varsB = hexContext.getDefaultVariable();

        if (varsA == null || varsB == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Both positions are required");
            return;
        }

        World world = hexContext.getAccessor().getExternalData().getWorld();

        Vector3d posA = varsA.position(hexContext.getAccessor());
        Vector3d posB = varsB.position(hexContext.getAccessor());
        if (posA != null && posB != null) {
            SwapStyle.render(this, posA, posB, hexContext, hexContext.getAccessor());
        }
        BlockUtils.swapPair(varsA, varsB, world, hexContext, TYPE);
        SwapStyle.applyEffect(this, varsA, hexContext, hexContext.getAccessor());
        SwapStyle.applyEffect(this, varsB, hexContext, hexContext.getAccessor());

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
