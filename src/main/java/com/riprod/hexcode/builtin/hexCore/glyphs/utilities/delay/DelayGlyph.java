package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.VarType;
import com.riprod.hexcode.utils.HexVarUtil;

public class DelayGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Delay";

    private static final Box FALLBACK_BOX = Box.horizontallyCentered(0.5, 0.5, 0.5);

    private double gravity = 0.0;

    public static final BuilderCodec<DelayGlyph> CODEC = BuilderCodec
            .builder(DelayGlyph.class, DelayGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Gravity", Codec.DOUBLE, true),
                    (c, v) -> c.gravity = v, c -> c.gravity)
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        float seconds = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(DelayGlyphSlots.DURATION, hexContext),
                getSlot(DelayGlyphSlots.DURATION)).floatValue();

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        HexVar incomingDefault = hexContext.getDefaultVariable();
        HexVar positionalDefault = incomingDefault instanceof NumberVar ? null : incomingDefault;
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
                EffectRenderer.render(EffectSlot.Start, this, hexContext, targetTransform.getPosition(), accessor);
            }
            HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, TYPE, state);
            return;
        }

        Vector3d spawnPos;
        Rotation3f rot;
        var type = sourceVar.type();
        switch (type) {
            case ROTATION -> {
                rot = sourceVar.rotation(accessor);
                spawnPos = positionalDefault != null ? positionalDefault.position(accessor) : null;
            }
            case TRANSFORM, POSITION -> {
                spawnPos = sourceVar.position(accessor);
                rot = incomingDefault != null ? incomingDefault.rotation(accessor) : null;
            }
            case null, default -> {
                spawnPos = positionalDefault != null ? positionalDefault.position(accessor) : null;
                rot = incomingDefault != null ? incomingDefault.rotation(accessor) : null;
            }
        }
        if (spawnPos == null) {
            Ref<EntityStore> casterRef = hexContext.getRootRef();
            if (casterRef != null && casterRef.isValid()) {
                TransformComponent tc = accessor.getComponent(
                        casterRef, TransformComponent.getComponentType());
                spawnPos = tc != null ? tc.getPosition() : new Vector3d();
            } else {
                spawnPos = new Vector3d();
            }
        }

        EffectRenderer.render(EffectSlot.Start, this, hexContext, spawnPos, accessor);

        Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                accessor, hexContext, glyph, DelayGlyph.TYPE, spawnPos, state);
        if (holder == null) {
            return;
        }

        if (rot != null) {
            holder.putComponent(TransformComponent.getComponentType(),
                    new TransformComponent(spawnPos, rot));
        }

        Model model = HexConstructSpawner.attachModel(holder, hexContext, this);
        Box box = model != null ? model.getBoundingBox() : FALLBACK_BOX;
        if (model == null) {
            LOGGER.atWarning().log("delay: no model resolved for construct");
        }

        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(box));
        holder.addComponent(Velocity.getComponentType(), new Velocity());
        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(HeadRotation.getComponentType());
        new DelayPhysicsConfig(gravity).apply(holder,
                hexContext.getRootRef(), new Vector3d(), accessor, false);

        Ref<EntityStore> delayRef = accessor.addEntity(holder, AddReason.SPAWN);

        hexContext.addDependency(delayRef);
    }
}
