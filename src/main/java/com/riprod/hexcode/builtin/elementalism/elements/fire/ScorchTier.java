package com.riprod.hexcode.builtin.elementalism.elements.fire;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;

public class ScorchTier extends ElementalTier {
    public static final String TYPE = "Scorch";
    public static final BuilderCodec<ScorchTier> CODEC;

    private float damage;
    private String damageCause;
    private float blockDamage = 5;
    @Nullable
    private ItemTool itemTool;

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
        var cause = DamageCause.getAssetMap().getAsset(this.damageCause);
        if (cause == null
                || !HexProtection.canAffectEntity(buffer.getExternalData().getWorld(), caster, buffer, target)) {
            return false;
        }
        var hit = new Damage(damageSource(ctx), cause, this.damage);
        DamageSystems.executeDamage(target, buffer, hit);
        return !hit.isCancelled();
    }

    @Override
    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var world = buffer.getExternalData().getWorld();
        var caster = ctx.getRootRef();
        var position = new Vector3i(block);
        if (!HexProtection.canModifyBlock(world, caster, buffer, position, BlockAction.BREAK)) {
            return false;
        }
        var blockType = BlockAccess.blockType(world, position);
        var sectionRef = BlockAccess.sectionRef(world, position);
        if (blockType == null || blockType.getGathering() == null || sectionRef == null) {
            return false;
        }
        BlockHarvestUtils.performBlockDamage(caster.isValid() ? caster : null, null, position, null,
                this.itemTool, null, false, this.blockDamage, SetBlockSettings.NONE, false, sectionRef,
                buffer, world.getChunkStore().getStore());
        return true;
    }

    static {
        CODEC = BuilderCodec.builder(ScorchTier.class, ScorchTier::new, ElementalTier.BASE_CODEC)
                .<Float>append(new KeyedCodec<>("Damage", Codec.FLOAT),
                        (t, v) -> t.damage = v, t -> t.damage)
                .addValidator(Validators.min(0.0f))
                .documentation("Damage dealt to the entity that is hit")
                .add()
                .append(new KeyedCodec<>("DamageCause", DamageCause.CHILD_ASSET_CODEC),
                        (t, v) -> t.damageCause = v, t -> t.damageCause)
                .addValidator(Validators.nonNull())
                .addValidator(DamageCause.VALIDATOR_CACHE.getValidator())
                .documentation("Damage cause of the hit, attributed to the caster")
                .add()
                .<Float>append(new KeyedCodec<>("BlockDamage", Codec.FLOAT),
                        (t, v) -> t.blockDamage = v, t -> t.blockDamage)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Damage scale applied to the block that is hit. Soft blocks such as foliage break in one hit")
                .add()
                .append(new KeyedCodec<>("ItemTool", ItemTool.CODEC),
                        (t, v) -> t.itemTool = v, t -> t.itemTool)
                .documentation("Tool specs used to damage the block. When unset each gather type uses its default spec")
                .add()
                .build();
    }
}
