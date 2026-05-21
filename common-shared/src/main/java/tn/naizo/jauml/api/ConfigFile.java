package tn.naizo.jauml.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigFile {

    private static final Logger LOGGER = LoggerFactory.getLogger("JaumlConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path filePath;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private JsonObject rootData;

    ConfigFile(Path filePath) {
        this.filePath = filePath;
        this.rootData = new JsonObject();
        loadFromDisk();
    }

    /**
     * Loads or reloads the configuration data from disk.
     * If the file does not exist, an empty configuration is maintained in memory.
     */
    public void reload() {
        lock.writeLock().lock();
        try {
            loadFromDisk();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void loadFromDisk() {
        if (!Files.exists(filePath)) {
            this.rootData = new JsonObject();
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed.isJsonObject()) {
                this.rootData = parsed.getAsJsonObject();
            } else {
                LOGGER.warn("Config file at {} did not contain a valid JSON object. Initializing empty config.", filePath);
                this.rootData = new JsonObject();
            }
        } catch (IOException | JsonParseException | IllegalStateException e) {
            LOGGER.error("Failed to load or parse config file: " + filePath, e);
            this.rootData = new JsonObject(); // Fallback to safe state to prevent null crashes
        }
    }

    /**
     * Writes the current in-memory configuration back to disk atomically.
     */
    public void save() {
        lock.readLock().lock();
        try {
            // Ensure parent directories exist
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(rootData, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config file: " + filePath, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if the configuration file physically exists on disk.
     */
    public boolean exists() {
        return Files.exists(filePath);
    }

    /**
     * Deletes the configuration file from disk and clears the in-memory cache.
     * @return true if successful, false otherwise
     */
    public boolean delete() {
        lock.writeLock().lock();
        try {
            this.rootData = new JsonObject();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete config file: " + filePath, e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the safe, normalized Path of this config file.
     */
    public Path path() {
        return filePath;
    }

    // ==================== GETTERS ====================

    public String getString(String key, String defaultValue) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                return el.getAsString();
            }
            return defaultValue;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key, int defaultValue) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsInt();
            }
            return defaultValue;
        } finally {
            lock.readLock().unlock();
        }
    }

    public OptionalInt getInt(String key) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return OptionalInt.of(el.getAsInt());
            }
            return OptionalInt.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getLong(String key, long defaultValue) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsLong();
            }
            return defaultValue;
        } finally {
            lock.readLock().unlock();
        }
    }

    public OptionalLong getLong(String key) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return OptionalLong.of(el.getAsLong());
            }
            return OptionalLong.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getDouble(String key, double defaultValue) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsDouble();
            }
            return defaultValue;
        } finally {
            lock.readLock().unlock();
        }
    }

    public OptionalDouble getDouble(String key) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return OptionalDouble.of(el.getAsDouble());
            }
            return OptionalDouble.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                return el.getAsBoolean();
            }
            return defaultValue;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Boolean getBoolean(String key) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                return el.getAsBoolean();
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ==================== SETTERS ====================

    public ConfigFile set(String key, String value) {
        lock.writeLock().lock();
        try {
            if (value == null) {
                rootData.remove(key);
            } else {
                rootData.addProperty(key, value);
            }
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ConfigFile set(String key, int value) {
        lock.writeLock().lock();
        try {
            rootData.addProperty(key, value);
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ConfigFile set(String key, long value) {
        lock.writeLock().lock();
        try {
            rootData.addProperty(key, value);
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ConfigFile set(String key, double value) {
        lock.writeLock().lock();
        try {
            rootData.addProperty(key, value);
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ConfigFile set(String key, boolean value) {
        lock.writeLock().lock();
        try {
            rootData.addProperty(key, value);
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== ARRAY OPERATIONS ====================

    /**
     * Gets a list of string elements from a JSON array under the given key.
     */
    public List<String> getStringList(String key) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonArray()) {
                JsonArray array = el.getAsJsonArray();
                List<String> list = new ArrayList<>(array.size());
                for (JsonElement item : array) {
                    if (item.isJsonPrimitive()) {
                        list.add(item.getAsString());
                    } else {
                        list.add(item.toString());
                    }
                }
                return list;
            }
            return Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a value to a JSON array under the given key.
     * Prevents duplicate strings.
     * @return true if the item was added, false if it already existed
     */
    public boolean addToList(String key, String value) {
        lock.writeLock().lock();
        try {
            JsonArray array;
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonArray()) {
                array = el.getAsJsonArray();
            } else {
                array = new JsonArray();
                rootData.add(key, array);
            }

            // Duplicate check (exact match)
            for (JsonElement item : array) {
                if (item.isJsonPrimitive() && item.getAsString().equals(value)) {
                    return false;
                }
            }

            array.add(value);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a value from a JSON array under the given key.
     * @return true if the item was removed, false if not found
     */
    public boolean removeFromList(String key, String value) {
        lock.writeLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el == null || !el.isJsonArray()) {
                return false;
            }

            JsonArray array = el.getAsJsonArray();
            JsonArray updated = new JsonArray();
            boolean removed = false;

            for (JsonElement item : array) {
                if (item.isJsonPrimitive() && item.getAsString().equals(value)) {
                    removed = true;
                    continue;
                }
                updated.add(item);
            }

            if (removed) {
                rootData.add(key, updated);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a JSON array contains the given value (EXACT match).
     */
    public boolean listContains(String key, String value) {
        lock.readLock().lock();
        try {
            JsonElement el = rootData.get(key);
            if (el != null && el.isJsonArray()) {
                for (JsonElement item : el.getAsJsonArray()) {
                    if (item.isJsonPrimitive() && item.getAsString().equals(value)) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clears all elements from the JSON array under the given key.
     */
    public void clearList(String key) {
        lock.writeLock().lock();
        try {
            rootData.add(key, new JsonArray());
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== STRUCTURAL ====================

    /**
     * Checks if a top-level key exists.
     */
    public boolean hasKey(String key) {
        lock.readLock().lock();
        try {
            return rootData.has(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns a set of all top-level keys in the configuration.
     */
    public Set<String> keys() {
        lock.readLock().lock();
        try {
            return new HashSet<>(rootData.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes a key entirely from the configuration.
     * @return true if the key existed and was removed, false otherwise
     */
    public boolean removeKey(String key) {
        lock.writeLock().lock();
        try {
            if (rootData.has(key)) {
                rootData.remove(key);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
