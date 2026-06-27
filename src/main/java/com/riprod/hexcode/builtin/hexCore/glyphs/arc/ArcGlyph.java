package com.riprod.hexcode.builtin.hexCore.glyphs.arc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.arc.style.ArcStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.arc.utils.ArcUtils;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class ArcGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Arc";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(ArcGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be an Entity");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        Ref<EntityStore> originRef = entityVar.getRef(accessor);
        if (originRef == null || !originRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is invalid");
            return;
        }

        List<String> branches = glyph.getNextLinks();
        if (branches.isEmpty()) {
            return;
        }

        double maxJump = HexVarUtil.numberOrDefault(
                glyph.readSlot(ArcGlyphSlots.JUMP, hexContext), 15.0);
        double delay = HexVarUtil.numberOrDefault(
                glyph.readSlot(ArcGlyphSlots.DELAY, hexContext), 0.75);

        Set<UUID> visited = new HashSet<>();
        UUIDComponent casterUuid = accessor.getComponent(
                hexContext.getCasterRef(accessor), UUIDComponent.getComponentType());
        if (casterUuid != null) visited.add(casterUuid.getUuid());

        UUIDComponent originUuid = accessor.getComponent(
                originRef, UUIDComponent.getComponentType());
        if (originUuid != null) visited.add(originUuid.getUuid());

        TransformComponent originTc = accessor.getComponent(
                originRef, TransformComponent.getComponentType());
        if (originTc == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target does not have a position");
            return;
        }
        Vector3d originPos = originTc.getPosition();

        Ref<EntityStore> firstJump = ArcUtils.getNextArcTarget(
                originPos, (float) maxJump, visited, accessor);
        if (firstJump == null) {
            ArcStyle.renderFizzle(accessor, originPos, hexContext);
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "No entities in range to arc to");
            return;
        }

        UUIDComponent firstJumpUuid = accessor.getComponent(
                firstJump, UUIDComponent.getComponentType());
        if (firstJumpUuid != null) visited.add(firstJumpUuid.getUuid());

        TransformComponent firstJumpTc = accessor.getComponent(
                firstJump, TransformComponent.getComponentType());
        Vector3d firstJumpPos = firstJumpTc != null ? firstJumpTc.getPosition() : originPos;

        World world = accessor.getExternalData().getWorld();
        ArcStyle.renderArc(accessor, world, originPos, firstJumpPos, hexContext);
        ArcStyle.renderHit(accessor, firstJumpPos, hexContext);

        ArcState state = new ArcState(glyph, new ArrayList<>(branches), visited,
                (float) maxJump, (float) delay);

        HexConstructSpawner.applyWithState(
                accessor, firstJump, hexContext, glyph, ArcGlyph.ID, state);
    }
}
