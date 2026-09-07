package com.riprod.hexcode.builtin.hexCore.imbuements.profiles;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class BlockProfile extends StaticSlotProfile {

    @Override
    public boolean writesBlockHolder() {
        return true;
    }

    public static final BuilderCodec<BlockProfile> CODEC =
            slotCodec(BlockProfile.class, BlockProfile::new);
}
