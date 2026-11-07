package com.riprod.hexcode.builtin.elementalism.elements.water;

import java.util.ArrayList;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector.Selector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.Knockback;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.VelocityUtil;

public class GeyserTier extends ElementalTier {
    public static final String TYPE = "Geyser";
    public static final BuilderCodec<GeyserTier> CODEC;

    private float radius = 1.5f;
    private float height = 4;
    private Knockback knockback;

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
        var transform = buffer.getComponent(target, TransformComponent.getComponentType());
        if (transform == null) {
            return false;
        }
        erupt(new Vector3d(transform.getPosition()), ctx, buffer);
        return true;
    }

    @Override
    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        erupt(new Vector3d(block.x + 0.5, block.y + 1, block.z + 0.5), ctx, buffer);
        return true;
    }

    private void erupt(Vector3d base, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var world = buffer.getExternalData().getWorld();
        var caster = ctx.getRootRef();
        var center = new Vector3d(base).add(0, this.height / 2.0, 0);
        var reach = Math.hypot(this.radius, this.height / 2.0);
        var caught = new ArrayList<Ref<EntityStore>>();
        Selector.selectNearbyEntities(buffer, center, reach, caught::add, ref -> inColumn(ref, base, buffer));
        for (var ref : caught) {
            if (!HexProtection.canAffectEntity(world, caster, buffer, ref)) {
                continue;
            }
            var position = buffer.getComponent(ref, TransformComponent.getComponentType()).getPosition();
            VelocityUtil.knockback(ref, this.knockback.calculateVector(base, 0f, position),
                    this.knockback.getVelocityType(), this.knockback.getVelocityConfig(),
                    this.knockback.getDuration(), buffer);
        }
    }

    private boolean inColumn(Ref<EntityStore> ref, Vector3d base, CommandBuffer<EntityStore> buffer) {
        var transform = buffer.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return false;
        }
        var position = transform.getPosition();
        var dx = position.x - base.x;
        var dz = position.z - base.z;
        var dy = position.y - base.y;
        return dy >= 0 && dy <= this.height && dx * dx + dz * dz <= this.radius * this.radius;
    }

    static {
        CODEC = BuilderCodec.builder(GeyserTier.class, GeyserTier::new, ElementalTier.BASE_CODEC)
                .<Float>append(new KeyedCodec<>("Radius", Codec.FLOAT),
                        (t, v) -> t.radius = v, t -> t.radius)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Horizontal radius of the water column in blocks")
                .add()
                .<Float>append(new KeyedCodec<>("Height", Codec.FLOAT),
                        (t, v) -> t.height = v, t -> t.height)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Height of the water column in blocks, measured up from the feet of the target entity or the top of the target block")
                .add()
                .append(new KeyedCodec<>("Knockback", Knockback.CODEC),
                        (t, v) -> t.knockback = v, t -> t.knockback)
                .addValidator(Validators.nonNull())
                .documentation("Engine knockback given once to every entity inside the column, calculated from the base of the column")
                .add()
                .build();
    }
}
