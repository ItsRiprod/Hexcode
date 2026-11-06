package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate;

import java.util.UUID;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

import lombok.Getter;

public class GlaciateGlyph extends GlyphAsset {
        public static final String TYPE = "Glaciate";

        @Getter
        private double minDamageSpeed = 0.1;
        @Getter
        private double knockbackScale = 0.3;
        @Getter
        private double minVerticalKnockback = 2.0;
        private float damageRadius = 1.2f;
        private float damageMultiplier = 1.0f;
        private String iceModelId = "Glaciate_Ice";
        private String hardCollisionConfigId = "Hexcode_Glaciate_HardCollision";

        public static final BuilderCodec<GlaciateGlyph> CODEC = BuilderCodec
                        .builder(GlaciateGlyph.class, GlaciateGlyph::new, GlyphAsset.ABSTRACT_CODEC)
                        .append(new KeyedCodec<>("MinDamageSpeed", Codec.DOUBLE, true),
                                        (c, v) -> c.minDamageSpeed = v, c -> c.minDamageSpeed)
                        .add()
                        .append(new KeyedCodec<>("KnockbackScale", Codec.DOUBLE, true),
                                        (c, v) -> c.knockbackScale = v, c -> c.knockbackScale)
                        .add()
                        .append(new KeyedCodec<>("MinVerticalKnockback", Codec.DOUBLE, true),
                                        (c, v) -> c.minVerticalKnockback = v, c -> c.minVerticalKnockback)
                        .add()
                        .append(new KeyedCodec<>("DamageRadius", Codec.FLOAT, true),
                                        (c, v) -> c.damageRadius = v, c -> c.damageRadius)
                        .add()
                        .append(new KeyedCodec<>("DamageMultiplier", Codec.FLOAT, true),
                                        (c, v) -> c.damageMultiplier = v, c -> c.damageMultiplier)
                        .add()
                        .append(new KeyedCodec<>("IceModel", Codec.STRING, true),
                                        (c, v) -> c.iceModelId = v, c -> c.iceModelId)
                        .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                        .add()
                        .append(new KeyedCodec<>("HardCollisionConfig", Codec.STRING, true),
                                        (c, v) -> c.hardCollisionConfigId = v, c -> c.hardCollisionConfigId)
                        .addValidatorLate(() -> HitboxCollisionConfig.VALIDATOR_CACHE.getValidator().late())
                        .add()
                        .build();

        @Override
        public void execute(Glyph glyph, HexContext hexContext) {
                HexVar targetVar = glyph.readSlot(GlaciateGlyphSlots.TARGET, hexContext);
                if (targetVar == null) {
                        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                                        "No target provided");
                        return;
                }

                Vector3d targetPos = targetVar.position(hexContext.getAccessor());
                if (targetPos == null) {
                        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                                        "Could not resolve target position");
                        return;
                }

                HexVar offsetVar = glyph.readSlot(GlaciateGlyphSlots.OFFSET, hexContext);
                double defaultHeight = HexVarUtil.numberOrSlotDefault(
                                null, getSlot(GlaciateGlyphSlots.OFFSET));
                double duration = HexVarUtil.numberOrSlotDefault(
                                glyph.readSlot(GlaciateGlyphSlots.DURATION, hexContext),
                                getSlot(GlaciateGlyphSlots.DURATION));
                if (duration <= 0) {
                        duration = HexVarUtil.numberOrSlotDefault(
                                        null, getSlot(GlaciateGlyphSlots.DURATION));
                }

                ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(iceModelId);
                if (modelAsset == null) {
                        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                                        "Model asset not found: " + iceModelId);
                        return;
                }

                HitboxCollisionConfig collisionConfig = HitboxCollisionConfig.getAssetMap()
                                .getAsset(hardCollisionConfigId);

                var spawnPos = new Vector3d(targetPos)
                                .add(new Vector3d(0, HexVarUtil.numberOrDefault(offsetVar, defaultHeight), 0));
                spawnIceBlock(glyph, hexContext, spawnPos, (float) duration,
                                modelAsset, collisionConfig);
        }

        private void spawnIceBlock(Glyph glyph, HexContext hexContext, Vector3d spawnPos,
                        float duration,
                        ModelAsset modelAsset, HitboxCollisionConfig collisionConfig) {
                Model model = Model.createRandomScaleModel(modelAsset);

                var accessor = hexContext.getAccessor();

                Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                                accessor, hexContext, glyph, GlaciateGlyph.TYPE, new Vector3d(spawnPos),
                                new GlaciateState(glyph.getNextLinks()));
                if (holder == null) {
                        return;
                }

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
                holder.ensureComponent(EffectControllerComponent.getComponentType());
                holder.addComponent(Velocity.getComponentType(), new Velocity());

                if (collisionConfig != null) {
                        holder.addComponent(HitboxCollision.getComponentType(),
                                        new HitboxCollision(collisionConfig));
                }

                holder.addComponent(GlaciateComponent.getComponentType(),
                                new GlaciateComponent(damageRadius, damageMultiplier,
                                                duration));

                GlaciatePhysicsConfig.INSTANCE.apply(holder, hexContext.getRootRef(),
                                new Vector3d(), accessor, false);

                Ref<EntityStore> iceRef = accessor.addEntity(holder, AddReason.SPAWN);

                UUIDComponent iceUuidComp = holder.getComponent(UUIDComponent.getComponentType());
                UUID iceUuid = iceUuidComp != null ? iceUuidComp.getUuid() : UUID.randomUUID();
                glyph.writeSelfOutput(new EntityVar(iceUuid, iceRef), hexContext);

                hexContext.addDependency(iceRef);

                EffectRenderer.render(EffectSlot.Start, this, hexContext, spawnPos, accessor);
        }
}
