package com.riprod.hexcode.builtin.hexCore;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.riprod.hexcode.builtin.hexCore.common.ContextForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingCleanupSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingImportSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingPrimarySystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingShapeDrawnSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingTickSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingEnterListener;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingShapeDrawnSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingTeardownSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingTickSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingUnequipSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingSlotSelectSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingTickSystem;
import com.riprod.hexcode.builtin.hexCore.eventListeners.CraftingNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.FizzleMessageListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphDrawNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphMemoryListener;
import com.riprod.hexcode.builtin.hexCore.execution.config.EncodedConfig;
import com.riprod.hexcode.builtin.hexCore.execution.config.ExecutionConfig;
import com.riprod.hexcode.builtin.hexCore.config.BasicConfig;
import com.riprod.hexcode.builtin.hexCore.pedestals.PedestalContextHandler;
import com.riprod.hexcode.core.common.pedestal.events.PedestalInteractEvent;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.common.node.NodeTypeId;
import com.riprod.hexcode.builtin.hexCore.nodes.anchor.AnchorNodeHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.container.ContainerNodeHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.glyph.GlyphNodeHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.absolute.AbsoluteGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.add.AddGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.arc.ArcConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.arc.ArcGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.area.AreaGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.beam.BeamGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.bolt.BoltGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.burning.BurningGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.ceiling.CeilingGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elementburning.ElementBurningGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elementfire.ElementFireGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.chaos.ChaosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.concentration.ConcentrationConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.concentration.ConcentrationGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.ConjureGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.system.ConjureConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.cos.CosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.cross.CrossGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.debug.DebugGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.delay.DelayConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.delay.DelayGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.divide.DivideGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.domain.DomainAuraConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.domain.DomainConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.domain.DomainGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.domain.component.DomainZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.dot.DotGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.drain.DrainConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.drain.DrainGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.ensnare.EnsnareConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.ensnare.EnsnareGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.equal.EqualGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.erode.ErodeConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.erode.ErodeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.erode.system.ErodeDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.floor.FloorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.force.ForceGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.fortify.FortifyConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.fortify.FortifyGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.fortify.system.FortifyDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.freeze.FreezeConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.freeze.FreezeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.glaciate.GlaciateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.glaciate.GlaciateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.greater.GreaterGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.growth.GrowthConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.growth.GrowthGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.gust.GustGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.halt.HaltConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.halt.HaltGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.ignite.IgniteConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.ignite.IgniteGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.interaction.InteractionGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.interfere.InterfereGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.isHolding.IsHoldingValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.less.LessGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.levitate.LevitateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.levitate.LevitateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.multiply.MultiplyGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.number.NumberValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.onCast.OnCastGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.onDeath.OnDeathGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.onPrimary.OnPrimaryGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.onSecondary.OnSecondaryGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.onUse.OnUseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.output.OutputGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.phase.PhaseComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.phase.PhaseConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.phase.PhaseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.pi.PiValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.position.PositionValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.power.PowerGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.ProjectileGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.interaction.HexProjectileBounceInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.interaction.HexProjectileHitInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.interaction.HexProjectileMissInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.system.ProjectileConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.resonate.ResonateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.root.RootGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.rotation.RotationValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.round.RoundGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.scale.ScaleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.scale.components.ScaleStackComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.scale.handler.ScaleConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.self.SelfGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.shatter.ShatterGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.hexCore.glyphs.shatter.interaction.HexShatterBounceInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.shatter.interaction.HexShatterHitInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.shatter.interaction.HexShatterMissInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.sin.SinGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.style.StyleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.subtract.SubtractGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.swap.SwapGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.tan.TanGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.terraform.TerraformGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.variable.VariableValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.warp.WarpGlyph;
import com.riprod.hexcode.builtin.hexCore.obelisks.accuracy.AccuracyObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.efficiency.EfficiencyObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.ImportExportObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.interactions.ImportInteraction;
import com.riprod.hexcode.builtin.hexCore.obelisks.seeker.SeekerObelisk;
import com.riprod.hexcode.builtin.hexCore.staffStyles.ArcStyle;
import com.riprod.hexcode.builtin.hexCore.staffStyles.RingStyle;
import com.riprod.hexcode.builtin.hexCore.staffStyles.SphereStyle;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;
import com.riprod.hexcode.builtin.hexCore.impact.ConstantImpact;
import com.riprod.hexcode.builtin.hexCore.impact.ExponentialImpact;
import com.riprod.hexcode.builtin.hexCore.impact.LinearImpact;
import com.riprod.hexcode.builtin.hexCore.impact.PowerLawImpact;
import com.riprod.hexcode.builtin.hexCore.impact.RatioToDefaultImpact;
import com.riprod.hexcode.builtin.hexCore.impact.SphereVolumeImpact;
import com.riprod.hexcode.builtin.hexCore.impact.ThresholdImpact;

public class HexCorePlugin extends JavaPlugin {
        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

        public HexCorePlugin(JavaPluginInit init) {
                super(init);
                LOGGER.atInfo().log("Hexcode %s sub-plugin v%s initializing...",
                                this.getManifest().getName().toString(), this.getManifest().getVersion().toString());
        }

        @Override
        public void setup() {
                RegisterImpacts();
                RegisterGlyphs();
                RegisterStyles();
                RegisterObelisks();
                RegisterNodes();
                RegisterComponents();
                RegisterSystems();
                RegisterConstructs();
                RegisterInteractions();
                RegisterEvents();
                RegisterHexConfigs();
                RegisterGlyphConfigs();
        }

        private void RegisterImpacts() {
                Impact.CODEC
                        .register(PowerLawImpact.ID, PowerLawImpact.class, PowerLawImpact.CODEC)
                        .register(SphereVolumeImpact.ID, SphereVolumeImpact.class, SphereVolumeImpact.CODEC)
                        .register(RatioToDefaultImpact.ID, RatioToDefaultImpact.class, RatioToDefaultImpact.CODEC)
                        .register(ThresholdImpact.ID, ThresholdImpact.class, ThresholdImpact.CODEC)
                        .register(ExponentialImpact.ID, ExponentialImpact.class, ExponentialImpact.CODEC)
                        .register(ConstantImpact.ID, ConstantImpact.class, ConstantImpact.CODEC)
                        .register(LinearImpact.ID, LinearImpact.class, LinearImpact.CODEC);
        }

        private void RegisterGlyphs() {

                GlyphRegistry.register(new SelfGlyph());
                GlyphRegistry.register(new ChaosGlyph());
                GlyphRegistry.register(new ForceGlyph());
                GlyphRegistry.register(new DelayGlyph());
                GlyphRegistry.register(new DrainGlyph());
                GlyphRegistry.register(new HaltGlyph());

                GlyphRegistry.register(new BeamGlyph());
                GlyphRegistry.register(new AreaGlyph());
                GlyphRegistry.register(new ProjectileGlyph());
                GlyphRegistry.register(new GustGlyph());
                GlyphRegistry.register(new ConjureGlyph());
                GlyphRegistry.register(new GrowthGlyph());
                GlyphRegistry.register(new FortifyGlyph());
                GlyphRegistry.register(new ErodeGlyph());
                GlyphRegistry.register(new InterfereGlyph());
                GlyphRegistry.register(new ResonateGlyph());
                GlyphRegistry.register(new LevitateGlyph());
                GlyphRegistry.register(new ScaleGlyph());
                GlyphRegistry.register(new DomainGlyph());

                GlyphRegistry.register(new IgniteGlyph());
                GlyphRegistry.register(new BoltGlyph());
                GlyphRegistry.register(new ArcGlyph());
                GlyphRegistry.register(new FreezeGlyph());
                GlyphRegistry.register(new ShatterGlyph());
                GlyphRegistry.register(new GlaciateGlyph());
                GlyphRegistry.register(new TerraformGlyph());
                GlyphRegistry.register(new BurningGlyph());
                GlyphRegistry.register(new ElementFireGlyph());
                GlyphRegistry.register(new ElementBurningGlyph());
                GlyphRegistry.register(new EnsnareGlyph());
                GlyphRegistry.register(new PhaseGlyph());
                GlyphRegistry.register(new WarpGlyph());
                GlyphRegistry.register(new SwapGlyph());

                GlyphRegistry.register(new MultiplyGlyph());
                GlyphRegistry.register(new AddGlyph());
                GlyphRegistry.register(new SubtractGlyph());
                GlyphRegistry.register(new DivideGlyph());
                GlyphRegistry.register(new EqualGlyph());
                GlyphRegistry.register(new GreaterGlyph());
                GlyphRegistry.register(new LessGlyph());
                GlyphRegistry.register(new SinGlyph());
                GlyphRegistry.register(new CosGlyph());
                GlyphRegistry.register(new TanGlyph());
                GlyphRegistry.register(new AbsoluteGlyph());
                GlyphRegistry.register(new FloorGlyph());
                GlyphRegistry.register(new CeilingGlyph());
                GlyphRegistry.register(new RoundGlyph());
                GlyphRegistry.register(new PowerGlyph());
                GlyphRegistry.register(new RootGlyph());
                GlyphRegistry.register(new StyleGlyph());

                GlyphRegistry.register(new PositionValue());
                GlyphRegistry.register(new RotationValue());
                GlyphRegistry.register(new DotGlyph());
                GlyphRegistry.register(new CrossGlyph());

                GlyphRegistry.register(new NumberValue());
                GlyphRegistry.register(new VariableValue());
                GlyphRegistry.register(new PiValue());

                GlyphRegistry.register(new DebugGlyph());

                GlyphRegistry.register(new OutputGlyph());

                GlyphRegistry.register(new InteractionGlyph());

                GlyphRegistry.register(new IsHoldingValue());
                GlyphRegistry.register(new ConcentrationGlyph());

                GlyphRegistry.register(new OnPrimaryGlyph());
                GlyphRegistry.register(new OnSecondaryGlyph());
                GlyphRegistry.register(new OnUseGlyph());
                GlyphRegistry.register(new OnDeathGlyph());
                GlyphRegistry.register(new OnCastGlyph());
        }

        private void RegisterObelisks() {
                ObeliskHandlerRegistry.register("seeker", new SeekerObelisk());
                ObeliskHandlerRegistry.register("accuracy", new AccuracyObelisk());
                ObeliskHandlerRegistry.register("efficiency", new EfficiencyObelisk());
                ObeliskHandlerRegistry.register("import_export", new ImportExportObelisk());
        }

        private void RegisterStyles() {
                CastingStyleRegistry.register(new ArcStyle());
                CastingStyleRegistry.register(new RingStyle());
                CastingStyleRegistry.register(new SphereStyle());
                CastingStyleRegistry.setDefault(RingStyle.ID);
        }

        private void RegisterNodes() {
                NodeRouter.register(NodeTypeId.ANCHOR, AnchorNodeHandler.INSTANCE);
                NodeRouter.register(NodeTypeId.CONTAINER, ContainerNodeHandler.INSTANCE);
                NodeRouter.register(NodeTypeId.GLYPH, GlyphNodeHandler.INSTANCE);
                NodeRouter.register(NodeTypeId.SLOT_STANDARD, SlotNodeHandler.INSTANCE);
        }

        private void RegisterInteractions() {
                Interaction.CODEC.register("HexImportExportInteraction", ImportInteraction.class,
                                ImportInteraction.CODEC);
                Interaction.CODEC.register("HexProjectileHit",
                                HexProjectileHitInteraction.class,
                                HexProjectileHitInteraction.CODEC);
                Interaction.CODEC.register("HexProjectileMiss",
                                HexProjectileMissInteraction.class,
                                HexProjectileMissInteraction.CODEC);
                Interaction.CODEC.register("HexProjectileBounce",
                                HexProjectileBounceInteraction.class,
                                HexProjectileBounceInteraction.CODEC);
                Interaction.CODEC.register("HexShatterHit",
                                HexShatterHitInteraction.class,
                                HexShatterHitInteraction.CODEC);
                Interaction.CODEC.register("HexShatterMiss",
                                HexShatterMissInteraction.class,
                                HexShatterMissInteraction.CODEC);
                Interaction.CODEC.register("HexShatterBounce",
                                HexShatterBounceInteraction.class,
                                HexShatterBounceInteraction.CODEC);
        }

        private void RegisterEvents() {
                this.getEventRegistry().registerGlobal(GlyphFizzleEvent.class, new FizzleMessageListener());
                this.getEventRegistry().registerGlobal(CraftingEvent.class, new CraftingNotificationListener());
                this.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphMemoryListener());
                this.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphDrawNotificationListener());
                this.getEventRegistry().registerGlobal(PedestalInteractEvent.class, new PedestalContextHandler());
                this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class,
                                ContextForceExitSystem::onPlayerDisconnect);
        }

        private void RegisterHexConfigs() {
                HexConfigAsset.CODEC.register("ExecutionConfig", ExecutionConfig.class, ExecutionConfig.CODEC);
                HexConfigAsset.CODEC.register("EncodedConfig", EncodedConfig.class, EncodedConfig.CODEC);
        }

        private void RegisterGlyphConfigs() {
                GlyphConfig.CODEC.register(BasicConfig.ID, BasicConfig.class, BasicConfig.CODEC);
        }

        private void RegisterComponents() {
                ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

                ComponentType<EntityStore, ProjectileState> hexProjectileStateType = entityStoreRegistry
                                .registerComponent(ProjectileState.class, ProjectileState::new);
                ProjectileState.setComponentType(hexProjectileStateType);

                ComponentType<EntityStore, ConjureZoneComponent> conjureZoneType = entityStoreRegistry
                                .registerComponent(ConjureZoneComponent.class, ConjureZoneComponent::new);
                ConjureZoneComponent.setComponentType(conjureZoneType);

                ComponentType<EntityStore, PhaseComponent> phaseComponentType = entityStoreRegistry
                                .registerComponent(PhaseComponent.class, PhaseComponent::new);
                PhaseComponent.setComponentType(phaseComponentType);

                ComponentType<EntityStore, EnsnareComponent> ensnareComponentType = entityStoreRegistry
                                .registerComponent(EnsnareComponent.class, EnsnareComponent::new);
                EnsnareComponent.setComponentType(ensnareComponentType);

                ComponentType<EntityStore, GlaciateComponent> glaciateComponentType = entityStoreRegistry
                                .registerComponent(GlaciateComponent.class, GlaciateComponent::new);
                GlaciateComponent.setComponentType(glaciateComponentType);

                ComponentType<EntityStore, ShatterState> shatterStateType = entityStoreRegistry
                                .registerComponent(ShatterState.class, ShatterState::new);
                ShatterState.setComponentType(shatterStateType);

                ComponentType<EntityStore, DomainZoneComponent> domainZoneComponentType = entityStoreRegistry
                                .registerComponent(DomainZoneComponent.class, DomainZoneComponent::new);
                DomainZoneComponent.setComponentType(domainZoneComponentType);

                ComponentType<EntityStore, HexEffectsComponent> hexConstructType = entityStoreRegistry
                                .registerComponent(HexEffectsComponent.class, HexEffectsComponent::new);
                HexEffectsComponent.setComponentType(hexConstructType);

                ComponentType<EntityStore, ScaleStackComponent> scaleStackComponentType = entityStoreRegistry
                                .registerComponent(ScaleStackComponent.class, "ScaleStack",
                                                ScaleStackComponent.CODEC);
                ScaleStackComponent.setComponentType(scaleStackComponentType);

                ComponentType<EntityStore, FlycastingState> flycastingStateType = entityStoreRegistry
                                .registerComponent(FlycastingState.class, FlycastingState::new);
                FlycastingState.setComponentType(flycastingStateType);

                ComponentType<EntityStore, SelectingState> selectingStateType = entityStoreRegistry
                                .registerComponent(SelectingState.class, SelectingState::new);
                SelectingState.setComponentType(selectingStateType);

                ComponentType<EntityStore, CraftingState> craftingStateType = entityStoreRegistry
                                .registerComponent(CraftingState.class, CraftingState::new);
                CraftingState.setComponentType(craftingStateType);
        }

        private void RegisterSystems() {
                ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

                entityStoreRegistry.registerSystem(new ErodeDamageSystem());
                entityStoreRegistry.registerSystem(new FortifyDamageSystem());

                entityStoreRegistry.registerSystem(new ContextForceExitSystem.OnDeath());

                entityStoreRegistry.registerSystem(new FlycastingEnterListener());
                entityStoreRegistry.registerSystem(new FlycastingChangeListener());
                entityStoreRegistry.registerSystem(new FlycastingTeardownSystem());
                entityStoreRegistry.registerSystem(new FlycastingTickSystem());
                entityStoreRegistry.registerSystem(new FlycastingShapeDrawnSystem());
                entityStoreRegistry.registerSystem(new FlycastingExitSystem());
                entityStoreRegistry.registerSystem(new FlycastingUnequipSystem());
                entityStoreRegistry.registerSystem(new FlycastingForceExitSystem());

                entityStoreRegistry.registerSystem(new SelectingChangeListener());
                entityStoreRegistry.registerSystem(new SelectingTickSystem());
                entityStoreRegistry.registerSystem(new SelectingSlotSelectSystem());
                entityStoreRegistry.registerSystem(new SelectingForceExitSystem());

                entityStoreRegistry.registerSystem(new CraftingChangeListener());
                entityStoreRegistry.registerSystem(new CraftingTickSystem());
                entityStoreRegistry.registerSystem(new CraftingPrimarySystem());
                entityStoreRegistry.registerSystem(new CraftingImportSystem());
                entityStoreRegistry.registerSystem(new CraftingShapeDrawnSystem());
                entityStoreRegistry.registerSystem(new CraftingForceExitSystem());
                entityStoreRegistry.registerSystem(new CraftingCleanupSystem());
        }

        private void RegisterConstructs() {
                ConstructRegistry.register(ScaleGlyph.ID, new ScaleConstructHandler());
                ConstructRegistry.register(ConcentrationGlyph.ID, new ConcentrationConstructHandler());
                ConstructRegistry.register(DomainGlyph.ID, new DomainConstructHandler());
                ConstructRegistry.register(DomainGlyph.AURA_ID, new DomainAuraConstructHandler());
                ConstructRegistry.register(GlaciateGlyph.ID, new GlaciateConstructHandler());
                ConstructRegistry.register(ArcGlyph.ID, new ArcConstructHandler());
                ConstructRegistry.register(PhaseGlyph.ID, new PhaseConstructHandler());
                ConstructRegistry.register(ConjureGlyph.ID, new ConjureConstructHandler());
                ConstructRegistry.register(ErodeGlyph.ID, new ErodeConstructHandler());
                ConstructRegistry.register(LevitateGlyph.ID, new LevitateConstructHandler());
                ConstructRegistry.register(HaltGlyph.ID, new HaltConstructHandler());
                ConstructRegistry.register(FortifyGlyph.ID, new FortifyConstructHandler());
                ConstructRegistry.register(DrainGlyph.ID, new DrainConstructHandler());
                ConstructRegistry.register(DelayGlyph.ID, new DelayConstructHandler());
                ConstructRegistry.register(EnsnareGlyph.ID, new EnsnareConstructHandler());
                ConstructRegistry.register(FreezeGlyph.ID, new FreezeConstructHandler());
                ConstructRegistry.register(ProjectileGlyph.ID, new ProjectileConstructHandler());
                ConstructRegistry.register(IgniteGlyph.ID, new IgniteConstructHandler());
                ConstructRegistry.register(GrowthGlyph.ID, new GrowthConstructHandler());
        }
}
