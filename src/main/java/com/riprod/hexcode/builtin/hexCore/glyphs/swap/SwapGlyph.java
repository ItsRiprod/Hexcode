package com.riprod.hexcode.builtin.hexCore.glyphs.swap;

import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.swap.style.SwapStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.BlockUtils;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class SwapGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Swap";

    @Override
    public boolean consumeVolatility(Glyph glyph, HexContext hexContext) {
        HexStats tracker = hexContext.getVolatilityTracker();
        if (tracker == null) return true;

        double distance = 0.0;
        HexVar varsA = glyph.readSlot(SwapGlyphSlots.A, hexContext);
        HexVar varsB = glyph.readSlot(SwapGlyphSlots.B, hexContext);
        if (varsA != null && varsB != null) {
            Vector3d posA = HexVarUtil.position(varsA, hexContext.getAccessor());
            Vector3d posB = HexVarUtil.position(varsB, hexContext.getAccessor());
            if (posA != null && posB != null) {
                double dx = posA.x - posB.x;
                double dy = posA.y - posB.y;
                double dz = posA.z - posB.z;
                distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            }
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getImpact();
        float cost = glyph.computeBaseCost() * Impact.scale(impact, distance);
        return tracker.consumeVolatility(cost) > 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar varsA = glyph.readSlot(SwapGlyphSlots.A, hexContext);
        HexVar varsB = glyph.readSlot(SwapGlyphSlots.B, hexContext);

        if (varsA == null || varsB == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Both positions are required");
            return;
        }

        World world = hexContext.getAccessor().getExternalData().getWorld();

        Vector3d posA = HexVarUtil.position(varsA, hexContext.getAccessor());
        Vector3d posB = HexVarUtil.position(varsB, hexContext.getAccessor());
        if (posA != null && posB != null) {
            SwapStyle.render(posA, posB, hexContext, hexContext.getAccessor());
        }
        BlockUtils.swapPair(varsA, varsB, world, hexContext);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
