package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
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
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.style.ProjectileStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

import com.riprod.hexcode.utils.HexVarUtil;

public class ProjectileGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Projectile";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ProjectileConfig.class, ProjectileConfig.CODEC);
    }

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
        return isPassive(glyph) ? PASSIVE_FLOOR : GlyphHandler.super.collectMana(glyph, asset);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR
                : GlyphHandler.super.getVolatilityCost(glyph, hexContext, asset);
    }


    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ProjectileConfig config = getConfig(ProjectileConfig.class, asset);
        if (config == null) config = ProjectileConfig.DEFAULTS;

        HexVar sourceVar = glyph.readSlot(ProjectileGlyphSlots.SOURCE, hexContext);
        HexVar directionVar = glyph.readSlot(ProjectileGlyphSlots.DIRECTION, hexContext);
        HexVar speedVar = glyph.readSlot(ProjectileGlyphSlots.SPEED, hexContext);
        HexVar gravityVar = glyph.readSlot(ProjectileGlyphSlots.GRAVITY, hexContext);
        HexVar bouncesVar = glyph.readSlot(ProjectileGlyphSlots.BOUNCES, hexContext);

        if (sourceVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine source position");
            return;
        }

        Vector3d spawnPos = HexVarUtil.resolveEyePosition(sourceVar, hexContext.getAccessor());
        if (spawnPos == null) {
            spawnPos = HexVarUtil.position(sourceVar, hexContext.getAccessor());
        }
        if (spawnPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine where to spawn");
            return;
        }

        Vector3d direction = HexVarUtil.resolveDirection(directionVar, spawnPos, hexContext.getAccessor());
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

        spawnPos.add(new Vector3d(direction).mul(config.getSpawnOffset()));

        double speedDefault = HexVarUtil.numberOrSlotDefault(null, asset.getSlot(ProjectileGlyphSlots.SPEED));
        double speed = HexVarUtil.numberOrSlotDefault(speedVar, asset.getSlot(ProjectileGlyphSlots.SPEED));
        if (speed <= 0)
            speed = speedDefault;

        double gravity = HexVarUtil.numberOrSlotDefault(gravityVar, asset.getSlot(ProjectileGlyphSlots.GRAVITY));

        int bounces = HexVarUtil.numberOrSlotDefault(
                bouncesVar, asset.getSlot(ProjectileGlyphSlots.BOUNCES)).intValue();
        if (bounces < 0)
            bounces = 0;

        Holder<EntityStore> holder = HexConstructSpawner.create(hexContext.getAccessor(), hexContext, glyph,
                ProjectileGlyph.ID, spawnPos);

        Rotation3f rotation = Rotation3f.lookAt(direction);

        holder.putComponent(TransformComponent.getComponentType(),
                new TransformComponent(new Vector3d(spawnPos), rotation));
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));

        Model model = HexConstructSpawner.attachModel(holder, hexContext,
                GlyphAsset.getAssetMap().getAsset(ID), config.getProjectileScale());
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
                new Interactions(buildInteractionsMap(config)));

        Vector3d launchVelocity = new Vector3d(direction).mul(speed);
        ProjectilePhysicsConfig physicsConfig = new ProjectilePhysicsConfig(gravity, bounces);

        var accessor = hexContext.getAccessor();

        Ref<EntityStore> parent = sourceVar instanceof EntityVar var ? var.getRef(accessor)
                : hexContext.getCasterRef(accessor);

        physicsConfig.apply(holder, parent,
                launchVelocity, accessor, false);

        Duration ttl = Duration.ofMillis((long) (config.getTtlSeconds() * 1000));
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

        ProjectileStyle.renderLaunch(spawnPos, direction, hexContext, hexContext.getAccessor());
    }

    private static Map<InteractionType, String> buildInteractionsMap(ProjectileConfig config) {
        Map<InteractionType, String> map = new EnumMap<>(InteractionType.class);
        map.put(InteractionType.ProjectileHit, config.getHitRootInteraction());
        map.put(InteractionType.ProjectileMiss, config.getMissRootInteraction());
        map.put(InteractionType.ProjectileBounce, config.getBounceRootInteraction());
        return map;
    }
}
