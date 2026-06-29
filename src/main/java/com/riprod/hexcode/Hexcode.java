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
import com.riprod.hexcode.core.common.construct.system.MountOrphanReaperSystem;
import com.riprod.hexcode.core.common.drawing.DrawingSlotLockEvent;
import com.riprod.hexcode.core.common.drawing.DrawingSystem;
import com.riprod.hexcode.core.common.drawing.component.HexcasterDrawingComponent;
import com.riprod.hexcode.core.common.drawing.interactions.HexDraw;
import com.riprod.hexcode.core.common.drawing.interactions.HexDrawMode;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.drawing.registry.TemplateAsset;
import com.riprod.hexcode.core.common.effect.GlyphEffectSystem;
import com.riprod.hexcode.core.common.execution.component.BlockHexRoot;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.events.HexCastEventSystem;
import com.riprod.hexcode.core.common.execution.queue.HexQueueDrainEventSystem;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionTickSystem;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotStyleAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.StaffUnequipEvent;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hexcaster.system.HexcasterCleanupSystem;
import com.riprod.hexcode.core.common.hexes.codec.HexCacheResource;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;
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
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.core.common.utilities.system.DebugTickSystem;
import com.riprod.hexcode.core.state.casting.CastingSystem;
import com.riprod.hexcode.core.state.casting.component.HexcasterCastingComponent;
import com.riprod.hexcode.core.state.casting.registery.CastingStyleRegistry;
import com.riprod.hexcode.core.state.crafting.CraftingSystem;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.component.NodeComponent;
import com.riprod.hexcode.core.state.crafting.component.SlotComponent;
import com.riprod.hexcode.core.state.crafting.handlers.node.NodeRouter;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionTickSystem;
import com.riprod.hexcode.core.state.idle.IdleSystem;
import com.riprod.hexcode.core.common.memories.GlyphMemory;
import com.riprod.hexcode.core.common.memories.GlyphMemoryProvider;
import com.riprod.hexcode.interaction.HexStateChange;
import com.riprod.hexcode.interaction.HexHold;
import com.riprod.hexcode.interaction.HexMode;
import com.riprod.hexcode.interaction.HexModeExit;
import com.riprod.hexcode.interaction.HexStateBranch;
import com.riprod.hexcode.core.common.execution.interactions.HexExecuteInteraction;
import com.riprod.hexcode.interaction.HexAbility;
import com.riprod.hexcode.interaction.HexItemCondition;
import com.riprod.hexcode.interaction.PedestalInteraction;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.state.HexTick;
import com.riprod.hexcode.state.StateRouter;
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
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class Hexcode extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final PatchManager patchManager;

    // Deprecated, waiting on a fix from hytale for proper implementation
    private HexCorePlugin hexCore;
//     private CounterspellPlugin counterspell;
//     private HexabilityPlugin hexability;
//     private HexomationPlugin hexomation;
//     private HextrasPlugin hextras;
//     private HextremePlugin hextreme;
//     private ImbuedPlugin imbued;
//     private RitualisticPlugin ritualistic;

    public Hexcode(JavaPluginInit init) {
        super(init);
        patchManager = new PatchManager(this); // setup patchly
        LOGGER.atInfo().log("Hexcode spell-crafting mod v%s initializing...",
                this.getManifest().getVersion().toString());

        // hexCore = new HexCorePlugin(init);
        // counterspell = new CounterspellPlugin(init);
        // hexability = new HexabilityPlugin(init);
        // hexomation = new HexomationPlugin(init);
        // hextras = new HextrasPlugin(init);
        // hextreme = new HextremePlugin(init);
        // imbued = new ImbuedPlugin(init);
        // ritualistic = new RitualisticPlugin(init);
    }

    @Override
    public CompletableFuture<Void> preLoad() {
        return super.preLoad();
    }

    @Override
    protected void setup() {
        patchManager.install(); // init patchly
        this.registerAssets();

        this.registerEntityComponents();
        this.registerBlockComponents();
        this.registerHexContent();
        this.registerInteractions();
        this.registerEvents();
        this.registerCommands();

        LOGGER.atInfo().log("Hexcode %s setup complete!", this.getManifest().getVersion().toString());

        // deprecated until hytale fixed implementation
        // hexCore.setup();
        // counterspell.setup();
        // hexability.setup();
        // hexomation.setup();
        // hextras.setup();
        // hextreme.setup();
        // imbued.setup();
        // ritualistic.setup();
    }

    @SuppressWarnings("null")
    private void registerAssets() {
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(SlotStyleAsset.class,
                                new DefaultAssetMap<String, SlotStyleAsset>())
                        .setPath("Hexcode/SlotStyles")
                        .setCodec(SlotStyleAsset.CODEC)
                        .setKeyFunction(SlotStyleAsset::getId)
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
                        .setPath("Hexcode/Hex")
                        .setCodec(HexConfigAsset.CODEC)
                        .setKeyFunction(HexConfigAsset::getId)
                        .loadsAfter(HexStyleAsset.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(GlyphAsset.class, new DefaultAssetMap<String, GlyphAsset>())
                        .setPath("Hexcode/Glyphs")
                        .setCodec(GlyphAsset.CODEC)
                        .setKeyFunction(GlyphAsset::getId)
                        .loadsAfter(SlotStyleAsset.class)
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
                        .builder(HexStaffAsset.class,
                                new DefaultAssetMap<String, HexStaffAsset>())
                        .setPath("Hexcode/HexStaffs")
                        .setCodec(HexStaffAsset.CODEC)
                        .setKeyFunction(HexStaffAsset::getId)
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
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(ImbuementProfileAsset.class,
                                new DefaultAssetMap<String, ImbuementProfileAsset>())
                        .setPath("Hexcode/Imbuement/Profiles")
                        .setCodec(ImbuementProfileAsset.CODEC)
                        .setKeyFunction(ImbuementProfileAsset::getId)
                        .loadsAfter(SlotStyleAsset.class)
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

        ComponentType<EntityStore, HexStaffComponent> hexStaffComponentType = entityStoreRegistry
                .registerComponent(
                        HexStaffComponent.class, "HexStaff",
                        HexStaffComponent.CODEC);
        HexStaffComponent.setComponentType(hexStaffComponentType);

        ComponentType<EntityStore, HexcasterComponent> hexcasterComponentType = entityStoreRegistry
                .registerComponent(
                        HexcasterComponent.class, "HexcasterComponent",
                        HexcasterComponent.CODEC);
        HexcasterComponent.setComponentType(hexcasterComponentType);

        ComponentType<EntityStore, HexcasterIdleComponent> executionComponentType = entityStoreRegistry
                .registerComponent(HexcasterIdleComponent.class,
                        HexcasterIdleComponent::new);
        HexcasterIdleComponent.setComponentType(executionComponentType);

        ComponentType<EntityStore, HexcasterCastingComponent> castingRootComponentType = entityStoreRegistry
                .registerComponent(HexcasterCastingComponent.class,
                        HexcasterCastingComponent::new);
        HexcasterCastingComponent.setComponentType(castingRootComponentType);

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

        entityStoreRegistry.registerSystem(new HexTick());
        entityStoreRegistry.registerSystem(new PedestalBlockEvent());
        entityStoreRegistry.registerSystem(new ObeliskBreakEvent());
        entityStoreRegistry.registerSystem(new DrawingSlotLockEvent());
        entityStoreRegistry.registerSystem(new StaffUnequipEvent());
        entityStoreRegistry.registerSystem(new DebugTickSystem());
        entityStoreRegistry.registerSystem(new GlyphEffectSystem());
        entityStoreRegistry.registerSystem(new HexCastEventSystem());
        entityStoreRegistry.registerSystem(new FireTriggerSystem());
        entityStoreRegistry.registerSystem(new HexcasterCleanupSystem());
        entityStoreRegistry.registerSystem(new SessionTickSystem());
        entityStoreRegistry.registerSystem(new ImbuedBlockBreakHandler());

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

        StateRouter.registerState(HexState.IDLE, new IdleSystem());
        StateRouter.registerState(HexState.CASTING, new CastingSystem());
        StateRouter.registerState(HexState.CRAFTING, new CraftingSystem());
        StateRouter.registerState(HexState.DRAWING, new DrawingSystem());

    }

    private void registerInteractions() {
        Interaction.CODEC.register("HexStateBranch", HexStateBranch.class, HexStateBranch.CODEC);
        Interaction.CODEC.register("HexStateChange", HexStateChange.class, HexStateChange.CODEC);
        Interaction.CODEC.register("HexHold", HexHold.class, HexHold.CODEC);
        Interaction.CODEC.register("HexMode", HexMode.class, HexMode.CODEC);
        Interaction.CODEC.register("HexDraw", HexDraw.class, HexDraw.CODEC);
        Interaction.CODEC.register("HexDrawMode", HexDrawMode.class, HexDrawMode.CODEC);
        Interaction.CODEC.register("HexModeExit", HexModeExit.class, HexModeExit.CODEC);
        Interaction.CODEC.register("PedestalInteraction", PedestalInteraction.class, PedestalInteraction.CODEC);
        Interaction.CODEC.register("HexItemCondition", HexItemCondition.class, HexItemCondition.CODEC);
        Interaction.CODEC.register("HexAbility", HexAbility.class, HexAbility.CODEC);
        Interaction.CODEC.register("HexExecute", HexExecuteInteraction.class, HexExecuteInteraction.CODEC);
    }

    private void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, Hexcode::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, Hexcode::onPlayerDisconnect);
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new HexcodeCommand());
    }

    @Override
    protected void start() {
        EntityStore.REGISTRY.registerSystem(new MountOrphanReaperSystem());
        EntityStore.REGISTRY.registerSystem(new HexConstructSystem());
        RegisterAssetEditorDataSets();
    }

    @Override
    protected void shutdown() {
        patchManager.shutdown();
    }

    private void RegisterAssetEditorDataSets() {
        EventRegistry events = AssetEditorPlugin.get().getEventRegistry();
        events.register(AssetEditorRequestDataSetEvent.class, "HexcodeObeliskHandlers",
                (Consumer<AssetEditorRequestDataSetEvent>) e -> e
                        .setResults(ObeliskHandlerRegistry.getAll().keySet()
                                .toArray(String[]::new)));
        events.register(AssetEditorRequestDataSetEvent.class, "HexcodeCastingStyles",
                (Consumer<AssetEditorRequestDataSetEvent>) e -> e
                        .setResults(CastingStyleRegistry.keys().toArray(String[]::new)));
        events.register(AssetEditorRequestDataSetEvent.class, "HexcodeNodeHandlers",
                (Consumer<AssetEditorRequestDataSetEvent>) e -> e
                        .setResults(NodeRouter.keys().toArray(String[]::new)));
    }

    private static void onPlayerConnect(PlayerConnectEvent event) {
        try {
            Holder<EntityStore> holder = event.getHolder();
            holder.ensureComponent(HexcasterComponent.getComponentType());
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] onPlayerConnect failed: %s", e.getMessage());
        }
    }

    private static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            PlayerRef playerRef = event.getPlayerRef();
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                world.execute(() -> {
                    try {
                        if (ref.isValid()) {
                            HexcasterComponent hexcaster = store.getComponent(ref,
                                    HexcasterComponent.getComponentType());
                            if (hexcaster != null
                                    && hexcaster.getState() != HexState.IDLE) {
                                hexcaster.requestStateChange(HexState.IDLE);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.atSevere().log(
                                "[hexcode] onPlayerDisconnect world.execute failed: %s",
                                e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] onPlayerDisconnect failed: %s", e.getMessage());
        }
    }
}
