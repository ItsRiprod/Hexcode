package com.riprod.hexcode.builtin.hexCore.imbuements.profiles;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class WeaponProfile extends StaticSlotProfile {

    public static final BuilderCodec<WeaponProfile> CODEC =
            slotCodec(WeaponProfile.class, WeaponProfile::new);
}
