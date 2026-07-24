package com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc.style.ArcStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc.utils.ArcUtils;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class ArcConstructHandler implements ConstructHandler<ArcState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float MIN_INTERVAL = 0.05f;

    @Override
    public void onFirstTick(HexStatus<ArcState> status, ConstructTickContext ctx) {
        ArcState state = status.getState();
        if (state == null) return;

        Vector3d origin = originPosition(ctx);
        if (origin != null) {
            ArcStyle.renderCast(ctx.getBuffer(), origin, status.getHexContext());
        }
    }

    @Override
    public boolean onTick(float dt, HexStatus<ArcState> status, ConstructTickContext ctx) {
        ArcState state = status.getState();
        if (state == null) return true;

        state.tick(dt);
        if (state.getElapsedSeconds() < state.getInterval()) return false;
        state.resetTimer();

        state.consumeIteration();

        boolean depleted = pulse(state, status, ctx);
        return depleted || state.getRemainingIterations() <= 0;
    }

    @Override
    public void onCleanup(HexStatus<ArcState> status, ConstructTickContext ctx) {
        ArcState state = status.getState();
        if (state != null && state.isSpawnedHost()) {
            Ref<EntityStore> host = ctx.getEntityRef();
            if (host != null && host.isValid()) {
                ctx.getBuffer().tryRemoveEntity(host, RemoveReason.REMOVE);
            }
        }
    }

    private boolean pulse(ArcState state, HexStatus<ArcState> status, ConstructTickContext ctx) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Vector3d origin = originPosition(ctx);
        if (origin == null) return true;

        HexContext hexContext = status.getHexContext();

        Ref<EntityStore> target = ArcUtils.getNextArcTarget(
                origin, state.getRange(), state.getVisited(), buffer);
        if (target == null) {
            return false;
        }

        TransformComponent targetTc = buffer.getComponent(target, TransformComponent.getComponentType());
        Vector3d targetPos = targetTc != null ? targetTc.getPosition() : origin;
        double distance = origin.distance(targetPos);

        if (!consumeArcCost(state, hexContext, (float) distance)) {
            return true;
        }

        UUIDComponent targetUuid = buffer.getComponent(target, UUIDComponent.getComponentType());
        if (targetUuid != null) state.getVisited().add(targetUuid.getUuid());

        World world = buffer.getExternalData().getWorld();
        ArcStyle.renderArc(buffer, world, origin, targetPos, hexContext);
        ArcStyle.renderHit(buffer, targetPos, hexContext);

        Glyph triggeringGlyph = status.getTriggeringGlyph();
        if (triggeringGlyph != null && targetUuid != null) {
            HexContext branchContext = hexContext.branch();
            branchContext.updateRuntimeAccessors(buffer);
            triggeringGlyph.writeOutput(new EntityVar(targetUuid.getUuid(), target), branchContext);
            HexExecuter.continueExecution(List.copyOf(state.getOutputLinks()), branchContext);
        }

        return false;
    }

    private boolean consumeArcCost(ArcState state, HexContext hexContext, float distance) {
        Glyph arcGlyph = state.getArcGlyph();
        if (arcGlyph == null) return true;

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(arcGlyph.getGlyphId());
        if (asset == null) return true;

        HexStats tracker = hexContext.getHexStats();
        if (tracker == null) return false;
        if (hexContext.getHexRoot() == null) return true;

        ArcConfig config = asset.getConfig() instanceof ArcConfig arcConfig ? arcConfig : ArcConfig.DEFAULTS;
        Impact impact = asset.getConfig() == null ? null : asset.getConfig().getVolatilityImpact();
        float intervalFactor = (float) (config.getReferenceInterval()
                / Math.max(MIN_INTERVAL, state.getInterval()));
        float cost = arcGlyph.computeBaseCost(asset) * Impact.scale(impact, distance) * intervalFactor;

        return tracker.consumeVolatility(cost) > 0f;
    }

    private Vector3d originPosition(ConstructTickContext ctx) {
        Ref<EntityStore> host = ctx.getEntityRef();
        if (host == null || !host.isValid()) return null;
        TransformComponent tc = ctx.getBuffer().getComponent(host, TransformComponent.getComponentType());
        return tc != null ? tc.getPosition() : null;
    }
}
