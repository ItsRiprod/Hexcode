package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.style.ProjectileStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class ProjectileGlyph extends GlyphAsset {

    public static final String TYPE = "Projectile";

    private float projectileScale = 0.5f;
    private double ttlSeconds = 600.0;
    private double spawnOffset = 1.1;
    private String hitRootInteraction = "Hex_Projectile_Hit";
    private String missRootInteraction = "Hex_Projectile_Miss";
    private String bounceRootInteraction = "Hex_Projectile_Bounce";

    public static final BuilderCodec<ProjectileGlyph> CODEC = BuilderCodec
            .builder(ProjectileGlyph.class, ProjectileGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("ProjectileScale", Codec.FLOAT, true),
                    (c, v) -> c.projectileScale = v, c -> c.projectileScale)
            .add()
            .append(new KeyedCodec<>("TtlSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.ttlSeconds = v, c -> c.ttlSeconds)
            .add()
            .append(new KeyedCodec<>("SpawnOffset", Codec.DOUBLE, true),
                    (c, v) -> c.spawnOffset = v, c -> c.spawnOffset)
            .add()
            .append(new KeyedCodec<>("HitRootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.hitRootInteraction = v, c -> c.hitRootInteraction)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("MissRootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.missRootInteraction = v, c -> c.missRootInteraction)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("BounceRootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.bounceRootInteraction = v, c -> c.bounceRootInteraction)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    private static final float PASSIVE_FLOOR = 0.1f;

    private static boolean hasLinks(Glyph glyph, String slotKey) {
        Slot s = glyph.getSlot(slotKey);
        return s != null && s.getLinks().length > 0;
    }

    private boolean isPassive(Glyph glyph) {
        return glyph.getNextLinks().isEmpty()
                && !hasLinks(glyph, ProjectileGlyphSlots.IMMEDIATE)
                && !hasLinks(glyph, ProjectileGlyphSlots.BOUNCE);
    }

    @Override
    public float collectMana(Glyph glyph, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR : super.collectMana(glyph, asset);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR
                : super.getVolatilityCost(glyph, hexContext, asset);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var sourceVar = glyph.readSlot(ProjectileGlyphSlots.SOURCE, hexContext);
        var directionVar = glyph.readSlot(ProjectileGlyphSlots.DIRECTION, hexContext);
        var speedVar = glyph.readSlot(ProjectileGlyphSlots.SPEED, hexContext);
        var gravityVar = glyph.readSlot(ProjectileGlyphSlots.GRAVITY, hexContext);
        var bouncesVar = glyph.readSlot(ProjectileGlyphSlots.BOUNCES, hexContext);

        if (sourceVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine source position");
            return;
        }

        Vector3d spawnPos = HexVarUtil.resolveEyePosition(sourceVar, hexContext.getAccessor());
        if (spawnPos == null) {
            spawnPos = sourceVar.position(hexContext.getAccessor());
        }
        if (spawnPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine where to spawn");
            return;
        }

        if (directionVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine direction");
            return;
        }

        Vector3d direction = directionVar.direction(hexContext.getAccessor());
        if (direction == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine direction");
            return;
        }

        double dirLen = direction.length();
        if (!Double.isFinite(dirLen) || dirLen < 1e-9) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Direction is invalid");
            return;
        }
        direction = new Vector3d(direction.x / dirLen, direction.y / dirLen, direction.z / dirLen);

        spawnPos.add(new Vector3d(direction).mul(spawnOffset));

        double speedDefault = HexVarUtil.numberOrSlotDefault(null, getSlot(ProjectileGlyphSlots.SPEED));
        double speed = HexVarUtil.numberOrSlotDefault(speedVar, getSlot(ProjectileGlyphSlots.SPEED));
        if (speed <= 0)
            speed = speedDefault;

        double gravity = HexVarUtil.numberOrSlotDefault(gravityVar, getSlot(ProjectileGlyphSlots.GRAVITY));

        int bounces = HexVarUtil.numberOrSlotDefault(
                bouncesVar, getSlot(ProjectileGlyphSlots.BOUNCES)).intValue();
        if (bounces < 0)
            bounces = 0;

        Holder<EntityStore> holder = HexConstructSpawner.create(hexContext.getAccessor(), hexContext, glyph,
                ProjectileGlyph.TYPE, spawnPos);

        Rotation3f rotation = Rotation3f.lookAt(direction);

        holder.putComponent(TransformComponent.getComponentType(),
                new TransformComponent(new Vector3d(spawnPos), rotation));
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));

        Model model = HexConstructSpawner.attachModel(holder, hexContext, this, projectileScale);
        if (model == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "model asset not found");
            return;
        }

        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));

        holder.addComponent(EntityScaleComponent.getComponentType(),
                new EntityScaleComponent(model.getScale()));

        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(EffectControllerComponent.getComponentType());
        holder.addComponent(Velocity.getComponentType(), new Velocity());

        holder.addComponent(Interactions.getComponentType(),
                new Interactions(buildInteractionsMap()));

        Vector3d launchVelocity = new Vector3d(direction).mul(speed);
        ProjectilePhysicsConfig physicsConfig = new ProjectilePhysicsConfig(gravity, bounces);

        var accessor = hexContext.getAccessor();

        Ref<EntityStore> parent = sourceVar instanceof EntityVar var ? var.getRef(accessor)
                : hexContext.getCasterRef(accessor);

        physicsConfig.apply(holder, parent,
                launchVelocity, accessor, false);

        Duration ttl = Duration.ofMillis((long) (ttlSeconds * 1000));
        holder.addComponent(DespawnComponent.getComponentType(),
                new DespawnComponent(hexContext.getAccessor()
                        .getResource(TimeResource.getResourceType()).getNow().plus(ttl)));

        holder.addComponent(ProjectileState.getComponentType(),
                new ProjectileState(hexContext, glyph));

        Ref<EntityStore> projectileRef = hexContext.getAccessor().addEntity(holder, AddReason.SPAWN);

        UUIDComponent uuidComp = holder.getComponent(UUIDComponent.getComponentType());
        if (uuidComp != null) {
            hexContext.addDependency(projectileRef);
            EntityVar projectileVar = new EntityVar(uuidComp.getUuid(), projectileRef);
            glyph.writeOutput(projectileVar, hexContext);
        }

        ProjectileStyle.renderLaunch(this, spawnPos, direction, hexContext, hexContext.getAccessor());
    }

    private Map<InteractionType, String> buildInteractionsMap() {
        Map<InteractionType, String> map = new EnumMap<>(InteractionType.class);
        map.put(InteractionType.ProjectileHit, hitRootInteraction);
        map.put(InteractionType.ProjectileMiss, missRootInteraction);
        map.put(InteractionType.ProjectileBounce, bounceRootInteraction);
        return map;
    }
}
