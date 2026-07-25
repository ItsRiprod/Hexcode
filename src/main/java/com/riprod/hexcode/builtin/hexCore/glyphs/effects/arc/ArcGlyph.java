package com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

import com.riprod.hexcode.utils.HexVarUtil;

public class ArcGlyph implements GlyphHandler {

    public static final String ID = "Arc";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return 0f;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ArcConfig.class, ArcConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ArcConfig config = getConfig(ArcConfig.class, asset);
        if (config == null) config = ArcConfig.DEFAULTS;

        List<String> outputLinks = glyph.getNextLinks();
        if (outputLinks.isEmpty()) {
            return;
        }

        HexVar targetVar = glyph.readSlot(ArcGlyphSlots.TARGET, hexContext);
        if (targetVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is required");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        int iterations = (int) Math.round(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ArcGlyphSlots.ITERATIONS, hexContext), asset.getSlot(ArcGlyphSlots.ITERATIONS)));
        float interval = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ArcGlyphSlots.INTERVAL, hexContext), asset.getSlot(ArcGlyphSlots.INTERVAL)).floatValue();
        float range = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ArcGlyphSlots.RANGE, hexContext), asset.getSlot(ArcGlyphSlots.RANGE)).floatValue();

        if (iterations <= 0) {
            return;
        }

        Set<UUID> exclusions = new HashSet<>();
        UUIDComponent casterUuid = accessor.getComponent(
                hexContext.getCasterRef(accessor), UUIDComponent.getComponentType());
        if (casterUuid != null) exclusions.add(casterUuid.getUuid());

        if (targetVar instanceof EntityVar entityVar) {
            Ref<EntityStore> hostRef = entityVar.getRef(accessor);
            if (hostRef == null || !hostRef.isValid()) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "Target is invalid");
                return;
            }
            UUIDComponent hostUuid = accessor.getComponent(hostRef, UUIDComponent.getComponentType());
            if (hostUuid != null) exclusions.add(hostUuid.getUuid());

            ArcState state = new ArcState(glyph, outputLinks, exclusions, range, interval, iterations, false);
            HexConstructSpawner.applyWithState(accessor, hostRef, hexContext, glyph, ArcGlyph.ID, state);
            return;
        }

        Vector3d origin = HexVarUtil.position(targetVar, accessor);
        if (origin == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be an entity, block, or position");
            return;
        }

        ArcState state = new ArcState(glyph, outputLinks, exclusions, range, interval, iterations, true);
        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, ArcGlyph.ID, new Vector3d(origin), state);
        applyMarkerModel(holder, config.getModel());
        accessor.addEntity(holder, AddReason.SPAWN);
    }

    private static void applyMarkerModel(Holder<EntityStore> holder, String modelId) {
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelId);
        if (modelAsset == null) return;

        Model model = Model.createUnitScaleModel(modelAsset);
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(),
                new PersistentModel(model.toReference()));
    }
}
