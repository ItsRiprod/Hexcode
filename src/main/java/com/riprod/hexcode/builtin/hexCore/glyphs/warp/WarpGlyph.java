package com.riprod.hexcode.builtin.hexCore.glyphs.warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.warp.style.WarpStyle;
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

public class WarpGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Warp";

    @Override
    public boolean consumeVolatility(Glyph glyph, HexContext hexContext) {
        HexStats tracker = hexContext.getVolatilityTracker();
        if (tracker == null) return true;

        double distance = 0.0;
        HexVar destInput = glyph.readSlot(WarpGlyphSlots.DESTINATION, hexContext);
        var accessor = hexContext.getAccessor();
        Ref<EntityStore> casterRef = hexContext.getCasterRef(accessor);
        if (destInput != null && casterRef != null && casterRef.isValid()) {
            Vector3d destination = HexVarUtil.position(destInput, accessor);
            TransformComponent tc = accessor.getComponent(
                    casterRef, TransformComponent.getComponentType());
            if (destination != null && tc != null) {
                Vector3d origin = tc.getPosition();
                double dx = destination.x - origin.x;
                double dy = destination.y - origin.y;
                double dz = destination.z - origin.z;
                distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            }
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getVolatilityImpact();
        float cost = glyph.computeBaseCost() * Impact.scale(impact, distance);
        return tracker.consumeVolatility(cost) > 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(WarpGlyphSlots.TARGET, hexContext);
        HexVar destInput = glyph.readSlot(WarpGlyphSlots.DESTINATION, hexContext);

        if (destInput == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Destination is required");
            return;
        }

        Vector3d destination = HexVarUtil.position(destInput, hexContext.getAccessor());

        if (destination == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Destination is no longer valid");
            return;
        }

        if (targets == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        World world = hexContext.getAccessor().getExternalData().getWorld();

        Vector3d departurePos = HexVarUtil.position(targets, hexContext.getAccessor());
        BlockUtils.moveToDestination(targets, destination, world, hexContext);
        if (departurePos != null) {
            WarpStyle.render(departurePos, destination, hexContext, hexContext.getAccessor());
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
