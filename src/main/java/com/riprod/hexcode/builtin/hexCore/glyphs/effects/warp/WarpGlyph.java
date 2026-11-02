package com.riprod.hexcode.builtin.hexCore.glyphs.effects.warp;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.warp.style.WarpStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.BlockUtils;

import lombok.Getter;

public class WarpGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Warp";

    @Getter
    private String effectId = "Teleportation";

    public static final BuilderCodec<WarpGlyph> CODEC = BuilderCodec
            .builder(WarpGlyph.class, WarpGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double distance = 0.0;
        HexVar destInput = glyph.readSlot(WarpGlyphSlots.DESTINATION, hexContext);
        if (destInput == null)
            destInput = hexContext.getDefaultVariable();
        var accessor = hexContext.getAccessor();
        Ref<EntityStore> casterRef = hexContext.getRootRef();
        if (destInput != null && casterRef != null && casterRef.isValid()) {
            Vector3d destination = destInput.position(accessor);
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

        var impact = asset == null
                ? null
                : asset.getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, distance);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(WarpGlyphSlots.TARGET, hexContext);
        if (targets == null)
            targets = hexContext.getDefaultVariable();
        HexVar destInput = glyph.readSlot(WarpGlyphSlots.DESTINATION, hexContext);
        if (destInput == null)
            destInput = hexContext.getDefaultVariable();

        if (destInput == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Destination is required");
            return;
        }

        Vector3d destination = destInput.position(hexContext.getAccessor());

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

        Vector3d departurePos = targets.position(hexContext.getAccessor());
        BlockUtils.moveToDestination(targets, destination, world, hexContext, TYPE);
        WarpStyle.applyEffect(this, targets, hexContext, hexContext.getAccessor());
        if (departurePos != null) {
            WarpStyle.render(this, departurePos, destination, hexContext, hexContext.getAccessor());
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
