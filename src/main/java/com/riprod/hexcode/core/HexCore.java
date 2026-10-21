package com.riprod.hexcode.core;

import java.util.function.Consumer;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorActivateButtonEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorRequestDataSetEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorSelectAssetEvent;
import com.hypixel.hytale.builtin.beam.asset.Beam;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.utils.CraftingGlyphEditorSpawn;
import com.riprod.hexcode.command.HexcodeCommand;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.core.common.construct.system.HexConstructSystem;
import com.riprod.hexcode.core.common.construct.system.HexConstructTeardownSystem;
import com.riprod.hexcode.core.common.construct.system.MountOrphanReaperSystem;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.interactions.HexContextAbility;
import com.riprod.hexcode.core.common.context.interactions.HexContextPrimary;
import com.riprod.hexcode.core.common.drawing.DrawAnchorSystem;
import com.riprod.hexcode.core.common.drawing.DrawModeLifecycleSystem;
import com.riprod.hexcode.core.common.drawing.DrawRecognitionSystem;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.interactions.HexDraw;
import com.riprod.hexcode.core.common.drawing.interactions.HexDrawMode;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.drawing.registry.TemplateAsset;
import com.riprod.hexcode.core.common.effect.GlyphEffectSystem;
import com.riprod.hexcode.core.common.execution.cast.CastRegistryProxy;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.component.CastDependenciesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastManaComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastPolicyComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastVariablesComponent;
import com.riprod.hexcode.core.common.execution.cast.component.GlyphBudgetComponent;
import com.riprod.hexcode.core.common.execution.cast.component.ResourcePoolComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.execution.condition.HexHoldingCondition;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.interactions.HexCastHoldInteraction;
import com.riprod.hexcode.core.common.execution.interactions.HexDispel;
import com.riprod.hexcode.core.common.execution.interactions.HexExecuteInteraction;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;
import com.riprod.hexcode.core.common.execution.root.BlockHexRoot;
import com.riprod.hexcode.core.common.execution.root.HexRoot;
import com.riprod.hexcode.core.common.execution.root.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.system.CastBookStyleSystem;
import com.riprod.hexcode.core.common.execution.system.CastChargesSystem;
import com.riprod.hexcode.core.common.execution.system.CastDecaySystem;
import com.riprod.hexcode.core.common.execution.system.CastSpellPowerSystem;
import com.riprod.hexcode.core.common.execution.system.CasterSpellTeardownSystem;
import com.riprod.hexcode.core.common.execution.system.HexCastEventSystem;
import com.riprod.hexcode.core.common.execution.system.HexExecutionTickSystem;
import com.riprod.hexcode.core.common.execution.system.HexQueueDrainEventSystem;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.icon.GlyphEditorPreview;
import com.riprod.hexcode.core.common.glyphs.icon.GlyphIconEditorButton;
import com.riprod.hexcode.core.common.glyphs.icon.GlyphIconStore;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexes.codec.HexCacheResource;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.system.HoverableSpatialSystem;
import com.riprod.hexcode.core.common.imbuement.asset.EssenceAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.block.ImbuedBlockBreakHandler;
import com.riprod.hexcode.core.common.imbuement.block.ImbuedBlockTickSystem;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedBlockComponent;
import com.riprod.hexcode.core.common.memories.GlyphMemory;
import com.riprod.hexcode.core.common.memories.GlyphMemoryProvider;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.events.ObeliskBreakEvent;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalBlockEvent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalPlaceEvent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionTickSystem;
import com.riprod.hexcode.core.common.pedestal.system.SessionRecoverySystem;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.redirect.EntityRedirectComponent;
import com.riprod.hexcode.core.common.triggers.registry.FireTriggerSystem;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.common.utilities.resource.DebugMessageQueue;
import com.riprod.hexcode.core.common.utilities.system.DebugMessageFlushSystem;
import com.riprod.hexcode.core.common.utilities.system.DebugTickSystem;
import com.riprod.hexcode.interaction.PedestalInteraction;

public class HexCore {
    private static final CastRegistryProxy castRegistry = new CastRegistryProxy(HexCast.REGISTRY);

    public static void setup(JavaPlugin plugin) {

        registerCastComponents(plugin);
        registerAssets(plugin);
        registerEntityComponents(plugin);
        registerBlockComponents(plugin);
        registerHexContent(plugin);
        registerInteractions(plugin);
        registerConditions(plugin);
        registerEvents(plugin);
        registerCommands(plugin);
    }

    public static void shutdown(JavaPlugin plugin) {
        castRegistry.unregisterAll();

    }

    private static void registerCastComponents(JavaPlugin plugin) {
        VolatilityComponent.setComponentType(castRegistry.registerComponent(
                VolatilityComponent.class, "Volatility",
                VolatilityComponent::new, VolatilityComponent.CODEC));
        ResourcePoolComponent.setComponentType(castRegistry.registerComponent(
                ResourcePoolComponent.class, "Resources",
                ResourcePoolComponent::new, ResourcePoolComponent.Seeds.CODEC));
        CastPolicyComponent.setComponentType(castRegistry.registerComponent(
                CastPolicyComponent.class, "Policy",
                CastPolicyComponent::new, CastPolicyComponent.Overlay.CODEC));
        GlyphBudgetComponent.setComponentType(castRegistry.registerComponent(
                GlyphBudgetComponent.class, GlyphBudgetComponent::new));
        CastDependenciesComponent.setComponentType(castRegistry.registerComponent(
                CastDependenciesComponent.class, CastDependenciesComponent::new));
        CastVariablesComponent.setComponentType(castRegistry.registerComponent(
                CastVariablesComponent.class, CastVariablesComponent::new));
        CastManaComponent.setComponentType(castRegistry.registerComponent(
                CastManaComponent.class, "Mana",
                CastManaComponent::new, CastManaComponent.CODEC));
    }

    @SuppressWarnings("null")
    private static void registerAssets(JavaPlugin plugin) {
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(NodeConfig.class,
                                new DefaultAssetMap<String, NodeConfig>())
                        .setPath("Hexcode/NodeConfigs")
                        .setCodec(NodeConfig.CODEC)
                        .setKeyFunction(NodeConfig::getId)
                        .loadsAfter(Beam.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(HexStyleAsset.class,
                                new DefaultAssetMap<String, HexStyleAsset>())
                        .setPath("Hexcode/HexStyles")
                        .setCodec(HexStyleAsset.CODEC)
                        .setKeyFunction(HexStyleAsset::getId)
                        .loadsAfter(ParticleSystem.class)
                        .loadsAfter(SoundEvent.class)
                        .loadsAfter(ModelAsset.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(HexConfigAsset.class,
                                new DefaultAssetMap<String, HexConfigAsset>())
                        .setPath("Hexcode/Execution/HexConfig")
                        .setCodec(HexConfigAsset.CODEC)
                        .setKeyFunction(HexConfigAsset::getId)
                        .loadsAfter(HexStyleAsset.class)
                        .loadsBefore(Item.class, Interaction.class, RootInteraction.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(GlyphAsset.class, new DefaultAssetMap<String, GlyphAsset>())
                        .setPath("Hexcode/Glyphs")
                        .setCodec(GlyphAsset.CODEC)
                        .setKeyFunction(GlyphAsset::getId)
                        .loadsAfter(NodeConfig.class)
                        .loadsAfter(HexStyleAsset.class)
                        .loadsAfter(ParticleSystem.class)
                        .loadsAfter(SoundEvent.class)
                        .loadsAfter(ModelAsset.class)
                        .loadsAfter(ShapeAsset.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(ShapeAsset.class, new DefaultAssetMap<String, ShapeAsset>())
                        .setPath("Hexcode/Shapes")
                        .setCodec(ShapeAsset.CODEC)
                        .setKeyFunction(ShapeAsset::getId)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(TemplateAsset.class,
                                new DefaultAssetMap<String, TemplateAsset>())
                        .setPath("Hexcode/Templates")
                        .setCodec(TemplateAsset.CODEC)
                        .setKeyFunction(TemplateAsset::getId)
                        .loadsAfter(ShapeAsset.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(HexBookAsset.class,
                                new DefaultAssetMap<String, HexBookAsset>())
                        .setPath("Hexcode/HexBooks")
                        .setCodec(HexBookAsset.CODEC)
                        .setKeyFunction(HexBookAsset::getId)
                        .loadsAfter(ParticleSystem.class)
                        .loadsAfter(Item.class)
                        .loadsAfter(HexStyleAsset.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(SavedHexAsset.class,
                                new DefaultAssetMap<String, SavedHexAsset>())
                        .setPath("Hexcode/SavedHexes")
                        .setCodec(SavedHexAsset.CODEC)
                        .setKeyFunction(SavedHexAsset::getId)
                        .loadsAfter(GlyphAsset.class)
                        .loadsAfter(NodeConfig.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(ImbuementProfileAsset.class,
                                new DefaultAssetMap<String, ImbuementProfileAsset>())
                        .setPath("Hexcode/Imbuement/Profiles")
                        .setCodec(ImbuementProfileAsset.CODEC)
                        .setKeyFunction(ImbuementProfileAsset::getId)
                        .loadsAfter(NodeConfig.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(EssenceAsset.class,
                                new DefaultAssetMap<String, EssenceAsset>())
                        .setPath("Hexcode/Imbuement/Essences")
                        .setCodec(EssenceAsset.CODEC)
                        .setKeyFunction(EssenceAsset::getId)
                        .loadsAfter(HexStyleAsset.class)
                        .loadsAfter(Item.class)
                        .build());

    }

    private static void registerEntityComponents(JavaPlugin plugin) {

        var entityStoreRegistry = plugin.getEntityStoreRegistry();

        var glyphComponentType = entityStoreRegistry.registerComponent(
                GlyphComponent.class, "Glyph",
                GlyphComponent.CODEC);
        GlyphComponent.setComponentType(glyphComponentType);

        var hexComponentType = entityStoreRegistry.registerComponent(
                HexComponent.class, "Hex",
                HexComponent.CODEC);
        HexComponent.setComponentType(hexComponentType);

        var casterStateComponentType = entityStoreRegistry
                .registerComponent(CasterStateComponent.class,
                        CasterStateComponent::new);
        CasterStateComponent.setComponentType(casterStateComponentType);

        var casterComponentType = entityStoreRegistry
                .registerComponent(CasterComponent.class, CasterComponent::new);
        CasterComponent.setComponentType(casterComponentType);

        var hexcodeComponentType = entityStoreRegistry
                .registerComponent(HexcodeComponent.class, HexcodeComponent::new);
        HexcodeComponent.setComponentType(hexcodeComponentType);

        var entityRedirectComponentType = entityStoreRegistry
                .registerComponent(EntityRedirectComponent.class, EntityRedirectComponent::new);
        EntityRedirectComponent.setComponentType(entityRedirectComponentType);

        var drawCaptureComponentType = entityStoreRegistry
                .registerComponent(DrawCaptureComponent.class, DrawCaptureComponent::new);
        DrawCaptureComponent.setComponentType(drawCaptureComponentType);

        var queuedExecutionComponentType = entityStoreRegistry
                .registerComponent(ExecutionComponent.class, ExecutionComponent::new);
        ExecutionComponent.setComponentType(queuedExecutionComponentType);

        var craftingRootComponentType = entityStoreRegistry
                .registerComponent(HexcasterCraftingComponent.class,
                        HexcasterCraftingComponent::new);
        HexcasterCraftingComponent.setComponentType(craftingRootComponentType);

        var sessionComponentType = entityStoreRegistry
                .registerComponent(HexcodeSessionComponent.class, "HexcodeSession",
                        HexcodeSessionComponent.CODEC);
        HexcodeSessionComponent.setComponentType(sessionComponentType);

        var nodeComponentType = entityStoreRegistry
                .registerComponent(NodeComponent.class,
                        NodeComponent::new);
        NodeComponent.setComponentType(nodeComponentType);

        var slotComponentType = entityStoreRegistry
                .registerComponent(SlotComponent.class, SlotComponent::new);
        SlotComponent.setComponentType(slotComponentType);

        var hoverableComponentType = entityStoreRegistry
                .registerComponent(HoverableComponent.class, HoverableComponent::new);
        HoverableComponent.setComponentType(hoverableComponentType);

        var debugComponentType = entityStoreRegistry
                .registerComponent(DebugComponent.class, DebugComponent::new);
        DebugComponent.setComponentType(debugComponentType);

        entityStoreRegistry.registerSystem(new PedestalBlockEvent());
        entityStoreRegistry.registerSystem(new ObeliskBreakEvent());
        entityStoreRegistry.registerSystem(new DebugTickSystem());
        entityStoreRegistry.registerSystem(new GlyphEffectSystem());
        entityStoreRegistry.registerSystem(new CastChargesSystem());
        entityStoreRegistry.registerSystem(new CastDecaySystem());
        entityStoreRegistry.registerSystem(new CastBookStyleSystem());
        entityStoreRegistry.registerSystem(new CastSpellPowerSystem());
        entityStoreRegistry.registerSystem(new HexCastEventSystem());
        entityStoreRegistry.registerSystem(new FireTriggerSystem());
        entityStoreRegistry.registerSystem(new SessionTickSystem());
        entityStoreRegistry.registerSystem(new SessionRecoverySystem());
        entityStoreRegistry.registerSystem(new ImbuedBlockBreakHandler());
        entityStoreRegistry.registerSystem(new DrawModeLifecycleSystem());
        entityStoreRegistry.registerSystem(new DrawRecognitionSystem());
        entityStoreRegistry.registerSystem(new DrawAnchorSystem());

        plugin.getEventRegistry().register(EventPriority.LATE, LoadAssetEvent.class, e -> {
            GlyphIconStore.generateMissing(plugin.getManifest());
        });

        var hoverableSpatialResourceType = entityStoreRegistry
                .registerSpatialResource(() -> new KDTree<>(Ref::isValid));
        entityStoreRegistry.registerSystem(new HoverableSpatialSystem(hoverableSpatialResourceType));

        var resourceType = entityStoreRegistry.registerResource(
                HexCacheResource.class, HexCacheResource::new);
        HexCacheResource.setResourceType(resourceType);

        var triggerRegistryType = entityStoreRegistry
                .registerResource(
                        TriggerListenerRegistry.class, TriggerListenerRegistry::new);
        TriggerListenerRegistry.setResourceType(triggerRegistryType);

        var hexCastStoreType = entityStoreRegistry
                .registerResource(HexCastStore.class, HexCastStore::new);
        HexCastStore.setResourceType(hexCastStoreType);

        var hexExecutionQueueType = entityStoreRegistry
                .registerResource(HexExecutionQueue.class, HexExecutionQueue::new);
        HexExecutionQueue.setResourceType(hexExecutionQueueType);
        entityStoreRegistry.registerSystem(new HexExecutionTickSystem());
        entityStoreRegistry.registerSystem(new HexQueueDrainEventSystem());

        var debugMessageQueueType = entityStoreRegistry
                .registerResource(DebugMessageQueue.class, DebugMessageQueue::new);
        DebugMessageQueue.setResourceType(debugMessageQueueType);
        entityStoreRegistry.registerSystem(new DebugMessageFlushSystem());

    }

    private static void registerBlockComponents(JavaPlugin plugin) {
        var chunkStoreRegistry = plugin.getChunkStoreRegistry();

        var pedestalBlockComponentType = chunkStoreRegistry
                .registerComponent(PedestalBlockComponent.class,
                        "Hexcode_PedestalBlock",
                        PedestalBlockComponent.CODEC);
        PedestalBlockComponent.setComponentType(pedestalBlockComponentType);

        var obeliskBlockComponentType = chunkStoreRegistry
                .registerComponent(ObeliskBlockComponent.class,
                        "Hexcode_ObeliskBlock",
                        ObeliskBlockComponent.CODEC);
        ObeliskBlockComponent.setComponentType(obeliskBlockComponentType);

        var imbuedBlockComponentType = chunkStoreRegistry
                .registerComponent(ImbuedBlockComponent.class,
                        "HexcodeImbuedBlock",
                        ImbuedBlockComponent.CODEC);
        ImbuedBlockComponent.setComponentType(imbuedBlockComponentType);

        chunkStoreRegistry.registerSystem(new PedestalPlaceEvent());
        chunkStoreRegistry.registerSystem(new ImbuedBlockTickSystem());

    }

    private static void registerHexContent(JavaPlugin plugin) {

        HexVar.CODEC.register("Entity", EntityVar.class, EntityVar.CODEC);
        HexVar.CODEC.register("Block", BlockVar.class, BlockVar.CODEC);
        HexVar.CODEC.register("Rotation", RotationVar.class, RotationVar.CODEC);
        HexVar.CODEC.register("Position", PositionVar.class, PositionVar.CODEC);
        HexVar.CODEC.register("Number", NumberVar.class, NumberVar.CODEC);

        HexRoot.CODEC.register("Player", PlayerHexRoot.class, PlayerHexRoot.CODEC);
        HexRoot.CODEC.register("Block", BlockHexRoot.class, BlockHexRoot.CODEC);

        if (MemoriesPlugin.get() != null) {
            Memory.CODEC.register(GlyphMemory.ID, GlyphMemory.class, GlyphMemory.CODEC);
            MemoriesPlugin.get().registerMemoryProvider(new GlyphMemoryProvider());
        } else {
            plugin.getLogger().atWarning().log("MemoriesPlugin unavailable; glyph memories disabled");
        }

    }

    private static void registerInteractions(JavaPlugin plugin) {
        Interaction.CODEC.register("HexDraw", HexDraw.class, HexDraw.CODEC);
        Interaction.CODEC.register("HexDrawMode", HexDrawMode.class, HexDrawMode.CODEC);
        Interaction.CODEC.register("HexContextPrimary", HexContextPrimary.class, HexContextPrimary.CODEC);
        Interaction.CODEC.register("HexContextAbility", HexContextAbility.class, HexContextAbility.CODEC);
        Interaction.CODEC.register("HexDispel", HexDispel.class, HexDispel.CODEC);
        Interaction.CODEC.register("PedestalInteraction", PedestalInteraction.class, PedestalInteraction.CODEC);
        Interaction.CODEC.register("HexExecute", HexExecuteInteraction.class, HexExecuteInteraction.CODEC);
        Interaction.CODEC.register("HexCastHold", HexCastHoldInteraction.class, HexCastHoldInteraction.CODEC);
    }

    private static void registerConditions(JavaPlugin plugin) {
        Condition.CODEC.register("HexHolding", HexHoldingCondition.class, HexHoldingCondition.CODEC);
    }

    private static void registerEvents(JavaPlugin plugin) {
    }

    private static void registerCommands(JavaPlugin plugin) {
        plugin.getCommandRegistry().registerCommand(new HexcodeCommand());
    }

    public static void start(JavaPlugin plugin) {
        EntityStore.REGISTRY.registerSystem(new MountOrphanReaperSystem());
        EntityStore.REGISTRY.registerSystem(new HexConstructSystem());
        EntityStore.REGISTRY.registerSystem(new HexConstructTeardownSystem());
        EntityStore.REGISTRY.registerSystem(new CasterSpellTeardownSystem());
        RegisterAssetEditorIntegrations(plugin);
    }

    private static void RegisterAssetEditorIntegrations(JavaPlugin plugin) {
        AssetEditorPlugin assetEditor = AssetEditorPlugin.get();
        if (assetEditor == null) {
            return;
        }
        EventRegistry events = assetEditor.getEventRegistry();
        events.register(AssetEditorActivateButtonEvent.class, GlyphIconEditorButton.BUTTON_ID,
                (Consumer<AssetEditorActivateButtonEvent>) event -> GlyphIconEditorButton.activate(event,
                        plugin.getManifest()));
        events.register(AssetEditorActivateButtonEvent.class, "HexcodeSpawnGlyph",
                (Consumer<AssetEditorActivateButtonEvent>) CraftingGlyphEditorSpawn::activate);
        events.registerGlobal(AssetEditorSelectAssetEvent.class,
                (Consumer<AssetEditorSelectAssetEvent>) GlyphEditorPreview::onSelectAsset);
        events.register(AssetEditorRequestDataSetEvent.class, "HexcodeObeliskHandlers",
                (Consumer<AssetEditorRequestDataSetEvent>) e -> e
                        .setResults(ObeliskHandlerRegistry.getAll().keySet()
                                .toArray(String[]::new)));
        events.register(AssetEditorRequestDataSetEvent.class, "HexcodeCastingStyles",
                (Consumer<AssetEditorRequestDataSetEvent>) e -> e
                        .setResults(CastingStyleRegistry.keys().toArray(String[]::new)));
        events.register(AssetEditorRequestDataSetEvent.class, "HexcodeGlyphHandlers",
                (Consumer<AssetEditorRequestDataSetEvent>) e -> e
                        .setResults(GlyphRegistry.getAll().keySet().toArray(String[]::new)));
    }
}
