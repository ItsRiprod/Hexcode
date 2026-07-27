package com.riprod.hexcode;

import com.riprod.hexcode.builtin.counterspell.CounterspellPlugin;
import com.riprod.hexcode.builtin.hexCore.HexCorePlugin;
import com.riprod.hexcode.builtin.hexability.HexabilityPlugin;
import com.riprod.hexcode.builtin.hexomation.HexomationPlugin;
import com.riprod.hexcode.builtin.hextras.HextrasPlugin;
import com.riprod.hexcode.builtin.hextreme.HextremePlugin;
import com.riprod.hexcode.builtin.imbued.ImbuedPlugin;
import com.riprod.hexcode.builtin.ritualistic.RitualisticPlugin;
import com.riprod.hexcode.command.HexcodeCommand;
import com.riprod.hexcode.core.common.construct.system.HexConstructSystem;
import com.riprod.hexcode.core.common.construct.system.HexConstructTeardownSystem;
import com.riprod.hexcode.core.common.construct.system.MountOrphanReaperSystem;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.redirect.EntityRedirectComponent;
import com.riprod.hexcode.core.common.context.interactions.HexContextAbility;
import com.riprod.hexcode.core.common.context.interactions.HexContextPrimary;
import com.riprod.hexcode.core.common.drawing.DrawAnchorSystem;
import com.riprod.hexcode.core.common.drawing.DrawModeLifecycleSystem;
import com.riprod.hexcode.core.common.drawing.DrawRecognitionSystem;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.component.HexcasterDrawingComponent;
import com.riprod.hexcode.core.common.drawing.interactions.HexDraw;
import com.riprod.hexcode.core.common.drawing.interactions.HexDrawMode;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.drawing.registry.TemplateAsset;
import com.riprod.hexcode.core.common.effect.GlyphEffectSystem;
import com.riprod.hexcode.core.common.execution.component.BlockHexRoot;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.ResourcePoolComponent;
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.precast.CasterStateProvisionSystem;
import com.riprod.hexcode.core.common.execution.precast.CastChargesSystem;
import com.riprod.hexcode.core.common.execution.precast.CastDecaySystem;
import com.riprod.hexcode.core.common.execution.precast.CastBookStyleSystem;
import com.riprod.hexcode.core.common.execution.precast.CastSpellPowerSystem;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.condition.HexHoldingCondition;
import com.riprod.hexcode.core.common.execution.interactions.HexCastHoldInteraction;
import com.riprod.hexcode.core.common.execution.interactions.HexDispel;
import com.riprod.hexcode.core.common.execution.events.HexCastEventSystem;
import com.riprod.hexcode.core.common.execution.queue.HexQueueDrainEventSystem;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionTickSystem;
import com.riprod.hexcode.core.common.execution.system.CasterSpellTeardownSystem;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.icon.GlyphIconStore;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hexes.codec.HexCacheResource;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hover.system.HoverableSpatialSystem;
import com.riprod.hexcode.core.common.imbuement.asset.EssenceAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.events.ObeliskBreakEvent;
import com.riprod.hexcode.core.common.triggers.registry.FireTriggerSystem;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedBlockComponent;
import com.riprod.hexcode.core.common.imbuement.block.ImbuedBlockTickSystem;
import com.riprod.hexcode.core.common.imbuement.block.ImbuedBlockBreakHandler;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalBlockEvent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalPlaceEvent;
import com.riprod.hexcode.core.common.pedestal.system.SessionRecoverySystem;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.common.utilities.system.DebugTickSystem;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionTickSystem;
import com.riprod.hexcode.core.common.memories.GlyphMemory;
import com.riprod.hexcode.core.common.memories.GlyphMemoryProvider;
import com.riprod.hexcode.core.common.execution.interactions.HexExecuteInteraction;
import com.riprod.hexcode.interaction.PedestalInteraction;
import com.riprod.patchly.PatchManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorRequestDataSetEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class Hexcode extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final PatchManager patchManager;

    public Hexcode(JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this);
        LOGGER.atInfo().log("Hexcode spell-crafting mod v%s initializing...",
                this.getManifest().getVersion().toString());
    }

    @Override
    public CompletableFuture<Void> preLoad() {
        return super.preLoad();
    }

    @Override
    protected void setup() {
        patchManager.install();
        this.registerCastComponents();
        this.registerAssets();

        this.registerEntityComponents();
        this.registerBlockComponents();
        this.registerHexContent();
        this.registerInteractions();
        this.registerConditions();
        this.registerEvents();
        this.registerCommands();

        LOGGER.atInfo().log("Hexcode %s setup complete!", this.getManifest().getVersion().toString());
    }

    private void registerCastComponents() {
        VolatilityComponent.setComponentType(
                HexCast.REGISTRY.registerComponent(VolatilityComponent.class, VolatilityComponent::new));
        ResourcePoolComponent.setComponentType(
                HexCast.REGISTRY.registerComponent(ResourcePoolComponent.class, ResourcePoolComponent::new));
    }

    @SuppressWarnings("null")
    private void registerAssets() {
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(NodeConfig.class,
                                new DefaultAssetMap<String, NodeConfig>())
                        .setPath("Hexcode/NodeConfigs")
                        .setCodec(NodeConfig.CODEC)
                        .setKeyFunction(NodeConfig::getId)
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

    private void registerEntityComponents() {

        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

        ComponentType<EntityStore, GlyphComponent> glyphComponentType = entityStoreRegistry.registerComponent(
                GlyphComponent.class, "Glyph",
                GlyphComponent.CODEC);
        GlyphComponent.setComponentType(glyphComponentType);

        ComponentType<EntityStore, HexComponent> hexComponentType = entityStoreRegistry.registerComponent(
                HexComponent.class, "Hex",
                HexComponent.CODEC);
        HexComponent.setComponentType(hexComponentType);

        ComponentType<EntityStore, HexcasterComponent> hexcasterComponentType = entityStoreRegistry
                .registerComponent(
                        HexcasterComponent.class, "HexcasterComponent",
                        HexcasterComponent.CODEC);
        HexcasterComponent.setComponentType(hexcasterComponentType);

        ComponentType<EntityStore, CasterStateComponent> casterStateComponentType = entityStoreRegistry
                .registerComponent(CasterStateComponent.class,
                        CasterStateComponent::new);
        CasterStateComponent.setComponentType(casterStateComponentType);

        ComponentType<EntityStore, CasterComponent> casterComponentType = entityStoreRegistry
                .registerComponent(CasterComponent.class, CasterComponent::new);
        CasterComponent.setComponentType(casterComponentType);

        ComponentType<EntityStore, HexcodeComponent> hexcodeComponentType = entityStoreRegistry
                .registerComponent(HexcodeComponent.class, HexcodeComponent::new);
        HexcodeComponent.setComponentType(hexcodeComponentType);

        ComponentType<EntityStore, EntityRedirectComponent> entityRedirectComponentType = entityStoreRegistry
                .registerComponent(EntityRedirectComponent.class, EntityRedirectComponent::new);
        EntityRedirectComponent.setComponentType(entityRedirectComponentType);

        ComponentType<EntityStore, DrawCaptureComponent> drawCaptureComponentType = entityStoreRegistry
                .registerComponent(DrawCaptureComponent.class, DrawCaptureComponent::new);
        DrawCaptureComponent.setComponentType(drawCaptureComponentType);

        ComponentType<EntityStore, ExecutionComponent> queuedExecutionComponentType = entityStoreRegistry
                .registerComponent(ExecutionComponent.class, ExecutionComponent::new);
        ExecutionComponent.setComponentType(queuedExecutionComponentType);

        ComponentType<EntityStore, HexcasterCraftingComponent> craftingRootComponentType = entityStoreRegistry
                .registerComponent(HexcasterCraftingComponent.class,
                        HexcasterCraftingComponent::new);
        HexcasterCraftingComponent.setComponentType(craftingRootComponentType);

        ComponentType<EntityStore, HexcodeSessionComponent> sessionComponentType = entityStoreRegistry
                .registerComponent(HexcodeSessionComponent.class, "HexcodeSession",
                        HexcodeSessionComponent.CODEC);
        HexcodeSessionComponent.setComponentType(sessionComponentType);

        ComponentType<EntityStore, HexcasterDrawingComponent> drawingRootComponentType = entityStoreRegistry
                .registerComponent(HexcasterDrawingComponent.class,
                        HexcasterDrawingComponent::new);
        HexcasterDrawingComponent.setComponentType(drawingRootComponentType);

        ComponentType<EntityStore, NodeComponent> nodeComponentType = entityStoreRegistry
                .registerComponent(NodeComponent.class,
                        NodeComponent::new);
        NodeComponent.setComponentType(nodeComponentType);

        ComponentType<EntityStore, SlotComponent> slotComponentType = entityStoreRegistry
                .registerComponent(SlotComponent.class, SlotComponent::new);
        SlotComponent.setComponentType(slotComponentType);

        ComponentType<EntityStore, HoverableComponent> hoverableComponentType = entityStoreRegistry
                .registerComponent(HoverableComponent.class, HoverableComponent::new);
        HoverableComponent.setComponentType(hoverableComponentType);

        ComponentType<EntityStore, DebugComponent> debugComponentType = entityStoreRegistry
                .registerComponent(DebugComponent.class, DebugComponent::new);
        DebugComponent.setComponentType(debugComponentType);

        entityStoreRegistry.registerSystem(new PedestalBlockEvent());
        entityStoreRegistry.registerSystem(new ObeliskBreakEvent());
        entityStoreRegistry.registerSystem(new DebugTickSystem());
        entityStoreRegistry.registerSystem(new GlyphEffectSystem());
        entityStoreRegistry.registerSystem(new CasterStateProvisionSystem());
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

        this.getEventRegistry().register(EventPriority.LATE, LoadAssetEvent.class, e -> {
            GlyphIconStore.generateMissing(this.getManifest());
        });

        ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> hoverableSpatialResourceType = entityStoreRegistry
                .registerSpatialResource(() -> new KDTree<>(Ref::isValid));
        entityStoreRegistry.registerSystem(new HoverableSpatialSystem(hoverableSpatialResourceType));

        ResourceType<EntityStore, HexCacheResource> resourceType = entityStoreRegistry.registerResource(
                HexCacheResource.class, HexCacheResource::new);
        HexCacheResource.setResourceType(resourceType);

        ResourceType<EntityStore, TriggerListenerRegistry> triggerRegistryType = entityStoreRegistry
                .registerResource(
                        TriggerListenerRegistry.class, TriggerListenerRegistry::new);
        TriggerListenerRegistry.setResourceType(triggerRegistryType);

        ResourceType<EntityStore, HexExecutionQueue> hexExecutionQueueType = entityStoreRegistry
                .registerResource(HexExecutionQueue.class, HexExecutionQueue::new);
        HexExecutionQueue.setResourceType(hexExecutionQueueType);
        entityStoreRegistry.registerSystem(new HexExecutionTickSystem());
        entityStoreRegistry.registerSystem(new HexQueueDrainEventSystem());

    }

    private void registerBlockComponents() {

        ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();

        ComponentType<ChunkStore, PedestalBlockComponent> pedestalBlockComponentType = chunkStoreRegistry
                .registerComponent(PedestalBlockComponent.class,
                        "Hexcode_PedestalBlock",
                        PedestalBlockComponent.CODEC);
        PedestalBlockComponent.setComponentType(pedestalBlockComponentType);

        ComponentType<ChunkStore, ObeliskBlockComponent> obeliskBlockComponentType = chunkStoreRegistry
                .registerComponent(ObeliskBlockComponent.class,
                        "Hexcode_ObeliskBlock",
                        ObeliskBlockComponent.CODEC);
        ObeliskBlockComponent.setComponentType(obeliskBlockComponentType);

        ComponentType<ChunkStore, ImbuedBlockComponent> imbuedBlockComponentType = chunkStoreRegistry
                .registerComponent(ImbuedBlockComponent.class,
                        "HexcodeImbuedBlock",
                        ImbuedBlockComponent.CODEC);
        ImbuedBlockComponent.setComponentType(imbuedBlockComponentType);

        chunkStoreRegistry.registerSystem(new PedestalPlaceEvent());
        chunkStoreRegistry.registerSystem(new ImbuedBlockTickSystem());

    }

    private void registerHexContent() {

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
            LOGGER.atWarning().log("[hexcode] MemoriesPlugin unavailable; glyph memories disabled");
        }

    }

    private void registerInteractions() {
        Interaction.CODEC.register("HexDraw", HexDraw.class, HexDraw.CODEC);
        Interaction.CODEC.register("HexDrawMode", HexDrawMode.class, HexDrawMode.CODEC);
        Interaction.CODEC.register("HexContextPrimary", HexContextPrimary.class, HexContextPrimary.CODEC);
        Interaction.CODEC.register("HexContextAbility", HexContextAbility.class, HexContextAbility.CODEC);
        Interaction.CODEC.register("HexDispel", HexDispel.class, HexDispel.CODEC);
        Interaction.CODEC.register("PedestalInteraction", PedestalInteraction.class, PedestalInteraction.CODEC);
        Interaction.CODEC.register("HexExecute", HexExecuteInteraction.class, HexExecuteInteraction.CODEC);
        Interaction.CODEC.register("HexCastHold", HexCastHoldInteraction.class, HexCastHoldInteraction.CODEC);
    }

    private void registerConditions() {
        Condition.CODEC.register("HexHolding", HexHoldingCondition.class, HexHoldingCondition.CODEC);
    }

    private void registerEvents() {
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new HexcodeCommand());
    }

    @Override
    protected void start() {
        EntityStore.REGISTRY.registerSystem(new MountOrphanReaperSystem());
        EntityStore.REGISTRY.registerSystem(new HexConstructSystem());
        EntityStore.REGISTRY.registerSystem(new HexConstructTeardownSystem());
        EntityStore.REGISTRY.registerSystem(new CasterSpellTeardownSystem());
        RegisterAssetEditorDataSets();
    }

    @Override
    protected void shutdown() {
        patchManager.shutdown();
    }

    private void RegisterAssetEditorDataSets() {
        AssetEditorPlugin assetEditor = AssetEditorPlugin.get();
        if (assetEditor == null) {
            return;
        }
        EventRegistry events = assetEditor.getEventRegistry();
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
