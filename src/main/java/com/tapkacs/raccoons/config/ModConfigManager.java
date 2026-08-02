package com.tapkacs.raccoons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.tapkacs.raccoons.Raccoons;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("raccoons.json");

    private static RaccoonsConfig instance;

    private ModConfigManager() {
    }

    public static RaccoonsConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    /** Reads {@code config/raccoons.json}, falling back to defaults if missing or unreadable, then
     *  rewrites the file - this both creates it on first run and adds any newly introduced fields
     *  to an older file (Gson silently ignores unknown/missing keys rather than erroring). */
    public static void load() {
        RaccoonsConfig loaded = null;
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                loaded = GSON.fromJson(reader, RaccoonsConfig.class);
            } catch (IOException | JsonParseException e) {
                Raccoons.LOGGER.warn("Failed to read config/raccoons.json, falling back to defaults", e);
            }
        }
        instance = loaded != null ? loaded : new RaccoonsConfig();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            Raccoons.LOGGER.error("Failed to write config/raccoons.json", e);
        }
    }
}
