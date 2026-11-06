package com.riprod.hexcode.builtin.elementalism;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.builtin.elementalism.charge.ElementalComponent;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeAsset;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeIndex;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;
import com.riprod.hexcode.builtin.elementalism.element.ElementTierIndex;
import com.riprod.hexcode.builtin.elementalism.elements.fire.ExplosionTier;
import com.riprod.hexcode.builtin.elementalism.elements.fire.FireElement;
import com.riprod.hexcode.builtin.elementalism.elements.fire.IgniteTier;
import com.riprod.hexcode.builtin.elementalism.elements.fire.ScorchTier;
import com.riprod.hexcode.builtin.elementalism.glyphs.catalyst.CatalystGlyph;
import com.riprod.hexcode.builtin.elementalism.status.StatusAsset;
import com.riprod.hexcode.builtin.elementalism.status.block.BlockStatusSection;
import com.riprod.hexcode.builtin.elementalism.status.block.BlockStatusSystem;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;

public class ElementalismPlugin {

    public static void setup(JavaPlugin plugin) {
        registerCastComponents();
        registerGlyphs();
        registerElements();
        registerAssets();
        registerListeners(plugin);
        registerAppliedState(plugin);
        plugin.getLogger().atFine().log("Hexcode %s sub-plugin v%s initializing...",
                "Elementalism", plugin.getManifest().getVersion().toString());
    }

    private static void registerCastComponents() {
        HexComponentRegistry.requireLoaded(ElementalComponent.TYPE);
    }

    private static void registerGlyphs() {
        GlyphAsset.register(CatalystGlyph.class, CatalystGlyph.TYPE, CatalystGlyph::new);
    }

    private static void registerElements() {
        ElementAsset.CODEC.register(FireElement.TYPE, FireElement.class, FireElement.CODEC);
        ElementalTier.CODEC.register(IgniteTier.TYPE, IgniteTier.class, IgniteTier.CODEC);
        ElementalTier.CODEC.register(ScorchTier.TYPE, ScorchTier.class, ScorchTier.CODEC);
        ElementalTier.CODEC.register(ExplosionTier.TYPE, ExplosionTier.class, ExplosionTier.CODEC);
    }

    private static void registerAssets() {
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(StatusAsset.class, new DefaultAssetMap<>())
                        .setPath("Hexcode/Elementalism/Status")
                        .setCodec(StatusAsset.CODEC)
                        .setKeyFunction(StatusAsset::getId)
                        .loadsAfter(EntityEffect.class, ParticleSystem.class, SoundEvent.class, GlyphEffects.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(ElementAsset.class, new DefaultAssetMap<>())
                        .setPath("Hexcode/Elementalism/Element")
                        .setCodec(ElementAsset.CODEC)
                        .setKeyFunction(ElementAsset::getId)
                        .loadsAfter(StatusAsset.class, ModelAsset.class, ParticleSystem.class, SoundEvent.class,
                                Fluid.class, DamageCause.class, GlyphEffects.class)
                        .loadsBefore(Interaction.class, RootInteraction.class, Item.class, HexConfigAsset.class,
                                ImbuementProfileAsset.class)
                        .build());
        AssetRegistry.register(
                HytaleAssetStore
                        .builder(CompositeAsset.class, new DefaultAssetMap<>())
                        .setPath("Hexcode/Elementalism/Composite")
                        .setCodec(CompositeAsset.CODEC)
                        .setKeyFunction(CompositeAsset::getId)
                        .loadsAfter(ElementAsset.class, StatusAsset.class, ParticleSystem.class, SoundEvent.class,
                                GlyphEffects.class)
                        .build());
    }

    private static void registerListeners(JavaPlugin plugin) {
        var events = plugin.getEventRegistry();
        events.register(LoadedAssetsEvent.class, ElementAsset.class, ElementTierIndex::onLoaded);
        events.register(RemovedAssetsEvent.class, ElementAsset.class, ElementTierIndex::onRemoved);
        events.register(LoadedAssetsEvent.class, CompositeAsset.class, CompositeIndex::onLoaded);
        events.register(RemovedAssetsEvent.class, CompositeAsset.class, CompositeIndex::onRemoved);
        CastEvent.register(events, EventPriority.LATE.getValue(), ElementalComponent::onCast);
    }

    private static void registerAppliedState(JavaPlugin plugin) {
        var chunkStoreRegistry = plugin.getChunkStoreRegistry();
        var blockStatusSectionType = chunkStoreRegistry
                .registerComponent(BlockStatusSection.class, BlockStatusSection::new);
        BlockStatusSection.setComponentType(blockStatusSectionType);
        chunkStoreRegistry.registerSystem(new BlockStatusSystem(blockStatusSectionType));
    }
}
