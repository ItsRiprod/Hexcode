package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class PhaseGlyph implements GlyphHandler {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Phase";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(PhaseConfig.class, PhaseConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(PhaseGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            LOGGER.atWarning().log("Phase: target required");
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: target required");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(getId());
        if (asset == null) {
            LOGGER.atWarning().log("Phase: missing asset, cannot compute volatility cost");
            return;
        }

        double duration = HexVarUtil.numberOrDefault(
                glyph.readSlot(PhaseGlyphSlots.DURATION, hexContext),
                asset.getSlot(PhaseGlyphSlots.DURATION).getDefaultValue());
        double intensity = HexVarUtil.numberOrDefault(
                glyph.readSlot(PhaseGlyphSlots.INTENSITY, hexContext),
                asset.getSlot(PhaseGlyphSlots.INTENSITY).getDefaultValue());

        PhaseConfig config = getConfig(PhaseConfig.class, asset);
        if (config == null) config = PhaseConfig.DEFAULTS;

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        Vector3i pos = resolveBlockPosition(targets, hexContext);
        if (pos == null) {
            LOGGER.atWarning().log("Phase: target ref unresolved");
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Phase: target ref unresolved");
            return;
        }

        int blockId = world.getBlock(pos.x, pos.y, pos.z);
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
        int rotationIndex = world.getBlockRotationIndex(pos.x, pos.y, pos.z);

        List<PhasedBlock> phasedBlocks = new ArrayList<>();
        phasedBlocks.add(new PhasedBlock(pos, typeId, rotationIndex));

        world.setBlock(pos.x, pos.y, pos.z, "Empty");

        Vector3d blockCenter = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
        PhaseStyle.renderPhaseOut(blockCenter, hexContext, accessor);

        glyph.writeOutput(new BlockVar(pos), hexContext);

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, PhaseGlyph.ID, blockCenter,
                new PhaseState(glyph.getNextLinks(), (float) config.getCrushDamage(),
                        config.getDamageCauseId()));

        holder.addComponent(PhaseComponent.getComponentType(),
                new PhaseComponent(phasedBlocks, (float) duration));

        Ref<EntityStore> phaseRef = accessor.addEntity(holder, AddReason.SPAWN);

        hexContext.getHexRoot().addDependency(hexContext, phaseRef);

        LOGGER.atInfo().log("phase: phased block for %.1f seconds", duration);
    }

    private Vector3i resolveBlockPosition(HexVar targets, HexContext hexContext) {
        BlockVar blockVar = HexVarUtil.resolveBlockVar(targets, hexContext);
        if (blockVar != null)
            return blockVar.getValue();
        PositionVar posVar = HexVarUtil.resolvePositionVar(targets, hexContext);
        if (posVar != null) {
            Vector3d pos = posVar.getValue();
            if (pos != null) {
                return new Vector3i((int) Math.floor(pos.x), (int) Math.floor(pos.y),
                        (int) Math.floor(pos.z));
            }
        }
        return null;
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
