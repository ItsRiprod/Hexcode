package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area.style.AreaStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class AreaGlyph implements GlyphHandler {

    public static final String ID = "Area";

    private static final float PASSIVE_FLOOR = 0.1f;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(AreaConfig.class, AreaConfig.CODEC);
    }

    private static List<String> linksOf(Glyph glyph, String slotKey) {
        Slot slot = glyph.getSlot(slotKey);
        String[] links = slot != null ? slot.getLinks() : null;
        return links == null || links.length == 0 ? List.of() : Arrays.asList(links);
    }

    private static boolean isDisplayOnly(Glyph glyph) {
        return linksOf(glyph, AreaGlyphSlots.ENTITIES).isEmpty()
                && linksOf(glyph, AreaGlyphSlots.BLOCKS).isEmpty();
    }

    private static boolean isAbsolute(@Nullable HexVar var) {
        if (var instanceof PositionVar positionVar)
            return positionVar.isAbsolute();
        return !(var instanceof NumberVar);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isDisplayOnly(glyph) ? PASSIVE_FLOOR : glyph.computeBaseCost(asset);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        AreaConfig config = getConfig(AreaConfig.class, asset);
        if (config == null)
            config = AreaConfig.DEFAULTS;

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        Vector3d anchorPos = HexVarUtil.position(
                glyph.readSlot(AreaGlyphSlots.ANCHOR, hexContext), accessor);
        if (anchorPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Anchor is not a valid position");
            return;
        }

        HexVar coordsAVar = glyph.readSlot(AreaGlyphSlots.COORDS_A, hexContext);
        HexVar coordsBVar = glyph.readSlot(AreaGlyphSlots.COORDS_B, hexContext);
        Vector3d coordsA = HexVarUtil.position(coordsAVar, accessor);
        Vector3d coordsB = HexVarUtil.position(coordsBVar, accessor);
        if (coordsA == null || coordsB == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Corner coordinates must be valid positions");
            return;
        }

        Vector3d cornerA = isAbsolute(coordsAVar) ? coordsA : new Vector3d(anchorPos).add(coordsA);
        Vector3d cornerB = isAbsolute(coordsBVar) ? coordsB : new Vector3d(anchorPos).add(coordsB);

        double minAxis = config.getMinAxisSize();
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
                asset != null ? asset.getSlot(AreaGlyphSlots.AREA_SHAPE) : null));

        double blocksPerSecond = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(AreaGlyphSlots.BLOCKS_PER_SECOND, hexContext),
                asset != null ? asset.getSlot(AreaGlyphSlots.BLOCKS_PER_SECOND) : null);
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

        double price = config.getPerBlockPrice()
                * Impact.scale(config.getRatePriceImpact(), blocksPerSecond)
                * glyph.computeDrawQuality();
        if (isDisplayOnly(glyph))
            price *= config.getDisplayPriceMultiplier();

        AreaState state = new AreaState(center, half, shape, blocksPerSecond, totalBlocks,
                (float) price, linksOf(glyph, AreaGlyphSlots.ENTITIES),
                linksOf(glyph, AreaGlyphSlots.BLOCKS));

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, ID, new Vector3d(center), state);

        float alpha = VfxUtil.resolveAlpha(hexContext, asset);
        if (alpha > 0f) {
            DebugComponent debug = new DebugComponent(shape.debugShape(),
                    VfxUtil.resolvePrimaryColor(hexContext, asset), new Vector3d(), 0.1f);
            debug.setOpacity(alpha * 0.15f);
            debug.setIntervalMultiplier(0.01f);
            debug.setFadeMultiplier(2.0f);
            debug.setFlags(DebugUtils.FLAG_FADE | DebugUtils.FLAG_NO_WIREFRAME);
            holder.addComponent(DebugComponent.getComponentType(), debug);
        }

        accessor.addEntity(holder, AddReason.SPAWN);

        AreaStyle.renderSpawn(new Vector3d(center), hexContext, accessor);
    }
}
