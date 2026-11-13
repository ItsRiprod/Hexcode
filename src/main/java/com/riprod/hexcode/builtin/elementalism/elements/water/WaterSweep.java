package com.riprod.hexcode.builtin.elementalism.elements.water;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3i;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.block.BlockSphereUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.fluid.FireFluidTicker;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.BlockAccess;

final class WaterSweep {

    private WaterSweep() {
    }

    static List<Vector3i> cells(Vector3i center, float radius) {
        var cells = new ArrayList<Vector3i>();
        BlockSphereUtil.forEachBlockExact(center.x, center.y, center.z, radius, cells, (x, y, z, out) -> {
            out.add(new Vector3i(x, y, z));
            return true;
        });
        return cells;
    }

    static boolean extinguish(BlockAccess.Cursor cursor, Vector3i pos, Ref<EntityStore> caster,
            CommandBuffer<EntityStore> buffer) {
        var sectionRef = cursor.sectionRef(pos);
        if (sectionRef == null) {
            return false;
        }
        var fluids = cursor.chunkStore().getStore().getComponent(sectionRef, FluidSection.getComponentType());
        if (fluids == null) {
            return false;
        }
        var fluid = Fluid.getAssetMap().getAsset(fluids.getFluidId(pos.x, pos.y, pos.z));
        if (fluid == null || !(fluid.getTicker() instanceof FireFluidTicker)
                || !HexProtection.canModifyBlock(cursor.world(), caster, buffer, new Vector3i(pos), BlockAction.BREAK)) {
            return false;
        }
        return fluids.setFluid(pos.x, pos.y, pos.z, Fluid.EMPTY_ID, (byte) 0);
    }

    static boolean hydrate(BlockAccess.Cursor cursor, Vector3i pos, Instant wateredUntil, Ref<EntityStore> caster,
            CommandBuffer<EntityStore> buffer) {
        var blockType = cursor.blockType(pos);
        var section = cursor.section(pos);
        var sectionRef = cursor.sectionRef();
        if (blockType == null || section == null || sectionRef == null) {
            return false;
        }
        var index = ChunkUtil.indexBlock(pos.x, pos.y, pos.z);
        if (section.getFiller(index) != FillerBlockUtil.NO_FILLER
                || !BlockEntity.declaresComponent(blockType, TilledSoilBlock.getComponentType())
                || !HexProtection.canModifyBlock(cursor.world(), caster, buffer, new Vector3i(pos), BlockAction.PLACE)) {
            return false;
        }
        var store = cursor.chunkStore().getStore();
        var components = store.getComponent(sectionRef, BlockComponentSection.getComponentType());
        if (components == null) {
            return false;
        }
        var blockRef = BlockEntity.ensureBlockEntity(store, sectionRef, components, pos.x, pos.y, pos.z, blockType,
                FillerBlockUtil.NO_FILLER);
        var soil = blockRef != null && blockRef.isValid()
                ? store.getComponent(blockRef, TilledSoilBlock.getComponentType())
                : null;
        if (soil == null) {
            return false;
        }
        soil.setWateredUntil(wateredUntil);
        components.markBlockNeedsSaving(index);
        section.setTicking(pos.x, pos.y, pos.z, true);
        section.scheduleTick(index, wateredUntil);
        var above = new Vector3i(pos.x, pos.y + 1, pos.z);
        var aboveSection = cursor.section(above);
        if (aboveSection != null) {
            aboveSection.setTicking(above.x, above.y, above.z, true);
        }
        return true;
    }
}
