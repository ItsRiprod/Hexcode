package com.riprod.hexcode.core.common.stats;

import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;

public abstract class HexcodeEntityStatTypes {
    public static int getVolatility() {
        return EntityStatType.getAssetMap().getIndex("Volatility");
    }

    public static int getStability() {
        return EntityStatType.getAssetMap().getIndex("Stability");
    }

    public static int getMagicPower() {
        return EntityStatType.getAssetMap().getIndex("Magic_Power");
    }

    public static int getMagicCharges() {
        return EntityStatType.getAssetMap().getIndex("MagicCharges");
    }

    public static int getInContext() {
        return EntityStatType.getAssetMap().getIndex("InContext");
    }

    public static int getIsHolding() {
        return EntityStatType.getAssetMap().getIndex("Flag_IsHolding");
    }
}
