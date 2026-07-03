package com.riprod.hexcode.builtin.hexCore.obelisks.importexport;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.interfaces.ObeliskInterface;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;

public class ImportExportObelisk implements ObeliskInterface {

    @Override
    public void onEnterCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            ObeliskBlockComponent obelisk) {

    }
}
