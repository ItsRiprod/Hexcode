package com.riprod.hexcode.core.common.execution.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;

public class PlayerHexRoot implements HexRoot {
    // canonical storage — codec'd. caches ref+uuid; setEntity(ref, accessor) populates
    // both, so subsequent getEntity(...) calls return the cached ref without re-lookup.
    private PersistentRef entity;
    // hot-path cache for getSourceRef() no-arg. set at runtime construction.
    // null after codec decode; callers needing live ref post-decode use getEntity().
    private transient Ref<EntityStore> playerRef;

    public PlayerHexRoot() {
    }

    public PlayerHexRoot(Ref<EntityStore> playerRef, ComponentAccessor<EntityStore> accessor) {
        this.playerRef = playerRef;
        this.entity = new PersistentRef();
        this.entity.setEntity(playerRef, accessor);
    }

    private PlayerHexRoot(PersistentRef entity, Ref<EntityStore> playerRef) {
        this.entity = entity;
        this.playerRef = playerRef;
    }

    public PersistentRef getEntity() {
        return entity;
    }

    @Override
    public boolean isAlive() {
        return playerRef != null && playerRef.isValid();
    }

    @Override
    public Ref<EntityStore> getSourceRef(ComponentAccessor<EntityStore> accessor) {
        return playerRef;
    }

    @Override
    public boolean tryConsumeMana(float cost, ComponentAccessor<EntityStore> accessor) {
        if (cost <= 0)
            return true;
        EntityStatMap statMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null)
            return false;
        int manaIndex = DefaultEntityStatTypes.getMana();
        if (statMap.get(manaIndex).get() < cost)
            return false;
        statMap.subtractStatValue(manaIndex, cost);
        return true;
    }

    @Override
    public float getCurrentMana(ComponentAccessor<EntityStore> accessor) {
        EntityStatMap statMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null)
            return 0f;
        return statMap.get(DefaultEntityStatTypes.getMana()).get();
    }

    @Override
    public boolean addMana(float amount, ComponentAccessor<EntityStore> accessor) {
        if (amount <= 0)
            return false;
        EntityStatMap statMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null)
            return false;
        int manaIndex = DefaultEntityStatTypes.getMana();
        float before = statMap.get(manaIndex).get();
        if (before >= statMap.get(manaIndex).getMax())
            return false;
        statMap.addStatValue(manaIndex, amount);
        return true;
    }

    @Override
    public void addDependency(HexContext ctx, Ref<EntityStore> ref) {
        HexcasterIdleComponent hexcasterComp = ctx.getAccessor().getComponent(
                playerRef, HexcasterIdleComponent.getComponentType());
        if (hexcasterComp != null) {
            hexcasterComp.addDependency(ctx.getExecutionId(), ref);
        } else {
            HexcasterIdleComponent newComp = new HexcasterIdleComponent();
            newComp.addDependency(ctx.getExecutionId(), ref);
            ctx.getAccessor().putComponent(playerRef, HexcasterIdleComponent.getComponentType(), newComp);
        }
    }

    public float resolveSpellPower(ComponentAccessor<EntityStore> accessor) {
        EntityStatMap statMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap == null)
            return 1.0f;
        int idx = HexcodeEntityStatTypes.getMagicPower();
        if (idx == Integer.MIN_VALUE)
            return 1.0f;

        EntityStatValue stat = statMap.get(idx);
        return stat != null ? stat.getMax() : 1.0f;
    }

    public float resolveVolatility(ComponentAccessor<EntityStore> accessor) {
        EntityStatMap statMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            int volIndex = HexcodeEntityStatTypes.getVolatility();
            if (volIndex != Integer.MIN_VALUE) {
                EntityStatValue volStat = statMap.get(volIndex);
                if (volStat != null)
                    return volStat.getMax();
            }
        }
        return 0.0f;
    }

    public float resolveMaxMagicCharges(ComponentAccessor<EntityStore> accessor) {
        int chargesIndex = HexcodeEntityStatTypes.getMagicCharges();
        if (chargesIndex != Integer.MIN_VALUE) {
            EntityStatMap statMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
            if (statMap != null) {
                EntityStatValue chargesStat = statMap.get(chargesIndex);
                if (chargesStat != null) {
                    return chargesStat.getMax();
                }
            }
        }
        return 0.0f;
    }

    @Override
    public HexVar getRootVar(HexContext ctx) {
        if (entity == null) return null;
        return new EntityVar(entity);
    }

    @Override
    public HexRoot copy() {
        return new PlayerHexRoot(entity, playerRef);
    }

    public static final BuilderCodec<PlayerHexRoot> CODEC = BuilderCodec
            .builder(PlayerHexRoot.class, PlayerHexRoot::new, HexRoot.BASE_CODEC)
            .append(new KeyedCodec<>("Player", PersistentRef.CODEC),
                    (c, v) -> c.entity = v,
                    c -> c.entity)
            .add()
            .build();
}
