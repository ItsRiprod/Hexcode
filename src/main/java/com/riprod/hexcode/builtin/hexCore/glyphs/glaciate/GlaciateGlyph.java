package com.riprod.hexcode.builtin.hexCore.glyphs.glaciate;

import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.glaciate.style.GlaciateStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.utils.HexVarUtil;

public class GlaciateGlyph implements GlyphHandler {
    @Override
public String getId() { return ID; };

public static final String ID = "Glaciate";

    private static final String ICE_MODEL = "Glaciate_Ice";
    private static final float ICE_SCALE = 2.0f;
    private static final double DEFAULT_HEIGHT = 10.0;
    private static final double DEFAULT_DURATION = 15.0;
    private static final float DEFAULT_DAMAGE_RADIUS = 1.2f;
    private static final float DEFAULT_DAMAGE_MULTIPLIER = 1.0f;
    private static final String HARD_COLLISION_CONFIG = "Hexcode_Glaciate_HardCollision";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(GlaciateGlyphSlots.TARGET, hexContext);
        if (targetVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "No target provided");
            return;
        }

        Vector3d targetPos = HexVarUtil.position(targetVar, hexContext.getAccessor());
        if (targetPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Could not resolve target position");
            return;
        }

        HexVar offsetVar = glyph.readSlot(GlaciateGlyphSlots.OFFSET, hexContext);
        double duration = HexVarUtil.numberOrDefault(
                glyph.readSlot(GlaciateGlyphSlots.DURATION, hexContext), DEFAULT_DURATION);
        if (duration <= 0) duration = DEFAULT_DURATION;

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(ICE_MODEL);
        if (modelAsset == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Model asset not found: " + ICE_MODEL);
            return;
        }

        HitboxCollisionConfig collisionConfig = HitboxCollisionConfig.getAssetMap()
                .getAsset(HARD_COLLISION_CONFIG);

        Vector3d spawnPos = resolveSpawnPosition(targetPos, offsetVar, hexContext);
        spawnIceBlock(glyph, hexContext, spawnPos, (float) duration,
                modelAsset, collisionConfig);
    }

    private Vector3d resolveSpawnPosition(Vector3d targetPos, HexVar offsetVar, HexContext hexContext) {
        if (offsetVar == null) {
            return new Vector3d(targetPos).add(new Vector3d(0, DEFAULT_HEIGHT, 0));
        }

        if (offsetVar instanceof EntityVar) {
            Vector3d absPos = HexVarUtil.position(offsetVar, hexContext.getAccessor());
            if (absPos != null) return absPos;
            return new Vector3d(targetPos).add(new Vector3d(0, DEFAULT_HEIGHT, 0));
        }

        if (offsetVar instanceof PositionVar posVar && posVar.getValue() != null) {
            if (posVar.isAbsolute()) return new Vector3d(posVar.getValue());
            return new Vector3d(targetPos).add(new Vector3d(posVar.getValue()));
        }

        if (offsetVar instanceof NumberVar) {
            double height = HexVarUtil.numberOrDefault(offsetVar, DEFAULT_HEIGHT);
            return new Vector3d(targetPos).add(new Vector3d(0, height, 0));
        }

        return new Vector3d(targetPos).add(new Vector3d(0, DEFAULT_HEIGHT, 0));
    }

    private void spawnIceBlock(Glyph glyph, HexContext hexContext, Vector3d spawnPos,
            float duration,
            ModelAsset modelAsset, HitboxCollisionConfig collisionConfig) {
        Model model = Model.createScaledModel(modelAsset, ICE_SCALE);

        var accessor = hexContext.getAccessor();

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, GlaciateGlyph.ID, new Vector3d(spawnPos),
                new GlaciateState(glyph.getNextLinks()));

        Rotation3f rotation = new Rotation3f();
        holder.getComponent(TransformComponent.getComponentType())
                .setRotation(rotation);
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(),
                new PersistentModel(model.toReference()));
        holder.addComponent(BoundingBox.getComponentType(),
                new BoundingBox(model.getBoundingBox()));
        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(EffectControllerComponent.getComponentType());
        holder.addComponent(Velocity.getComponentType(), new Velocity());

        if (collisionConfig != null) {
            holder.addComponent(HitboxCollision.getComponentType(),
                    new HitboxCollision(collisionConfig));
        }

        holder.addComponent(GlaciateComponent.getComponentType(),
                new GlaciateComponent(DEFAULT_DAMAGE_RADIUS, DEFAULT_DAMAGE_MULTIPLIER, duration));

        GlaciatePhysicsConfig.INSTANCE.apply(holder, hexContext.getCasterRef(accessor),
                new Vector3d(), accessor, false);

        Ref<EntityStore> iceRef = accessor.addEntity(holder, AddReason.SPAWN);

        UUIDComponent iceUuidComp = holder.getComponent(UUIDComponent.getComponentType());
        UUID iceUuid = iceUuidComp != null ? iceUuidComp.getUuid() : UUID.randomUUID();
        glyph.writeSelfOutput(new EntityVar(iceUuid, iceRef), hexContext);

        hexContext.getHexRoot().addDependency(hexContext, iceRef);

        GlaciateStyle.renderSpawn(spawnPos, hexContext, accessor);
    }
}
