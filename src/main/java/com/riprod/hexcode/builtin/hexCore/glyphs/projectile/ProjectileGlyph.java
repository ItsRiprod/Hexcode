package com.riprod.hexcode.builtin.hexCore.glyphs.projectile;

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
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.style.ProjectileStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class ProjectileGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Projectile";

    private static final String PROJECTILE_MODEL = "Glyph_Projectile_Flight";
    private static final float PROJECTILE_SCALE = 0.5f;
    private static final Duration PROJECTILE_TTL = Duration.ofMinutes(10);

    private static final String HIT_ROOT_INTERACTION = "Hex_Projectile_Hit";
    private static final String MISS_ROOT_INTERACTION = "Hex_Projectile_Miss";
    private static final String BOUNCE_ROOT_INTERACTION = "Hex_Projectile_Bounce";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
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

        Vector3d spawnPos = HexDirectionUtil.resolveEyePosition(sourceVar, hexContext.getAccessor());
        if (spawnPos == null) {
            spawnPos = HexVarUtil.position(sourceVar, hexContext.getAccessor());
        }
        if (spawnPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot determine where to spawn");
            return;
        }

        Vector3d direction = HexDirectionUtil.resolveDirection(directionVar, spawnPos, hexContext.getAccessor());
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

        spawnPos.add(new Vector3d(direction).mul(1.5));

        double speed = HexVarUtil.numberOrDefault(speedVar, 30.0);
        if (speed <= 0)
            speed = 30.0;

        double gravity = HexVarUtil.numberOrDefault(gravityVar, 0.0);

        int bounces = HexVarUtil.numberOrDefault(bouncesVar, 0.0).intValue();
        if (bounces < 0)
            bounces = 0;

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(PROJECTILE_MODEL);
        if (modelAsset == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "model asset not found: " + PROJECTILE_MODEL);
            return;
        }

        Holder<EntityStore> holder = HexConstructSpawner.create(hexContext.getAccessor(), hexContext, glyph,
                ProjectileGlyph.ID, spawnPos);

        Rotation3f rotation = new Rotation3f();
        rotation.y = (float) Math.atan2(-direction.x, direction.z);
        rotation.x = (float) Math.asin(Math.max(-1.0, Math.min(1.0, -direction.y)));

        holder.putComponent(TransformComponent.getComponentType(),
                new TransformComponent(new Vector3d(spawnPos), rotation));
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));

        Model model = Model.createScaledModel(modelAsset, PROJECTILE_SCALE);

        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));

        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.addComponent(Velocity.getComponentType(), new Velocity());

        holder.addComponent(Interactions.getComponentType(),
                new Interactions(buildInteractionsMap()));

        Vector3d launchVelocity = new Vector3d(direction).mul(speed);
        ProjectilePhysicsConfig physicsConfig = new ProjectilePhysicsConfig(gravity, bounces);

        Ref<EntityStore> parent = sourceVar instanceof EntityVar var ? var.getRef(hexContext.getAccessor())
                : hexContext.getCasterRef();

        physicsConfig.apply(holder, parent,
                launchVelocity, hexContext.getAccessor(), false);

        holder.addComponent(DespawnComponent.getComponentType(),
                new DespawnComponent(hexContext.getAccessor()
                        .getResource(TimeResource.getResourceType()).getNow().plus(PROJECTILE_TTL)));

        holder.addComponent(ProjectileState.getComponentType(),
                new ProjectileState(hexContext, glyph));

        Ref<EntityStore> projectileRef = hexContext.getAccessor().addEntity(holder, AddReason.SPAWN);

        UUIDComponent uuidComp = holder.getComponent(UUIDComponent.getComponentType());
        if (uuidComp != null) {
            hexContext.getHexRoot().addDependency(hexContext, projectileRef);
            EntityVar projectileVar = new EntityVar(uuidComp.getUuid(), projectileRef);
            glyph.writeOutput(projectileVar, hexContext);
        }

        ProjectileStyle.renderLaunch(spawnPos, direction, hexContext, hexContext.getAccessor());
    }

    private static Map<InteractionType, String> buildInteractionsMap() {
        Map<InteractionType, String> map = new EnumMap<>(InteractionType.class);
        map.put(InteractionType.ProjectileHit, HIT_ROOT_INTERACTION);
        map.put(InteractionType.ProjectileMiss, MISS_ROOT_INTERACTION);
        map.put(InteractionType.ProjectileBounce, BOUNCE_ROOT_INTERACTION);
        return map;
    }
}
