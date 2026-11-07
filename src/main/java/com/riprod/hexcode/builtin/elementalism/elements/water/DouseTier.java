package com.riprod.hexcode.builtin.elementalism.elements.water;

import java.time.temporal.ChronoUnit;

import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.status.ElementStatuses;
import com.riprod.hexcode.builtin.elementalism.tier.EffectElement;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;

public class DouseTier extends EffectElement {
    public static final String TYPE = "Douse";
    public static final BuilderCodec<DouseTier> CODEC;

    private float duration = 10;
    private float radius = 2;
    private long hydrateDuration = 300;

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
        var caster = ctx.getRootRef();
        if (!HexProtection.canAffectEntity(buffer.getExternalData().getWorld(), caster, buffer, target)) {
            return false;
        }
        return ElementStatuses.applyEntity(target, getStatusId(), this.duration, caster.isValid() ? caster : null,
                buffer);
    }

    @Override
    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var world = buffer.getExternalData().getWorld();
        var caster = ctx.getRootRef();
        var wateredUntil = buffer.getResource(WorldTimeResource.getResourceType()).getGameTime()
                .plus(this.hydrateDuration, ChronoUnit.SECONDS);
        var cursor = new BlockAccess.Cursor(world);
        var changed = false;
        for (var cell : WaterSweep.cells(block, this.radius)) {
            changed |= WaterSweep.extinguish(cursor, cell, caster, buffer);
            changed |= WaterSweep.hydrate(cursor, cell, wateredUntil, caster, buffer);
        }
        var wet = ElementStatuses.applyBlock(world, new Vector3i(block), getStatusId(), this.duration);
        return changed || wet;
    }

    static {
        CODEC = BuilderCodec.builder(DouseTier.class, DouseTier::new, EffectElement.BASE_CODEC)
                .<Float>append(new KeyedCodec<>("Duration", Codec.FLOAT),
                        (t, v) -> t.duration = v, t -> t.duration)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Seconds of the status added to the entity or block that is doused")
                .add()
                .<Float>append(new KeyedCodec<>("Radius", Codec.FLOAT),
                        (t, v) -> t.radius = v, t -> t.radius)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Radius in blocks around a doused block in which fire is put out and farmland is watered")
                .add()
                .<Long>append(new KeyedCodec<>("HydrateDuration", Codec.LONG),
                        (t, v) -> t.hydrateDuration = v, t -> t.hydrateDuration)
                .addValidator(Validators.greaterThan(0L))
                .documentation("Game seconds farmland stays watered, matching the watering can's Duration")
                .add()
                .build();
    }
}
