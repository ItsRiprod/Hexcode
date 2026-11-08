package com.riprod.hexcode.core.common.execution.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.hypixel.hytale.codec.Codec;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class CastOverlayMapCodec
        extends InheritProvidedMapCodec<CastOverlay<?>, CastComponentType<?>, Map<String, CastOverlay<?>>> {

    @SuppressWarnings("unchecked")
    public CastOverlayMapCodec() {
        super(HexComponentRegistry.getOverlayIdView(),
                type -> (Codec<CastOverlay<?>>) (Codec<?>) HexComponentRegistry.getOverlayCodec(type),
                LinkedHashMap::new);
    }
}
