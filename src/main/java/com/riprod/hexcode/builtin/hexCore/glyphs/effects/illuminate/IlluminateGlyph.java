package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate;

import java.util.Arrays;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.style.IlluminateStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.utils.GlowUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

import lombok.Getter;

public class IlluminateGlyph extends GlyphAsset {

    public static final String TYPE = "Illuminate";

    private int lightRadius = 8;
    @Getter
    private Color defaultColor = new Color((byte) 0, (byte) 204, (byte) 204);

    public static final BuilderCodec<IlluminateGlyph> CODEC = BuilderCodec
            .builder(IlluminateGlyph.class, IlluminateGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("LightRadius", Codec.INTEGER, true),
                    (c, v) -> c.lightRadius = v, c -> c.lightRadius)
            .add()
            .append(new KeyedCodec<>("DefaultColor", ProtocolCodecs.COLOR, true),
                    (c, v) -> c.defaultColor = v, c -> c.defaultColor)
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        double mode = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(IlluminateGlyphSlots.MODE, hexContext),
                getSlot(IlluminateGlyphSlots.MODE)).doubleValue();
        float seconds = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(IlluminateGlyphSlots.DURATION, hexContext),
                getSlot(IlluminateGlyphSlots.DURATION)).floatValue();
        if (seconds <= 0f) {
            finish(glyph, hexContext);
            return;
        }

        Color color = IlluminateStyle.resolveColor(hexContext, this);
        ColorLight light = IlluminateStyle.toColorLight(color, lightRadius);
        Vector3f boxColor = IlluminateStyle.toVector3f(color);
        boolean showBox = mode == 0;

        HexVar targetSlot = glyph.readSlot(IlluminateGlyphSlots.TARGET, hexContext);

        if (targetSlot instanceof EntityVar entityVar && mode >= 0) {
            var targetRef = entityVar.getRef(accessor);
            // glow the target entity in place; it owns the effect
            var state = new IlluminateState(seconds, showBox, false,
                    GlowUtil.nextVolumeId(), boxColor, glyph.getNextLinks());
            accessor.putComponent(targetRef, DynamicLight.getComponentType(), new DynamicLight(light));
            HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, TYPE, state);
        } else {
            // blocks can't be lit, and mode -1 wants a separate entity: spawn a light
            // holder that owns the effect
            Vector3d spawnPos;
            Ref<EntityStore> mountTo = null;
            Vector3i blockPos = null;
            if (targetSlot instanceof EntityVar entityVar) {
                var targetRef = entityVar.getRef(accessor);

                TransformComponent tc = accessor.getComponent(targetRef, TransformComponent.getComponentType());
                spawnPos = tc != null ? new Vector3d(tc.getPosition()) : new Vector3d();
                mountTo = targetRef;
            } else {
                var pos = targetSlot.position(accessor);
                if (pos == null) {
                    finish(glyph, hexContext);
                    return;
                }
                blockPos = new Vector3i(MathUtil.floor(pos.x() - 0.5), MathUtil.floor(pos.y() - 0.5),
                        MathUtil.floor(pos.z() - 0.5));

                spawnPos = pos;
            }

            IlluminateState state = new IlluminateState(seconds, showBox, true,
                    GlowUtil.nextVolumeId(), boxColor, glyph.getNextLinks(), blockPos);
            // createWithState bakes the construct into the holder so a fresh
            // (not-yet-valid) ref is never used
            Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                    accessor, hexContext, glyph, TYPE, spawnPos, state);
            holder.addComponent(DynamicLight.getComponentType(), new DynamicLight(light));
            if (mountTo != null) {
                holder.addComponent(MountedComponent.getComponentType(),
                        new MountedComponent(mountTo, new Vector3f(), MountController.Minecart));
            }
            accessor.addEntity(holder, AddReason.SPAWN);
        }

        fireImmediate(glyph, hexContext);
        // Next is deferred to the construct's onEnd
    }

    // no glow to attach; still fire Immediate and continue the main chain
    private void finish(Glyph glyph, HexContext hexContext) {
        fireImmediate(glyph, hexContext);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void fireImmediate(Glyph glyph, HexContext hexContext) {
        Slot immediate = glyph.getSlot(IlluminateGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
