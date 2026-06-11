package com.riprod.hexcode.builtin.glyphs.delay;

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
import com.riprod.hexcode.builtin.glyphs.delay.style.DelayStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.utils.HexVarUtil;

public class DelayGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Delay";
    private static final String MODEL_ID = "Delay";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        float seconds = HexVarUtil.numberOrDefault(
                glyph.readSlot(DelayGlyphSlots.DURATION, hexContext), 1.0).floatValue();

        if (seconds <= 0f) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        if (seconds < 0.5f) { // early gate

            // check if the delay is shorter than the TPS of the world
            World world = hexContext.getAccessor().getExternalData().getWorld();
            if (1.0f / world.getTps() > seconds) {
                HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
                return;
            }
        }

        List<String> nextLinks = glyph.getNextLinks();
        if (nextLinks.isEmpty()) {
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        HexVar defaultVar = hexContext.getVariable(hexContext.getDefaultSlot());
        Vector3d spawnPos = HexVarUtil.position(defaultVar, accessor);
        if (spawnPos == null) {
            Ref<EntityStore> casterRef = hexContext.getCasterRef();
            if (casterRef != null && casterRef.isValid()) {
                TransformComponent tc = accessor.getComponent(
                        casterRef, TransformComponent.getComponentType());
                spawnPos = tc != null ? tc.getPosition() : new Vector3d();
            } else {
                spawnPos = new Vector3d();
            }
        }
        EntityVar entityVar = HexVarUtil.resolveEntityVar(defaultVar, hexContext);

        DelayStyle.render(hexContext);

        DelayState state = new DelayState(seconds, new ArrayList<>(nextLinks), hexContext.getColors(),
                seconds >= 1f || entityVar == null);

        if (seconds < 1f && entityVar != null) {
            Ref<EntityStore> targetRef = entityVar.getRef(accessor);
            if (targetRef == null || !targetRef.isValid()) {
                HexExecuter.fail(glyph, hexContext,
                        GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "delay target entity gone");
                return;
            }
            HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, ID, state);
            return;
        }
        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, DelayGlyph.ID, spawnPos, state);

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(MODEL_ID);
        if (modelAsset != null) {
            Model model = new Model(modelAsset.getId(), 1.0f, (Map<String, String>) null,
                    modelAsset.getAttachments(null),
                    modelAsset.getBoundingBox(), modelAsset.getModel(), modelAsset.getTexture(),
                    modelAsset.getGradientSet(), modelAsset.getGradientId(), modelAsset.getEyeHeight(),
                    modelAsset.getCrouchOffset(), modelAsset.getSittingOffset(),
                    modelAsset.getSleepingOffset(),
                    modelAsset.getAnimationSetMap(), modelAsset.getCamera(),
                    modelAsset.getLight(), modelAsset.getParticles(), modelAsset.getTrails(),
                    modelAsset.getPhysicsValues(),
                    modelAsset.getDetailBoxes(), modelAsset.getPhobia(),
                    modelAsset.getPhobiaModelAssetId()
            );

            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(),
                    new PersistentModel(model.toReference()));

            Rotation3f rotVar = HexVarUtil.rotation(defaultVar, accessor);
            if (rotVar != null) {
                holder.putComponent(TransformComponent.getComponentType(),
                        new TransformComponent(spawnPos, rotVar));
            }
        } else {
            LOGGER.atWarning().log("delay: model asset '%s' not found", MODEL_ID);
        }

        Ref<EntityStore> delayRef = accessor.addEntity(holder, AddReason.SPAWN);

        hexContext.getHexRoot().addDependency(hexContext, delayRef);
    }
}
