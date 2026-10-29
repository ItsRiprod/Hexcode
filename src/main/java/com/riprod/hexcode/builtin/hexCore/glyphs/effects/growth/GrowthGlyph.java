package com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth.style.GrowthStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.LogScopes;

public class GrowthGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Growth";

    private double minAmount = 1.0;
    private double maxAmount = 20.0;
    private double stagesReferenceAmount = 5.0;
    private int bonemealRadius = 2;
    private float bonemealChance = 0.35f;
    private int attemptsFloor = 3;
    private double attemptsPerAmount = 1.5;
    private String growthEffectId = "Hexcode_Growth";
    private String[] vegetationBlocks = {
            "Plant_Grass_Lush_Short",
            "Plant_Grass_Lush",
            "Plant_Grass_Lush_Tall",
            "Plant_Flower_Common_Lime",
            "Plant_Fern",
            "Plant_Flower_Common_Blue",
            "Plant_Flower_Common_Pink"
    };
    private String[] grassDirtPrefixes = { "Soil_Grass", "Soil_Dirt" };

    public static final BuilderCodec<GrowthGlyph> CODEC = BuilderCodec
            .builder(GrowthGlyph.class, GrowthGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("MinAmount", Codec.DOUBLE, true),
                    (c, v) -> c.minAmount = v, c -> c.minAmount)
            .add()
            .append(new KeyedCodec<>("MaxAmount", Codec.DOUBLE, true),
                    (c, v) -> c.maxAmount = v, c -> c.maxAmount)
            .add()
            .append(new KeyedCodec<>("StagesReferenceAmount", Codec.DOUBLE, true),
                    (c, v) -> c.stagesReferenceAmount = v, c -> c.stagesReferenceAmount)
            .add()
            .append(new KeyedCodec<>("BonemealRadius", Codec.INTEGER, true),
                    (c, v) -> c.bonemealRadius = v, c -> c.bonemealRadius)
            .add()
            .append(new KeyedCodec<>("BonemealChance", Codec.FLOAT, true),
                    (c, v) -> c.bonemealChance = v, c -> c.bonemealChance)
            .add()
            .append(new KeyedCodec<>("AttemptsFloor", Codec.INTEGER, true),
                    (c, v) -> c.attemptsFloor = v, c -> c.attemptsFloor)
            .add()
            .append(new KeyedCodec<>("AttemptsPerAmount", Codec.DOUBLE, true),
                    (c, v) -> c.attemptsPerAmount = v, c -> c.attemptsPerAmount)
            .add()
            .append(new KeyedCodec<>("GrowthEffect", Codec.STRING, true),
                    (c, v) -> c.growthEffectId = v, c -> c.growthEffectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("VegetationBlocks", Codec.STRING_ARRAY, true),
                    (c, v) -> c.vegetationBlocks = v, c -> c.vegetationBlocks)
            .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getArrayValidator().late())
            .add()
            .append(new KeyedCodec<>("GrassDirtPrefixes", Codec.STRING_ARRAY, true),
                    (c, v) -> c.grassDirtPrefixes = v, c -> c.grassDirtPrefixes)
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(GrowthGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double amount = Math.max(minAmount, Math.min(maxAmount,
                HexVarUtil.numberOrSlotDefault(
                        glyph.readSlot(GrowthGlyphSlots.AMOUNT, hexContext),
                        getSlot(GrowthGlyphSlots.AMOUNT))));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        if (targets instanceof EntityVar entityVar) {

            applyToEntity(entityVar, amount, glyph, hexContext, accessor);
            return;
        }

        if (targets instanceof BlockVar blockVar) {
            applyToBlock(blockVar, amount, hexContext, accessor);

        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void applyToEntity(EntityVar entityVar, double amount,
            Glyph glyph, HexContext hexContext,
            CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        String effectId = growthEffectId;
        EntityEffect growthEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (growthEffect == null) {
            LOGGER.atWarning().log("growth: %s effect asset not found", effectId);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GrowthGlyphSlots.DURATION, hexContext),
                getSlot(GrowthGlyphSlots.DURATION));
        float durationSeconds = (float) duration;

        HexConstructSpawner.abortExisting(accessor, ref, GrowthGlyph.TYPE);

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.addEffect(ref, growthEffect, durationSeconds,
                    OverlapBehavior.OVERWRITE, accessor);
        }

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            GrowthStyle.renderEntityHit(this, tc.getPosition(), hexContext, accessor);
        }

        var state = new GrowthState(durationSeconds, effectId, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(
                accessor, ref, hexContext, glyph, GrowthGlyph.TYPE, state);

        Slot immediate = glyph.getSlot(GrowthGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            immediateCtx.setDefaultVariable(entityVar);
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }

        LOGGER.atFine().log("growth: applied regen buff for %.1fs to entity", durationSeconds);
    }

    private void applyToBlock(BlockVar blockVar, double amount,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Vector3i pos = blockVar.getValue();
        if (pos == null)
            return;

        World world = accessor.getExternalData().getWorld();
        BlockType blockType = BlockAccess.blockType(world, pos);
        if (blockType == null)
            return;

        if (tryAdvanceGrowth(world, pos, blockType, amount, hexContext, accessor)) {
            return;
        }

        if (isGrassDirtBlock(blockType)) {
            applyBonemeal(world, pos, amount, hexContext, accessor);
        }
    }

    private boolean tryAdvanceGrowth(World world, Vector3i pos, BlockType blockType,
            double amount, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        FarmingData farmingConfig = blockType.getFarming();
        if (farmingConfig == null || farmingConfig.getStages() == null)
            return false;

        Ref<ChunkStore> sectionRef = BlockAccess.sectionRef(world, pos);
        if (sectionRef == null)
            return false;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        BlockSection blockSection = chunkStore.getComponent(sectionRef, BlockSection.getComponentType());
        BlockComponentSection blockComponentSection = chunkStore.getComponent(
                sectionRef, BlockComponentSection.getComponentType());
        if (blockSection == null || blockComponentSection == null)
            return false;

        int blockIndex = ChunkUtil.indexBlock(pos.x, pos.y, pos.z);
        Ref<ChunkStore> blockRef = blockComponentSection.getBlockReference(blockIndex);
        if ((blockRef == null || !blockRef.isValid())
                && BlockEntity.declaresComponent(blockType, FarmingBlock.getComponentType())) {
            blockRef = BlockEntity.ensureBlockEntity(chunkStore, sectionRef, blockComponentSection,
                    pos.x, pos.y, pos.z, blockType, blockSection.getFiller(blockIndex));
        }
        if (blockRef == null || !blockRef.isValid())
            return false;

        FarmingBlock farmingBlock = chunkStore.getComponent(blockRef, FarmingBlock.getComponentType());
        if (farmingBlock == null)
            return false;

        String stageSetName = farmingBlock.getCurrentStageSet();
        FarmingStageData[] stages = farmingConfig.getStages().get(stageSetName);
        if (stages == null || stages.length == 0)
            return false;

        int currentStage = (int) farmingBlock.getGrowthProgress();
        if (currentStage >= stages.length)
            currentStage = stages.length - 1;

        int stagesToAdvance = Math.max(1, (int) (amount / stagesReferenceAmount));
        int newStage = Math.min(currentStage + stagesToAdvance, stages.length - 1);

        if (newStage <= currentStage)
            return true;

        WorldTimeResource worldTimeResource = world.getEntityStore().getStore()
                .getResource(WorldTimeResource.getResourceType());
        Instant now = worldTimeResource.getGameTime();

        FarmingStageData previousStage = (currentStage >= 0 && currentStage < stages.length)
                ? stages[currentStage]
                : null;

        farmingBlock.setGrowthProgress(newStage);
        farmingBlock.setExecutions(0);
        farmingBlock.setGeneration(farmingBlock.getGeneration() + 1);
        farmingBlock.setLastTickGameTime(now);
        blockComponentSection.markBlockNeedsSaving(blockIndex);

        blockSection.scheduleTick(blockIndex, now);
        stages[newStage].apply(chunkStore, sectionRef, blockRef,
                ChunkUtil.localCoordinate(pos.x), ChunkUtil.localCoordinate(pos.y),
                ChunkUtil.localCoordinate(pos.z), previousStage);
        blockSection.setTicking(pos.x, pos.y, pos.z, true);

        Vector3d blockCenter = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
        GrowthStyle.renderBlockHit(this, blockCenter, hexContext, accessor);

        LOGGER.atFine().log("growth: advanced crop at %s from stage %d to %d", pos, currentStage, newStage);
        return true;
    }

    private boolean isGrassDirtBlock(BlockType blockType) {
        String id = blockType.getId();
        if (id == null)
            return false;
        for (String prefix : grassDirtPrefixes) {
            if (id.startsWith(prefix))
                return true;
        }
        return false;
    }

    private void applyBonemeal(World world, Vector3i pos, double amount,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int attempts = Math.max(attemptsFloor, (int) (amount * attemptsPerAmount));

        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        boolean blocked = false;
        BlockAccess.Cursor cursor = new BlockAccess.Cursor(world);

        var base = new Vector3i();
        var block = new Vector3i();
        var blockPos = new Vector3d(0.5);

        for (int a = 0; a < attempts; a++) {
            int dx = rng.nextInt(-bonemealRadius, bonemealRadius + 1);
            int dz = rng.nextInt(-bonemealRadius, bonemealRadius + 1);
            base.set(pos).add(dx, 0, dz);
            block.set(pos).add(dx, 1, dz);

            if (cursor.blockId(base) == BlockType.EMPTY_ID)
                continue;

            if (cursor.blockId(block) != BlockType.EMPTY_ID)
                continue;

            if (rng.nextFloat() > bonemealChance)
                continue;

            if (!HexProtection.canModifyBlock(world, caster, accessor,
                    block, BlockAction.PLACE)) {
                blocked = true;
                continue;
            }

            String vegetation = vegetationBlocks[rng.nextInt(vegetationBlocks.length)];
            BlockType vegetationType = BlockType.getAssetMap().getAsset(vegetation);
            if (vegetationType == null)
                continue;

            var vegetationBlockSection = cursor.section(block);
            if (vegetationBlockSection == null)
                continue;

            if (!BlockOperations.testPlaceBlock(cursor.chunkStore().getStore(), vegetationBlockSection,
                    block.x, block.y, block.z, vegetationType, RotationTuple.NONE_INDEX)) {
                continue;
            }

            BlockAccess.setBlock(cursor, block, vegetationType,
                    RotationTuple.NONE_INDEX, SetBlockSettings.PERFORM_BLOCK_UPDATE);

            var effectPos = blockPos.set(0.5).add(block.x, block.y, block.z);
            GrowthStyle.renderBlockHit(this, effectPos, hexContext, accessor);
        }

        if (blocked) {
            HexProtection.notifyBlocked(caster, accessor, getId());
        }

        LOGGER.atFine().log("growth: applied bonemeal around %s", pos);
    }
}
