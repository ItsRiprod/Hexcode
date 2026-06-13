package com.riprod.hexcode.builtin.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.triggers.component.TriggerListenerComponent;
import com.riprod.hexcode.core.common.triggers.component.TriggerSubscription;
import com.riprod.hexcode.core.common.triggers.handler.TriggerCallback;
import com.riprod.hexcode.core.common.triggers.handler.TriggerConstructHandler;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.triggers.state.TriggerState;

public abstract class AbstractTriggerGlyph implements GlyphHandler {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public abstract String triggerKey();

    @Nullable
    protected BiFunction<CommandBuffer<EntityStore>, Object, HexVar> payloadProjection() {
        return null;
    }

    protected Ref<EntityStore> resolveSubject(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> caster = hexContext.getCasterRef();
        HexVar slotZero = hexContext.getDefaultVariable();
        if (slotZero instanceof EntityVar ev) {
            Ref<EntityStore> r = ev.getRef(hexContext.getAccessor());
            if (r != null && r.isValid()) return r;
        }
        return caster;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> buffer = hexContext.getAccessor();
        Ref<EntityStore> caster = hexContext.getCasterRef();
        if (caster == null || !caster.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED, "trigger has no caster");
            return;
        }

        Ref<EntityStore> subject = resolveSubject(glyph, hexContext);
        if (subject == null || !subject.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED, "trigger subject invalid");
            return;
        }
        UUIDComponent subjectUuidComp = buffer.getComponent(subject, UUIDComponent.getComponentType());
        if (subjectUuidComp == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED, "trigger subject has no UUID");
            return;
        }
        UUID subjectUuid = subjectUuidComp.getUuid();

        List<String> nextLinks = new ArrayList<>(glyph.getNextLinks());
        if (nextLinks.isEmpty()) return;

        UUID subscriptionId = UUID.randomUUID();
        UUID effectId = UUID.randomUUID();
        TriggerState state = new TriggerState(triggerKey(), subscriptionId, nextLinks);

        HexStatus<TriggerState> construct = new HexStatus<>(
                TriggerConstructHandler.HANDLER_ID, hexContext, effectId, glyph, state);
        HexEffectsComponent existing = buffer.getComponent(caster, HexEffectsComponent.getComponentType());
        if (existing != null) {
            existing.addEffect(effectId, construct);
        } else {
            HexEffectsComponent fresh = new HexEffectsComponent();
            fresh.addEffect(effectId, construct);
            buffer.putComponent(caster, HexEffectsComponent.getComponentType(), fresh);
        }

        TriggerListenerRegistry registry = buffer.getResource(TriggerListenerRegistry.getResourceType());
        if (registry == null) {
            LOGGER.atSevere().log("trigger registry resource missing for key %s", triggerKey());
            return;
        }
        TriggerCallback callback = HexResumeCallback.build(
                hexContext, nextLinks, caster, effectId, payloadProjection());
        TriggerSubscription sub = new TriggerSubscription(
                subscriptionId, triggerKey(), subjectUuid, subject, caster, null, callback, true);
        registry.subscribe(sub);

        buffer.putComponent(subject, TriggerListenerComponent.getComponentType(), new TriggerListenerComponent());
    }
}
