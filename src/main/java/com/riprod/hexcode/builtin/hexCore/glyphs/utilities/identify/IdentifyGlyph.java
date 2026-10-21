package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.BlockAccess;

public class IdentifyGlyph implements GlyphHandler {

    public static final String ID = "Identify";

    private static final int PLAYER_KIND = Integer.MAX_VALUE;
    private static final int UNKNOWN_KIND = Integer.MIN_VALUE;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar cached = hexContext.getOwnVariable(glyph.getId());
        if (cached != null)
            return cached;
        HexVar a = glyph.readSlot(IdentifyGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(IdentifyGlyphSlots.B, hexContext);
        return new NumberVar(compareIdentity(a, b, hexContext));
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(IdentifyGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(IdentifyGlyphSlots.B, hexContext);
        glyph.writeOutput(new NumberVar(compareIdentity(a, b, hexContext)), hexContext);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private int compareIdentity(HexVar a, HexVar b, HexContext ctx) {
        if (a == null && b == null)
            return 0;
        if (a == null || b == null)
            return -1;
        CommandBuffer<EntityStore> accessor = ctx.getAccessor();

        var baseType = a.type();

        return switch (baseType) {
            case POSITION, TRANSFORM -> {
                yield compareBlocks(a.position(accessor), b.position(accessor), accessor);
            }
            case ROTATION, NUMBER -> {
                yield a.equalTo(b) ? 0 : 1;
            }
            default -> -1;
        };
    }

    private int compareBlocks(Vector3d a, Vector3d b, CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        Integer idA = blockId(world, Vector3dUtil.toVector3i(a));
        Integer idB = blockId(world, Vector3dUtil.toVector3i(b));
        boolean airA = idA == null || idA == BlockType.EMPTY_ID;
        boolean airB = idB == null || idB == BlockType.EMPTY_ID;
        if (airA && airB)
            return 0;
        if (airA != airB)
            return -1;
        return idA.intValue() == idB.intValue() ? 0 : 1;
    }

    private Integer blockId(World world, Vector3i pos) {
        if (pos == null)
            return null;
        try {
            return BlockAccess.blockId(world, pos);
        } catch (Exception e) {
            return null;
        }
    }

    private int compareEntities(EntityVar a, EntityVar b, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> refA = a.getRef(accessor);
        Ref<EntityStore> refB = b.getRef(accessor);
        if (refA == null || !refA.isValid() || refB == null || !refB.isValid())
            return -1;
        return entityKind(refA, accessor) == entityKind(refB, accessor) ? 0 : 1;
    }

    private int entityKind(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        if (accessor.getComponent(ref, Player.getComponentType()) != null)
            return PLAYER_KIND;
        NPCEntity npc = accessor.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null)
            return npc.getNPCTypeIndex();
        return UNKNOWN_KIND;
    }
}
