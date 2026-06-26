package com.riprod.hexcode.builtin.hexCore.glyphs.freeze;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.freeze.component.FrozenBlock;
import com.riprod.hexcode.builtin.hexCore.glyphs.freeze.style.FreezeStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class FreezeGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() { return ID; }

    public static final String ID = "Freeze";

    private static final double DEFAULT_DURATION = 3.0;

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(FreezeGlyphSlots.TARGET, hexContext);
        if (targets == null) return;

        double duration = HexVarUtil.numberOrDefault(
                glyph.readSlot(FreezeGlyphSlots.DURATION, hexContext), DEFAULT_DURATION);

        EntityEffect freezeEffect = EntityEffect.getAssetMap().getAsset("Hexcode_Freeze");
        if (freezeEffect == null) {
            LOGGER.atWarning().log("freeze: Freeze effect asset not found");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) return;

        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) return;

        try {
            EffectControllerComponent controller = accessor.getComponent(
                    targetRef, EffectControllerComponent.getComponentType());
            if (controller != null) {
                controller.addEffect(targetRef, freezeEffect, (float) duration,
                        OverlapBehavior.OVERWRITE, accessor);
            }

            List<FrozenBlock> frozenBlocks = new ArrayList<>();
            TransformComponent tc = accessor.getComponent(targetRef, TransformComponent.getComponentType());
            if (tc != null) {
                Vector3d pos = tc.getPosition();
                placeIceBlock(world, pos, frozenBlocks);
                FreezeStyle.renderFreeze(pos, hexContext, accessor);
            }

            FreezeState state = new FreezeState(frozenBlocks, (float) duration, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, targetRef, hexContext, glyph, FreezeGlyph.ID, state);
        } catch (Exception e) {
            LOGGER.atWarning().log("freeze: failed on entity: %s", e.getMessage());
        }
    }

    private static void placeIceBlock(World world, Vector3d pos, List<FrozenBlock> frozenBlocks) {
        int footX = (int) Math.floor(pos.x());
        int footY = (int) Math.floor(pos.y()) - 1;
        int footZ = (int) Math.floor(pos.z());

        int blockId = world.getBlock(footX, footY, footZ);
        if (blockId == BlockType.EMPTY_ID) return;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null) return;

        String typeId = blockType.getId();
        int rotationIndex = world.getBlockRotationIndex(footX, footY, footZ);
        frozenBlocks.add(new FrozenBlock(new Vector3i(footX, footY, footZ), typeId, rotationIndex));

        world.setBlock(footX, footY, footZ, "Rock_Ice");
    }
}
