package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import java.util.ArrayList;
import java.util.List;


import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area.style.AreaStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class AreaGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    
    @Override
    public String getId() { return ID; };

    public static final String ID = "Area";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(AreaConfig.class, AreaConfig.CODEC);
    }

    private static final float PASSIVE_FLOOR = 0.1f;

    private boolean isPassive(Glyph glyph) {
        return !hasLinks(glyph, AreaGlyphSlots.BLOCKS) && !hasLinks(glyph, AreaGlyphSlots.ENTITIES);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR
                : GlyphHandler.super.getVolatilityCost(glyph, hexContext, asset);
    }


    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        AreaConfig config = getConfig(AreaConfig.class, asset);
        if (config == null) config = AreaConfig.DEFAULTS;

        HexVar centerVar = glyph.readSlot(AreaGlyphSlots.CENTER, hexContext);
        double radius = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(AreaGlyphSlots.RADIUS, hexContext), asset.getSlot(AreaGlyphSlots.RADIUS));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Vector3d center = HexVarUtil.position(centerVar, accessor);

        if (center == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Center Variable is not a valid position");
            return;
        }

        AreaStyle.render(center, radius, hexContext, accessor);

        boolean blocksLinked = hasLinks(glyph, AreaGlyphSlots.BLOCKS);
        boolean entitiesLinked = hasLinks(glyph, AreaGlyphSlots.ENTITIES);
        List<Ref<EntityStore>> particleRecipients = blocksLinked || entitiesLinked
                ? VfxUtil.collectParticleRecipients(center, radius + config.getParticleMargin(), accessor)
                : null;

        if (blocksLinked) {
            List<Vector3i> blocks = gatherBlocks(center, radius, accessor);
            for (Vector3i pos : blocks) {
                AreaStyle.renderHit(new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
                        hexContext, accessor, particleRecipients);
                HexContext copy = hexContext.branch();
                copy.enterLocalScope();
                glyph.writeOutput(new BlockVar(pos), copy);
                HexExecuter.continueFromSlot(glyph, AreaGlyphSlots.BLOCKS, copy);
            }
        }

        if (entitiesLinked) {
            List<PersistentRef> entities = gatherEntities(center, radius, hexContext);
            for (PersistentRef ref : entities) {
                Ref<EntityStore> entRef = ref.getEntity(accessor);
                if (entRef != null && entRef.isValid()) {
                    TransformComponent t = accessor.getComponent(entRef, TransformComponent.getComponentType());
                    if (t != null) {
                        AreaStyle.renderHit(t.getPosition(), hexContext, accessor, particleRecipients);
                    }
                }
                HexContext copy = hexContext.branch();
                copy.enterLocalScope();
                glyph.writeOutput(new EntityVar(ref), copy);
                HexExecuter.continueFromSlot(glyph, AreaGlyphSlots.ENTITIES, copy);
            }
        }
    }

    private static boolean hasLinks(Glyph glyph, String slotKey) {
        Slot s = glyph.getSlot(slotKey);
        return s != null && s.getLinks().length > 0;
    }

    private List<PersistentRef> gatherEntities(Vector3d center, double radius, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        List<PersistentRef> gathered = new ArrayList<>();

        List<Ref<EntityStore>> nearby = TargetUtil.getAllEntitiesInSphere(center, radius, accessor);
        for (Ref<EntityStore> ref : nearby) {
            if (ref == null || !ref.isValid()) continue;

            UUIDComponent uuid = accessor.getComponent(ref, UUIDComponent.getComponentType());
            if (uuid == null) continue;

            gathered.add(EntityVar.createRef(uuid.getUuid(), ref));
        }

        return gathered;
    }

    private List<Vector3i> gatherBlocks(Vector3d center, double radius,
            CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        List<Vector3i> gathered = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        double radiusSq = radius * radius;

        int cx = (int) Math.floor(center.x);
        int cy = (int) Math.floor(center.y);
        int cz = (int) Math.floor(center.z);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) continue;

                    int bx = cx + dx;
                    int by = cy + dy;
                    int bz = cz + dz;

                    int blockId = world.getBlock(bx, by, bz);
                    if (blockId == BlockType.EMPTY_ID) continue;

                    gathered.add(new Vector3i(bx, by, bz));
                }
            }
        }

        return gathered;
    }
}
