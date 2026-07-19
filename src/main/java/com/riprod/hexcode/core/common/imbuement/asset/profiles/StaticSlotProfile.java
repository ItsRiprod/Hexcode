package com.riprod.hexcode.core.common.imbuement.asset.profiles;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementSlotKeyValidator;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

// profiles whose available slots are authored directly in JSON (weapon triggers, block/armor slots)
public abstract class StaticSlotProfile extends ImbuementProfileAsset {

    protected Map<String, PedestalSlot> slots = new LinkedHashMap<>();

    @Override
    public Map<String, PedestalSlot> resolveSlots(@Nullable ItemStack stored) {
        return slots;
    }

    protected static <T extends StaticSlotProfile> BuilderCodec<T> slotCodec(Class<T> cls, Supplier<T> ctor) {
        return BuilderCodec
                .builder(cls, ctor, ImbuementProfileAsset.BASE_CODEC)
                .append(new KeyedCodec<>("Slots",
                        new MapCodec<>(PedestalSlot.CODEC, LinkedHashMap::new, false)),
                        (a, v) -> { if (v != null) a.slots = new LinkedHashMap<>(v); },
                        a -> a.slots)
                .documentation("Slot key -> PedestalSlot. Insertion order drives radial layout. Keys must match a registered Trigger id (TriggerRegistry).")
                .addValidatorLate(() -> ImbuementSlotKeyValidator.INSTANCE.late())
                .add()
                .build();
    }
}
