package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interaction;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector4d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.utils.LogScopes;

public class InteractionGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String ID = "Interaction";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(InteractionConfig.class, InteractionConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());

        InteractionConfig config = getConfig(InteractionConfig.class, asset);
        if (config == null) {
            config = InteractionConfig.DEFAULTS;
        }

        RootInteraction root = config.resolveRootInteraction();
        if (root == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "interaction: root interaction missing or unresolvable: " + config.getRootInteractionId());
            return;
        }

        Ref<EntityStore> casterRef = hexContext.getCasterRef(accessor);

        Ref<EntityStore> sourceRef = null;
        if (config.getAnchor() != InteractionAnchor.CASTER
                && hasSlot(asset, InteractionGlyphSlots.SOURCE)) {
            HexVar source = glyph.readSlot(InteractionGlyphSlots.SOURCE, hexContext);
            if (source instanceof EntityVar sourceEntity) {
                Ref<EntityStore> ref = sourceEntity.getRef(accessor);
                if (ref != null && ref.isValid()) {
                    sourceRef = ref;
                }
            }
        }
        if (sourceRef == null) {
            sourceRef = casterRef;
        }
        if (sourceRef == null || !sourceRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "interaction: no source or caster entity to run from");
            return;
        }

        InteractionManager manager = accessor.getComponent(sourceRef,
                InteractionModule.get().getInteractionManagerComponent());
        if (manager == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "interaction: source entity has no interaction manager");
            return;
        }

        HexVar target = hasSlot(asset, InteractionGlyphSlots.TARGET)
                ? glyph.readSlot(InteractionGlyphSlots.TARGET, hexContext)
                : null;
        Ref<EntityStore> targetRef = null;
        int targetNetworkId = Entity.UNASSIGNED_ID;
        BlockPosition targetBlock = null;
        Vector4d targetHit = null;
        HexVar resultVar = null;

        if (target instanceof EntityVar entityVar) {
            Ref<EntityStore> ref = entityVar.getRef(accessor);
            if (ref == null || !ref.isValid()) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "interaction: target entity is gone");
                return;
            }
            targetRef = ref;
            NetworkId networkId = accessor.getComponent(targetRef, NetworkId.getComponentType());
            targetNetworkId = networkId != null ? networkId.getId() : Entity.UNASSIGNED_ID;
            resultVar = target;
        } else if (target instanceof BlockVar blockVar && blockVar.getValue() != null) {
            Vector3i pos = blockVar.getValue();
            targetBlock = new BlockPosition(pos.x, pos.y, pos.z);
            targetHit = new Vector4d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 1);
            resultVar = target;
        } else if (target instanceof PositionVar positionVar && positionVar.getValue() != null) {
            Vector3d pos = positionVar.getValue();
            targetHit = new Vector4d(pos.x, pos.y, pos.z, 1);
            BlockVar snapped = positionVar.toBlockVar(accessor);
            if (snapped != null && snapped.getValue() != null) {
                Vector3i b = snapped.getValue();
                targetBlock = new BlockPosition(b.x, b.y, b.z);
            }
            resultVar = target;
        }

        InteractionType type = config.getInteractionType();
        InteractionContext interactionContext;
        if (config.getAnchor() == InteractionAnchor.TARGET && targetRef != null) {
            interactionContext = InteractionContext.forProxyEntity(manager, sourceRef, targetRef, accessor);
        } else {
            if (config.getAnchor() == InteractionAnchor.TARGET) {
                LOGGER.atFine().log(
                        "interaction: RunOn=TARGET needs an entity target, falling back to source anchoring");
            }
            interactionContext = InteractionContext.forInteraction(manager, sourceRef, type, accessor);
        }

        if (targetRef != null) {
            interactionContext.getMetaStore().putMetaObject(Interaction.TARGET_ENTITY, targetRef);
        }
        if (targetBlock != null) {
            interactionContext.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, targetBlock);
            interactionContext.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, targetBlock);
        }
        if (targetHit != null) {
            interactionContext.getMetaStore().putMetaObject(Interaction.HIT_LOCATION, targetHit);
        }

        if (!config.getInteractionVars().isEmpty()) {
            Map<String, String> merged = new HashMap<>(interactionContext.getInteractionVars());
            merged.putAll(config.getInteractionVars());
            interactionContext.setInteractionVarsGetter(c -> merged);
        }

        boolean writeResult = config.isWriteResult() && resultVar != null;
        HexVar output = writeResult ? resultVar : null;

        InteractionChain chain;
        if (config.isAwaitCompletion()) {
            InteractionChainData data = new InteractionChainData(targetNetworkId,
                    UUIDUtil.EMPTY_UUID, null, null, targetBlock, Integer.MIN_VALUE, null);
            Runnable onCompletion = () -> {
                CommandBuffer<EntityStore> buffer = interactionContext.getCommandBuffer();
                if (buffer == null) {
                    LOGGER.atWarning().log(
                            "interaction: no command buffer at chain completion, dropping continuation");
                    return;
                }
                hexContext.updateRuntimeAccessors(buffer);
                if (output != null) {
                    glyph.writeOutput(output, hexContext);
                }
                HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            };
            chain = manager.initChain(data, type, interactionContext, root, onCompletion, false);
        } else {
            chain = manager.initChain(type, interactionContext, root, targetNetworkId, targetBlock, false);
        }

        if (config.isUseRules()
                && !manager.applyRules(interactionContext, chain.getChainData(), type, root)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "interaction: rules rejected chain for " + config.getRootInteractionId());
            return;
        }

        manager.queueExecuteChain(chain);

        if (!config.isAwaitCompletion()) {
            if (output != null) {
                glyph.writeOutput(output, hexContext);
            }
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }

    private static boolean hasSlot(GlyphAsset asset, String key) {
        return asset != null && asset.getSlot(key) != null;
    }
}
