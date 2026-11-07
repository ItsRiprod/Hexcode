package com.riprod.hexcode.builtin.elementalism.elements.water;

import javax.annotation.Nullable;

import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.utils.BlockAccess;

public class TidalWaveTier extends ElementalTier {
    public static final String TYPE = "TidalWave";
    public static final BuilderCodec<TidalWaveTier> CODEC;

    private ExplosionConfig wave;
    private float radius = 4;

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
        return surge(EffectRenderer.entityCenter(target, buffer), ctx, buffer);
    }

    @Override
    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        return surge(EffectRenderer.blockCenter(block), ctx, buffer);
    }

    private boolean surge(@Nullable Vector3d center, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        if (center == null) {
            return false;
        }
        var world = buffer.getExternalData().getWorld();
        var caster = ctx.getRootRef();
        var origin = new Vector3i(MathUtil.floor(center.x), MathUtil.floor(center.y), MathUtil.floor(center.z));
        var cursor = new BlockAccess.Cursor(world);
        for (var cell : WaterSweep.cells(origin, this.radius)) {
            WaterSweep.extinguish(cursor, cell, caster, buffer);
            WaterSweep.washAway(cursor, cell, caster, buffer);
        }
        ExplosionUtils.performExplosion(damageSource(ctx), center, new Rotation3f(), this.wave, null, buffer,
                world.getChunkStore().getStore());
        return true;
    }

    static {
        CODEC = BuilderCodec.builder(TidalWaveTier.class, TidalWaveTier::new, ElementalTier.BASE_CODEC)
                .append(new KeyedCodec<>("Wave", ExplosionConfig.CODEC),
                        (t, v) -> t.wave = v, t -> t.wave)
                .addValidator(Validators.nonNull())
                .documentation("Engine explosion that carries the wave to entities: radius, falloff and a Point knockback that pushes away from the center. Never damages blocks")
                .add()
                .<Float>append(new KeyedCodec<>("Radius", Codec.FLOAT),
                        (t, v) -> t.radius = v, t -> t.radius)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Radius in blocks in which soft blocks are washed away and fire is put out")
                .add()
                .afterDecode(t -> t.wave = ExplosionConfig.CODEC.decodeAndInherit(
                        new BsonDocument("DamageBlocks", BsonBoolean.FALSE)
                                .append("DetonateExplosiveBlocks", BsonBoolean.FALSE),
                        t.wave, new ExtraInfo()))
                .build();
    }
}
