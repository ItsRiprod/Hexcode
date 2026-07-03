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
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
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

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(GlaciateConfig.class, GlaciateConfig.CODEC);
    }

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

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        GlaciateConfig config = getConfig(GlaciateConfig.class, asset);
        if (config == null) config = GlaciateConfig.DEFAULTS;

        HexVar offsetVar = glyph.readSlot(GlaciateGlyphSlots.OFFSET, hexContext);
        double defaultHeight = HexVarUtil.numberOrSlotDefault(
                null, asset.getSlot(GlaciateGlyphSlots.OFFSET));
        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GlaciateGlyphSlots.DURATION, hexContext),
                asset.getSlot(GlaciateGlyphSlots.DURATION));
        if (duration <= 0) {
            duration = HexVarUtil.numberOrSlotDefault(
                    null, asset.getSlot(GlaciateGlyphSlots.DURATION));
        }

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(config.getIceModelId());
        if (modelAsset == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Model asset not found: " + config.getIceModelId());
            return;
        }

        HitboxCollisionConfig collisionConfig = HitboxCollisionConfig.getAssetMap()
                .getAsset(config.getHardCollisionConfigId());

        Vector3d spawnPos = resolveSpawnPosition(targetPos, offsetVar, hexContext, defaultHeight);
        spawnIceBlock(glyph, hexContext, spawnPos, (float) duration,
                modelAsset, collisionConfig, config);
    }

    private Vector3d resolveSpawnPosition(Vector3d targetPos, HexVar offsetVar, HexContext hexContext,
            double defaultHeight) {
        if (offsetVar == null) {
            return new Vector3d(targetPos).add(new Vector3d(0, defaultHeight, 0));
        }

        if (offsetVar instanceof EntityVar) {
            Vector3d absPos = HexVarUtil.position(offsetVar, hexContext.getAccessor());
            if (absPos != null) return absPos;
            return new Vector3d(targetPos).add(new Vector3d(0, defaultHeight, 0));
        }

        if (offsetVar instanceof PositionVar posVar && posVar.getValue() != null) {
            if (posVar.isAbsolute()) return new Vector3d(posVar.getValue());
            return new Vector3d(targetPos).add(new Vector3d(posVar.getValue()));
        }

        if (offsetVar instanceof NumberVar) {
            double height = HexVarUtil.numberOrDefault(offsetVar, defaultHeight);
            return new Vector3d(targetPos).add(new Vector3d(0, height, 0));
        }

        return new Vector3d(targetPos).add(new Vector3d(0, defaultHeight, 0));
    }

    private void spawnIceBlock(Glyph glyph, HexContext hexContext, Vector3d spawnPos,
            float duration,
            ModelAsset modelAsset, HitboxCollisionConfig collisionConfig, GlaciateConfig config) {
        Model model = Model.createScaledModel(modelAsset, config.getIceScale());

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
                new GlaciateComponent(config.getDamageRadius(), config.getDamageMultiplier(), duration));

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
