package com.riprod.hexcode.core.common.imbuement.asset.profiles;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class ArmorProfile extends StaticSlotProfile {

    public static final BuilderCodec<ArmorProfile> CODEC =
            slotCodec(ArmorProfile.class, ArmorProfile::new);
}
