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
        LOGGER.log(Level.INFO, "Attempting to create or verify config file: {0} in directory: {1}", new Object[]{fileName, dir});

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
            LOGGER.log(Level.FINE, "Appended .json extension to filename: {0}", fileName);
        }

        File configDir = FMLPaths.CONFIGDIR.get().resolve(dir).toFile();
        File configFile = new File(configDir, fileName);

        if (!configDir.exists()) {
            LOGGER.log(Level.FINE, "Config directory does not exist, creating: {0}", configDir.getPath());
            configDir.mkdirs();
            LOGGER.log(Level.FINE, "Config directory created: {0}", configDir.exists());
        } else {
            LOGGER.log(Level.FINE, "Config directory already exists: {0}", configDir.getPath());
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

    public static boolean stringExistsInArray(String dir, String fileName, String arrayKey, String targetString) {
        LOGGER.log(Level.INFO, "Checking if string exists in file: {0}, key: {1}, target: {2}", new Object[]{fileName, arrayKey, targetString});

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = FMLPaths.CONFIGDIR.get().resolve(dir).resolve(fileName).toFile();

        if (!configFile.exists()) {
            LOGGER.log(Level.WARNING, "Config file not found: {0}", configFile.getPath());
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
                            LOGGER.log(Level.INFO, "Found string in array: {0}", targetString);
                            return true;
                        }
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Error reading or parsing config file: {0}", e.getMessage());
        }

        LOGGER.log(Level.INFO, "String not found in array: {0}", targetString);
        return false;
    }

    public static boolean addStringToArray(String dir, String fileName, String arrayKey, String stringToAdd) {
        LOGGER.log(Level.INFO, "Attempting to add string to file: {0}, key: {1}, value: {2}", new Object[]{fileName, arrayKey, stringToAdd});

        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        File configFile = FMLPaths.CONFIGDIR.get().resolve(dir).resolve(fileName).toFile();

        JsonObject root = new JsonObject();

        // Load existing JSON if file exists
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) {
                    root = new JsonObject(); // fallback to avoid null pointer
                }
            } catch (IOException | JsonParseException e) {
                LOGGER.log(Level.SEVERE, "Failed to read or parse config file: {0}", e.getMessage());
                return false;
            }
        }

        // Get or create the array
        JsonArray array;
        if (root.has(arrayKey) && root.get(arrayKey).isJsonArray()) {
            array = root.getAsJsonArray(arrayKey);
        } else {
            array = new JsonArray();
            root.add(arrayKey, array);
        }

        // Check if string already exists
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsString().equals(stringToAdd)) {
                LOGGER.log(Level.INFO, "String already exists in array: {0}", stringToAdd);
                return false;
            }
        }

        // Add string to array
        array.add(stringToAdd);

        // Write back to file
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
            LOGGER.log(Level.INFO, "Successfully added string to array and saved file: {0}", configFile.getPath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write to config file: {0}", e.getMessage());
            return false;
        }
    }

}