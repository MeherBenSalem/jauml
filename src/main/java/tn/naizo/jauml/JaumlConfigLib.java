package tn.naizo.jauml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraftforge.fml.loading.FMLPaths;
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

        File configDir = FMLPaths.CONFIGDIR.get().resolve(dir).toFile();
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

        File configFile = FMLPaths.CONFIGDIR.get().resolve(dir).resolve(fileName).toFile();

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
                        if (item.isJsonPrimitive() && item.getAsString().equals(targetString)) {
                            return true;
                        }
                    }
                }
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

        File configFile = FMLPaths.CONFIGDIR.get().resolve(dir).resolve(fileName).toFile();

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
        File configFile = FMLPaths.CONFIGDIR.get().resolve(dir).resolve(fileName).toFile();
        return configFile.exists();
    }

}