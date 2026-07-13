package com.riprod.hexcode.core.common.drawing.system;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.drawing.registry.TemplateAsset;
import com.riprod.hexcode.core.common.drawing.system.shapes.DollarOneFixedDetector;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class ShapeTemplateStore {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String TEMPLATES_SUBPATH = "Server/Hexcode/Templates";

    public static final class Result {
        public final boolean success;
        @Nullable public final String error;
        @Nullable public final Path path;
        @Nullable public final String packName;

        private Result(boolean success, @Nullable String error, @Nullable Path path, @Nullable String packName) {
            this.success = success;
            this.error = error;
            this.path = path;
            this.packName = packName;
        }

        public static Result ok(Path path, String packName) {
            return new Result(true, null, path, packName);
        }

        public static Result fail(String error) {
            return new Result(false, error, null, null);
        }
    }

    @Nonnull
    public static Result saveTemplate(@Nonnull String shapeId, @Nonnull FloatArrayList rawAngles,
                                      @Nullable String overridePackName) {
        AssetPack pack = resolvePack(shapeId, overridePackName);
        if (pack == null) {
            return Result.fail("no writable asset pack found for shape '" + shapeId + "' (override="
                    + overridePackName + "); pass --pack=<group:name> to a non-immutable pack");
        }

        try {
            Path templateDir = pack.getRoot().resolve(TEMPLATES_SUBPATH);
            if (!Files.exists(templateDir)) {
                Files.createDirectories(templateDir);
            }

            int count = rawAngles.size() / 2;
            float[][] raw = new float[count][2];
            for (int i = 0; i < count; i++) {
                raw[i][0] = rawAngles.getFloat(i * 2);
                raw[i][1] = rawAngles.getFloat(i * 2 + 1);
            }

            float[][] processed = DollarOneFixedDetector.preprocess(raw);

            float[] flat = new float[processed.length * 2];
            for (int i = 0; i < processed.length; i++) {
                flat[i * 2] = processed[i][0];
                flat[i * 2 + 1] = processed[i][1];
            }

            int index = nextFreeIndex(templateDir, shapeId);
            String filename = shapeId + "_" + index + ".json";
            Path file = templateDir.resolve(filename);

            TemplateData data = new TemplateData();
            data.ShapeId = shapeId;
            data.Points = flat;
            data.IsTraining = true;

            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(data, writer);
            }

            LOGGER.atFine().log("saved training template for '" + shapeId + "' (" + processed.length
                    + " points) to " + file + " in pack '" + pack.getName() + "'");
            return Result.ok(file, pack.getName());
        } catch (IOException e) {
            LOGGER.atSevere().withCause(e).log("failed to save template for " + shapeId);
            return Result.fail("io error: " + e.getMessage());
        }
    }

    @Nullable
    private static AssetPack resolvePack(@Nonnull String shapeId, @Nullable String overrideName) {
        AssetModule mod = AssetModule.get();

        if (overrideName != null && !overrideName.isEmpty()) {
            AssetPack override = mod.getAssetPack(overrideName);
            if (override == null) {
                logAvailablePacks(overrideName);
                LOGGER.atWarning().log("override pack '" + overrideName + "' not found");
                return null;
            }
            if (override.isImmutable()) {
                LOGGER.atWarning().log("override pack '" + overrideName + "' is immutable; cannot write");
                return null;
            }
            return override;
        }

        String ownerName = ShapeAsset.getAssetMap().getAssetPack(shapeId);
        if (ownerName != null) {
            AssetPack owner = mod.getAssetPack(ownerName);
            if (owner != null && !owner.isImmutable()) {
                return owner;
            }
            if (owner != null) {
                LOGGER.atWarning().log("owner pack '" + ownerName + "' is immutable; falling back");
            }
        }

        return null;
    }

    private static int nextFreeIndex(Path templateDir, String shapeId) {
        int max = -1;
        String prefix = shapeId + "_";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(templateDir, prefix + "*.json")) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                String mid = name.substring(prefix.length(), name.length() - ".json".length());
                try {
                    int n = Integer.parseInt(mid);
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

        int registrySize = TemplateAsset.getTemplatesForShape(shapeId).size();
        return Math.max(max + 1, registrySize);
    }

    private static void logAvailablePacks(@Nonnull String requested) {
        LOGGER.atWarning().log("pack must be one of (requested: '" + requested + "'):");
        for (AssetPack p : AssetModule.get().getAssetPacks()) {
            LOGGER.atWarning().log("  - '" + p.getName() + "' (immutable=" + p.isImmutable()
                    + ", root=" + p.getRoot() + ")");
        }
    }

    @SuppressWarnings("unused")
    private static class TemplateData {
        String ShapeId;
        float[] Points;
        Boolean IsTraining;
    }
}
