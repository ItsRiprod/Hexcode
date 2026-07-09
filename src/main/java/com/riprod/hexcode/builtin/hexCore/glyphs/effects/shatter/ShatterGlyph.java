package com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.style.ShatterStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class ShatterGlyph implements GlyphHandler {

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Shatter";

    private static final String HIT_ROOT_INTERACTION = "Hex_Shatter_Hit";
    private static final String MISS_ROOT_INTERACTION = "Hex_Shatter_Miss";
    private static final String BOUNCE_ROOT_INTERACTION = "Hex_Shatter_Bounce";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ShatterConfig.class, ShatterConfig.CODEC);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        if (asset == null) return 0f;
        ShatterConfig config = getConfig(ShatterConfig.class, asset);
        if (config == null) config = ShatterConfig.DEFAULTS;

        int count = HexVarUtil.numberOrDefault(
                glyph.readSlot(ShatterGlyphSlots.COUNT, hexContext),
                (double) config.getDefaultCount()).intValue();
        if (count < 1) count = 1;

        // shards are priced per projectile, discounted from a full projectile; draw quality
        // reduction is carried through computeBaseCost / InstantCost
        float instantCost = asset.getVolatility().getInstantCost();
        float drawFactor = instantCost > 0f ? glyph.computeBaseCost(asset) / instantCost : 1f;
        return (float) (count * config.getPerShardPrice() * drawFactor);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar sourceVar = glyph.readSlot(ShatterGlyphSlots.SOURCE, hexContext);
        HexVar directionVar = glyph.readSlot(ShatterGlyphSlots.DIRECTION, hexContext);
        HexVar countVar = glyph.readSlot(ShatterGlyphSlots.COUNT, hexContext);
        HexVar spreadVar = glyph.readSlot(ShatterGlyphSlots.SPREAD, hexContext);
        HexVar speedVar = glyph.readSlot(ShatterGlyphSlots.SPEED, hexContext);
        HexVar gravityVar = glyph.readSlot(ShatterGlyphSlots.GRAVITY, hexContext);

        if (sourceVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "no source provided");
            return;
        }

        Vector3d spawnPos = HexDirectionUtil.resolveEyePosition(sourceVar, hexContext.getAccessor());
        if (spawnPos == null) {
            spawnPos = HexVarUtil.position(sourceVar, hexContext.getAccessor());
        }
        if (spawnPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "could not resolve spawn position");
            return;
        }

        Vector3d centralDir = HexDirectionUtil.resolveDirection(
                directionVar, spawnPos, hexContext.getAccessor());
        if (centralDir == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "could not resolve direction");
            return;
        }

        double dirLen = centralDir.length();
        if (!Double.isFinite(dirLen) || dirLen < 1e-9) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "direction is degenerate (zero or NaN)");
            return;
        }
        centralDir = new Vector3d(centralDir.x / dirLen, centralDir.y / dirLen, centralDir.z / dirLen);

        var accessor = hexContext.getAccessor();

        Ref<EntityStore> parent = sourceVar instanceof EntityVar var
                ? var.getRef(accessor)
                : hexContext.getCasterRef(accessor);

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ShatterConfig config = getConfig(ShatterConfig.class, asset);
        if (config == null) config = ShatterConfig.DEFAULTS;

        int count = HexVarUtil.numberOrDefault(countVar, (double) config.getDefaultCount()).intValue();
        if (count < 1) count = 1;

        double spread = HexVarUtil.numberOrDefault(spreadVar, config.getDefaultSpread());
        double speed = HexVarUtil.numberOrDefault(speedVar, config.getDefaultSpeed());
        if (speed <= 0) speed = config.getDefaultSpeed();
        double gravity = HexVarUtil.numberOrDefault(gravityVar, config.getDefaultGravity());

        Duration shardTtl = Duration.ofMillis((long) (config.getShardTtlSeconds() * 1000));

        List<Vector3d> shardDirections = computeConeDirections(centralDir, count, spread);

        String modelId = VfxUtil.resolveModelId(hexContext, asset);
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;
        if (modelAsset == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "model asset not found: " + modelId);
            return;
        }
        Model model = Model.createScaledModel(modelAsset, 1.0f);

        for (Vector3d dir : shardDirections) {
            Vector3d shardSpawn = new Vector3d(spawnPos).add(new Vector3d(dir).mul(1.0));
            spawnShard(hexContext, glyph, parent, shardSpawn, dir, speed, gravity, model,
                    shardDirections.size(), shardTtl);
        }

        ShatterStyle.renderLaunch(spawnPos, centralDir, hexContext, hexContext.getAccessor());
    }

    private void spawnShard(HexContext hexContext, Glyph glyph, Ref<EntityStore> parent,
            Vector3d position, Vector3d direction,
            double speed, double gravity, Model model, int splitFactor, Duration shardTtl) {

        HexContext branched = hexContext.branch(splitFactor);

        Rotation3f rotation = Rotation3f.lookAt(direction);

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(new Vector3d(position), rotation));
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));

        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));

        holder.addComponent(NetworkId.getComponentType(),
                new NetworkId(hexContext.getAccessor().getExternalData().takeNextNetworkId()));

        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.addComponent(Velocity.getComponentType(), new Velocity());

        holder.addComponent(Interactions.getComponentType(),
                new Interactions(buildInteractionsMap()));

        Vector3d launchVelocity = new Vector3d(direction).mul(speed);
        new ShatterPhysicsConfig(gravity, 0).apply(holder, parent,
                launchVelocity, hexContext.getAccessor(), false);

        holder.addComponent(DespawnComponent.getComponentType(),
                new DespawnComponent(hexContext.getAccessor()
                        .getResource(TimeResource.getResourceType()).getNow().plus(shardTtl)));

        holder.addComponent(ShatterState.getComponentType(),
                new ShatterState(branched, glyph));

        Ref<EntityStore> shardRef = hexContext.getAccessor().addEntity(holder, AddReason.SPAWN);
        hexContext.getHexRoot().addDependency(branched, shardRef);
    }

    private List<Vector3d> computeConeDirections(Vector3d center, int count, double spread) {
        Vector3d forward = new Vector3d(center).normalize();

        Vector3d arbitrary = (Math.abs(forward.y) < 0.9)
                ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);

        Vector3d right = cross(arbitrary, forward).normalize();
        Vector3d up = cross(forward, right).normalize();

        List<Vector3d> directions = new ArrayList<>();

        if (count == 1) {
            directions.add(new Vector3d(forward));
            return directions;
        }

        directions.add(new Vector3d(forward));

        int ringCount = count - 1;
        double cosSpread = Math.cos(spread);
        double sinSpread = Math.sin(spread);

        for (int i = 0; i < ringCount; i++) {
            double azimuth = (2.0 * Math.PI * i) / ringCount;

            Vector3d dir = new Vector3d(forward).mul(cosSpread)
                    .add(new Vector3d(right).mul(sinSpread * Math.cos(azimuth)))
                    .add(new Vector3d(up).mul(sinSpread * Math.sin(azimuth)));
            dir.normalize();
            directions.add(dir);
        }

        return directions;
    }

    private static Vector3d cross(Vector3d a, Vector3d b) {
        return new Vector3d(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
        );
    }

    private static Map<InteractionType, String> buildInteractionsMap() {
        Map<InteractionType, String> map = new EnumMap<>(InteractionType.class);
        map.put(InteractionType.ProjectileHit, HIT_ROOT_INTERACTION);
        map.put(InteractionType.ProjectileMiss, MISS_ROOT_INTERACTION);
        map.put(InteractionType.ProjectileBounce, BOUNCE_ROOT_INTERACTION);
        return map;
    }
}
