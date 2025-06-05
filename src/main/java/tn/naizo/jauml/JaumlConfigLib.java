package tn.naizo.jauml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;

public class JaumlConfigLib {
    private static final Logger LOGGER = Logger.getLogger("JaumlConfigLib");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean createConfigFile(String fileName) {
        String CONFIG_DIR = String.valueOf(FMLPaths.CONFIGDIR.get().resolve(fileName + ".json"));

        LOGGER.log(Level.INFO, "Attempting to create or verify config file: {0}", fileName);

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
            LOGGER.log(Level.FINE, "Appended .json extension to filename: {0}", fileName);
        }

        File configDir = new File(CONFIG_DIR);
        File configFile = new File(CONFIG_DIR + "/" + fileName);

        if (!configDir.exists()) {
            LOGGER.log(Level.FINE, "Config directory does not exist, creating: {0}", CONFIG_DIR);
            configDir.mkdirs();
            LOGGER.log(Level.FINE, "Config directory created: {0}", configDir.exists());
        } else {
            LOGGER.log(Level.FINE, "Config directory already exists: {0}", CONFIG_DIR);
        }

        if (configFile.exists()) {
            LOGGER.log(Level.INFO, "Config file already exists: {0}", configFile.getPath());
            return true;
        }

        LOGGER.log(Level.FINE, "Config file does not exist, attempting to create: {0}", configFile.getPath());
        try {
            boolean created = configFile.createNewFile();
            LOGGER.log(Level.INFO, "Config file creation {0}: {1}", new Object[]{created ? "succeeded" : "failed", configFile.getPath()});
            return created;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create config file: {0}", configFile.getPath());
            LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            return false;
        }
    }

    public static boolean addArrayToConfig(String fileName, String arrayKey, JsonArray array) {
        String CONFIG_DIR = String.valueOf(FMLPaths.CONFIGDIR.get().resolve(fileName + ".json"));

        LOGGER.log(Level.INFO, "Attempting to add array with key '{0}' to config file: {1}", new Object[]{arrayKey, fileName});

        if (!createConfigFile(fileName)) {
            LOGGER.log(Level.SEVERE, "Failed to ensure config file exists: {0}", fileName);
            return false;
        }

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
            LOGGER.log(Level.FINE, "Appended .json extension to filename: {0}", fileName);
        }

        File configFile = new File(CONFIG_DIR + "/" + fileName);
        JsonObject jsonObject = new JsonObject();

        if (configFile.length() > 0) {
            try (FileReader reader = new FileReader(configFile)) {
                LOGGER.log(Level.FINE, "Reading existing content from: {0}", configFile.getPath());
                JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
                if (jsonElement != null && jsonElement.isJsonObject()) {
                    jsonObject = jsonElement.getAsJsonObject();
                    LOGGER.log(Level.FINE, "Loaded existing JSON content: {0}", jsonObject.toString());
                } else {
                    LOGGER.log(Level.WARNING, "Config file is empty or not a valid JSON object: {0}", configFile.getPath());
                }
            } catch (IOException | JsonParseException e) {
                LOGGER.log(Level.SEVERE, "Failed to read config file: {0}", configFile.getPath());
                LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
                return false;
            }
        } else {
            LOGGER.log(Level.FINE, "Config file is empty, initializing new JSON object: {0}", configFile.getPath());
        }

        jsonObject.add(arrayKey, array);
        LOGGER.log(Level.FINE, "Added/updated array with key '{0}' in JSON: {1}", new Object[]{arrayKey, array.toString()});

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(jsonObject, writer);
            LOGGER.log(Level.INFO, "Successfully wrote array to config file: {0}", configFile.getPath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write array to config file: {0}", configFile.getPath());
            LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            return false;
        }
    }

    // Retrieves the array from the JSON config file for the specified key
    public static JsonArray getArrayFromConfig(String fileName, String arrayKey) {
        String CONFIG_DIR = String.valueOf(FMLPaths.CONFIGDIR.get().resolve(fileName + ".json"));

        LOGGER.log(Level.INFO, "Attempting to retrieve array with key '{0}' from config file: {1}", new Object[]{arrayKey, fileName});

        if (!createConfigFile(fileName)) {
            LOGGER.log(Level.SEVERE, "Failed to ensure config file exists: {0}", fileName);
            return null;
        }

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
            LOGGER.log(Level.FINE, "Appended .json extension to filename: {0}", fileName);
        }

        File configFile = new File(CONFIG_DIR + "/" + fileName);

        if (configFile.length() == 0) {
            LOGGER.log(Level.WARNING, "Config file is empty: {0}", configFile.getPath());
            return null;
        }

        try (FileReader reader = new FileReader(configFile)) {
            LOGGER.log(Level.FINE, "Reading content from: {0}", configFile.getPath());
            JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
            if (jsonElement != null && jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement arrayElement = jsonObject.get(arrayKey);
                if (arrayElement != null && arrayElement.isJsonArray()) {
                    LOGGER.log(Level.INFO, "Successfully retrieved array for key '{0}': {1}", new Object[]{arrayKey, arrayElement.toString()});
                    return arrayElement.getAsJsonArray();
                } else {
                    LOGGER.log(Level.WARNING, "No array found for key '{0}' in config file: {1}", new Object[]{arrayKey, configFile.getPath()});
                    return null;
                }
            } else {
                LOGGER.log(Level.WARNING, "Config file is not a valid JSON object: {0}", configFile.getPath());
                return null;
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Failed to read config file: {0}", configFile.getPath());
            LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            return null;
        }
    }
}