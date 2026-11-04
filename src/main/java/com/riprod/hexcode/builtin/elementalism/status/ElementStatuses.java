package com.riprod.hexcode.builtin.elementalism.status;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.status.block.BlockStatusEntry;
import com.riprod.hexcode.builtin.elementalism.status.block.BlockStatusSection;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.utils.VfxUtil;

public final class ElementStatuses {

    private ElementStatuses() {
    }

    public static boolean hasEntity(Ref<EntityStore> ref, String statusId, ComponentAccessor<EntityStore> accessor) {
        var effectId = effectIdOf(statusId);
        if (effectId == null || !ref.isValid()) {
            return false;
        }
        var controller = accessor.getComponent(ref, EffectControllerComponent.getComponentType());
        return controller != null && controller.hasEffect(EntityEffect.getAssetMap().getAsset(effectId));
    }

    public static boolean applyEntity(Ref<EntityStore> ref, String statusId, float duration,
            @Nullable Ref<EntityStore> owner, CommandBuffer<EntityStore> buffer) {
        var effectId = effectIdOf(statusId);
        if (effectId == null || !(duration > 0f) || !ref.isValid()) {
            return false;
        }
        return VfxUtil.applyEffect(buffer, ref, effectId, duration, OverlapBehavior.EXTEND, owner);
    }

    public static void removeEntity(Ref<EntityStore> ref, String statusId, CommandBuffer<EntityStore> buffer) {
        VfxUtil.removeEffect(buffer, ref, effectIdOf(statusId));
    }

    public static boolean hasBlock(World world, Vector3i position, String statusId) {
        var section = blockSection(world, position);
        if (section == null) {
            return false;
        }
        var index = ChunkUtil.indexBlock(position.x, position.y, position.z);
        section.prune(index, BlockAccess.blockId(world, position));
        return find(section, index, statusId) != null;
    }

    public static boolean applyBlock(World world, Vector3i position, String statusId, float duration) {
        var asset = StatusAsset.getAssetMap().getAsset(statusId);
        var sectionRef = BlockAccess.sectionRef(world, position);
        if (asset == null || !(duration > 0f) || sectionRef == null) {
            return false;
        }
        var store = world.getChunkStore().getStore();
        var blocks = store.getComponent(sectionRef, BlockSection.getComponentType());
        var index = ChunkUtil.indexBlock(position.x, position.y, position.z);
        var blockId = blocks != null ? blocks.get(index) : BlockType.EMPTY_ID;
        if (blockId == BlockType.EMPTY_ID) {
            return false;
        }
        var section = store.getComponent(sectionRef, BlockStatusSection.getComponentType());
        if (section == null) {
            section = new BlockStatusSection();
            store.addComponent(sectionRef, BlockStatusSection.getComponentType(), section);
        }
        section.prune(index, blockId);
        var existing = find(section, index, asset.getId());
        if (existing != null) {
            existing.extend(duration);
            return true;
        }
        section.getOrCreate(index).add(new BlockStatusEntry(asset.getId(), duration, blockId));
        return true;
    }

    public static void removeBlock(World world, Vector3i position, String statusId) {
        var section = blockSection(world, position);
        if (section == null) {
            return;
        }
        var index = ChunkUtil.indexBlock(position.x, position.y, position.z);
        var entry = find(section, index, statusId);
        if (entry != null) {
            section.remove(index, entry);
        }
    }

    @Nullable
    private static String effectIdOf(String statusId) {
        var asset = StatusAsset.getAssetMap().getAsset(statusId);
        return asset != null ? asset.getEffectId() : null;
    }

    @Nullable
    private static BlockStatusEntry find(BlockStatusSection section, int index, String statusId) {
        var entries = section.get(index);
        if (entries == null) {
            return null;
        }
        for (var entry : entries) {
            if (entry.getStatusId().equalsIgnoreCase(statusId)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private static BlockStatusSection blockSection(World world, Vector3i position) {
        var sectionRef = BlockAccess.sectionRef(world, position);
        return sectionRef != null
                ? world.getChunkStore().getStore().getComponent(sectionRef, BlockStatusSection.getComponentType())
                : null;
    }
}
