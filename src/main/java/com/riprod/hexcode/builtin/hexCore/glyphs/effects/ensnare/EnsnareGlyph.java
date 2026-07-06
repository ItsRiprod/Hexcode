package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.SpikeEntry;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.style.EnsnareStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class EnsnareGlyph implements GlyphHandler {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Ensnare";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(EnsnareConfig.class, EnsnareConfig.CODEC);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double radius = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(EnsnareGlyphSlots.RADIUS, hexContext), asset.getSlot(EnsnareGlyphSlots.RADIUS)));

        float scale = Math.max(1f, Impact.scale(slotImpact(asset, EnsnareGlyphSlots.RADIUS), radius));

        return glyph.computeBaseCost(asset) * scale;
    }

    private static Impact slotImpact(GlyphAsset asset, String key) {
        if (asset == null) return null;
        SlotAsset slot = asset.getSlot(key);
        return slot == null ? null : slot.getImpact();
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        EnsnareConfig config = getConfig(EnsnareConfig.class, asset);
        if (config == null) config = EnsnareConfig.DEFAULTS;

        double radius = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(EnsnareGlyphSlots.RADIUS, hexContext), asset.getSlot(EnsnareGlyphSlots.RADIUS)));
        double duration = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(EnsnareGlyphSlots.DURATION, hexContext), asset.getSlot(EnsnareGlyphSlots.DURATION)));

        Vector3d center = HexVarUtil.position(
                glyph.readSlot(EnsnareGlyphSlots.TARGET, hexContext), accessor);
        if (center == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target position required");
            return;
        }

        int centerBlockX = (int) Math.floor(center.x);
        int centerBlockY = (int) Math.floor(center.y);
        int centerBlockZ = (int) Math.floor(center.z);

        String modelId = VfxUtil.resolveModelId(hexContext, asset);
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;
        if (modelAsset == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Missing asset " + modelId);
            return;
        }
        Model spikeModel = Model.createScaledModel(modelAsset, config.getSpikeScale());

        List<SpikeEntry> spikes = new ArrayList<>();
        int intRadius = (int) Math.ceil(radius);
        long seed = centerBlockX * 73856093L ^ centerBlockZ * 19349663L ^ centerBlockY * 83492791L;

        for (int dx = -intRadius; dx <= intRadius && spikes.size() < config.getMaxSpikes(); dx++) {
            for (int dz = -intRadius; dz <= intRadius && spikes.size() < config.getMaxSpikes(); dz++) {
                if (dx * dx + dz * dz > radius * radius)
                    continue;

                long hash = (dx * 73856093L ^ dz * 19349663L ^ seed) & 0xFFFFFFFFL;
                if ((hash % 100) >= (long) (config.getDensity() * 100))
                    continue;

                int worldX = centerBlockX + dx;
                int worldZ = centerBlockZ + dz;

                int groundY = findGround(world, worldX, centerBlockY, worldZ, config);
                if (groundY < 0)
                    continue;
                if (Math.abs(groundY - centerBlockY) > config.getHeightTolerance())
                    continue;

                Vector3d spikePos = new Vector3d(worldX + 0.5, groundY + 1.0, worldZ + 0.5);

                float yaw = (hash % 4) * (float) (Math.PI / 2.0);
                Vector3f rotation = new Vector3f(0, yaw, 0);

                Ref<EntityStore> spikeRef = spawnSpikeEntity(
                        spikePos, rotation, spikeModel, accessor);
                if (spikeRef != null) {
                    spikes.add(new SpikeEntry(spikePos, spikeRef));
                }
            }
        }

        if (spikes.isEmpty()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "No valid spike positions");
            return;
        }

        spawnTrackerEntity(glyph, hexContext, spikes, (float) duration,
                center, radius, config, accessor);

        EnsnareStyle.renderSeismicBurst(center, hexContext, accessor);
    }

    private int findGround(World world, int x, int centerY, int z, EnsnareConfig config) {
        for (int y = centerY + config.getGroundScanRange(); y >= centerY - config.getGroundScanRange(); y--) {
            int blockId = world.getBlock(x, y, z);
            if (blockId != BlockType.EMPTY_ID) {
                int aboveId = world.getBlock(x, y + 1, z);
                if (aboveId == BlockType.EMPTY_ID) {
                    return y;
                }
            }
        }
        return -1;
    }

    private Ref<EntityStore> spawnSpikeEntity(Vector3d position, Vector3f rotation,
            Model model, CommandBuffer<EntityStore> accessor) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(new Vector3d(position), new Rotation3f(rotation.x, rotation.y, rotation.z)));
        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(),
                new PersistentModel(model.toReference()));
        holder.addComponent(BoundingBox.getComponentType(),
                new BoundingBox(model.getBoundingBox()));
        holder.addComponent(NetworkId.getComponentType(),
                new NetworkId(accessor.getExternalData().takeNextNetworkId()));
        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());

        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    private void spawnTrackerEntity(Glyph glyph, HexContext hexContext,
            List<SpikeEntry> spikes, float durationSeconds,
            Vector3d center, double radius, EnsnareConfig config, CommandBuffer<EntityStore> accessor) {
        Holder<EntityStore> holder = HexConstructSpawner.create(
                accessor, hexContext, glyph, EnsnareGlyph.ID, new Vector3d(center));

        holder.addComponent(EnsnareComponent.getComponentType(),
                new EnsnareComponent(spikes, durationSeconds, config.getSpikeDamage(),
                        config.getDamageCooldownSeconds(), center, radius,
                        config.getSpikeHitYMin(), config.getSpikeHitYMax()));

        Ref<EntityStore> trackerRef = accessor.addEntity(holder, AddReason.SPAWN);

        hexContext.getHexRoot().addDependency(hexContext, trackerRef);
    }

}
