package com.riprod.hexcode.builtin.elementalism.elements.fire;

import javax.annotation.Nullable;

import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;

public class ExplosionTier extends ElementalTier {
    public static final String TYPE = "Explosion";
    public static final BuilderCodec<ExplosionTier> CODEC;

    private ExplosionConfig explosion;
    private ExplosionConfig entityOnlyExplosion;

    @Override
    public boolean supportsEntity() {
        return true;
    }

    @Override
    public boolean supportsBlock() {
        return true;
    }

    @Override
    public boolean applyEntity(Ref<EntityStore> target, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        return explode(EffectRenderer.entityCenter(target, buffer), ctx, buffer);
    }

    @Override
    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        return explode(EffectRenderer.blockCenter(block), ctx, buffer);
    }

    private boolean explode(@Nullable Vector3d center, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        if (center == null) {
            return false;
        }
        var world = buffer.getExternalData().getWorld();
        var canBreak = HexProtection.canModifyBlock(world, ctx.getRootRef(), buffer,
                new Vector3i((int) Math.floor(center.x), (int) Math.floor(center.y), (int) Math.floor(center.z)),
                BlockAction.BREAK);
        ExplosionUtils.performExplosion(damageSource(ctx), center, new Rotation3f(),
                canBreak ? this.explosion : this.entityOnlyExplosion, null, buffer,
                world.getChunkStore().getStore());
        return true;
    }

    static {
        CODEC = BuilderCodec.builder(ExplosionTier.class, ExplosionTier::new, ElementalTier.BASE_CODEC)
                .append(new KeyedCodec<>("Explosion", ExplosionConfig.CODEC),
                        (t, v) -> t.explosion = v, t -> t.explosion)
                .addValidator(Validators.nonNull())
                .documentation("Engine explosion run at the target: radii, damage, damage cause, knockback, block drops and the tool that breaks blocks")
                .add()
                .afterDecode(t -> t.entityOnlyExplosion = ExplosionConfig.CODEC.decodeAndInherit(
                        new BsonDocument("DamageBlocks", BsonBoolean.FALSE), t.explosion, new ExtraInfo()))
                .build();
    }
}
