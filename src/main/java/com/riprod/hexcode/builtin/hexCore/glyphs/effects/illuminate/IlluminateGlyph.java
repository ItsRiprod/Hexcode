package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate;

import java.util.Arrays;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.style.IlluminateStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.utils.GlowUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class IlluminateGlyph implements GlyphHandler {

    public static final String ID = "Illuminate";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(IlluminateConfig.class, IlluminateConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        IlluminateConfig config = getConfig(IlluminateConfig.class, asset);
        if (config == null) config = IlluminateConfig.DEFAULTS;

        double mode = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(IlluminateGlyphSlots.MODE, hexContext),
                asset != null ? asset.getSlot(IlluminateGlyphSlots.MODE) : null).doubleValue();
        float seconds = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(IlluminateGlyphSlots.DURATION, hexContext),
                asset != null ? asset.getSlot(IlluminateGlyphSlots.DURATION) : null).floatValue();
        if (seconds <= 0f) {
            finish(glyph, hexContext);
            return;
        }

        Color color = IlluminateStyle.resolveColor(hexContext, asset, config);
        ColorLight light = IlluminateStyle.toColorLight(color, config.getLightRadius());
        Vector3f boxColor = IlluminateStyle.toVector3f(color);
        boolean showBox = mode == 0;

        HexVar targetSlot = glyph.readSlot(IlluminateGlyphSlots.TARGET, hexContext);
        EntityVar targetEntity = HexVarUtil.resolveEntityVar(targetSlot, hexContext);
        Ref<EntityStore> targetRef = targetEntity != null ? targetEntity.getRef(accessor) : null;
        boolean isEntity = targetRef != null && targetRef.isValid();

        if (isEntity && mode >= 0) {
            // glow the target entity in place; it owns the effect
            IlluminateState state = new IlluminateState(seconds, showBox, false,
                    GlowUtil.nextVolumeId(), boxColor, glyph.getNextLinks());
            accessor.putComponent(targetRef, DynamicLight.getComponentType(), new DynamicLight(light));
            HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, ID, state);
        } else {
            // blocks can't be lit, and mode -1 wants a separate entity: spawn a light holder that owns the effect
            Vector3d spawnPos;
            Ref<EntityStore> mountTo = null;
            Vector3i blockPos = null;
            if (isEntity) {
                TransformComponent tc = accessor.getComponent(targetRef, TransformComponent.getComponentType());
                spawnPos = tc != null ? new Vector3d(tc.getPosition()) : new Vector3d();
                mountTo = targetRef;
            } else {
                BlockVar blockVar = HexVarUtil.resolveBlockVar(targetSlot, hexContext);
                if (blockVar == null || blockVar.getValue() == null) {
                    finish(glyph, hexContext);
                    return;
                }
                blockPos = blockVar.getValue();
                spawnPos = new Vector3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5);
            }

            IlluminateState state = new IlluminateState(seconds, showBox, true,
                    GlowUtil.nextVolumeId(), boxColor, glyph.getNextLinks(), blockPos);
            // createWithState bakes the construct into the holder so a fresh (not-yet-valid) ref is never used
            Holder<EntityStore> holder = HexConstructSpawner.createWithState(
                    accessor, hexContext, glyph, ID, spawnPos, state);
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
