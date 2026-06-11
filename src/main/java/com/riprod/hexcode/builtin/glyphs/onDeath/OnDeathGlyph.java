package com.riprod.hexcode.builtin.glyphs.onDeath;

import java.util.function.BiFunction;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.triggers.TriggerKey;
import com.riprod.hexcode.builtin.triggers.death.DeathPayload;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class OnDeathGlyph extends AbstractTriggerGlyph {

    public static final String ID = "OnDeath";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String triggerKey() {
        return TriggerKey.DEATH;
    }

    @Override
    protected BiFunction<CommandBuffer<EntityStore>, Object, HexVar> payloadProjection() {
        return (buffer, payload) -> {
            if (!(payload instanceof DeathPayload dp)) return null;
            Ref<EntityStore> killer = dp.killer();
            if (killer == null || !killer.isValid()) return null;
            UUIDComponent uuidComp = buffer.getComponent(killer, UUIDComponent.getComponentType());
            if (uuidComp == null) return null;
            return new EntityVar(EntityVar.createRef(uuidComp.getUuid(), killer));
        };
    }
}
