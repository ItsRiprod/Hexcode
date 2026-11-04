package com.riprod.hexcode.builtin.elementalism.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.composite.action.CompositeContext;
import com.riprod.hexcode.builtin.elementalism.effects.ElementEffects;
import com.riprod.hexcode.builtin.elementalism.effects.ResolvedEffects;
import com.riprod.hexcode.builtin.elementalism.element.ElementTierIndex;
import com.riprod.hexcode.builtin.elementalism.status.ElementStatuses;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.utils.LogScopes;

public final class CompositeResolver {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.ELEMENT);
    private static final Result NONE = new Result(List.of(), true, null);

    private CompositeResolver() {
    }

    public record Result(List<CompositeAsset> fired, boolean incomingApplies, @Nullable ElementEffects overrides) {
        public ResolvedEffects effects(ElementalTier incoming) {
            var element = ElementTierIndex.getElement(incoming.getId());
            return ElementEffects.resolve(this.overrides, incoming.getEffects(),
                    element != null ? element.getEffects() : null);
        }
    }

    public static Result resolveEntity(Ref<EntityStore> entity, ElementalTier incoming, HexContext context,
            CommandBuffer<EntityStore> buffer) {
        return resolve(CompositeAsset::getEntity, statusId -> ElementStatuses.hasEntity(entity, statusId, buffer),
                composite -> new CompositeContext(composite, composite.getExisting(), incoming, context, buffer,
                        entity, null),
                statusId -> ElementStatuses.removeEntity(entity, statusId, buffer), incoming);
    }

    public static Result resolveBlock(Vector3i block, ElementalTier incoming, HexContext context,
            CommandBuffer<EntityStore> buffer) {
        var world = buffer.getExternalData().getWorld();
        return resolve(CompositeAsset::getBlock, statusId -> ElementStatuses.hasBlock(world, block, statusId),
                composite -> new CompositeContext(composite, composite.getExisting(), incoming, context, buffer,
                        null, block),
                statusId -> ElementStatuses.removeBlock(world, block, statusId), incoming);
    }

    public static Result preview(CompositeAsset composite, CompositeOutcome outcome) {
        return result(List.of(composite), List.of(outcome));
    }

    private static Result resolve(Function<CompositeAsset, CompositeOutcome> outcomeOf, Predicate<String> present,
            Function<CompositeAsset, CompositeContext> contextOf, Consumer<String> remove,
            ElementalTier incoming) {
        var matching = new ArrayList<CompositeAsset>();
        for (var composite : CompositeIndex.lookup(incoming.getId())) {
            if (outcomeOf.apply(composite) != null && present.test(composite.getExisting())) {
                matching.add(composite);
            }
        }
        if (matching.isEmpty()) {
            return NONE;
        }
        var fired = new ArrayList<CompositeAsset>();
        var outcomes = new ArrayList<CompositeOutcome>();
        for (var composite : matching) {
            var outcome = outcomeOf.apply(composite);
            if (fire(outcome, contextOf.apply(composite), remove)) {
                fired.add(composite);
                outcomes.add(outcome);
            }
        }
        return fired.isEmpty() ? NONE : result(fired, outcomes);
    }

    private static boolean fire(CompositeOutcome outcome, CompositeContext context,
            Consumer<String> remove) {
        try {
            for (var action : outcome.getActions()) {
                if (!action.canApply(context)) {
                    return false;
                }
            }
            for (var action : outcome.getActions()) {
                action.apply(context);
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Composite '%s' failed: %s",
                    context.composite().getId(), e.getMessage());
            return false;
        }
        if (outcome.getExisting() == Retention.REMOVE) {
            remove.accept(context.existingStatusId());
        }
        return true;
    }

    private static Result result(List<CompositeAsset> fired, List<CompositeOutcome> outcomes) {
        var incomingApplies = true;
        var layers = new ArrayList<ElementEffects>(outcomes.size());
        for (var outcome : outcomes) {
            incomingApplies &= outcome.getIncoming() == Retention.KEEP;
            layers.add(outcome.getEffects());
        }
        return new Result(List.copyOf(fired), incomingApplies, ElementEffects.stack(layers));
    }
}
