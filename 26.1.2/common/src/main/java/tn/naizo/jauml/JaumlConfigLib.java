package tn.naizo.jauml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import tn.naizo.jauml.platform.Services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JaumlConfigLib {
    private static final Logger LOGGER = Logger.getLogger("JaumlConfigLib");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean createConfigFile(String dir, String fileName) {

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configDir = Services.PLATFORM.getConfigDirectory().resolve(dir).toFile();
        File configFile = new File(configDir, fileName);

        if (!configDir.exists()) {
            configDir.mkdirs();
        } else {
            LOGGER.log(Level.FINE, "Config directory already exists: {0}", configDir.getPath());
        }

        if (configFile.exists()) {
            return true;
        }

        try {
            return configFile.createNewFile();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create config file: {0}", configFile.getPath());
            LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            return false;
        }
    }

    public static boolean stringExistsInArray(String dir, String fileName, String arrayKey, String targetString) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(arrayKey)) {
                JsonElement element = root.get(arrayKey);
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement item : array) {
                        if (item.isJsonPrimitive()) {
                            String value = item.getAsString();
                            // Check if the array element contains the target string
                            if (value.contains(targetString)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return false;
    }

    public static boolean arrayKeyExists(String dir, String fileName, String key) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null) {
                return root.has(key);
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return false;
    }

    public static boolean addStringToArray(String dir, String fileName, String arrayKey, String stringToAdd) {

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        JsonObject root = new JsonObject();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) {
                    root = new JsonObject();
                }
            } catch (IOException | JsonParseException e) {
                return false;
            }
        }

        JsonArray array;
        if (root.has(arrayKey) && root.get(arrayKey).isJsonArray()) {
            array = root.getAsJsonArray(arrayKey);
        } else {
            array = new JsonArray();
            root.add(arrayKey, array);
        }

        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsString().equals(stringToAdd)) {
                return false;
            }
        }

        array.add(stringToAdd);

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean configFileExists(String dir, String fileName) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }
        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();
        return configFile.exists();
    }

    public static int getArrayLength(String dir, String fileName, String arrayKey) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return 0;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(arrayKey)) {
                JsonElement element = root.get(arrayKey);
                if (element.isJsonArray()) {
                    return element.getAsJsonArray().size();
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return 0;
    }

    public static String getArrayElement(String dir, String fileName, String arrayKey, int index) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(arrayKey)) {
                JsonElement element = root.get(arrayKey);
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    if (index >= 0 && index < array.size()) {
                        JsonElement item = array.get(index);
                        if (item.isJsonPrimitive()) {
                            return item.getAsString();
                        }
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return null;
    }

    public static String getStringValue(String dir, String fileName, String key) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(key)) {
                JsonElement element = root.get(key);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    return element.getAsString();
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return null;
    }

    public static double getNumberValue(String dir, String fileName, String key) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return 0.0;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(key)) {
                JsonElement element = root.get(key);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsNumber().doubleValue();
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return 0.0;
    }

    public static boolean setStringValue(String dir, String fileName, String key, String value) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            if (!createConfigFile(dir, fileName)) {
                LOGGER.log(Level.SEVERE, "Failed to create config file: {0}", configFile.getPath());
                return false;
            }
        }

        JsonObject root = new JsonObject();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) {
                    root = new JsonObject();
                }
            } catch (IOException | JsonParseException e) {
                LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
                return false;
            }
        }

        root.addProperty(key, value);

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing to config file: {0}", e.getMessage());
            return false;
        }
    }

    public static boolean setNumberValue(String dir, String fileName, String key, double value) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            if (!createConfigFile(dir, fileName)) {
                LOGGER.log(Level.SEVERE, "Failed to create config file: {0}", configFile.getPath());
                return false;
            }
        }

        JsonObject root = new JsonObject();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) {
                    root = new JsonObject();
                }
            } catch (IOException | JsonParseException e) {
                LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
                return false;
            }
        }

        root.addProperty(key, value);

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing to config file: {0}", e.getMessage());
            return false;
        }
    }

    public static boolean clearArray(String dir, String fileName, String arrayKey) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            LOGGER.log(Level.WARNING, "Config file does not exist: {0}", configFile.getPath());
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null)
                root = new JsonObject();

            // Replace the array with an empty one
            root.add(arrayKey, new JsonArray());

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(root, writer);
                return true;
            }

        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error clearing array in config file: {0}", e.getMessage());
            return false;
        }
    }

    public static boolean removeArrayElement(String dir, String fileName, String arrayKey, String valueToRemove) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            LOGGER.log(Level.WARNING, "Config file does not exist: {0}", configFile.getPath());
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has(arrayKey) || !root.get(arrayKey).isJsonArray()) {
                LOGGER.log(Level.WARNING, "Array key not found or not a valid array: {0}", arrayKey);
                return false;
            }

            JsonArray array = root.getAsJsonArray(arrayKey);
            JsonArray updatedArray = new JsonArray();

            boolean removed = false;
            for (JsonElement element : array) {
                if (element.isJsonPrimitive() && element.getAsString().equals(valueToRemove)) {
                    removed = true;
                    continue;
                }
                updatedArray.add(element);
            }

            if (!removed) {
                return false;
            }

            root.add(arrayKey, updatedArray);

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(root, writer);
                return true;
            }

        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error removing array element: {0}", e.getMessage());
            return false;
        }
    }

    public static java.util.List<String> getArrayAsList(String dir, String fileName, String arrayKey) {
        java.util.List<String> result = new java.util.ArrayList<>();

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return result;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(arrayKey)) {
                JsonElement element = root.get(arrayKey);
                if (element.isJsonArray()) {
                    for (JsonElement item : element.getAsJsonArray()) {
                        if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
                            result.add(item.getAsString());
                        } else {
                            result.add(item.toString());
                        }
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading array as list: {0}", e.getMessage());
        }

        return result;
    }

    // ==================== v1.2.0 ADDITIONS ====================

    /**
     * Gets a string value with a default fallback if the key doesn't exist.
     * 
     * @since 1.2.0
     */
    public static String getStringValue(String dir, String fileName, String key, String defaultValue) {
        String value = getStringValue(dir, fileName, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a number value with a default fallback if the key doesn't exist.
     * 
     * @since 1.2.0
     */
    public static double getNumberValue(String dir, String fileName, String key, double defaultValue) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return defaultValue;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(key)) {
                JsonElement element = root.get(key);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsNumber().doubleValue();
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return defaultValue;
    }

    /**
     * Gets a boolean value with a default fallback if the key doesn't exist.
     * 
     * @since 1.2.0
     */
    public static boolean getBooleanValue(String dir, String fileName, String key, boolean defaultValue) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return defaultValue;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has(key)) {
                JsonElement element = root.get(key);
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                    return element.getAsBoolean();
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        return defaultValue;
    }

    /**
     * Gets an integer value from the config file.
     * 
     * @since 1.2.0
     */
    public static int getIntValue(String dir, String fileName, String key) {
        return (int) getNumberValue(dir, fileName, key);
    }

    /**
     * Gets an integer value with a default fallback.
     * 
     * @since 1.2.0
     */
    public static int getIntValue(String dir, String fileName, String key, int defaultValue) {
        return (int) getNumberValue(dir, fileName, key, defaultValue);
    }

    /**
     * Gets a long value from the config file.
     * 
     * @since 1.2.0
     */
    public static long getLongValue(String dir, String fileName, String key) {
        return (long) getNumberValue(dir, fileName, key);
    }

    /**
     * Gets a long value with a default fallback.
     * 
     * @since 1.2.0
     */
    public static long getLongValue(String dir, String fileName, String key, long defaultValue) {
        return (long) getNumberValue(dir, fileName, key, defaultValue);
    }

    /**
     * Sets an integer value in the config file.
     * 
     * @since 1.2.0
     */
    public static boolean setIntValue(String dir, String fileName, String key, int value) {
        return setNumberValue(dir, fileName, key, value);
    }

    /**
     * Sets a long value in the config file.
     * 
     * @since 1.2.0
     */
    public static boolean setLongValue(String dir, String fileName, String key, long value) {
        return setNumberValue(dir, fileName, key, value);
    }

    /**
     * Deletes a config file.
     * 
     * @return true if the file was deleted, false otherwise
     * @since 1.2.0
     */
    public static boolean deleteConfigFile(String dir, String fileName) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return false;
        }

        try {
            return configFile.delete();
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete config file: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a key exists in the config file (any type, not just arrays).
     * 
     * @since 1.2.0
     */
    public static boolean hasKey(String dir, String fileName, String key) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            return root != null && root.has(key);
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets all top-level keys from a config file.
     * 
     * @return List of key names, empty list if file doesn't exist or on error
     * @since 1.2.0
     */
    public static java.util.List<String> getAllKeys(String dir, String fileName) {
        java.util.List<String> keys = new java.util.ArrayList<>();

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return keys;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null) {
                for (String key : root.keySet()) {
                    keys.add(key);
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading config file keys: {0}", e.getMessage());
        }

        return keys;
    }

    /**
     * Removes a key from the config file.
     * 
     * @return true if the key was removed, false if it didn't exist or on error
     * @since 1.2.0
     */
    public static boolean removeKey(String dir, String fileName, String key) {
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = Services.PLATFORM.getConfigDirectory().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has(key)) {
                return false;
            }

            root.remove(key);

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(root, writer);
                return true;
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error removing key from config file: {0}", e.getMessage());
            return false;
        }
    }
}
