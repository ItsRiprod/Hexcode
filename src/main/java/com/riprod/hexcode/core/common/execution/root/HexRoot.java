package com.riprod.hexcode.core.common.execution.root;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

import javax.annotation.Nullable;

public interface HexRoot {

    CodecMapCodec<HexRoot> CODEC = new CodecMapCodec<>("Type");
    BuilderCodec<HexRoot> BASE_CODEC = BuilderCodec.abstractBuilder(HexRoot.class).build();

    boolean isAlive();
    Ref<EntityStore> getSourceRef(ComponentAccessor<EntityStore> accessor);
    boolean tryConsumeMana(float cost, ComponentAccessor<EntityStore> accessor);
    float getCurrentMana(ComponentAccessor<EntityStore> accessor);
    boolean addMana(float amount, ComponentAccessor<EntityStore> accessor);

    @Nullable
    HexVar getRootVar(HexContext ctx);

    HexRoot copy();

}
