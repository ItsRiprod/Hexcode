package com.riprod.hexcode.builtin.hexCore.glyphs.projectile.system;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.NoState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.style.ConjureStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.ProjectileGlyphSlots;
import com.riprod.hexcode.builtin.hexCore.glyphs.projectile.style.ProjectileStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class ProjectileConstructHandler implements ConstructHandler<NoState> {

    @Override
    public void onFirstTick(HexStatus<NoState> status, ConstructTickContext ctx) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null)
            return;
        Slot immediate = triggering.getSlot(ProjectileGlyphSlots.IMMEDIATE);
        if (immediate == null)
            return;
        String[] links = immediate.getLinks();
        if (links == null || links.length == 0)
            return;
        HexContext hexContext = status.getHexContext();
        hexContext.updateRuntimeAccessors(ctx.getBuffer());
        UUID entityId = ctx.getBuffer().getComponent(ctx.getEntityRef(), UUIDComponent.getComponentType())
                .getUuid();

        hexContext.setDefaultVariable(new EntityVar(entityId, ctx.getEntityRef()));
        HexExecuter.continueExecution(Arrays.asList(links), hexContext);
    }

    @Override
    public void onCleanup(HexStatus<NoState> status, ConstructTickContext ctx) {
        TransformComponent transform = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (transform != null) {
            ProjectileStyle.renderMiss(transform.getPosition(),
                    status.getHexContext(), ctx.getBuffer());
        }
        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }
}
