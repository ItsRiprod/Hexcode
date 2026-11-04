package com.riprod.hexcode.builtin.hexCore;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.hypixel.hytale.event.EventPriority;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.builtin.hexCore.common.ContextForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.components.component.ComponentPasteCache;
import com.riprod.hexcode.builtin.hexCore.components.system.ComponentCacheCleanupSystem;
import com.riprod.hexcode.builtin.hexCore.components.system.ComponentCacheSystem;
import com.riprod.hexcode.builtin.hexCore.components.system.ComponentResolveListener;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.anchor.AnchorNodeConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.glyph.GlyphNodeConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.LinkSlot;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.input.InputSlotConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.named.NamedSlot;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.named.NamedSlotConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.next.NextSlotConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean.TrileanSlot;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean.TrileanSlotConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingCleanupSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingDrawModeEnterListener;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingImportSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingPlaceSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingPrimarySystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingShapeDrawnSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.CraftingTickSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.EncodingCaptureSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component.DecryptingState;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system.DecryptingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system.DecryptingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system.DecryptingShapeDrawnSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system.DecryptingTickSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingEnterListener;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingPlaceSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingShapeDrawnSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingTeardownSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingTickSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system.FlycastingUnequipSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.nodes.preview.PreviewNodeConfig;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingChangeListener;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingForceExitSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingSlotSelectSystem;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.system.SelectingTickSystem;
import com.riprod.hexcode.builtin.hexCore.eventListeners.CastGateListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.CraftingNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.FizzleMessageListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphDrawNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphDrawnDisabledNotificationListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphGateListener;
import com.riprod.hexcode.builtin.hexCore.eventListeners.GlyphMemoryListener;
import com.riprod.hexcode.builtin.hexCore.execution.config.EncodedConfig;
import com.riprod.hexcode.builtin.hexCore.execution.config.ExecutionConfig;
import com.riprod.hexcode.builtin.hexCore.execution.config.OverlayConfig;
import com.riprod.hexcode.builtin.hexCore.glyphs.deprecated.DeprecatedGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.chaos.ChaosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.ConcentrationConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.ConcentrationGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.ConjureGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.system.ConjureConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.DisguiseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.handler.DisguiseConstructHandler;
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
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.GlaciateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.GlaciateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth.GrowthConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth.GrowthGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.gust.GustGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt.HaltConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.halt.HaltGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.IlluminateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.IlluminateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact.InteractGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interaction.InteractionGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interfere.InterfereGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.InvisibilityConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.InvisibilityDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.InvisibilityGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.LevitateConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.LevitateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.MageArmorConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.MageArmorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.MagicHealthDamageSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase.PhaseComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase.PhaseConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase.PhaseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.resonate.ResonateGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.ScaleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.handler.ScaleConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.ShatterGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.system.ShatterTeardownSystem;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction.HexShatterBounceInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction.HexShatterHitInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction.HexShatterMissInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap.SwapGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.terraform.TerraformGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.WardConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.WardGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.warp.WarpGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.arc.ArcConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.arc.ArcGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area.AreaConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area.AreaGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.beam.BeamGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.ProjectileGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction.HexProjectileBounceInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction.HexProjectileHitInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction.HexProjectileMissInteraction;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.system.ProjectileConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.absolute.AbsoluteGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.add.AddGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.arccos.ArccosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.arcsin.ArcsinGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.arctan.ArctanGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utils.BaseGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.ceiling.CeilingGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.color.ColorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.compare.CompareGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component.ComponentExpander;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component.ComponentGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component.ComponentPorts;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.cos.CosGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.cross.CrossGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.debug.DebugGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.DelayConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.DelayGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.divide.DivideGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.dot.DotGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.equal.EqualGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.floor.FloorGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.greater.GreaterGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.IdentifyGlyph;
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
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.query.QueryGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.root.RootGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.RotationValue;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.handler.RotationConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.round.RoundGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.self.SelfGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.shape.ShapeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.sin.SinGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot.SlotGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.sound.SoundGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.style.StyleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.subtract.SubtractGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.tan.TanGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.variable.VariableValue;
import com.riprod.hexcode.builtin.hexCore.imbuements.profiles.ArmorProfile;
import com.riprod.hexcode.builtin.hexCore.imbuements.profiles.BlockProfile;
import com.riprod.hexcode.builtin.hexCore.imbuements.profiles.BookProfile;
import com.riprod.hexcode.builtin.hexCore.imbuements.profiles.ComponentProfile;
import com.riprod.hexcode.builtin.hexCore.imbuements.profiles.WeaponProfile;
import com.riprod.hexcode.builtin.hexCore.impact.ConstantImpact;
import com.riprod.hexcode.builtin.hexCore.impact.ExponentialImpact;
import com.riprod.hexcode.builtin.hexCore.impact.LinearImpact;
import com.riprod.hexcode.builtin.hexCore.impact.PowerLawImpact;
import com.riprod.hexcode.builtin.hexCore.impact.RatioToDefaultImpact;
import com.riprod.hexcode.builtin.hexCore.impact.SphereVolumeImpact;
import com.riprod.hexcode.builtin.hexCore.impact.ThresholdImpact;
import com.riprod.hexcode.builtin.hexCore.obelisks.accuracy.AccuracyObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.efficiency.EfficiencyObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.interactions.EncryptionArmInteraction;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.ImportExportObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.interactions.ImportInteraction;
import com.riprod.hexcode.builtin.hexCore.obelisks.seeker.SeekerObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.seeker.interactions.HexNameInteraction;
import com.riprod.hexcode.builtin.hexCore.pedestals.PedestalContextHandler;
import com.riprod.hexcode.builtin.hexCore.staffStyles.ArcStyle;
import com.riprod.hexcode.builtin.hexCore.staffStyles.RingStyle;
import com.riprod.hexcode.builtin.hexCore.staffStyles.SphereStyle;
import com.riprod.hexcode.core.common.appearance.HexAppearanceComponent;
import com.riprod.hexcode.core.common.appearance.HexAppearanceRevertSystem;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleRegistry;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.execution.resource.GateStateResource;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.RawStateSlot;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;
import com.riprod.hexcode.core.common.pedestal.events.PedestalInteractEvent;

public class HexCorePlugin {

        public static void setup(JavaPlugin plugin) {
                RegisterImpacts(plugin);
                RegisterGlyphs(plugin);
                RegisterStyles(plugin);
                RegisterObelisks(plugin);
                RegisterNodes(plugin);
                RegisterComponents(plugin);
                RegisterSystems(plugin);
                RegisterConstructs(plugin);
                RegisterInteractions(plugin);
                RegisterEvents(plugin);
                RegisterHexConfigs(plugin);
                RegisterProfiles(plugin);
                plugin.getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                                "Internal", plugin.getManifest().getVersion().toString());
        }

        private static void RegisterImpacts(JavaPlugin plugin) {
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

        private static void RegisterGlyphs(JavaPlugin plugin) {

                GlyphAsset.register(BaseGlyph.class, BaseGlyph.TYPE, BaseGlyph::new);
                GlyphAsset.register(DeprecatedGlyph.class, DeprecatedGlyph.TYPE, DeprecatedGlyph::new);

                // utilities
                GlyphAsset.register(MultiplyGlyph.class, MultiplyGlyph.TYPE, MultiplyGlyph::new);
                GlyphAsset.register(AddGlyph.class, AddGlyph.TYPE, AddGlyph::new);
                GlyphAsset.register(SubtractGlyph.class, SubtractGlyph.TYPE, SubtractGlyph::new);
                GlyphAsset.register(DivideGlyph.class, DivideGlyph.TYPE, DivideGlyph::new);
                GlyphAsset.register(ModuloGlyph.class, ModuloGlyph.TYPE, ModuloGlyph::new);
                GlyphAsset.register(EqualGlyph.class, EqualGlyph.TYPE, EqualGlyph::new);
                GlyphAsset.register(CompareGlyph.class, CompareGlyph.TYPE, CompareGlyph::new);
                GlyphAsset.register(GreaterGlyph.class, GreaterGlyph.TYPE, GreaterGlyph::new);
                GlyphAsset.register(LessGlyph.class, LessGlyph.TYPE, LessGlyph::new);
                GlyphAsset.register(SinGlyph.class, SinGlyph.TYPE, SinGlyph::new);
                GlyphAsset.register(CosGlyph.class, CosGlyph.TYPE, CosGlyph::new);
                GlyphAsset.register(TanGlyph.class, TanGlyph.TYPE, TanGlyph::new);
                GlyphAsset.register(ArcsinGlyph.class, ArcsinGlyph.TYPE, ArcsinGlyph::new);
                GlyphAsset.register(ArccosGlyph.class, ArccosGlyph.TYPE, ArccosGlyph::new);
                GlyphAsset.register(ArctanGlyph.class, ArctanGlyph.TYPE, ArctanGlyph::new);
                GlyphAsset.register(AbsoluteGlyph.class, AbsoluteGlyph.TYPE, AbsoluteGlyph::new);
                GlyphAsset.register(FloorGlyph.class, FloorGlyph.TYPE, FloorGlyph::new);
                GlyphAsset.register(CeilingGlyph.class, CeilingGlyph.TYPE, CeilingGlyph::new);
                GlyphAsset.register(RoundGlyph.class, RoundGlyph.TYPE, RoundGlyph::new);
                GlyphAsset.register(PowerGlyph.class, PowerGlyph.TYPE, PowerGlyph::new);
                GlyphAsset.register(RootGlyph.class, RootGlyph.TYPE, RootGlyph::new);
                GlyphAsset.register(StyleGlyph.class, StyleGlyph.TYPE, StyleGlyph::new);
                GlyphAsset.register(ColorGlyph.class, ColorGlyph.TYPE, ColorGlyph::new);
                GlyphAsset.register(ShapeGlyph.class, ShapeGlyph.TYPE, ShapeGlyph::new);
                GlyphAsset.register(SoundGlyph.class, SoundGlyph.TYPE, SoundGlyph::new);
                GlyphAsset.register(PositionValue.class, PositionValue.TYPE, PositionValue::new);
                GlyphAsset.register(RotationValue.class, RotationValue.TYPE, RotationValue.CODEC);
                GlyphAsset.register(QueryGlyph.class, QueryGlyph.TYPE, QueryGlyph::new);
                GlyphAsset.register(DotGlyph.class, DotGlyph.TYPE, DotGlyph::new);
                GlyphAsset.register(CrossGlyph.class, CrossGlyph.TYPE, CrossGlyph::new);
                GlyphAsset.register(NumberValue.class, NumberValue.TYPE, NumberValue.CODEC);
                GlyphAsset.register(VariableValue.class, VariableValue.TYPE, VariableValue::new);
                GlyphAsset.register(PiValue.class, PiValue.TYPE, PiValue::new);
                GlyphAsset.register(DebugGlyph.class, DebugGlyph.TYPE, DebugGlyph::new);
                GlyphAsset.register(OutputGlyph.class, OutputGlyph.TYPE, OutputGlyph::new);
                GlyphAsset.register(SlotGlyph.class, SlotGlyph.TYPE, SlotGlyph::new);
                GlyphAsset.register(ComponentGlyph.class, ComponentGlyph.TYPE, ComponentGlyph::new);
                CastEvent.register(plugin.getEventRegistry(), EventPriority.FIRST.getValue(), new ComponentExpander());
                Glyph.registerPortSchemaSynthesizer(ComponentPorts::schema);
                GlyphAsset.register(IsHoldingValue.class, IsHoldingValue.TYPE, IsHoldingValue::new);
                GlyphAsset.register(OnPrimaryGlyph.class, OnPrimaryGlyph.TYPE, OnPrimaryGlyph::new);
                GlyphAsset.register(OnSecondaryGlyph.class, OnSecondaryGlyph.TYPE, OnSecondaryGlyph::new);
                GlyphAsset.register(OnUseGlyph.class, OnUseGlyph.TYPE, OnUseGlyph::new);
                GlyphAsset.register(OnDeathGlyph.class, OnDeathGlyph.TYPE, OnDeathGlyph::new);
                GlyphAsset.register(OnCastGlyph.class, OnCastGlyph.TYPE, OnCastGlyph::new);

                // tier 1 glyphs
                GlyphAsset.register(SelfGlyph.class, SelfGlyph.TYPE, SelfGlyph::new);
                GlyphAsset.register(ChaosGlyph.class, ChaosGlyph.TYPE, ChaosGlyph::new);
                GlyphAsset.register(ForceGlyph.class, ForceGlyph.TYPE, ForceGlyph::new);
                GlyphAsset.register(DelayGlyph.class, DelayGlyph.TYPE, DelayGlyph.CODEC);
                GlyphAsset.register(DrainGlyph.class, DrainGlyph.TYPE, DrainGlyph.CODEC);
                GlyphAsset.register(HaltGlyph.class, HaltGlyph.TYPE, HaltGlyph.CODEC);

                // tier 2 glyphs
                GlyphAsset.register(BeamGlyph.class, BeamGlyph.TYPE, BeamGlyph.CODEC);
                GlyphAsset.register(AreaGlyph.class, AreaGlyph.TYPE, AreaGlyph.CODEC);
                GlyphAsset.register(ProjectileGlyph.class, ProjectileGlyph.TYPE, ProjectileGlyph.CODEC);
                GlyphAsset.register(ArcGlyph.class, ArcGlyph.TYPE, ArcGlyph.CODEC);

                // tier 3 glyphs
                GlyphAsset.register(InterfereGlyph.class, InterfereGlyph.TYPE, InterfereGlyph::new);
                GlyphAsset.register(ResonateGlyph.class, ResonateGlyph.TYPE, ResonateGlyph::new);
                GlyphAsset.register(GustGlyph.class, GustGlyph.TYPE, GustGlyph.CODEC);
                GlyphAsset.register(ConjureGlyph.class, ConjureGlyph.TYPE, ConjureGlyph.CODEC);
                GlyphAsset.register(GrowthGlyph.class, GrowthGlyph.TYPE, GrowthGlyph.CODEC);
                GlyphAsset.register(ErodeGlyph.class, ErodeGlyph.TYPE, ErodeGlyph.CODEC);
                GlyphAsset.register(LevitateGlyph.class, LevitateGlyph.TYPE, LevitateGlyph.CODEC);
                GlyphAsset.register(InvisibilityGlyph.class, InvisibilityGlyph.TYPE, InvisibilityGlyph.CODEC);
                GlyphAsset.register(ScaleGlyph.class, ScaleGlyph.TYPE, ScaleGlyph.CODEC);
                GlyphAsset.register(DisguiseGlyph.class, DisguiseGlyph.TYPE, DisguiseGlyph.CODEC);
                GlyphAsset.register(DomainGlyph.class, DomainGlyph.TYPE, DomainGlyph.CODEC);
                GlyphAsset.register(InteractGlyph.class, InteractGlyph.TYPE, InteractGlyph.CODEC);
                GlyphAsset.register(ShatterGlyph.class, ShatterGlyph.TYPE, ShatterGlyph.CODEC);
                GlyphAsset.register(GlaciateGlyph.class, GlaciateGlyph.TYPE, GlaciateGlyph.CODEC);
                GlyphAsset.register(TerraformGlyph.class, TerraformGlyph.TYPE, TerraformGlyph::new);
                GlyphAsset.register(EnsnareGlyph.class, EnsnareGlyph.TYPE, EnsnareGlyph.CODEC);
                GlyphAsset.register(PhaseGlyph.class, PhaseGlyph.TYPE, PhaseGlyph.CODEC);
                GlyphAsset.register(WarpGlyph.class, WarpGlyph.TYPE, WarpGlyph.CODEC);
                GlyphAsset.register(SwapGlyph.class, SwapGlyph.TYPE, SwapGlyph.CODEC);
                GlyphAsset.register(InteractionGlyph.class, InteractionGlyph.TYPE, InteractionGlyph.CODEC);
                GlyphAsset.register(ConcentrationGlyph.class, ConcentrationGlyph.TYPE, ConcentrationGlyph.CODEC);
                GlyphAsset.register(WardGlyph.class, WardGlyph.TYPE, WardGlyph.CODEC);

                // tier 4 glyphs
                GlyphAsset.register(MageArmorGlyph.class, MageArmorGlyph.TYPE, MageArmorGlyph.CODEC);
                GlyphAsset.register(IdentifyGlyph.class, IdentifyGlyph.TYPE, IdentifyGlyph::new);
                GlyphAsset.register(IlluminateGlyph.class, IlluminateGlyph.TYPE, IlluminateGlyph.CODEC);

        }

        private static void RegisterObelisks(JavaPlugin plugin) {
                ObeliskHandlerRegistry.register("seeker", new SeekerObelisk());
                ObeliskHandlerRegistry.register("accuracy", new AccuracyObelisk());
                ObeliskHandlerRegistry.register("efficiency", new EfficiencyObelisk());
                ObeliskHandlerRegistry.register("import_export", new ImportExportObelisk());
                ObeliskHandlerRegistry.register(EncryptionObelisk.HANDLER_ID, new EncryptionObelisk());
        }

        private static void RegisterStyles(JavaPlugin plugin) {
                CastingStyleRegistry.register(new ArcStyle());
                CastingStyleRegistry.register(new RingStyle());
                CastingStyleRegistry.register(new SphereStyle());
                CastingStyleRegistry.setDefault(RingStyle.ID);
        }

        private static void RegisterNodes(JavaPlugin plugin) {
                Slot.registerType(Priority.DEFAULT, "Link", LinkSlot.class, LinkSlot.CODEC);
                Slot.registerType("Boolean", TrileanSlot.class, TrileanSlot.CODEC);
                Slot.registerType(RawStateSlot.TYPE, RawStateSlot.class, RawStateSlot.CODEC);
                Slot.registerType(NamedSlotConfig.TYPE, NamedSlot.class, NamedSlot.CODEC);

                NodeConfig.CODEC.register(InputSlotConfig.TYPE, InputSlotConfig.class, InputSlotConfig.CODEC);
                NodeConfig.CODEC.register(NextSlotConfig.TYPE, NextSlotConfig.class, NextSlotConfig.CODEC);
                NodeConfig.CODEC.register(TrileanSlotConfig.TYPE, TrileanSlotConfig.class, TrileanSlotConfig.CODEC);
                NodeConfig.CODEC.register(NamedSlotConfig.TYPE, NamedSlotConfig.class, NamedSlotConfig.CODEC);
                NodeConfig.CODEC.register(AnchorNodeConfig.TYPE, AnchorNodeConfig.class, AnchorNodeConfig.CODEC);
                NodeConfig.CODEC.register(PreviewNodeConfig.TYPE, PreviewNodeConfig.class,
                                PreviewNodeConfig.CODEC);
                NodeConfig.CODEC.register(GlyphNodeConfig.TYPE, GlyphNodeConfig.class, GlyphNodeConfig.CODEC);
        }

        private static void RegisterInteractions(JavaPlugin plugin) {
                Interaction.CODEC.register("HexImportExportInteraction", ImportInteraction.class,
                                ImportInteraction.CODEC);
                Interaction.CODEC.register("HexNameInteraction", HexNameInteraction.class,
                                HexNameInteraction.CODEC);
                Interaction.CODEC.register("HexEncryptionArmInteraction", EncryptionArmInteraction.class,
                                EncryptionArmInteraction.CODEC);
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

        private static void RegisterEvents(JavaPlugin plugin) {
                plugin.getEventRegistry().registerGlobal(GlyphFizzleEvent.class, new FizzleMessageListener());
                plugin.getEventRegistry().registerGlobal(CraftingEvent.class, new CraftingNotificationListener());
                plugin.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphMemoryListener());
                plugin.getEventRegistry().registerGlobal(GlyphDrawnEvent.class, new GlyphDrawNotificationListener());
                plugin.getEventRegistry().registerGlobal(GlyphDrawnEvent.class,
                                new GlyphDrawnDisabledNotificationListener());
                plugin.getEventRegistry().registerGlobal(PedestalInteractEvent.class, new PedestalContextHandler());
                plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class,
                                ContextForceExitSystem::onPlayerDisconnect);
        }

        private static void RegisterHexConfigs(JavaPlugin plugin) {
                HexConfigAsset.CODEC.register(Priority.DEFAULT, "Overlay", OverlayConfig.class, OverlayConfig.CODEC);
                HexConfigAsset.CODEC.register("ExecutionConfig", ExecutionConfig.class, ExecutionConfig.CODEC);
                HexConfigAsset.CODEC.register("EncodedConfig", EncodedConfig.class, EncodedConfig.CODEC);
        }

        private static void RegisterProfiles(JavaPlugin plugin) {
                ImbuementProfileAsset.CODEC.register("Book", BookProfile.class, BookProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Weapon", WeaponProfile.class, WeaponProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Armor", ArmorProfile.class, ArmorProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Block", BlockProfile.class, BlockProfile.CODEC);
                ImbuementProfileAsset.CODEC.register("Component", ComponentProfile.class, ComponentProfile.CODEC);

        }

        private static void RegisterComponents(JavaPlugin plugin) {
                ComponentRegistryProxy<EntityStore> entityStoreRegistry = plugin.getEntityStoreRegistry();

                var hexProjectileStateType = entityStoreRegistry
                                .registerComponent(ProjectileState.class, ProjectileState::new);
                ProjectileState.setComponentType(hexProjectileStateType);

                var conjureZoneType = entityStoreRegistry
                                .registerComponent(ConjureZoneComponent.class, ConjureZoneComponent::new);
                ConjureZoneComponent.setComponentType(conjureZoneType);

                var phaseComponentType = entityStoreRegistry
                                .registerComponent(PhaseComponent.class, PhaseComponent::new);
                PhaseComponent.setComponentType(phaseComponentType);

                var ensnareComponentType = entityStoreRegistry
                                .registerComponent(EnsnareComponent.class, EnsnareComponent::new);
                EnsnareComponent.setComponentType(ensnareComponentType);

                var glaciateComponentType = entityStoreRegistry
                                .registerComponent(GlaciateComponent.class, GlaciateComponent::new);
                GlaciateComponent.setComponentType(glaciateComponentType);

                var shatterStateType = entityStoreRegistry
                                .registerComponent(ShatterState.class, ShatterState::new);
                ShatterState.setComponentType(shatterStateType);

                var domainZoneComponentType = entityStoreRegistry
                                .registerComponent(DomainZoneComponent.class, DomainZoneComponent::new);
                DomainZoneComponent.setComponentType(domainZoneComponentType);

                var hexConstructType = entityStoreRegistry
                                .registerComponent(HexEffectsComponent.class, HexEffectsComponent::new);
                HexEffectsComponent.setComponentType(hexConstructType);

                var hexAppearanceComponentType = entityStoreRegistry
                                .registerComponent(HexAppearanceComponent.class, "HexAppearance",
                                                HexAppearanceComponent.CODEC);
                HexAppearanceComponent.setComponentType(hexAppearanceComponentType);

                var magicHealthComponentType = entityStoreRegistry
                                .registerComponent(MagicHealthComponent.class, MagicHealthComponent::new);
                MagicHealthComponent.setComponentType(magicHealthComponentType);

                var flycastingStateType = entityStoreRegistry
                                .registerComponent(FlycastingState.class, FlycastingState::new);
                FlycastingState.setComponentType(flycastingStateType);

                var selectingStateType = entityStoreRegistry
                                .registerComponent(SelectingState.class, SelectingState::new);
                SelectingState.setComponentType(selectingStateType);

                var decryptingStateType = entityStoreRegistry
                                .registerComponent(DecryptingState.class, DecryptingState::new);
                DecryptingState.setComponentType(decryptingStateType);

                var craftingStateType = entityStoreRegistry
                                .registerComponent(CraftingState.class, CraftingState::new);
                CraftingState.setComponentType(craftingStateType);

                var pasteCacheType = plugin.getEntityStoreRegistry()
                                .registerComponent(ComponentPasteCache.class, ComponentPasteCache::new);
                ComponentPasteCache.setComponentType(pasteCacheType);
        }

        private static void RegisterSystems(JavaPlugin plugin) {
                ComponentRegistryProxy<EntityStore> entityStoreRegistry = plugin.getEntityStoreRegistry();

                entityStoreRegistry.registerSystem(new HexAppearanceRevertSystem());

                entityStoreRegistry.registerSystem(new MagicHealthDamageSystem());

                entityStoreRegistry.registerSystem(new InvisibilityDamageSystem());
                entityStoreRegistry.registerSystem(new ShatterTeardownSystem());

                entityStoreRegistry.registerSystem(new ContextForceExitSystem.OnDeath());
                entityStoreRegistry.registerSystem(new ContextForceExitSystem.OnUnload());

                entityStoreRegistry.registerSystem(new FlycastingEnterListener());
                entityStoreRegistry.registerSystem(new FlycastingChangeListener());
                entityStoreRegistry.registerSystem(new FlycastingTeardownSystem());
                entityStoreRegistry.registerSystem(new FlycastingTickSystem());
                entityStoreRegistry.registerSystem(new FlycastingShapeDrawnSystem());
                entityStoreRegistry.registerSystem(new FlycastingPlaceSystem());
                entityStoreRegistry.registerSystem(new FlycastingExitSystem());
                entityStoreRegistry.registerSystem(new FlycastingUnequipSystem());
                entityStoreRegistry.registerSystem(new FlycastingForceExitSystem());

                entityStoreRegistry.registerSystem(new SelectingChangeListener());
                entityStoreRegistry.registerSystem(new SelectingTickSystem());
                entityStoreRegistry.registerSystem(new SelectingSlotSelectSystem());
                entityStoreRegistry.registerSystem(new SelectingForceExitSystem());

                entityStoreRegistry.registerSystem(new DecryptingChangeListener());
                entityStoreRegistry.registerSystem(new DecryptingTickSystem());
                entityStoreRegistry.registerSystem(new DecryptingShapeDrawnSystem());
                entityStoreRegistry.registerSystem(new DecryptingForceExitSystem());

                entityStoreRegistry.registerSystem(new CraftingChangeListener());
                entityStoreRegistry.registerSystem(new CraftingDrawModeEnterListener());
                entityStoreRegistry.registerSystem(new CraftingTickSystem());
                entityStoreRegistry.registerSystem(new CraftingPrimarySystem());
                entityStoreRegistry.registerSystem(new CraftingImportSystem());
                entityStoreRegistry.registerSystem(new CraftingShapeDrawnSystem());
                entityStoreRegistry.registerSystem(new CraftingPlaceSystem());
                entityStoreRegistry.registerSystem(new EncodingCaptureSystem());
                entityStoreRegistry.registerSystem(new CraftingForceExitSystem());
                entityStoreRegistry.registerSystem(new CraftingCleanupSystem());

                ResourceType<EntityStore, GateStateResource> gateStateType = entityStoreRegistry
                                .registerResource(GateStateResource.class, GateStateResource::new);
                GateStateResource.setResourceType(gateStateType);
                CastEvent.register(plugin.getEventRegistry(), EventPriority.EARLY.getValue(), new CastGateListener());
                entityStoreRegistry.registerSystem(new GlyphGateListener());

                entityStoreRegistry.registerSystem(new ComponentCacheSystem());
                entityStoreRegistry.registerSystem(new ComponentCacheCleanupSystem());
                entityStoreRegistry.registerSystem(new ComponentResolveListener());
        }

        private static void RegisterConstructs(JavaPlugin plugin) {
                ConstructRegistry.register(ScaleGlyph.TYPE, new ScaleConstructHandler());
                ConstructRegistry.register(DisguiseGlyph.TYPE, new DisguiseConstructHandler());
                ConstructRegistry.register(ConcentrationGlyph.TYPE, new ConcentrationConstructHandler());
                ConstructRegistry.register(WardGlyph.TYPE, new WardConstructHandler());
                ConstructRegistry.register(DomainGlyph.TYPE, new DomainConstructHandler());
                ConstructRegistry.register(GlaciateGlyph.TYPE, new GlaciateConstructHandler());
                ConstructRegistry.register(ArcGlyph.TYPE, new ArcConstructHandler());
                ConstructRegistry.register(AreaGlyph.TYPE, new AreaConstructHandler());
                ConstructRegistry.register(PhaseGlyph.TYPE, new PhaseConstructHandler());
                ConstructRegistry.register(ConjureGlyph.TYPE, new ConjureConstructHandler());
                ConstructRegistry.register(ErodeGlyph.TYPE, new ErodeConstructHandler());
                ConstructRegistry.register(InvisibilityGlyph.TYPE, new InvisibilityConstructHandler());
                ConstructRegistry.register(LevitateGlyph.TYPE, new LevitateConstructHandler());
                ConstructRegistry.register(HaltGlyph.TYPE, new HaltConstructHandler());
                ConstructRegistry.register(DrainGlyph.TYPE, new DrainConstructHandler());
                ConstructRegistry.register(DelayGlyph.TYPE, new DelayConstructHandler());
                ConstructRegistry.register(EnsnareGlyph.TYPE, new EnsnareConstructHandler());
                ConstructRegistry.register(MageArmorGlyph.TYPE, new MageArmorConstructHandler());
                ConstructRegistry.register(ProjectileGlyph.TYPE, new ProjectileConstructHandler());
                ConstructRegistry.register(GrowthGlyph.TYPE, new GrowthConstructHandler());
                ConstructRegistry.register(IlluminateGlyph.TYPE, new IlluminateConstructHandler());
                ConstructRegistry.register(RotationValue.TYPE, new RotationConstructHandler());
        }
}
