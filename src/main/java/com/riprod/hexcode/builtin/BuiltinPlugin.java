package com.riprod.hexcode.builtin;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.glyphs.isHolding.IsHoldingValue;
import com.riprod.hexcode.builtin.triggers.Ability1Trigger;
import com.riprod.hexcode.builtin.triggers.Ability2Trigger;
import com.riprod.hexcode.builtin.triggers.Ability3Trigger;
import com.riprod.hexcode.builtin.triggers.AttackedTrigger;
import com.riprod.hexcode.builtin.triggers.BlockTrigger;
import com.riprod.hexcode.builtin.triggers.InteractionTriggerSource;
import com.riprod.hexcode.builtin.triggers.OnAttackTrigger;
import com.riprod.hexcode.builtin.triggers.OnShootTrigger;
import com.riprod.hexcode.builtin.triggers.PrimaryTrigger;
import com.riprod.hexcode.builtin.triggers.SecondaryTrigger;
import com.riprod.hexcode.builtin.triggers.UseTrigger;
import com.riprod.hexcode.builtin.triggers.cast.CastTriggerSource;
import com.riprod.hexcode.builtin.triggers.death.DeathTriggerSource;
import com.riprod.hexcode.builtin.triggers.sources.EntityHitEventSource;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedArmorMarker;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedHotbarMarker;
import com.riprod.hexcode.core.common.imbuement.dispatch.ImbuementTriggerBootstrap;
import com.riprod.hexcode.core.common.imbuement.system.ImbuementMarkerSystem;
import com.riprod.hexcode.core.common.triggers.component.TriggerListenerComponent;
import com.riprod.hexcode.core.common.triggers.registry.ManualTrigger;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.triggers.registry.TriggerRegistry;
import com.riprod.hexcode.core.state.casting.registery.CastingStyleRegistry;
import com.riprod.hexcode.core.common.triggers.handler.TriggerConstructHandler;
import com.riprod.hexcode.builtin.glyphs.absolute.AbsoluteGlyph;
import com.riprod.hexcode.builtin.glyphs.add.AddGlyph;
import com.riprod.hexcode.builtin.glyphs.ceiling.CeilingGlyph;
import com.riprod.hexcode.builtin.glyphs.cos.CosGlyph;
import com.riprod.hexcode.builtin.glyphs.cross.CrossGlyph;
import com.riprod.hexcode.builtin.glyphs.floor.FloorGlyph;
import com.riprod.hexcode.builtin.glyphs.round.RoundGlyph;
import com.riprod.hexcode.builtin.glyphs.pi.PiValue;
import com.riprod.hexcode.builtin.glyphs.power.PowerGlyph;
import com.riprod.hexcode.builtin.glyphs.root.RootGlyph;
import com.riprod.hexcode.builtin.glyphs.sin.SinGlyph;
import com.riprod.hexcode.builtin.glyphs.style.StyleGlyph;
import com.riprod.hexcode.builtin.glyphs.tan.TanGlyph;
import com.riprod.hexcode.builtin.glyphs.arc.ArcConstructHandler;
import com.riprod.hexcode.builtin.glyphs.arc.ArcGlyph;
import com.riprod.hexcode.builtin.glyphs.area.AreaGlyph;
import com.riprod.hexcode.builtin.glyphs.beam.BeamGlyph;
import com.riprod.hexcode.builtin.glyphs.bolt.BoltGlyph;
import com.riprod.hexcode.builtin.glyphs.burning.BurningGlyph;
import com.riprod.hexcode.builtin.glyphs.chaos.ChaosGlyph;
import com.riprod.hexcode.builtin.glyphs.concentration.ConcentrationConstructHandler;
import com.riprod.hexcode.builtin.glyphs.concentration.ConcentrationGlyph;
import com.riprod.hexcode.builtin.glyphs.conjure.ConjureGlyph;
import com.riprod.hexcode.builtin.glyphs.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.glyphs.conjure.system.ConjureConstructHandler;
import com.riprod.hexcode.builtin.glyphs.debug.DebugGlyph;
import com.riprod.hexcode.builtin.glyphs.delay.DelayConstructHandler;
import com.riprod.hexcode.builtin.glyphs.delay.DelayGlyph;
import com.riprod.hexcode.builtin.glyphs.divide.DivideGlyph;
import com.riprod.hexcode.builtin.glyphs.domain.DomainConstructHandler;
import com.riprod.hexcode.builtin.glyphs.domain.DomainGlyph;
import com.riprod.hexcode.builtin.glyphs.domain.DomainAuraConstructHandler;
import com.riprod.hexcode.builtin.glyphs.domain.component.DomainZoneComponent;
import com.riprod.hexcode.builtin.glyphs.dot.DotGlyph;
import com.riprod.hexcode.builtin.glyphs.drain.DrainGlyph;
import com.riprod.hexcode.builtin.glyphs.drain.DrainConstructHandler;
import com.riprod.hexcode.builtin.glyphs.ensnare.EnsnareConstructHandler;
import com.riprod.hexcode.builtin.glyphs.ensnare.EnsnareGlyph;
import com.riprod.hexcode.builtin.glyphs.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.glyphs.equal.EqualGlyph;
import com.riprod.hexcode.builtin.glyphs.erode.ErodeConstructHandler;
import com.riprod.hexcode.builtin.glyphs.erode.ErodeGlyph;
import com.riprod.hexcode.builtin.glyphs.erode.system.ErodeDamageSystem;
import com.riprod.hexcode.builtin.glyphs.force.ForceGlyph;
import com.riprod.hexcode.builtin.glyphs.fortify.FortifyGlyph;
import com.riprod.hexcode.builtin.glyphs.fortify.FortifyConstructHandler;
import com.riprod.hexcode.builtin.glyphs.fortify.system.FortifyDamageSystem;
import com.riprod.hexcode.builtin.glyphs.freeze.FreezeConstructHandler;
import com.riprod.hexcode.builtin.glyphs.freeze.FreezeGlyph;
import com.riprod.hexcode.builtin.glyphs.glaciate.GlaciateConstructHandler;
import com.riprod.hexcode.builtin.glyphs.glaciate.GlaciateGlyph;
import com.riprod.hexcode.builtin.glyphs.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.builtin.glyphs.greater.GreaterGlyph;
import com.riprod.hexcode.builtin.glyphs.growth.GrowthConstructHandler;
import com.riprod.hexcode.builtin.glyphs.growth.GrowthGlyph;
import com.riprod.hexcode.builtin.glyphs.gust.GustGlyph;
import com.riprod.hexcode.builtin.glyphs.halt.HaltGlyph;
import com.riprod.hexcode.builtin.glyphs.halt.HaltConstructHandler;
import com.riprod.hexcode.builtin.glyphs.ignite.IgniteConstructHandler;
import com.riprod.hexcode.builtin.glyphs.ignite.IgniteGlyph;
import com.riprod.hexcode.builtin.glyphs.interfere.InterfereGlyph;
import com.riprod.hexcode.builtin.glyphs.less.LessGlyph;
import com.riprod.hexcode.builtin.glyphs.levitate.LevitateConstructHandler;
import com.riprod.hexcode.builtin.glyphs.levitate.LevitateGlyph;
import com.riprod.hexcode.builtin.glyphs.multiply.MultiplyGlyph;
import com.riprod.hexcode.builtin.glyphs.number.NumberValue;
import com.riprod.hexcode.builtin.glyphs.onCast.OnCastGlyph;
import com.riprod.hexcode.builtin.glyphs.onDeath.OnDeathGlyph;
import com.riprod.hexcode.builtin.glyphs.onPrimary.OnPrimaryGlyph;
import com.riprod.hexcode.builtin.glyphs.onSecondary.OnSecondaryGlyph;
import com.riprod.hexcode.builtin.glyphs.onUse.OnUseGlyph;
import com.riprod.hexcode.builtin.glyphs.output.OutputGlyph;
import com.riprod.hexcode.builtin.glyphs.phase.PhaseComponent;
import com.riprod.hexcode.builtin.glyphs.phase.PhaseConstructHandler;
import com.riprod.hexcode.builtin.glyphs.phase.PhaseGlyph;
import com.riprod.hexcode.builtin.glyphs.position.PositionValue;
import com.riprod.hexcode.builtin.glyphs.projectile.ProjectileGlyph;
import com.riprod.hexcode.builtin.glyphs.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.glyphs.projectile.system.ProjectileConstructHandler;
import com.riprod.hexcode.builtin.glyphs.resonate.ResonateGlyph;
import com.riprod.hexcode.builtin.glyphs.rotation.RotationValue;
import com.riprod.hexcode.builtin.glyphs.scale.ScaleGlyph;
import com.riprod.hexcode.builtin.glyphs.scale.handler.ScaleConstructHandler;
import com.riprod.hexcode.builtin.glyphs.self.SelfGlyph;
import com.riprod.hexcode.builtin.glyphs.shatter.ShatterGlyph;
import com.riprod.hexcode.builtin.glyphs.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.glyphs.subtract.SubtractGlyph;
import com.riprod.hexcode.builtin.glyphs.swap.SwapGlyph;
import com.riprod.hexcode.builtin.glyphs.terraform.TerraformGlyph;
import com.riprod.hexcode.builtin.glyphs.variable.VariableValue;
import com.riprod.hexcode.builtin.glyphs.warp.WarpGlyph;
import com.riprod.hexcode.builtin.obelisks.accuracy.AccuracyObelisk;
import com.riprod.hexcode.builtin.obelisks.efficiency.EfficiencyObelisk;
import com.riprod.hexcode.builtin.obelisks.importexport.ImportExportObelisk;
import com.riprod.hexcode.builtin.obelisks.importexport.interactions.ImportInteraction;
import com.riprod.hexcode.builtin.obelisks.seeker.SeekerObelisk;
import com.riprod.hexcode.builtin.staffStyles.ArcStyle;
import com.riprod.hexcode.builtin.staffStyles.RingStyle;
import com.riprod.hexcode.builtin.staffStyles.SphereStyle;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;

public class BuiltinPlugin extends JavaPlugin {

    private boolean initialized = false;

    public BuiltinPlugin(JavaPluginInit init) {
        super(init);
    }

    public void startup() {
        if (initialized) {
            return;
        }
        RegisterGlyphs();
        RegisterStyles();
        RegisterObelisks();
        RegisterComponents();
        RegisterSystems();
        RegisterConstructs();
        RegisterInteractions();            

        initialized = true;
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

        for (int i = 1; i <= 16; i++) {
            GlyphRegistry.register(new NumberValue(i));
        }
        GlyphRegistry.register(new VariableValue());
        GlyphRegistry.register(new PiValue());

        GlyphRegistry.register(new DebugGlyph());

        GlyphRegistry.register(new OutputGlyph());

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

    private void RegisterInteractions() {
        Interaction.CODEC.register("HexImportExportInteraction", ImportInteraction.class, ImportInteraction.CODEC);
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

        ComponentType<EntityStore, TriggerListenerComponent> triggerListenerType = entityStoreRegistry
                .registerComponent(TriggerListenerComponent.class, TriggerListenerComponent::new);
        TriggerListenerComponent.setComponentType(triggerListenerType);

        ComponentType<EntityStore, ImbuedHotbarMarker> hotbarMarkerType = entityStoreRegistry
                .registerComponent(ImbuedHotbarMarker.class, ImbuedHotbarMarker::new);
        ImbuedHotbarMarker.setComponentType(hotbarMarkerType);

        ComponentType<EntityStore, ImbuedArmorMarker> armorMarkerType = entityStoreRegistry
                .registerComponent(ImbuedArmorMarker.class, ImbuedArmorMarker::new);
        ImbuedArmorMarker.setComponentType(armorMarkerType);
    }

    private void RegisterSystems() {
        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();

        entityStoreRegistry.registerSystem(new ErodeDamageSystem());
        entityStoreRegistry.registerSystem(new FortifyDamageSystem());

        entityStoreRegistry.registerSystem(new DeathTriggerSource());
        entityStoreRegistry.registerSystem(new CastTriggerSource());
        InteractionTriggerSource.register();

        entityStoreRegistry.registerSystem(new EntityHitEventSource.OnDamageDealtSystem());
        entityStoreRegistry.registerSystem(new EntityHitEventSource.OnDamageReceivedSystem());

        entityStoreRegistry.registerSystem(new ImbuementMarkerSystem());

        TriggerRegistry.register(new PrimaryTrigger());
        TriggerRegistry.register(new SecondaryTrigger());
        TriggerRegistry.register(new UseTrigger());
        TriggerRegistry.register(new Ability1Trigger());
        TriggerRegistry.register(new Ability2Trigger());
        TriggerRegistry.register(new Ability3Trigger());
        TriggerRegistry.register(new OnAttackTrigger());
        TriggerRegistry.register(new OnShootTrigger());
        TriggerRegistry.register(new BlockTrigger());
        TriggerRegistry.register(new AttackedTrigger());

        for (int i = 1; i <= 10; i++) {
            TriggerRegistry.register(new ManualTrigger(Integer.toString(i)));
        }
        TriggerRegistry.register(new ManualTrigger("Default"));

        TriggerListenerRegistry.registerBootstrap(ImbuementTriggerBootstrap::register);
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

        ConstructRegistry.register(TriggerConstructHandler.HANDLER_ID, new TriggerConstructHandler());
    }
}
