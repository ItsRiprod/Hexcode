package com.riprod.hexcode.core.common.imbuement.asset.profiles;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class WeaponProfile extends StaticSlotProfile {

    public static final BuilderCodec<WeaponProfile> CODEC =
            slotCodec(WeaponProfile.class, WeaponProfile::new);
}
