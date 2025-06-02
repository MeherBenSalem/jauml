package tn.naizo.jauml;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JaumlConfigLib {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String modId;
    private final Path configPath;
    private final Gson gson;
    private JsonObject configRoot;

    public JaumlConfigLib(String modId) {
        this.modId = modId;
        this.configPath = FMLPaths.CONFIGDIR.get().resolve(modId + ".json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.configRoot = loadConfig();
    }

    private JsonObject loadConfig() {
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                JsonElement parsed = JsonParser.parseString(json);
                if (parsed.isJsonObject()) {
                    LOGGER.info("Loaded config for mod {} from {}", modId, configPath);
                    return parsed.getAsJsonObject();
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to load config for {}: {}", modId, e.getMessage());
        }

        JsonObject defaultConfig = new JsonObject(); // empty default
        saveConfig(defaultConfig);
        LOGGER.info("Created default config for mod {} at {}", modId, configPath);
        return defaultConfig;
    }

    private void saveConfig(JsonObject config) {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, gson.toJson(config));
        } catch (IOException e) {
            LOGGER.error("Failed to save config for {}: {}", modId, e.getMessage());
        }
    }

    public void save() {
        saveConfig(configRoot);
    }

    public void reload() {
        this.configRoot = loadConfig();
    }

    public void reset() {
        this.configRoot = new JsonObject();
        save();
    }

    public JsonObject getRoot() {
        return configRoot;
    }

    public boolean has(String key) {
        return configRoot.has(key);
    }

    public void remove(String key) {
        configRoot.remove(key);
        save();
    }

    public void set(String key, JsonElement value) {
        configRoot.add(key, value);
        save();
    }

    public JsonElement get(String key) {
        return configRoot.get(key);
    }

    public void setString(String key, String value) {
        set(key, new JsonPrimitive(value));
    }

    public void setInt(String key, int value) {
        set(key, new JsonPrimitive(value));
    }

    public void setBoolean(String key, boolean value) {
        set(key, new JsonPrimitive(value));
    }

    public String getString(String key, String def) {
        return has(key) ? get(key).getAsString() : def;
    }

    public int getInt(String key, int def) {
        return has(key) ? get(key).getAsInt() : def;
    }

    public boolean getBoolean(String key, boolean def) {
        return has(key) ? get(key).getAsBoolean() : def;
    }

    public List<String> getStringList(String key) {
        List<String> result = new ArrayList<>();
        if (has(key) && get(key).isJsonArray()) {
            for (JsonElement e : get(key).getAsJsonArray()) {
                result.add(e.getAsString());
            }
        }
        return result;
    }

    public void setStringList(String key, List<String> list) {
        JsonArray array = new JsonArray();
        for (String val : list) {
            array.add(val);
        }
        set(key, array);
    }

    public Set<String> getKeys() {
        return configRoot.keySet();
    }

    public void logAll() {
        LOGGER.info("CONFIG [{}]:\n{}", modId, gson.toJson(configRoot));
    }

    public String getModId() {
        return modId;
    }
}
