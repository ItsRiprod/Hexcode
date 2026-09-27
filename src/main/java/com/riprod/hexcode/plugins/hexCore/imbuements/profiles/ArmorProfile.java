package com.riprod.hexcode.plugins.hexCore.imbuements.profiles;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class ArmorProfile extends StaticSlotProfile {

    public static final BuilderCodec<ArmorProfile> CODEC =
            slotCodec(ArmorProfile.class, ArmorProfile::new);
}
