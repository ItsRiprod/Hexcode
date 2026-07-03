package com.riprod.hexcode.command.hex;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexWriter;

public class HexSaveCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<String> nameArg =
            this.withRequiredArg("name", "saved hex name (pascal case recommended)", ArgTypes.STRING);

    @Nonnull
    private final OptionalArg<String> packArg =
            this.withOptionalArg("pack", "target asset pack name", ArgTypes.STRING);

    @Nonnull
    private final OptionalArg<String> displayNameArg =
            this.withOptionalArg("displayName", "human-friendly display name", ArgTypes.STRING);

    @Nonnull
    private final OptionalArg<String> descriptionArg =
            this.withOptionalArg("description", "short description", ArgTypes.STRING);

    public HexSaveCommand() {
        super("save", "save the active hex on your staff as a reusable preset");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        if (!packArg.provided(context)) {
            playerRef.sendMessage(Message.raw("usage: /hc save <name> --pack=<packName> [--displayName=...] [--description=...]"));
            playerRef.sendMessage(Message.raw("a writable pack name is required; see server log on failure for the list"));
            return;
        }

        var execComp = store.getComponent(playerEntityRef, ExecutionComponent.getComponentType());
        if (execComp == null) {
            playerRef.sendMessage(Message.raw("no execution component found on player"));
            return;
        }

        var activeHex = execComp.getQueuedHex();
        if (activeHex == null) {
            playerRef.sendMessage(Message.raw("no active hex on your staff - nothing to save"));
            return;
        }

        String rawName = nameArg.get(context);
        String id = normalizeId(rawName);
        if (id.isEmpty()) {
            playerRef.sendMessage(Message.raw("invalid name"));
            return;
        }

        String displayName = displayNameArg.provided(context) ? displayNameArg.get(context) : rawName;
        String description = descriptionArg.provided(context) ? descriptionArg.get(context) : null;

        SavedHexAsset asset = new SavedHexAsset(id, activeHex.clone(), displayName, description, null);

        SavedHexWriter.Result result = SavedHexWriter.writeSavedHex(packArg.get(context), asset);
        if (!result.success) {
            playerRef.sendMessage(Message.raw("failed to save: " + result.error));
            return;
        }

        playerRef.sendMessage(Message.raw("saved hex '" + id + "' to " + result.path));
        playerRef.sendMessage(Message.raw("reload assets for it to be referenceable in imbuements"));
    }

    private static String normalizeId(String raw) {
        if (raw == null) return "";
        StringBuilder out = new StringBuilder(raw.length());
        boolean nextUpper = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                out.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            } else {
                if (out.length() > 0 && out.charAt(out.length() - 1) != '_') {
                    out.append('_');
                }
                nextUpper = true;
            }
        }
        while (out.length() > 0 && out.charAt(out.length() - 1) == '_') {
            out.deleteCharAt(out.length() - 1);
        }
        return out.toString();
    }
}
