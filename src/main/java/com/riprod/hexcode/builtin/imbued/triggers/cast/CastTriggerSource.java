package com.riprod.hexcode.builtin.imbued.triggers.cast;

import java.util.function.Consumer;

import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;

public class CastTriggerSource implements Consumer<CastEvent> {

    @Override
    public void accept(CastEvent event) {
        var context = event.getContext();
        var buffer = context.getAccessor();
        TriggerListenerRegistry registry = buffer.getResource(TriggerListenerRegistry.getResourceType());
        if (registry == null || registry.countListeners(TriggerKey.CAST) == 0) return;

        var caster = context.getRootRef();
        if (!caster.isValid()) return;
        UUIDComponent uuidComp = buffer.getComponent(caster, UUIDComponent.getComponentType());
        if (uuidComp == null) return;

        CastPayload payload = new CastPayload(caster);
        registry.fire(buffer, new TriggerEvent(TriggerKey.CAST, uuidComp.getUuid(), caster, payload));
    }
}
