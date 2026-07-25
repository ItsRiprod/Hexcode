package com.riprod.hexcode.core.common.execution.condition;

import java.time.Instant;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.interactions.HexCastHoldInteraction;

public class HexHoldingCondition extends Condition {

    @Nonnull
    public static final BuilderCodec<HexHoldingCondition> CODEC = BuilderCodec
            .builder(HexHoldingCondition.class, HexHoldingCondition::new, BASE_CODEC)
            .build();

    protected HexHoldingCondition() {
    }

    @Override
    public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref,
            @Nonnull Instant currentTime) {
        var interactionManager = componentAccessor.getComponent(ref,
                InteractionModule.get().getInteractionManagerComponent());
        if (interactionManager == null) {
            return false;
        }

        return interactionManager.forEachInteraction((chain, interaction, val) -> {
            if (val) {
                return Boolean.TRUE;
            }
            return Boolean.valueOf(chainHasCastHold(chain.getRootInteraction()));
        }, Boolean.FALSE);
    }

    private static boolean chainHasCastHold(@Nonnull RootInteraction root) {
        for (int i = 0; i < root.getOperationMax(); i++) {
            var operation = root.getOperation(i);
            if (operation != null && operation.getInnerOperation() instanceof HexCastHoldInteraction) {
                return true;
            }
        }
        return false;
    }
}
