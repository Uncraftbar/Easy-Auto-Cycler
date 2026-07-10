package com.uncraftbar.easyautocycler.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uncraftbar.easyautocycler.EasyAutoCyclerMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/** Loader-independent client preferences that are reloaded when a merchant screen opens. */
public final class ClientConfig {
    private static final File CONFIG_FILE = new File("config/easyautocycler-client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ClientConfig() {}

    public enum ButtonLocation {
        TOP_LEFT,
        TOP_RIGHT,
        NONE
    }

    public static final class Config {
        public String buttonLocation = ButtonLocation.TOP_LEFT.name();
        public int buttonOffsetX = 0;
        public int buttonOffsetY = 0;

        public ButtonLocation parsedButtonLocation() {
            try {
                return ButtonLocation.valueOf(buttonLocation.trim().toUpperCase(Locale.ROOT));
            } catch (RuntimeException ignored) {
                EasyAutoCyclerMod.LOGGER.warn("Unknown button location '{}'; using TOP_LEFT", buttonLocation);
                return ButtonLocation.TOP_LEFT;
            }
        }
    }

    public static Config load() {
        if (!CONFIG_FILE.exists()) {
            Config defaults = new Config();
            save(defaults);
            return defaults;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Config config = GSON.fromJson(reader, Config.class);
            return config == null ? new Config() : config;
        } catch (IOException | RuntimeException exception) {
            EasyAutoCyclerMod.LOGGER.error("Failed to load client configuration", exception);
            return new Config();
        }
    }

    private static void save(Config config) {
        File parent = CONFIG_FILE.getParentFile();
        if (parent != null) parent.mkdirs();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            EasyAutoCyclerMod.LOGGER.error("Failed to create client configuration", exception);
        }
    }
}
