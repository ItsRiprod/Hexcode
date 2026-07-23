package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.IdentifyState.Glow;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.style.IdentifyStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.utils.GlowUtil;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class IdentifyGlyph implements GlyphHandler {

    public static final String ID = "Identify";

    private static final int PLAYER_KIND = Integer.MAX_VALUE;
    private static final int UNKNOWN_KIND = Integer.MIN_VALUE;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(IdentifyConfig.class, IdentifyConfig.CODEC);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar cached = hexContext.getVariable(glyph.getId());
        if (cached != null) return cached;
        HexVar a = glyph.readSlot(IdentifyGlyphSlots.TARGET, hexContext);
        HexVar b = glyph.readSlot(IdentifyGlyphSlots.REFERENCE, hexContext);
        return new NumberVar(compareIdentity(a, b, hexContext));
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (caster == null || !caster.isValid()) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        EntityVar viewerVar = HexVarUtil.resolveEntityVar(
                glyph.readSlot(IdentifyGlyphSlots.REFERENCE, hexContext), hexContext);
        Ref<EntityStore> viewerRef = viewerVar != null ? viewerVar.getRef(accessor) : null;
        PersistentRef viewerPref = viewerVar != null ? viewerVar.getPersistentRef() : null;
        if (viewerRef == null || !viewerRef.isValid() || viewerPref == null
                || accessor.getComponent(viewerRef, PlayerRef.getComponentType()) == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        IdentifyConfig config = getConfig(IdentifyConfig.class, asset);
        if (config == null) config = IdentifyConfig.DEFAULTS;

        float life = hexContext.consumeResource(config.getResourceId(), config.getCap());
        float seconds = life * config.getDurationPerLife();
        if (seconds <= 0f) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        Vector3f color = IdentifyStyle.resolveColor(hexContext, asset, config);
        Glow glow = buildGlow(glyph.readSlot(IdentifyGlyphSlots.TARGET, hexContext), viewerPref, color, hexContext);
        if (glow == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        IdentifyState state = ConstructStateUtil.findState(accessor, caster, ID, IdentifyState.class);
        if (state == null) {
            state = new IdentifyState(config.getEffectId());
            state.add(glow);
            state.extend(seconds);
            HexConstructSpawner.applyWithState(accessor, caster, hexContext, glyph, ID, state);
        } else {
            state.add(glow);
            state.extend(seconds);
        }

        GlowUtil.applyCasterEffect(accessor, caster, state.getEffectId(), state.getRemainingSeconds());
        GlowUtil.sendGlow(accessor, glow);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private Glow buildGlow(HexVar targetSlot, PersistentRef viewer, Vector3f color, HexContext ctx) {
        EntityVar targetVar = HexVarUtil.resolveEntityVar(targetSlot, ctx);
        if (targetVar != null) {
            Ref<EntityStore> ref = targetVar.getRef(ctx.getAccessor());
            PersistentRef pref = targetVar.getPersistentRef();
            if (ref != null && ref.isValid() && pref != null) {
                return new Glow(viewer, pref, null, GlowUtil.nextVolumeId(), color);
            }
        }
        BlockVar blockVar = HexVarUtil.resolveBlockVar(targetSlot, ctx);
        if (blockVar != null && blockVar.getValue() != null) {
            return new Glow(viewer, null, blockVar.getValue(), GlowUtil.nextVolumeId(), color);
        }
        return null;
    }

    private int compareIdentity(HexVar a, HexVar b, HexContext ctx) {
        if (a == null && b == null) return 0;
        if (a == null || b == null) return -1;
        CommandBuffer<EntityStore> accessor = ctx.getAccessor();
        HexVar av = a instanceof PositionVar ? a.toBlockVar(accessor) : a;
        HexVar bv = b instanceof PositionVar ? b.toBlockVar(accessor) : b;
        if (av == null || bv == null || av.getClass() != bv.getClass()) return -1;
        if (av instanceof BlockVar ab) return compareBlocks(ab, (BlockVar) bv, accessor);
        if (av instanceof EntityVar ae) return compareEntities(ae, (EntityVar) bv, accessor);
        if (av instanceof NumberVar || av instanceof RotationVar || av instanceof ColorVar) {
            return av.equalTo(bv) ? 0 : 1;
        }
        return 1;
    }

    private int compareBlocks(BlockVar a, BlockVar b, CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        Integer idA = blockId(world, a.getValue());
        Integer idB = blockId(world, b.getValue());
        boolean airA = idA == null || idA == BlockType.EMPTY_ID;
        boolean airB = idB == null || idB == BlockType.EMPTY_ID;
        if (airA && airB) return 0;
        if (airA != airB) return -1;
        return idA.intValue() == idB.intValue() ? 0 : 1;
    }

    private Integer blockId(World world, Vector3i pos) {
        if (pos == null) return null;
        try {
            return world.getBlock(pos.x, pos.y, pos.z);
        } catch (Exception e) {
            return null;
        }
    }

    private int compareEntities(EntityVar a, EntityVar b, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> refA = a.getRef(accessor);
        Ref<EntityStore> refB = b.getRef(accessor);
        if (refA == null || !refA.isValid() || refB == null || !refB.isValid()) return -1;
        return entityKind(refA, accessor) == entityKind(refB, accessor) ? 0 : 1;
    }

    private int entityKind(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        if (accessor.getComponent(ref, Player.getComponentType()) != null) return PLAYER_KIND;
        NPCEntity npc = accessor.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null) return npc.getNPCTypeIndex();
        return UNKNOWN_KIND;
    }
}
