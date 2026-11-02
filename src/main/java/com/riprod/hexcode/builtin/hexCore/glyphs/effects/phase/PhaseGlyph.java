package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.LogScopes;

public class PhaseGlyph extends GlyphAsset {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Phase";

    @Nullable
    private Impact crushDamageImpact;
    private String damageCauseId = "Environment";

    public static final BuilderCodec<PhaseGlyph> CODEC = BuilderCodec
            .builder(PhaseGlyph.class, PhaseGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("CrushDamageImpact", Impact.CODEC),
                    (c, v) -> c.crushDamageImpact = v, c -> c.crushDamageImpact)
            .add()
            .append(new KeyedCodec<>("DamageCause", DamageCause.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.damageCauseId = v, c -> c.damageCauseId)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(PhaseGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            LOGGER.atWarning().log("Phase: target required");
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: target required");
            return;
        }

        double duration = HexVarUtil.numberOrDefault(
                glyph.readSlot(PhaseGlyphSlots.DURATION, hexContext),
                getSlot(PhaseGlyphSlots.DURATION).getDefaultValue());
        double intensity = HexVarUtil.numberOrDefault(
                glyph.readSlot(PhaseGlyphSlots.INTENSITY, hexContext),
                getSlot(PhaseGlyphSlots.INTENSITY).getDefaultValue());

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        var rawPos = targets.position(accessor);
        if (rawPos == null) {
            LOGGER.atWarning().log("Phase: target ref unresolved");
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: target ref unresolved");
            return;
        }
        Vector3i pos = new Vector3i(MathUtil.floor(rawPos.x()), MathUtil.floor(rawPos.y()), MathUtil.floor(rawPos.z()));

        Vector3i base = BlockAccess.resolveBase(world, pos);
        if (base != null) {
            pos = base;
        }

        int blockId = BlockAccess.blockId(world, pos);
        if (blockId == BlockType.EMPTY_ID) {
            LOGGER.atWarning().log("Phase: target block is empty");
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: target block is empty");
            return;
        }

        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null) {
            LOGGER.atWarning().log("Phase: missing asset BlockType id=%d", blockId);
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: missing asset BlockType");
            return;
        }

        int quality = getBlockQuality(blockType);
        if (quality > intensity) {
            LOGGER.atWarning().log("Phase: block quality %d exceeds intensity %.1f", quality, intensity);
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: intensity domain error (quality > intensity)");
            return;
        }

        String typeId = blockType.getId();
        int rotationIndex = BlockAccess.rotationIndex(world, pos);

        Ref<EntityStore> caster = hexContext.getRootRef();
        if (!HexProtection.canModifyBlock(world, caster, accessor, pos, BlockAction.BREAK)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        Holder<ChunkStore> blockEntity = BlockAccess.takeBlockEntity(world, pos);

        if (!BlockAccess.clearBlock(world, pos, SetBlockSettings.NO_SEND_PARTICLES)) {
            BlockAccess.putBlockEntity(world, pos, blockType, rotationIndex, blockEntity);
            LOGGER.atWarning().log("Phase: could not clear target block");
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: could not clear target block");
            return;
        }

        List<PhasedBlock> phasedBlocks = new ArrayList<>();
        phasedBlocks.add(new PhasedBlock(pos, typeId, rotationIndex, blockEntity));

        Vector3d blockCenter = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
        PhaseStyle.renderPhaseOut(this, blockCenter, hexContext, accessor);

        glyph.writeOutput(new BlockVar(pos), hexContext);

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, PhaseGlyph.TYPE, blockCenter,
                new PhaseState(glyph.getNextLinks(), crushDamageImpact,
                        damageCauseId));

        holder.addComponent(PhaseComponent.getComponentType(),
                new PhaseComponent(phasedBlocks, (float) duration));

        Ref<EntityStore> phaseRef = accessor.addEntity(holder, AddReason.SPAWN);

        Slot immediate = glyph.getSlot(PhaseGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }

        hexContext.addDependency(phaseRef);

        LOGGER.atFine().log("phase: phased block for %.1f seconds", duration);
    }

    private int getBlockQuality(BlockType blockType) {
        BlockGathering gathering = blockType.getGathering();
        if (gathering == null)
            return 0;
        var breaking = gathering.getBreaking();
        if (breaking == null)
            return 0;
        return breaking.getQuality();
    }
}
