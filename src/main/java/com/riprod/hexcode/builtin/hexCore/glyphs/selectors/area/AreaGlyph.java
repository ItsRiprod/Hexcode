package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class AreaGlyph extends GlyphAsset {

    public static final String TYPE = "Area";

    private double minAxisSize = 1.0;
    private double perBlockPrice = 0.002;
    private double displayPriceMultiplier = 0.1;
    @Nullable
    private Impact ratePriceImpact;

    public static final BuilderCodec<AreaGlyph> CODEC = BuilderCodec
            .builder(AreaGlyph.class, AreaGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("MinAxisSize", Codec.DOUBLE, true),
                    (c, v) -> c.minAxisSize = v, c -> c.minAxisSize)
            .add()
            .append(new KeyedCodec<>("PerBlockPrice", Codec.DOUBLE, true),
                    (c, v) -> c.perBlockPrice = v, c -> c.perBlockPrice)
            .add()
            .append(new KeyedCodec<>("DisplayPriceMultiplier", Codec.DOUBLE, true),
                    (c, v) -> c.displayPriceMultiplier = v, c -> c.displayPriceMultiplier)
            .add()
            .append(new KeyedCodec<>("RatePriceImpact", Impact.CODEC, true),
                    (c, v) -> c.ratePriceImpact = v, c -> c.ratePriceImpact)
            .add()
            .build();

    private static List<String> linksOf(Glyph glyph, String slotKey) {
        Slot slot = glyph.getSlot(slotKey);
        String[] links = slot != null ? slot.getLinks() : null;
        return links == null || links.length == 0 ? List.of() : Arrays.asList(links);
    }

    @Override
    public boolean isPassive(Glyph glyph) {
        return !glyph.hasLinks(AreaGlyphSlots.ENTITIES) && !glyph.hasLinks(AreaGlyphSlots.BLOCKS);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR : glyph.computeBaseCost(asset);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        HexVar anchorVar = glyph.readSlot(AreaGlyphSlots.ANCHOR, hexContext);
        Vector3d anchorPos = anchorVar != null ? anchorVar.position(accessor) : null;
        if (anchorPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Anchor is not a valid position");
            return;
        }

        HexVar coordsAVar = glyph.readSlot(AreaGlyphSlots.COORDS_A, hexContext);
        HexVar coordsBVar = glyph.readSlot(AreaGlyphSlots.COORDS_B, hexContext);
        Vector3d coordsA = coordsAVar != null ? coordsAVar.position(accessor) : null;
        Vector3d coordsB = coordsBVar != null ? coordsBVar.position(accessor) : null;
        if (coordsA == null || coordsB == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Corner coordinates must be valid positions");
            return;
        }

        var cornerA = coordsA.add(anchorPos);
        var cornerB = coordsB.add(anchorPos);

        double minAxis = minAxisSize;
        Vector3d center = new Vector3d(
                (cornerA.x + cornerB.x) * 0.5,
                (cornerA.y + cornerB.y) * 0.5,
                (cornerA.z + cornerB.z) * 0.5);
        Vector3d half = new Vector3d(
                Math.max(minAxis, Math.abs(cornerA.x - cornerB.x)) * 0.5,
                Math.max(minAxis, Math.abs(cornerA.y - cornerB.y)) * 0.5,
                Math.max(minAxis, Math.abs(cornerA.z - cornerB.z)) * 0.5);

        AreaShape shape = AreaShape.fromSlotValue(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(AreaGlyphSlots.AREA_SHAPE, hexContext),
                getSlot(AreaGlyphSlots.AREA_SHAPE)));

        double blocksPerSecond = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(AreaGlyphSlots.BLOCKS_PER_SECOND, hexContext),
                getSlot(AreaGlyphSlots.BLOCKS_PER_SECOND));
        if (!(blocksPerSecond > 0)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "BlocksPerSecond must be greater than zero");
            return;
        }

        double totalBlocks = shape.volume(half);
        if (!(totalBlocks > 0)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Area has no volume");
            return;
        }

        double price = perBlockPrice
                * Impact.scale(ratePriceImpact, blocksPerSecond)
                * glyph.computeDrawQuality();
        if (isPassive(glyph))
            price *= displayPriceMultiplier;

        AreaState state = new AreaState(center, half, shape, blocksPerSecond, totalBlocks,
                (float) price, linksOf(glyph, AreaGlyphSlots.ENTITIES),
                linksOf(glyph, AreaGlyphSlots.BLOCKS));

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, TYPE, new Vector3d(center), state);
        if (holder == null) {
            return;
        }

        float alpha = VfxUtil.resolveAlpha(EffectRenderer.resolve(this, hexContext));
        if (alpha > 0f) {
            DebugComponent debug = new DebugComponent(shape.debugShape(),
                    VfxUtil.resolvePrimaryColor(EffectRenderer.resolve(this, hexContext)), new Vector3d(), 0.1f);
            debug.setOpacity(alpha * 0.15f);
            debug.setIntervalMultiplier(0.01f);
            debug.setFadeMultiplier(2.0f);
            debug.setFlags(DebugUtils.FLAG_FADE | DebugUtils.FLAG_NO_WIREFRAME);
            holder.addComponent(DebugComponent.getComponentType(), debug);
        }

        accessor.addEntity(holder, AddReason.SPAWN);

        EffectRenderer.render(EffectSlot.Start, this, hexContext, new Vector3d(center), accessor);
    }
}
