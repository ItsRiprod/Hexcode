package com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode.style.ErodeStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.utils.BlockUtils;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class ErodeGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Erode";

    private boolean impactEntities = true;
    private int minTier = 1;
    private int maxTier = 6;
    private float blockDamageScale = 0.05f;
    private String effectId = "Hexcode_Erode";
    @Nullable 
    protected ExplosionConfig explosionConfig;

    public static final BuilderCodec<ErodeGlyph> CODEC = BuilderCodec
            .builder(ErodeGlyph.class, ErodeGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("ImpactEntities", Codec.BOOLEAN, true),
                    (c, v) -> c.impactEntities = v, c -> c.impactEntities)
            .add()
            .append(new KeyedCodec<>("MinTier", Codec.INTEGER, true),
                    (c, v) -> c.minTier = v, c -> c.minTier)
            .add()
            .append(new KeyedCodec<>("MaxTier", Codec.INTEGER, true),
                    (c, v) -> c.maxTier = v, c -> c.maxTier)
            .add()
            .append(new KeyedCodec<>("BlockDamageScale", Codec.FLOAT, true),
                    (c, v) -> c.blockDamageScale = v, c -> c.blockDamageScale)
            .add()
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .appendInherited(
                    new KeyedCodec<>("ExplosionConfig", ExplosionConfig.CODEC),
                    (blockType, s) -> blockType.explosionConfig = s,
                    blockType -> blockType.explosionConfig,
                    (blockType, parent) -> blockType.explosionConfig = parent.explosionConfig)
            .documentation("The explosion config associated with the block damage")
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(ErodeGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Erode: target required");
            return;
        }

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ErodeGlyphSlots.DURATION, hexContext),
                getSlot(ErodeGlyphSlots.DURATION));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        if (targets instanceof EntityVar entityVar) {
            // sustained entity vulnerability defers continuation to the construct's onEnd
            if (impactEntities
                    && applyToEntities(glyph, entityVar, (float) duration, hexContext, accessor)) {
                Slot immediate = glyph.getSlot(ErodeGlyphSlots.IMMEDIATE);
                if (immediate != null && immediate.getLinks().length > 0) {
                    HexContext immediateCtx = hexContext.branch();
                    immediateCtx.setDefaultVariable(entityVar);
                    HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
                }
                return;
            }
        } else {
            var pos = Vector3dUtil.toVector3i(targets.position(accessor));
            if (pos != null) {
                double amount = HexVarUtil.numberOrSlotDefault(
                        glyph.readSlot(ErodeGlyphSlots.AMOUNT, hexContext),
                        getSlot(ErodeGlyphSlots.AMOUNT));
                applyToBlocks(pos, amount, hexContext, accessor);
            }
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private boolean applyToEntities(Glyph glyph, EntityVar entityVar, float durationSeconds,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return false;

        World world = accessor.getExternalData().getWorld();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(world, caster, accessor, ref)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            return false;
        }

        var erodeEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (erodeEffect == null) {
            LOGGER.atWarning().log("erode: %s effect asset not found", effectId);
            return false;
        }

        HexConstructSpawner.abortExisting(accessor, ref, ErodeGlyph.TYPE);

        VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                OverlapBehavior.OVERWRITE);

        var state = new ErodeState(durationSeconds, effectId, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(
                accessor, ref, hexContext, glyph, ErodeGlyph.TYPE, state);

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            ErodeStyle.renderEntityHit(this, tc.getPosition(), hexContext, accessor);
        }

        LOGGER.atFine().log("erode: applied vulnerability effect for %.1fs to entity", durationSeconds);
        return true;
    }

    private void applyToBlocks(Vector3i pos, double amount,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {

        if (pos == null)
            return;

        World world = accessor.getExternalData().getWorld();

        var blockType = BlockAccess.blockType(world, pos);

        if (!HexProtection.isBlockActionAllowed(world, BlockAction.BREAK, blockType)) {
            HexProtection.notifyBlocked(hexContext.getCasterRef(accessor), accessor, getId());
            return;
        }

        if (pos.y < ChunkUtil.MIN_Y)
            return;

        ChunkStore chunkStore = world.getChunkStore();
        var chunkSection = chunkStore.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (chunkSection == null || !chunkSection.isValid())
            return;

        float damageScale = (float) (amount * blockDamageScale);

        var cursor = new BlockAccess.Cursor(world);
        cursor.set(pos);
        BlockUtils.damageBlock(cursor, hexContext.getCasterRef(accessor), accessor, damageScale);
    }
}
