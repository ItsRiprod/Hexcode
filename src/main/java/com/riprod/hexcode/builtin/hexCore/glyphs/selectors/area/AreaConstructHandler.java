package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.BlockAccess;

public class AreaConstructHandler implements ConstructHandler<AreaState> {

    @Override
    public boolean onTick(float dt, HexStatus<AreaState> status, ConstructTickContext ctx) {
        AreaState state = status.getState();
        if (state == null)
            return true;

        Vector3d halfPrevious = state.scaledExtents(state.getScale());
        double newBlocks = state.advanceScale(dt);
        Vector3d halfCurrent = state.scaledExtents(state.getScale());

        VolatilityComponent volatility = status.getHexContext().volatility();
        if (volatility != null && newBlocks > 0)
            volatility.consume((float) (newBlocks * state.getCostPerBlock()));

        DebugComponent debug = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), DebugComponent.getComponentType());
        if (debug != null)
            debug.setScale(new Vector3d(halfCurrent.x * 2, halfCurrent.y * 2, halfCurrent.z * 2));

        if (state.isEntitiesWired())
            sweepEntities(state, status, ctx, halfCurrent);
        if (state.isBlocksWired())
            sweepBlocks(state, status, ctx, halfPrevious, halfCurrent);

        if (state.isComplete())
            return true;
        return !drainSustain(dt, status);
    }

    private void sweepEntities(AreaState state, HexStatus<AreaState> status,
            ConstructTickContext ctx, Vector3d half) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null)
            return;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Vector3d center = state.getCenter();
        List<Ref<EntityStore>> found = new ObjectArrayList<>(TargetUtil.getAllEntitiesInBox(
                new Vector3d(center).sub(half), new Vector3d(center).add(half), buffer));

        for (Ref<EntityStore> ref : found) {
            if (ref == null || !ref.isValid() || ref.equals(ctx.getEntityRef()))
                continue;
            if (buffer.getComponent(ref, Intangible.getComponentType()) != null)
                continue;
            UUIDComponent uuid = buffer.getComponent(ref, UUIDComponent.getComponentType());
            if (uuid == null)
                continue;
            TransformComponent transform = buffer.getComponent(
                    ref, TransformComponent.getComponentType());
            if (transform == null)
                continue;

            Vector3d pos = transform.getPosition();
            if (!state.getShape().contains(half,
                    pos.x - center.x, pos.y - center.y, pos.z - center.z))
                continue;
            if (!state.markFired(uuid.getUuid()))
                continue;

            HexContext branch = status.getHexContext().branch();
            branch.updateRuntimeAccessors(buffer);
            branch.enterLocalScope();
            triggering.writeOutput(new EntityVar(uuid.getUuid(), ref), branch);
            HexExecuter.continueExecution(state.getEntityLinks(), branch);
        }
    }

    private void sweepBlocks(AreaState state, HexStatus<AreaState> status,
            ConstructTickContext ctx, Vector3d halfPrevious, Vector3d halfCurrent) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null)
            return;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        World world = buffer.getExternalData().getWorld();
        BlockAccess.Cursor cursor = new BlockAccess.Cursor(world);
        Vector3d center = state.getCenter();
        AreaShape shape = state.getShape();

        int minY = (int) Math.ceil(center.y - halfCurrent.y - 0.5);
        int maxY = (int) Math.floor(center.y + halfCurrent.y - 0.5);
        int minZ = (int) Math.ceil(center.z - halfCurrent.z - 0.5);
        int maxZ = (int) Math.floor(center.z + halfCurrent.z - 0.5);

        int[] runFrom = new int[2];
        int[] runTo = new int[2];

        for (int by = minY; by <= maxY; by++) {
            double dy = by + 0.5 - center.y;
            for (int bz = minZ; bz <= maxZ; bz++) {
                double dz = bz + 0.5 - center.z;

                double outer = shape.halfWidthX(halfCurrent, dy, dz);
                if (outer < 0)
                    continue;
                double inner = shape.halfWidthX(halfPrevious, dy, dz);

                int runs;
                if (inner < 0) {
                    runs = 1;
                    runFrom[0] = (int) Math.ceil(center.x - outer - 0.5);
                    runTo[0] = (int) Math.floor(center.x + outer - 0.5);
                } else {
                    runs = 2;
                    runFrom[0] = (int) Math.ceil(center.x - outer - 0.5);
                    runTo[0] = (int) Math.ceil(center.x - inner - 0.5) - 1;
                    runFrom[1] = (int) Math.floor(center.x + inner - 0.5) + 1;
                    runTo[1] = (int) Math.floor(center.x + outer - 0.5);
                }

                for (int run = 0; run < runs; run++) {
                    for (int bx = runFrom[run]; bx <= runTo[run]; bx++) {
                        if (cursor.blockId(bx, by, bz) == BlockType.EMPTY_ID)
                            continue;

                        HexContext branch = status.getHexContext().branch();
                        branch.updateRuntimeAccessors(buffer);
                        branch.enterLocalScope();
                        triggering.writeOutput(new BlockVar(new Vector3i(bx, by, bz)), branch);
                        HexExecuter.continueExecution(state.getBlockLinks(), branch);
                    }
                }
            }
        }
    }

    @Override
    public void onCleanup(HexStatus<AreaState> status, ConstructTickContext ctx) {
        status.getHexContext().endBranch();
        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }
}
