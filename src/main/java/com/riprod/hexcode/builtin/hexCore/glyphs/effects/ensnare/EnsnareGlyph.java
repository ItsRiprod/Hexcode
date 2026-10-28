package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.SpikeEntry;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.style.EnsnareStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.utils.HexVarUtil;

import lombok.Getter;

public class EnsnareGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String TYPE = "Ensnare";

    private float spikeScale = 0.5f;
    private float damageCooldownSeconds = 1.0f;
    private int maxSpikes = 64;
    private int groundScanRange = 3;
    private float density = 0.5f;
    private int heightTolerance = 2;
    private float spikeDamage = 0.0f;
    @Getter
    private double spikeHitRadiusSq = 0.49;
    @Getter
    private double boxPaddingXZ = 1.0;
    @Getter
    private double boxPaddingYMin = 3.0;
    @Getter
    private double boxPaddingYMax = 4.0;
    private double spikeHitYMin = -0.5;
    private double spikeHitYMax = 1.5;

    public static final BuilderCodec<EnsnareGlyph> CODEC = BuilderCodec
            .builder(EnsnareGlyph.class, EnsnareGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("SpikeScale", Codec.FLOAT, true),
                    (c, v) -> c.spikeScale = v, c -> c.spikeScale)
            .add()
            .append(new KeyedCodec<>("DamageCooldownSeconds", Codec.FLOAT, true),
                    (c, v) -> c.damageCooldownSeconds = v, c -> c.damageCooldownSeconds)
            .add()
            .append(new KeyedCodec<>("MaxSpikes", Codec.INTEGER, true),
                    (c, v) -> c.maxSpikes = v, c -> c.maxSpikes)
            .add()
            .append(new KeyedCodec<>("GroundScanRange", Codec.INTEGER, true),
                    (c, v) -> c.groundScanRange = v, c -> c.groundScanRange)
            .add()
            .append(new KeyedCodec<>("Density", Codec.FLOAT, true),
                    (c, v) -> c.density = v, c -> c.density)
            .add()
            .append(new KeyedCodec<>("HeightTolerance", Codec.INTEGER, true),
                    (c, v) -> c.heightTolerance = v, c -> c.heightTolerance)
            .add()
            .append(new KeyedCodec<>("SpikeDamage", Codec.FLOAT, true),
                    (c, v) -> c.spikeDamage = v, c -> c.spikeDamage)
            .add()
            .append(new KeyedCodec<>("SpikeHitRadiusSq", Codec.DOUBLE, true),
                    (c, v) -> c.spikeHitRadiusSq = v, c -> c.spikeHitRadiusSq)
            .add()
            .append(new KeyedCodec<>("BoxPaddingXZ", Codec.DOUBLE, true),
                    (c, v) -> c.boxPaddingXZ = v, c -> c.boxPaddingXZ)
            .add()
            .append(new KeyedCodec<>("BoxPaddingYMin", Codec.DOUBLE, true),
                    (c, v) -> c.boxPaddingYMin = v, c -> c.boxPaddingYMin)
            .add()
            .append(new KeyedCodec<>("BoxPaddingYMax", Codec.DOUBLE, true),
                    (c, v) -> c.boxPaddingYMax = v, c -> c.boxPaddingYMax)
            .add()
            .append(new KeyedCodec<>("SpikeHitYMin", Codec.DOUBLE, true),
                    (c, v) -> c.spikeHitYMin = v, c -> c.spikeHitYMin)
            .add()
            .append(new KeyedCodec<>("SpikeHitYMax", Codec.DOUBLE, true),
                    (c, v) -> c.spikeHitYMax = v, c -> c.spikeHitYMax)
            .add()
            .build();

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double radius = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(EnsnareGlyphSlots.RADIUS, hexContext), asset.getSlot(EnsnareGlyphSlots.RADIUS)));

        float scale = Math.max(1f, Impact.scale(slotImpact(asset, EnsnareGlyphSlots.RADIUS), radius));

        return glyph.computeBaseCost(asset) * scale;
    }

    private static Impact slotImpact(GlyphAsset asset, String key) {
        if (asset == null)
            return null;
        SlotConfig slot = asset.getSlot(key);
        return slot == null ? null : slot.getImpact();
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        double radius = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(EnsnareGlyphSlots.RADIUS, hexContext), getSlot(EnsnareGlyphSlots.RADIUS)));
        double duration = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(EnsnareGlyphSlots.DURATION, hexContext), getSlot(EnsnareGlyphSlots.DURATION)));

        var center = glyph.readSlot(EnsnareGlyphSlots.TARGET, hexContext).position(accessor);
        if (center == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target position required");
            return;
        }

        int centerBlockX = (int) Math.floor(center.x);
        int centerBlockY = (int) Math.floor(center.y);
        int centerBlockZ = (int) Math.floor(center.z);

        Model spikeModel = HexConstructSpawner.resolveModel(hexContext, this, spikeScale);
        if (spikeModel == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Missing model asset");
            return;
        }

        List<SpikeEntry> spikes = new ArrayList<>();
        int intRadius = (int) Math.ceil(radius);
        long seed = centerBlockX * 73856093L ^ centerBlockZ * 19349663L ^ centerBlockY * 83492791L;

        var cursor = new BlockAccess.Cursor(world);

        for (int dx = -intRadius; dx <= intRadius && spikes.size() < maxSpikes; dx++) {
            for (int dz = -intRadius; dz <= intRadius && spikes.size() < maxSpikes; dz++) {
                if (dx * dx + dz * dz > radius * radius)
                    continue;

                long hash = (dx * 73856093L ^ dz * 19349663L ^ seed) & 0xFFFFFFFFL;
                if ((hash % 100) >= (long) (density * 100))
                    continue;

                int worldX = centerBlockX + dx;
                int worldZ = centerBlockZ + dz;

                var spikeWorldPos = new Vector3i(worldX, centerBlockY, worldZ);
                int groundY = findGround(cursor, spikeWorldPos);
                if (groundY < 0)
                    continue;
                if (Math.abs(groundY - centerBlockY) > heightTolerance)
                    continue;

                Vector3d spikePos = new Vector3d(0.5, 1, 0.5).add(spikeWorldPos.x, spikeWorldPos.y, spikeWorldPos.z);

                float yaw = (hash % 4) * (float) (Math.PI / 2.0);
                Vector3f rotation = new Vector3f(0, yaw, 0);

                Ref<EntityStore> spikeRef = spawnSpikeEntity(
                        spikePos, rotation, spikeModel, hexContext, accessor);
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
                center, radius, accessor);

        EnsnareStyle.renderSeismicBurst(this, center, hexContext, accessor);
    }

    /** Mutates the initialPos in-place until it finds the ground */
    private int findGround(BlockAccess.Cursor cursor, Vector3i pos) {
        int initialY = pos.y;
        int scanRange = groundScanRange;
        for (int y = initialY + scanRange; y >= initialY - scanRange; y--) {
            pos.y = y;
            int blockId = cursor.blockId(pos);
            if (blockId != BlockType.EMPTY_ID) {
                pos.y = y + 1;
                int aboveId = cursor.blockId(pos);
                pos.y = y;
                if (aboveId == BlockType.EMPTY_ID) {
                    return y;
                }
            }
        }
        return -1;
    }

    private Ref<EntityStore> spawnSpikeEntity(Vector3d position, Vector3f rotation,
            Model model, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(HexcodeComponent.getComponentType(), new HexcodeComponent());
        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(new Vector3d(position), new Rotation3f(rotation.x, rotation.y, rotation.z)));
        holder.ensureComponent(UUIDComponent.getComponentType());
        HexConstructSpawner.applyModel(holder, hexContext, model);
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
            Vector3d center, double radius, CommandBuffer<EntityStore> accessor) {
        Holder<EntityStore> holder = HexConstructSpawner.create(
                accessor, hexContext, glyph, EnsnareGlyph.TYPE, new Vector3d(center));

        holder.addComponent(EnsnareComponent.getComponentType(),
                new EnsnareComponent(spikes, durationSeconds, spikeDamage,
                        damageCooldownSeconds, center, radius,
                        spikeHitYMin, spikeHitYMax));

        Ref<EntityStore> trackerRef = accessor.addEntity(holder, AddReason.SPAWN);

        hexContext.addDependency(trackerRef);
    }

}
