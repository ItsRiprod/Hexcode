package com.riprod.hexcode.builtin.elementalism.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.CaseInsensitiveHashStrategy;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.utils.LogScopes;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public final class ElementTierIndex {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.ELEMENT);

    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new TierIdValidator());

    private static volatile Map<String, String> elementByTier = Map.of();

    private ElementTierIndex() {
    }

    public static boolean contains(String tierId) {
        return elementByTier.containsKey(tierId);
    }

    @Nullable
    public static ElementAsset getElement(String tierId) {
        var elementId = elementByTier.get(tierId);
        return elementId != null ? ElementAsset.getAssetMap().getAsset(elementId) : null;
    }

    @Nullable
    public static ElementalTier getTier(String tierId) {
        var element = getElement(tierId);
        return element != null ? element.getTiers().byId(tierId) : null;
    }

    public static void onLoaded(LoadedAssetsEvent<String, ElementAsset, DefaultAssetMap<String, ElementAsset>> event) {
        var rejected = rebuild(event.getLoadedAssets().keySet());
        if (!rejected.isEmpty()) {
            ElementAsset.getAssetStore().removeAssets(rejected);
        }
    }

    public static void onRemoved(RemovedAssetsEvent<String, ElementAsset, DefaultAssetMap<String, ElementAsset>> event) {
        rebuild(Set.of());
    }

    private static List<String> rebuild(Set<String> loaded) {
        var settled = new TreeMap<String, ElementAsset>(String.CASE_INSENSITIVE_ORDER);
        var incoming = new TreeMap<String, ElementAsset>(String.CASE_INSENSITIVE_ORDER);
        for (var element : ElementAsset.getAssetMap().getAssetMap().values()) {
            (loaded.contains(element.getId()) ? incoming : settled).put(element.getId(), element);
        }

        var claims = new Object2ObjectOpenCustomHashMap<String, String>(CaseInsensitiveHashStrategy.getInstance());
        var rejected = new ArrayList<String>();
        for (var element : settled.values()) {
            claim(element, claims, rejected);
        }
        for (var element : incoming.values()) {
            claim(element, claims, rejected);
        }
        elementByTier = Object2ObjectMaps.unmodifiable(claims);
        return rejected;
    }

    private static void claim(ElementAsset element, Map<String, String> claims, List<String> rejected) {
        var entries = element.getTiers().entries();
        for (var entry : entries) {
            var owner = claims.get(entry.tier().getId());
            if (owner != null) {
                LOGGER.atSevere().log("Rejected element '%s': tier Id '%s' is already used by element '%s'",
                        element.getId(), entry.tier().getId(), owner);
                rejected.add(element.getId());
                return;
            }
        }
        for (var entry : entries) {
            claims.put(entry.tier().getId(), element.getId());
        }
    }

    private static final class TierIdValidator implements Validator<String> {
        @Override
        public void accept(String tierId, ValidationResults results) {
            if (tierId != null && !contains(tierId)) {
                results.fail("Tier '" + tierId + "' doesn't exist on any loaded element");
            }
        }

        @Override
        public void updateSchema(SchemaContext context, Schema target) {
        }
    }
}
