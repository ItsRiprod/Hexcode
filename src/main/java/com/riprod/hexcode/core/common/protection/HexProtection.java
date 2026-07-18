package com.riprod.hexcode.core.common.protection;

import java.util.concurrent.TimeUnit;

import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;

public final class HexProtection {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String PROBE_CAUSE_ID = "Hex_Protection_Probe";
    private static final String BLOCKED_NOTIFICATION = "server.hexcode.notifications.glyphBlocked";

    private HexProtection() {
    }

    public static boolean canModifyBlock(World world, Ref<EntityStore> casterRef,
            CommandBuffer<EntityStore> accessor, Vector3i pos, BlockAction action) {
        var worldConfig = world.getGameplayConfig().getWorldConfig();
        if (action == BlockAction.BREAK && !worldConfig.isBlockBreakingAllowed()) {
            return false;
        }
        if (action == BlockAction.PLACE && !worldConfig.isBlockPlacementAllowed()) {
            return false;
        }

        if (!isEnvironmentModifiable(world, pos)) {
            return false;
        }

        if (casterRef == null || !casterRef.isValid()) {
            return true;
        }

        if (action == BlockAction.BREAK) {
            BlockType type = BlockType.getAssetMap().getAsset(world.getBlock(pos.x, pos.y, pos.z));
            BreakBlockEvent event = new BreakBlockEvent(null, pos, type);
            accessor.invoke(casterRef, event);
            if (event.isCancelled()) {
                return false;
            }
            pos.set(event.getTargetBlock());
        } else {
            PlaceBlockEvent event = new PlaceBlockEvent(null, pos, RotationTuple.NONE);
            accessor.invoke(casterRef, event);
            if (event.isCancelled()) {
                return false;
            }
            pos.set(event.getTargetBlock());
        }
        return true;
    }

    public static boolean canAffectEntity(World world, Ref<EntityStore> casterRef,
            CommandBuffer<EntityStore> accessor, Ref<EntityStore> targetRef) {
        if (casterRef == null || !casterRef.isValid() || targetRef == null || !targetRef.isValid()) {
            return true;
        }
        if (targetRef.equals(casterRef)) {
            return true;
        }
        if (accessor.getComponent(targetRef, Intangible.getComponentType()) != null) {
            return false;
        }
        if (accessor.getComponent(targetRef, HexcodeComponent.getComponentType()) != null) {
            return true;
        }

        boolean targetIsPlayer = accessor.getComponent(targetRef, Player.getComponentType()) != null;
        boolean casterIsPlayer = accessor.getComponent(casterRef, Player.getComponentType()) != null;
        if (targetIsPlayer && casterIsPlayer && !world.getWorldConfig().isPvpEnabled()) {
            return false;
        }

        DamageCause cause = DamageCause.getAssetMap().getAsset(PROBE_CAUSE_ID);
        if (cause == null) {
            return true;
        }
        Damage probe = new Damage(new Damage.EntitySource(casterRef), cause, 0f);
        DamageSystems.executeDamage(targetRef, accessor, probe);
        return !probe.isCancelled();
    }

    public static void notifyBlocked(Ref<EntityStore> casterRef, CommandBuffer<EntityStore> accessor,
            String glyphDisplayName) {
        LOGGER.atFine().atMostEvery(5, TimeUnit.SECONDS)
                .log("Blocked %s in a protected area", glyphDisplayName);

        if (casterRef == null || !casterRef.isValid()) {
            return;
        }
        PlayerRef pr = accessor.getComponent(casterRef, PlayerRef.getComponentType());
        if (pr == null) {
            return;
        }
        Message notification = Message.translation(BLOCKED_NOTIFICATION).param("glyphName", glyphDisplayName);
        NotificationUtil.sendNotification(pr.getPacketHandler(), notification);
    }

    private static boolean isEnvironmentModifiable(World world, Vector3i pos) {
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunk == null) {
            return true;
        }
        int environmentId = chunk.getBlockChunk().getEnvironment(pos.x, pos.y, pos.z);
        Environment environment = Environment.getAssetMap().getAsset(environmentId);
        return environment == null || environment.isBlockModificationAllowed();
    }
}
