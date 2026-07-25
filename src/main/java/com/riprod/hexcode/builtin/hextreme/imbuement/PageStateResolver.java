package com.riprod.hexcode.builtin.hextreme.imbuement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public final class PageStateResolver {

    private static volatile Map<String, String> stateToBase = null;

    private PageStateResolver() {
    }

    @Nullable
    public static String resolveBase(@Nullable ItemStack stack, String stateKey) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();
        if (item == null || !item.isState()) {
            return null;
        }
        Map<String, String> map = stateToBase;
        if (map == null) {
            map = build(stateKey);
        }
        String baseId = map.get(item.getId());
        if (baseId != null) {
            return baseId;
        }
        // a miss on a genuine state variant means the cache predates an asset reload
        return build(stateKey).get(item.getId());
    }

    private static Map<String, String> build(String stateKey) {
        Map<String, String> map = new HashMap<>();
        for (Item item : Item.getAssetMap().getAssetMap().values()) {
            if (item == null || item.isState()) {
                continue;
            }
            String stateId = item.getItemIdForState(stateKey);
            if (stateId != null) {
                map.put(stateId, item.getId());
            }
        }
        Map<String, String> built = Collections.unmodifiableMap(map);
        stateToBase = built;
        return built;
    }
}
