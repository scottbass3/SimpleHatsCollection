package io.github.scottbass3.simplehatscollection.client;

import io.github.scottbass3.simplehatscollection.Constants;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client-side, per-installation set of favourited hat names, persisted to a small text
 * file in the game config folder (one hat name per line). Favourites are a personal UI
 * preference, so they are kept client-side and are independent of world/server.
 */
public final class FavoritesStore {

    private static final Set<String> FAVORITES = new LinkedHashSet<>();
    private static boolean loaded = false;

    private FavoritesStore() {}

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("simplehatscollection-favorites.txt");
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            Path path = file();
            if (Files.exists(path)) {
                for (String line : Files.readAllLines(path)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) FAVORITES.add(trimmed);
                }
            }
        } catch (IOException e) {
            Constants.LOG.warn("Failed to load hat favorites", e);
        }
    }

    public static boolean isFavorite(String hatName) {
        ensureLoaded();
        return FAVORITES.contains(hatName);
    }

    public static void toggle(String hatName) {
        ensureLoaded();
        if (!FAVORITES.remove(hatName)) FAVORITES.add(hatName);
        save();
    }

    private static void save() {
        try {
            Path path = file();
            Files.createDirectories(path.getParent());
            Files.write(path, FAVORITES);
        } catch (IOException e) {
            Constants.LOG.warn("Failed to save hat favorites", e);
        }
    }
}
