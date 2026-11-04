package com.riprod.hexcode.builtin.elementalism.charge;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeResolver;
import com.riprod.hexcode.builtin.elementalism.effects.EffectPlayer;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;
import com.riprod.hexcode.builtin.elementalism.reclaim.ElementReclaimEvent;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.utils.LogScopes;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;

public final class ElementDischarge {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.ELEMENT);

    private ElementDischarge() {
    }

    public static void discharge(Object2FloatMap<String> charges, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var target = ctx.getDefaultVariable();
        var entity = entityOf(target, buffer);
        var block = entity == null ? blockOf(target) : null;
        for (var entry : charges.object2FloatEntrySet()) {
            discharge(entry.getKey(), entry.getFloatValue(), entity, block, ctx, buffer);
        }
    }

    private static void discharge(String elementId, float charge, @Nullable Ref<EntityStore> entity,
            @Nullable Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        var element = ElementAsset.getAssetMap().getAsset(elementId);
        if (element == null) {
            reclaim(ctx, elementId, charge, buffer);
            return;
        }
        var reached = element.getTiers().tierFor(charge);
        var landed = reached != null && land(reached.tier(), entity, block, ctx, buffer);
        LOGGER.atFine().log("Discharged %.2f %s as tier %s on %s, landed=%s", charge, element.getId(),
                reached != null ? reached.tier().getId() : "none", entity != null ? entity : block, landed);
        var leftover = landed ? charge - reached.threshold() : charge;
        reclaim(ctx, element.getId(), leftover * element.getReclaimEfficiency(), buffer);
    }

    private static boolean land(ElementalTier tier, @Nullable Ref<EntityStore> entity, @Nullable Vector3i block,
            HexContext ctx, CommandBuffer<EntityStore> buffer) {
        try {
            if (entity != null && tier.supportsEntity() && entity.isValid()) {
                return land(tier, CompositeResolver.resolveEntity(entity, tier, ctx, buffer),
                        () -> tier.applyEntity(entity, ctx, buffer), () -> EffectPlayer.entityCenter(entity, buffer),
                        true, buffer);
            }
            if (block != null && tier.supportsBlock()) {
                return land(tier, CompositeResolver.resolveBlock(block, tier, ctx, buffer),
                        () -> tier.applyBlock(block, ctx, buffer), () -> EffectPlayer.blockCenter(block),
                        false, buffer);
            }
            return false;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Composites for tier '%s' failed to resolve: %s", tier.getId(),
                    e.getMessage());
            return false;
        }
    }

    private static boolean land(ElementalTier tier, CompositeResolver.Result result, BooleanSupplier apply,
            Supplier<Vector3d> center, boolean entity, CommandBuffer<EntityStore> buffer) {
        var effects = result.effects(tier);
        var landed = !result.fired().isEmpty();
        if (result.incomingApplies()) {
            try {
                landed |= apply.getAsBoolean();
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Tier '%s' failed to discharge: %s", tier.getId(), e.getMessage());
            }
        }
        var position = landed ? center.get() : null;
        if (position != null) {
            EffectPlayer.playHit(effects, position, entity, buffer);
        }
        return landed;
    }

    private static void reclaim(HexContext ctx, String elementId, float amount, CommandBuffer<EntityStore> buffer) {
        if (!(amount > 0f)) {
            return;
        }
        LOGGER.atFine().log("Reclaimed %.2f %s", amount, elementId);
        buffer.invoke(new ElementReclaimEvent(ctx.getRootRef(), elementId, amount));
    }

    @Nullable
    private static Ref<EntityStore> entityOf(@Nullable HexVar target, CommandBuffer<EntityStore> buffer) {
        if (!(target instanceof EntityVar entity)) {
            return null;
        }
        var ref = entity.getRef(buffer);
        return ref != null && ref.isValid() ? ref : null;
    }

    @Nullable
    private static Vector3i blockOf(@Nullable HexVar target) {
        return switch (target) {
            case BlockVar block -> block.getValue() != null ? new Vector3i(block.getValue()) : null;
            case PositionVar position -> {
                var at = position.getValue();
                yield at != null
                        ? new Vector3i(MathUtil.floor(at.x), MathUtil.floor(at.y), MathUtil.floor(at.z))
                        : null;
            }
            case null, default -> null;
        };
    }
}
