package com.riprod.hexcode.builtin.hexCore.glyphs.delay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.delay.style.DelayStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class DelayGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Delay";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        float seconds = HexVarUtil.numberOrDefault(
                glyph.readSlot(DelayGlyphSlots.DURATION, hexContext), 1.0).floatValue();

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        HexVar incomingDefault = hexContext.getDefaultVariable();
        HexVar sourceVar = glyph.readSlot(DelayGlyphSlots.SOURCE, hexContext);
        if (sourceVar == null) {
            sourceVar = incomingDefault;
        }

        boolean indefinite = seconds < 0f;
        EntityVar entityVar = sourceVar instanceof EntityVar ev ? ev : null;

        if (!indefinite && entityVar == null) {
            if (seconds <= 0f) {
                HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
                return;
            }
            if (seconds < 0.5f) {
                World world = accessor.getExternalData().getWorld();
                if (1.0f / world.getTps() > seconds) {
                    HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
                    return;
                }
            }
        }

        List<String> nextLinks = glyph.getNextLinks();

        DelayState state = new DelayState(seconds, new ArrayList<>(nextLinks),
                hexContext.getColors(), entityVar == null);

        if (entityVar != null) {
            Ref<EntityStore> targetRef = entityVar.getRef(accessor);
            if (targetRef == null || !targetRef.isValid()) {
                HexExecuter.fail(glyph, hexContext,
                        GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "delay target entity gone");
                return;
            }
            TransformComponent targetTransform = accessor.getComponent(
                    targetRef, TransformComponent.getComponentType());
            if (targetTransform != null) {
                DelayStyle.renderAt(targetTransform.getPosition(), hexContext);
            }
            HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, ID, state);
            return;
        }

        Vector3d spawnPos;
        Rotation3f rot;
        switch (sourceVar) {
            case RotationVar r -> {
                rot = HexVarUtil.rotation(r, accessor);
                spawnPos = HexVarUtil.position(incomingDefault, accessor);
            }
            case PositionVar p -> {
                spawnPos = HexVarUtil.position(p, accessor);
                rot = HexVarUtil.rotation(incomingDefault, accessor);
            }
            case BlockVar b -> {
                spawnPos = HexVarUtil.position(b, accessor);
                rot = HexVarUtil.rotation(b, accessor);
            }
            case null, default -> {
                spawnPos = HexVarUtil.position(incomingDefault, accessor);
                rot = HexVarUtil.rotation(incomingDefault, accessor);
            }
        }
        if (spawnPos == null) {
            Ref<EntityStore> casterRef = hexContext.getCasterRef(accessor);
            if (casterRef != null && casterRef.isValid()) {
                TransformComponent tc = accessor.getComponent(
                        casterRef, TransformComponent.getComponentType());
                spawnPos = tc != null ? tc.getPosition() : new Vector3d();
            } else {
                spawnPos = new Vector3d();
            }
        }

        DelayStyle.renderAt(spawnPos, hexContext);

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, DelayGlyph.ID, spawnPos, state);

        if (rot != null) {
            holder.putComponent(TransformComponent.getComponentType(),
                    new TransformComponent(spawnPos, rot));
        }

        String modelId = VfxUtil.resolveModelId(hexContext, GlyphAsset.getAssetMap().getAsset(ID));
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;
        if (modelAsset != null) {
            Model model = Model.createScaledModel(modelAsset, 1.0f);

            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(),
                    new PersistentModel(model.toReference()));
        } else {
            LOGGER.atWarning().log("delay: model asset '%s' not found", modelId);
        }

        Ref<EntityStore> delayRef = accessor.addEntity(holder, AddReason.SPAWN);

        hexContext.getHexRoot().addDependency(hexContext, delayRef);
    }
}
