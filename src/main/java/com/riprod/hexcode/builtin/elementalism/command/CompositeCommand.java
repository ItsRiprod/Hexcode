package com.riprod.hexcode.builtin.elementalism.command;

import javax.annotation.Nonnull;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeAsset;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeIndex;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeResolver;
import com.riprod.hexcode.builtin.elementalism.effects.EffectPlayer;
import com.riprod.hexcode.builtin.elementalism.element.ElementTierIndex;

public class CompositeCommand extends AbstractPlayerCommand {
    private static final double BLOCK_REACH = 30.0;

    private static final SingleArgumentType<CompositeAsset> COMPOSITE_ASSET =
            new AssetArgumentType<CompositeAsset, DefaultAssetMap<String, CompositeAsset>>(
                    "Composite", CompositeAsset.class, "<compositeId>");

    @Nonnull
    private final RequiredArg<CompositeAsset> compositeArg =
            this.withRequiredArg("compositeId", "composite whose effects to play", COMPOSITE_ASSET);

    @Nonnull
    private final OptionalArg<String> tierArg =
            this.withOptionalArg("tier", "incoming tier id, defaults to the composite's first Incoming", ArgTypes.STRING);

    public CompositeCommand() {
        super("composite", "play a composite's effects on the entity or block you are looking at");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        var composite = this.compositeArg.get(context);
        var tierId = this.tierArg.provided(context) ? this.tierArg.get(context) : composite.getIncoming().getFirst();
        var tier = ElementTierIndex.getTier(tierId);
        if (tier == null) {
            context.sendMessage(Message.raw("No loaded tier '" + tierId + "'"));
            return;
        }
        var indexed = CompositeIndex.lookup(tier.getId()).stream()
                .anyMatch(match -> match.getId().equals(composite.getId()));
        if (!indexed) {
            context.sendMessage(Message.raw("Composite '" + composite.getId() + "' does not trigger on tier '"
                    + tier.getId() + "'"));
            return;
        }

        var entity = TargetUtil.getTargetEntity(ref, store);
        var isEntity = entity != null && entity.isValid();
        var block = isEntity ? null : TargetUtil.getTargetBlock(ref, BLOCK_REACH, store);
        var position = isEntity ? EffectPlayer.entityCenter(entity, store)
                : block != null ? EffectPlayer.blockCenter(block) : null;
        if (position == null) {
            context.sendMessage(Message.raw("Look at an entity or block"));
            return;
        }
        var outcome = isEntity ? composite.getEntity() : composite.getBlock();
        if (outcome == null) {
            context.sendMessage(Message.raw("Composite '" + composite.getId() + "' has no "
                    + (isEntity ? "Entity" : "Block") + " outcome"));
            return;
        }

        var result = CompositeResolver.preview(composite, outcome);
        EffectPlayer.playHit(result.effects(tier), position, isEntity, store);
        context.sendMessage(Message.raw("Played '" + composite.getId() + "' (" + composite.getExisting() + " + "
                + tier.getId() + "), incoming " + (result.incomingApplies() ? "applies" : "is consumed")));
    }
}
