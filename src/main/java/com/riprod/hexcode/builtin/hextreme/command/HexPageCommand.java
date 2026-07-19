package com.riprod.hexcode.builtin.hextreme.command;

import java.util.Set;

import javax.annotation.Nonnull;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.ItemDisplayMetadata;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.utils.HexSlot;

public class HexPageCommand extends AbstractPlayerCommand {

    private static final SingleArgumentType<SavedHexAsset> SAVED_HEX_ASSET =
            new AssetArgumentType<SavedHexAsset, DefaultAssetMap<String, SavedHexAsset>>(
                    "SavedHex", SavedHexAsset.class, "<savedHexId>");

    @Nonnull
    private final RequiredArg<SavedHexAsset> hexArg =
            this.withRequiredArg("hexId", "SavedHex asset id to inscribe on the page", SAVED_HEX_ASSET);

    @Nonnull
    private final OptionalArg<String> nameArg =
            this.withOptionalArg("name", "display name override for the page", ArgTypes.STRING);

    @Nonnull
    private final OptionalArg<Integer> quantityArg =
            this.withOptionalArg("quantity", "number of pages to create", ArgTypes.INTEGER);

    @Nonnull
    private final OptionalArg<String> rarityArg =
            this.withOptionalArg("rarity", "page rarity: Common, Rare or Legendary", ArgTypes.STRING);

    private static final Set<String> RARITIES =
            Set.of("Common", "Rare", "Legendary");

    public HexPageCommand() {
        super("page", "create Spell Page item(s) inscribed with a saved hex");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        SavedHexAsset saved = hexArg.get(context);
        if (saved == null) {
            playerRef.sendMessage(Message.raw("unknown saved hex"));
            return;
        }
        String hexId = saved.getId();

        int quantity = quantityArg.provided(context) ? quantityArg.get(context) : 1;
        if (quantity < 1) quantity = 1;

        String rarity = rarityArg.provided(context) ? rarityArg.get(context) : "Common";
        if (!RARITIES.contains(rarity)) {
            playerRef.sendMessage(Message.raw("unknown rarity '" + rarity + "', expected one of " + RARITIES));
            return;
        }

        ItemStack page = new ItemStack("Hex_Page_" + rarity, 1)
                .withMetadata(PageConfig.METADATA_KEY, Codec.STRING, hexId);
        if (nameArg.provided(context)) {
            page = page.withMetadata(ItemDisplayMetadata.KEYED_CODEC,
                    new ItemDisplayMetadata(Message.raw(nameArg.get(context)), null));
        }

        for (int i = 0; i < quantity; i++) {
            PlayerUtils.addHandItem(store, playerEntityRef, HexSlot.MainHand, page.withQuantity(1));
        }

        playerRef.sendMessage(Message.raw("created " + quantity + " page(s) for hex '" + hexId + "'"));
    }
}
