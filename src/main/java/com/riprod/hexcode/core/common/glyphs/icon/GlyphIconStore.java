package com.riprod.hexcode.core.common.glyphs.icon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.common.asset.FileCommonAsset;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.patchly.PatchManager;

public final class GlyphIconStore {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.ASSETS);

    public static final String ICON_COMMON_SUBPATH = "UI/Custom/Pages/Memories/glyphs";
    public static final String ICON_PATH_TEMPLATE = ICON_COMMON_SUBPATH + "/{assetId}.png";

    public static final CommonAssetValidator ICON_VALIDATOR =
            new CommonAssetValidator("png", true, ICON_COMMON_SUBPATH, "Icons");

    private static final String ICONS_SUBPATH = "Common/" + ICON_COMMON_SUBPATH;
    private static final String SYNTHETIC_SUFFIX = "_GlyphIcons";

    private GlyphIconStore() {
    }

    @Nonnull
    public static String derivedIconPath(@Nonnull String glyphId) {
        return ICON_COMMON_SUBPATH + "/" + glyphId + ".png";
    }

    public enum RegenerateStatus {
        OK,
        UNKNOWN_GLYPH,
        NOT_RENDERABLE,
        NO_TARGET,
        WRITE_FAILED
    }

    public static final class Result {
        public final int generated;
        public final int skipped;
        public final int failed;
        @Nullable
        public final String packName;

        private Result(int generated, int skipped, int failed, @Nullable String packName) {
            this.generated = generated;
            this.skipped = skipped;
            this.failed = failed;
            this.packName = packName;
        }
    }

    @Nonnull
    public static Result generateMissing(@Nonnull PluginManifest manifest) {
        Target target = resolveTarget(manifest);
        if (target == null) {
            logAvailablePacks();
            LOGGER.atWarning().log("glyph icons: no writable pack and could not create one; skipping");
            return new Result(0, 0, 0, null);
        }

        int[] counts = new int[3]; // generated, skipped, failed
        GlyphAsset.getAssetMap().getAssetMap().forEach((id, asset) -> {
            String rel = ICONS_SUBPATH + "/" + id + ".png";

            if (existsInAnyPack(rel) || Files.exists(target.root.resolve(rel))) {
                counts[1]++;
                return;
            }

            byte[] png = GlyphIconRenderer.render(id);
            if (png == null) {
                // glyphs without renderable attachments (e.g. templates) are expected
                return;
            }

            try {
                writeIcon(target.root.resolve(rel), png);
                counts[0]++;
            } catch (Exception e) {
                counts[2]++;
                LOGGER.atWarning().withCause(e).log("glyph icon: failed to write '" + id + "'");
            }
        });

        if (target.register && counts[0] > 0) {
            try {
                AssetModule.get().registerPack(target.packName, target.root, manifest,
                        AssetPack.PackSource.RUNTIME);
                LOGGER.atFine().log("glyph icons: registered synthetic pack '" + target.packName
                        + "' at " + target.root);
            } catch (Exception e) {
                LOGGER.atWarning().withCause(e).log("glyph icons: failed to register synthetic pack '"
                        + target.packName + "'");
            }
        }

        LOGGER.atFine().log("glyph icons: generated=" + counts[0] + " skipped(existing)=" + counts[1]
                + " failed=" + counts[2] + " pack='" + target.packName + "'");

        return new Result(counts[0], counts[1], counts[2], target.packName);
    }

    @Nonnull
    public static RegenerateStatus regenerate(@Nonnull String glyphId, @Nonnull PluginManifest manifest) {
        if (GlyphAsset.getAssetMap().getAsset(glyphId) == null) {
            return RegenerateStatus.UNKNOWN_GLYPH;
        }

        byte[] png = GlyphIconRenderer.render(glyphId);
        if (png == null) {
            return RegenerateStatus.NOT_RENDERABLE;
        }

        String rel = ICONS_SUBPATH + "/" + glyphId + ".png";
        Target target = resolveRegenerateTarget(rel, manifest);
        if (target == null) {
            logAvailablePacks();
            LOGGER.atWarning().log("glyph icons: no writable pack to regenerate '" + glyphId + "' into");
            return RegenerateStatus.NO_TARGET;
        }

        Path file = target.root.resolve(rel);
        try {
            writeIcon(file, png);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("glyph icon: failed to regenerate '" + glyphId + "'");
            return RegenerateStatus.WRITE_FAILED;
        }

        CommonAssetModule.get().addCommonAsset(target.packName,
                new FileCommonAsset(file, derivedIconPath(glyphId), png));

        LOGGER.atFine().log("glyph icon: regenerated '" + glyphId + "' into pack '" + target.packName + "'");
        return RegenerateStatus.OK;
    }

    private static void writeIcon(Path file, byte[] png) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, png);
    }

    @Nullable
    private static Target resolveRegenerateTarget(String rel, PluginManifest manifest) {
        for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            if (pack.isImmutable() || PatchManager.isSyntheticOverridePack(pack.getName())) {
                continue;
            }
            if (Files.exists(pack.getRoot().resolve(rel))) {
                return new Target(pack.getRoot(), pack.getName(), false);
            }
        }
        return resolveTarget(manifest);
    }

    private static boolean existsInAnyPack(String rel) {
        for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            if (Files.exists(pack.getRoot().resolve(rel))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Target resolveTarget(PluginManifest manifest) {
        for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            if (pack.isImmutable() || PatchManager.isSyntheticOverridePack(pack.getName())) {
                continue;
            }
            return new Target(pack.getRoot(), pack.getName(), false);
        }
        return prepareSyntheticPack(manifest);
    }

    @Nullable
    private static Target prepareSyntheticPack(PluginManifest manifest) {
        String name = manifest.getGroup() + ":" + manifest.getName() + SYNTHETIC_SUFFIX;
        try {
            Path dir = PluginManager.MODS_PATH
                    .resolve(manifest.getGroup())
                    .resolve(manifest.getName() + SYNTHETIC_SUFFIX);
            Files.createDirectories(dir);
            // if a previous boot already registered it, reuse it as a plain target
            if (AssetModule.get().getAssetPack(name) != null) {
                return new Target(dir.toAbsolutePath().normalize(), name, false);
            }
            return new Target(dir.toAbsolutePath().normalize(), name, true);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("glyph icons: failed to prepare synthetic pack dir");
            return null;
        }
    }

    private static void logAvailablePacks() {
        LOGGER.atWarning().log("glyph icons: packs are:");
        for (AssetPack p : AssetModule.get().getAssetPacks()) {
            LOGGER.atWarning().log("  - '" + p.getName() + "' (immutable=" + p.isImmutable()
                    + ", synthetic=" + PatchManager.isSyntheticOverridePack(p.getName())
                    + ", root=" + p.getRoot() + ")");
        }
    }

    private static final class Target {
        final Path root;
        final String packName;
        final boolean register;

        Target(Path root, String packName, boolean register) {
            this.root = root;
            this.packName = packName;
            this.register = register;
        }
    }
}
