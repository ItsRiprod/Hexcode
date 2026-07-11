package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;

public class PhaseConstructHandler implements ConstructHandler<PhaseState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<PhaseState> status, ConstructTickContext ctx) {
        PhaseComponent phase = ctx.getChunk().getComponent(
                ctx.getIndex(), PhaseComponent.getComponentType());
        if (phase == null)
            return true;

        if (phase.decrementDuration(dt)) {
            return true;
        }

        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<PhaseState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        PhaseState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("phase: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<PhaseState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("phase: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<PhaseState> status) {
        PhaseState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<PhaseState> status, List<String> ids) {
        PhaseState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<PhaseState> status, ConstructTickContext ctx) {
        PhaseComponent phase = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), PhaseComponent.getComponentType());
        if (phase != null) {
            World world = ctx.getBuffer().getExternalData().getWorld();

            for (PhasedBlock block : phase.getPhasedBlocks()) {
                Vector3i pos = block.getPosition();
                Vector3d blockCenter = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);

                applyCrushDamage(pos, blockCenter, status, ctx.getBuffer());

                int blockId = BlockType.getAssetMap().getIndex(block.getBlockTypeId());
                BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z))
                        .setBlock(pos.x, pos.y, pos.z, blockId, blockType, block.getRotationIndex(), 0, 0);

                PhaseStyle.renderPhaseIn(blockCenter, status.getHexContext(), ctx.getBuffer());
            }

            LOGGER.atInfo().log("phase: restored %d blocks", phase.getPhasedBlocks().size());
        }

        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }

    private void applyCrushDamage(Vector3i pos, Vector3d blockCenter,
            HexStatus<PhaseState> status, CommandBuffer<EntityStore> buffer) {
        Vector3d min = new Vector3d(pos.x, pos.y, pos.z);
        Vector3d max = new Vector3d(pos.x + 1.0, pos.y + 1.0, pos.z + 1.0);
        List<Ref<EntityStore>> entities = new ObjectArrayList<>(TargetUtil.getAllEntitiesInBox(min, max, buffer));

        PhaseState state = status.getState();
        int damageCauseIndex = state != null
                ? DamageCause.getAssetMap().getIndex(state.getDamageCauseId())
                : -1;

        for (Ref<EntityStore> ref : entities) {
            if (ref == null || !ref.isValid())
                continue;

            TransformComponent tc = buffer.getComponent(ref, TransformComponent.getComponentType());
            if (tc == null)
                continue;

            if (damageCauseIndex >= 0) {
                DamageCause cause = DamageCause.getAssetMap().getAsset(damageCauseIndex);
                if (cause != null) {
                    Damage damage = new Damage(
                            new Damage.EnvironmentSource("hex_phase"), cause, state.getCrushDamage());
                    DamageSystems.executeDamage(ref, buffer, damage);
                }
            }

            PhaseStyle.renderCrush(blockCenter, status.getHexContext(), buffer);
        }
    }
}
