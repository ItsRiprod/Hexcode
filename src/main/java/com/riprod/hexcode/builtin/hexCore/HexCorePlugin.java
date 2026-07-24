package com.riprod.hexcode.builtin.hexCore;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
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
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingDrawModeEnterListener;
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
import com.riprod.hexcode.builtin.hexCore.eventListeners.CastGateListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.CraftingNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.FizzleMessageListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphGateListener;
import com.riprod.hexcode.core.common.execution.gate.GateStateResource;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphDrawNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphDrawnDisabledNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphMemoryListener;
import com.riprod.hexcode.builtin.hexCore.execution.config.EncodedConfig;
import com.riprod.hexcode.builtin.hexCore.execution.config.ExecutionConfig;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.asset.profiles.ArmorProfile;
import com.riprod.hexcode.core.common.imbuement.asset.profiles.BlockProfile;
import com.riprod.hexcode.core.common.imbuement.asset.profiles.BookProfile;
import com.riprod.hexcode.core.common.imbuement.asset.profiles.WeaponProfile;
import com.riprod.hexcode.builtin.hexCore.config.BasicConfig;
import com.riprod.hexcode.builtin.hexCore.pedestals.PedestalContextHandler;
import com.riprod.hexcode.core.common.pedestal.events.PedestalInteractEvent;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.builtin.hexCore.nodes.anchor.AnchorNodeConfig;
import com.riprod.hexcode.builtin.hexCore.nodes.container.ContainerNodeConfig;
import com.riprod.hexcode.builtin.hexCore.nodes.glyph.GlyphNodeConfig;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.BooleanSlot;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.BooleanSlotConfig;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.InputSlotConfig;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.LinkSlot;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.NextSlotConfig;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.hypixel.hytale.codec.lookup.Priority;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc.ArcConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc.ArcGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact.InteractGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.chaos.ChaosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.ConcentrationConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.ConcentrationGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.ConjureGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.system.ConjureConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.DisguiseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.handler.DisguiseConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.DomainAuraConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.DomainConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.DomainGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.component.DomainZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.drain.DrainConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.drain.DrainGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.EnsnareConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.EnsnareGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode.ErodeConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode.ErodeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.force.ForceGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify.FortifyConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify.FortifyGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify.FortifyWardDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify.component.FortifyWardComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.InvisibilityConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.InvisibilityDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.InvisibilityGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.GlaciateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.GlaciateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.greater.GreaterGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth.GrowthConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth.GrowthGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.gust.GustGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt.HaltConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt.HaltGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interaction.InteractionGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interfere.InterfereGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.LevitateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.LevitateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase.PhaseComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase.PhaseConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase.PhaseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.resonate.ResonateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.ScaleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.handler.ScaleConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.ShatterGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction.HexShatterBounceInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction.HexShatterHitInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction.HexShatterMissInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap.SwapGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.terraform.TerraformGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.warp.WarpGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.bolt.BoltGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.healthsurge.HealthSurgeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.MageArmorConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.MageArmorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.MagicHealthDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.rebreathing.RebreathingConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.rebreathing.RebreathingGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.drown.DrownGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.scorch.ScorchGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.electrocute.ElectrocuteConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.electrocute.ElectrocuteGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.snap.SnapGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.FreezeConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.FreezeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ignite.IgniteConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ignite.IgniteGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area.AreaGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.beam.BeamGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.burning.BurningGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.ProjectileGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction.HexProjectileBounceInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction.HexProjectileHitInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction.HexProjectileMissInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.system.ProjectileConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.absolute.AbsoluteGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.add.AddGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.ceiling.CeilingGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.cos.CosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.cross.CrossGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.debug.DebugGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.DelayConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.DelayGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.divide.DivideGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.dot.DotGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.compare.CompareGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.equal.EqualGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.floor.FloorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.isHolding.IsHoldingValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.less.LessGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.modulo.ModuloGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.multiply.MultiplyGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.number.NumberValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onCast.OnCastGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onDeath.OnDeathGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onPrimary.OnPrimaryGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onSecondary.OnSecondaryGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onUse.OnUseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.output.OutputGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.pi.PiValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.position.PositionValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.power.PowerGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.root.RootGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.RotationValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.round.RoundGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.self.SelfGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.IdentifyGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.IlluminateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.IlluminateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.sin.SinGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.color.ColorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.shape.ShapeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.sound.SoundGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.style.StyleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.subtract.SubtractGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.tan.TanGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.variable.VariableValue;
import com.riprod.hexcode.builtin.hexCore.obelisks.accuracy.AccuracyObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.efficiency.EfficiencyObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.ImportExportObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.interactions.ImportInteraction;
import com.riprod.hexcode.builtin.hexCore.obelisks.seeker.SeekerObelisk;
import com.riprod.hexcode.builtin.hexCore.staffStyles.ArcStyle;
import com.riprod.hexcode.builtin.hexCore.staffStyles.RingStyle;
import com.riprod.hexcode.builtin.hexCore.staffStyles.SphereStyle;
import com.riprod.hexcode.core.common.appearance.HexAppearanceComponent;
import com.riprod.hexcode.core.common.appearance.HexAppearanceRevertSystem;
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
                RegisterProfiles();
        }

        private void RegisterImpacts() {
                Impact.CODEC
                                .register(PowerLawImpact.ID, PowerLawImpact.class, PowerLawImpact.CODEC)
                                .register(SphereVolumeImpact.ID, SphereVolumeImpact.class, SphereVolumeImpact.CODEC)
                                .register(RatioToDefaultImpact.ID, RatioToDefaultImpact.class,
                                                RatioToDefaultImpact.CODEC)
                                .register(ThresholdImpact.ID, ThresholdImpact.class, ThresholdImpact.CODEC)
                                .register(ExponentialImpact.ID, ExponentialImpact.class, ExponentialImpact.CODEC)
                                .register(ConstantImpact.ID, ConstantImpact.class, ConstantImpact.CODEC)
                                .register(LinearImpact.ID, LinearImpact.class, LinearImpact.CODEC);
        }

        private void RegisterGlyphs() {

                // utilities
                GlyphRegistry.register(new MultiplyGlyph());
                GlyphRegistry.register(new AddGlyph());
                GlyphRegistry.register(new SubtractGlyph());
                GlyphRegistry.register(new DivideGlyph());
                GlyphRegistry.register(new ModuloGlyph());
                GlyphRegistry.register(new EqualGlyph());
                GlyphRegistry.register(new CompareGlyph());
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
                GlyphRegistry.register(new ColorGlyph());
                GlyphRegistry.register(new ShapeGlyph());
                GlyphRegistry.register(new SoundGlyph());
                GlyphRegistry.register(new PositionValue());
                GlyphRegistry.register(new RotationValue());
                GlyphRegistry.register(new DotGlyph());
                GlyphRegistry.register(new CrossGlyph());
                GlyphRegistry.register(new NumberValue());
                GlyphRegistry.register(new VariableValue());
                GlyphRegistry.register(new PiValue());
                GlyphRegistry.register(new DebugGlyph());
                GlyphRegistry.register(new OutputGlyph());
                GlyphRegistry.register(new IsHoldingValue());
                GlyphRegistry.register(new OnPrimaryGlyph());
                GlyphRegistry.register(new OnSecondaryGlyph());
                GlyphRegistry.register(new OnUseGlyph());
                GlyphRegistry.register(new OnDeathGlyph());
                GlyphRegistry.register(new OnCastGlyph());

                // tier 1 glyphs
                GlyphRegistry.register(new SelfGlyph());
                GlyphRegistry.register(new ChaosGlyph());
                GlyphRegistry.register(new ForceGlyph());
                GlyphRegistry.register(new DelayGlyph());
                GlyphRegistry.register(new DrainGlyph());
                GlyphRegistry.register(new HaltGlyph());

                // tier 2 glyphs
                GlyphRegistry.register(new BeamGlyph());
                GlyphRegistry.register(new AreaGlyph());
                GlyphRegistry.register(new ProjectileGlyph());
                GlyphRegistry.register(new BurningGlyph());

                // tier 3 glyphs
                GlyphRegistry.register(new InterfereGlyph());
                GlyphRegistry.register(new ResonateGlyph());
                GlyphRegistry.register(new GustGlyph());
                GlyphRegistry.register(new ConjureGlyph());
                GlyphRegistry.register(new GrowthGlyph());
                GlyphRegistry.register(new FortifyGlyph());
                GlyphRegistry.register(new ErodeGlyph());
                GlyphRegistry.register(new LevitateGlyph());
                GlyphRegistry.register(new InvisibilityGlyph());
                GlyphRegistry.register(new ScaleGlyph());
                GlyphRegistry.register(new DisguiseGlyph());
                GlyphRegistry.register(new DomainGlyph());
                GlyphRegistry.register(new InteractGlyph());
                GlyphRegistry.register(new ArcGlyph());
                GlyphRegistry.register(new ShatterGlyph());
                GlyphRegistry.register(new GlaciateGlyph());
                GlyphRegistry.register(new TerraformGlyph());
                GlyphRegistry.register(new EnsnareGlyph());
                GlyphRegistry.register(new PhaseGlyph());
                GlyphRegistry.register(new WarpGlyph());
                GlyphRegistry.register(new SwapGlyph());
                GlyphRegistry.register(new InteractionGlyph());
                GlyphRegistry.register(new ConcentrationGlyph());
                
                // tier 4 glyphs
                GlyphRegistry.register(new FreezeGlyph());
                GlyphRegistry.register(new IgniteGlyph());
                GlyphRegistry.register(new ScorchGlyph());
                GlyphRegistry.register(new SnapGlyph());
                GlyphRegistry.register(new BoltGlyph());
                GlyphRegistry.register(new ElectrocuteGlyph());
                GlyphRegistry.register(new DrownGlyph());
                GlyphRegistry.register(new RebreathingGlyph());
                GlyphRegistry.register(new HealthSurgeGlyph());
                GlyphRegistry.register(new MageArmorGlyph());
                GlyphRegistry.register(new IdentifyGlyph());
                GlyphRegistry.register(new IlluminateGlyph());


                
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
                Slot.registerType(Priority.DEFAULT, "Link", LinkSlot.class, LinkSlot.CODEC);
                Slot.registerType("Boolean", BooleanSlot.class, BooleanSlot.CODEC);

                NodeConfig.CODEC.register(InputSlotConfig.TYPE, InputSlotConfig.class, InputSlotConfig.CODEC);
                NodeConfig.CODEC.register(NextSlotConfig.TYPE, NextSlotConfig.class, NextSlotConfig.CODEC);
                NodeConfig.CODEC.register(BooleanSlotConfig.TYPE, BooleanSlotConfig.class, BooleanSlotConfig.CODEC);
                NodeConfig.CODEC.register(AnchorNodeConfig.TYPE, AnchorNodeConfig.class, AnchorNodeConfig.CODEC);
                NodeConfig.CODEC.register(ContainerNodeConfig.TYPE, ContainerNodeConfig.class, ContainerNodeConfig.CODEC);
                NodeConfig.CODEC.register(GlyphNodeConfig.TYPE, GlyphNodeConfig.class, GlyphNodeConfig.CODEC);
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
                this.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphDrawnDisabledNotificationListener());
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

        private void RegisterProfiles() {
                ImbuementProfileAsset.CODEC.register("Book", BookProfile.class, BookProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Weapon", WeaponProfile.class, WeaponProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Armor", ArmorProfile.class, ArmorProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Block", BlockProfile.class, BlockProfile.CODEC);
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

                ComponentType<EntityStore, HexAppearanceComponent> hexAppearanceComponentType = entityStoreRegistry
                                .registerComponent(HexAppearanceComponent.class, "HexAppearance",
                                                HexAppearanceComponent.CODEC);
                HexAppearanceComponent.setComponentType(hexAppearanceComponentType);

                ComponentType<EntityStore, MagicHealthComponent> magicHealthComponentType = entityStoreRegistry
                                .registerComponent(MagicHealthComponent.class, MagicHealthComponent::new);
                MagicHealthComponent.setComponentType(magicHealthComponentType);

                ComponentType<EntityStore, FortifyWardComponent> fortifyWardComponentType = entityStoreRegistry
                                .registerComponent(FortifyWardComponent.class, () -> FortifyWardComponent.INSTANCE);
                FortifyWardComponent.setComponentType(fortifyWardComponentType);

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

                entityStoreRegistry.registerSystem(new HexAppearanceRevertSystem());

                entityStoreRegistry.registerSystem(new MagicHealthDamageSystem());
                entityStoreRegistry.registerSystem(new FortifyWardDamageSystem());

                entityStoreRegistry.registerSystem(new InvisibilityDamageSystem());

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
                entityStoreRegistry.registerSystem(new CraftingDrawModeEnterListener());
                entityStoreRegistry.registerSystem(new CraftingTickSystem());
                entityStoreRegistry.registerSystem(new CraftingPrimarySystem());
                entityStoreRegistry.registerSystem(new CraftingImportSystem());
                entityStoreRegistry.registerSystem(new CraftingShapeDrawnSystem());
                entityStoreRegistry.registerSystem(new CraftingForceExitSystem());
                entityStoreRegistry.registerSystem(new CraftingCleanupSystem());

                ResourceType<EntityStore, GateStateResource> gateStateType = entityStoreRegistry
                        .registerResource(GateStateResource.class, GateStateResource::new);
                GateStateResource.setResourceType(gateStateType);
                entityStoreRegistry.registerSystem(new CastGateListener());
                entityStoreRegistry.registerSystem(new GlyphGateListener());
        }

        private void RegisterConstructs() {
                ConstructRegistry.register(ScaleGlyph.ID, new ScaleConstructHandler());
                ConstructRegistry.register(DisguiseGlyph.ID, new DisguiseConstructHandler());
                ConstructRegistry.register(ConcentrationGlyph.ID, new ConcentrationConstructHandler());
                ConstructRegistry.register(DomainGlyph.ID, new DomainConstructHandler());
                ConstructRegistry.register(DomainGlyph.AURA_ID, new DomainAuraConstructHandler());
                ConstructRegistry.register(GlaciateGlyph.ID, new GlaciateConstructHandler());
                ConstructRegistry.register(ArcGlyph.ID, new ArcConstructHandler());
                ConstructRegistry.register(PhaseGlyph.ID, new PhaseConstructHandler());
                ConstructRegistry.register(ConjureGlyph.ID, new ConjureConstructHandler());
                ConstructRegistry.register(ErodeGlyph.ID, new ErodeConstructHandler());
                ConstructRegistry.register(FortifyGlyph.ID, new FortifyConstructHandler());
                ConstructRegistry.register(InvisibilityGlyph.ID, new InvisibilityConstructHandler());
                ConstructRegistry.register(LevitateGlyph.ID, new LevitateConstructHandler());
                ConstructRegistry.register(HaltGlyph.ID, new HaltConstructHandler());
                ConstructRegistry.register(DrainGlyph.ID, new DrainConstructHandler());
                ConstructRegistry.register(DelayGlyph.ID, new DelayConstructHandler());
                ConstructRegistry.register(EnsnareGlyph.ID, new EnsnareConstructHandler());
                ConstructRegistry.register(FreezeGlyph.ID, new FreezeConstructHandler());
                ConstructRegistry.register(ElectrocuteGlyph.ID, new ElectrocuteConstructHandler());
                ConstructRegistry.register(MageArmorGlyph.ID, new MageArmorConstructHandler());
                ConstructRegistry.register(RebreathingGlyph.ID, new RebreathingConstructHandler());
                ConstructRegistry.register(ProjectileGlyph.ID, new ProjectileConstructHandler());
                ConstructRegistry.register(IgniteGlyph.ID, new IgniteConstructHandler());
                ConstructRegistry.register(GrowthGlyph.ID, new GrowthConstructHandler());
                ConstructRegistry.register(IlluminateGlyph.ID, new IlluminateConstructHandler());
        }
}
