package com.riprod.hexcode.builtin.hexCore.glyphs.rotation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VelocityUtil;

public class RotationValue implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String ID = "Rotation";

    @Override
    public String getId() {
        return ID;
    }

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar xVar = glyph.readSlot(RotationValueSlots.X, hexContext);
        HexVar yVar = glyph.readSlot(RotationValueSlots.Y, hexContext);
        HexVar zVar = glyph.readSlot(RotationValueSlots.Z, hexContext);

        var accessor = hexContext.getAccessor();
        return new RotationVar(new Rotation3f(
                (float) HexVarUtil.rotationAxis(xVar, 0, accessor),
                (float) HexVarUtil.rotationAxis(yVar, 1, accessor),
                (float) HexVarUtil.rotationAxis(zVar, 2, accessor)));
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getVariable(glyph.getId());

        if (self != null) {
            return self;
        }

        return compute(glyph, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        if (result instanceof RotationVar rotVar && rotVar.getValue() != null) {
            HexVar target = glyph.readSlot(RotationValueSlots.TARGET, hexContext);
            EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
            if (entityVar != null) {
                applyToEntity(entityVar, rotVar.getValue(), hexContext);
            } else {
                BlockVar blockVar = HexVarUtil.resolveBlockVar(target, hexContext);
                if (blockVar != null && blockVar.getValue() != null) {
                    applyToBlock(blockVar.getValue(), rotVar.getValue(), hexContext);
                }
            }
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void applyToEntity(EntityVar entityVar, Rotation3f rotation, HexContext hexContext) {
        Ref<EntityStore> ref = entityVar.getRef(hexContext.getAccessor());
        if (ref == null || !ref.isValid())
            return;

        if (VelocityUtil.isProjectile(ref, hexContext.getAccessor())) {
            applyToProjectile(ref, rotation, hexContext);
            return;
        }

        try {
            TransformComponent tc = hexContext.getAccessor().getComponent(ref,
                    TransformComponent.getComponentType());
            if (tc == null)
                return;

            HeadRotation hr = hexContext.getAccessor().getComponent(ref,
                    HeadRotation.getComponentType());
            if (hr == null) {
                tc.setRotation(rotation);
                return;
            }

            Vector3d position = tc.getPosition();
            Rotation3f headRotation = new Rotation3f(rotation);
            Rotation3f bodyRotation = new Rotation3f(0.0f, headRotation.y, 0.0f);
            Teleport teleport = Teleport.createExact(position, bodyRotation, headRotation);
            hexContext.getAccessor().addComponent(ref, Teleport.getComponentType(), teleport);
        } catch (Exception e) {
            LOGGER.atWarning().log("rotation glyph: could not rotate entity: %s", e.getMessage());
        }
    }

    private void applyToProjectile(Ref<EntityStore> ref, Rotation3f rotation, HexContext hexContext) {
        Velocity vel = hexContext.getAccessor().getComponent(ref, Velocity.getComponentType());
        double speed = 0.0;
        if (vel != null) {
            Vector3d current = vel.getVelocity();
            if (current != null) {
                speed = Math.sqrt(current.x * current.x + current.y * current.y + current.z * current.z);
            }

        }
        if (speed <= 0.0001)
            speed = 1.0;

        double yaw = rotation.y;
        double pitch = rotation.x;
        double cosPitch = Math.cos(pitch);
        Vector3d newVel = new Vector3d(
                -Math.sin(yaw) * cosPitch * speed,
                -Math.sin(pitch) * speed,
                Math.cos(yaw) * cosPitch * speed);

        try {
            VelocityUtil.applyVelocity(ref, newVel, ChangeVelocityType.Set,
                    new VelocityConfig(), hexContext.getAccessor());
        } catch (Exception e) {
            LOGGER.atWarning().log("rotation glyph: could not re-aim projectile: %s", e.getMessage());
        }
    }

    private void applyToBlock(Vector3i pos, Rotation3f rotation, HexContext hexContext) {
        try {
            World world = hexContext.getAccessor().getExternalData().getWorld();
            int blockId = world.getBlock(pos.x, pos.y, pos.z);
            if (blockId == BlockType.EMPTY_ID)
                return;
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType == null)
                return;
            Rotation yaw = quarter(rotation.y);
            int rotationIndex = RotationTuple.index(yaw, Rotation.None, Rotation.None);
            int settings = 0x02 | 0x04 | 0x10;
            world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z))
                    .setBlock(pos.x, pos.y, pos.z, blockId, blockType, rotationIndex, 0, settings);
        } catch (Exception e) {
            LOGGER.atWarning().log("rotation glyph: could not rotate block: %s", e.getMessage());
        }
    }

    private static Rotation quarter(float radians) {
        int steps = ((Math.round(radians / (float) (Math.PI / 2)) % 4) + 4) % 4;
        return Rotation.VALUES[steps];
    }
}
