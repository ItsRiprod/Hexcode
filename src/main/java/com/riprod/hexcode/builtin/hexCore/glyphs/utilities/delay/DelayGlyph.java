package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.style.DelayStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class DelayGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Delay";

    private static final Box FALLBACK_BOX = Box.horizontallyCentered(0.5, 0.5, 0.5);

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(DelayConfig.class, DelayConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        if (asset == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "delay glyph asset not found");
            return;
        }
        float seconds = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(DelayGlyphSlots.DURATION, hexContext),
                asset.getSlot(DelayGlyphSlots.DURATION)).floatValue();

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

        DelayState state = new DelayState(seconds, new ArrayList<>(nextLinks), entityVar == null);

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

        Model model = HexConstructSpawner.attachModel(holder, hexContext, asset, 1.0f);
        Box box = model != null ? model.getBoundingBox() : FALLBACK_BOX;
        if (model == null) {
            LOGGER.atWarning().log("delay: no model resolved for construct");
        }

        DelayConfig config = getConfig(DelayConfig.class, asset);
        if (config == null)
            config = DelayConfig.DEFAULTS;

        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(box));
        holder.addComponent(Velocity.getComponentType(), new Velocity());
        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(HeadRotation.getComponentType());
        new DelayPhysicsConfig(config.getGravity()).apply(holder,
                hexContext.getCasterRef(accessor), new Vector3d(), accessor, false);

        Ref<EntityStore> delayRef = accessor.addEntity(holder, AddReason.SPAWN);

        if (hexContext.getHexRoot() != null) {
            hexContext.getHexRoot().addDependency(hexContext, delayRef);
        }
    }
}
