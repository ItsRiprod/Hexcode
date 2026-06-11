package com.riprod.hexcode.core.common.imbuement.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.imbuement.ImbuementMetadata;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedArmorMarker;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedHotbarMarker;

public final class ImbuementMarkerSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {

    public ImbuementMarkerSystem() {
        super(InventoryChangeEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(int index,
            @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull InventoryChangeEvent event) {
        ComponentType<EntityStore, ? extends InventoryComponent> changedType = event.getComponentType();
        Ref<EntityStore> ref = chunk.getReferenceTo(index);

        if (changedType == InventoryComponent.Armor.getComponentType()) {
            updateMarker(buffer, ref, event.getItemContainer(), ImbuedArmorMarker.getComponentType(), new ImbuedArmorMarker());
        } else if (changedType == InventoryComponent.Hotbar.getComponentType()) {
            updateMarker(buffer, ref, event.getItemContainer(), ImbuedHotbarMarker.getComponentType(), new ImbuedHotbarMarker());
        }
    }

    private static <C extends Component<EntityStore>> void updateMarker(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            ItemContainer container, ComponentType<EntityStore, C> markerType, C newMarker) {
        if (containerHasImbuement(container)) {
            buffer.putComponent(ref, markerType, newMarker);
        } else {
            buffer.tryRemoveComponent(ref, markerType);
        }
    }

    private static boolean containerHasImbuement(ItemContainer container) {
        if (container == null) return false;
        short capacity = container.getCapacity();
        for (short i = 0; i < capacity; i++) {
            ItemStack stack = container.getItemStack(i);
            if (stack == null || stack.isEmpty()) continue;
            if (stack.getFromMetadataOrNull(ImbuementMetadata.KEY, ImbuementMetadata.CODEC) != null) return true;
        }
        return false;
    }
}
