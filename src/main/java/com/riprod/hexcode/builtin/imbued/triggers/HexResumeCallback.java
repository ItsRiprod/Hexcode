package com.riprod.hexcode.builtin.imbued.triggers;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.triggers.handler.TriggerCallback;
import com.riprod.hexcode.core.common.triggers.state.TriggerState;

public final class HexResumeCallback {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private HexResumeCallback() {
    }

    public static TriggerCallback build(@Nonnull HexContext hexContext,
                                        @Nonnull List<String> nextGlyphIds,
                                        @Nonnull Ref<EntityStore> constructHostRef,
                                        @Nonnull UUID effectId,
                                        @Nullable BiFunction<CommandBuffer<EntityStore>, Object, HexVar> projection) {
        return (buffer, sub, event) -> {
            try {
                hexContext.updateRuntimeAccessors(buffer);

                if (projection != null) {
                    HexVar projected = projection.apply(buffer, event.payload());
                    if (projected != null) {
                        hexContext.setDefaultVariable(projected);
                    }
                }

                HexEffectsComponent effects = buffer.getComponent(constructHostRef, HexEffectsComponent.getComponentType());
                if (effects != null) {
                    HexStatus<?> status = effects.getEffects().get(effectId);
                    if (status != null && status.getState() instanceof TriggerState ts) {
                        ts.markFired();
                    }
                }

                HexExecuter.continueExecution(nextGlyphIds, hexContext);
            } catch (Exception e) {
                LOGGER.atSevere().log("hex resume failed: %s", e.getMessage());
            }
        };
    }
}
