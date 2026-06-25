package tn.naizo.jauml.api;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup initialization helper that runs compatibility checks, registers migrations,
 * loads the application configuration, validates it, and ensures startup resilience.
 */
public final class JaumlInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("JaumlInit");

    private JaumlInitializer() {}

    /**
     * Runs JAUML configuration startup initialization.
     * Performs compatibility checks, registers migrations, loads the config,
     * normalizes and validates it, and saves a verified copy to disk.
     */
    public static void initialize() {
        LOGGER.info("Initializing JAUML JSON library and verifying configuration...");

        // 1. Runtime compatibility check
        String requiredVersion = "2.1.0";
        if (!JaumlConfig.isCompatible(requiredVersion)) {
            LOGGER.warn("COMPATIBILITY WARNING: App requires JSON library version {}, but current library version is {}.", 
                    requiredVersion, JaumlConfig.LIBRARY_VERSION);
        } else {
            LOGGER.info("Compatibility check passed. Library version: {}, Required: {}", JaumlConfig.LIBRARY_VERSION, requiredVersion);
        }

        // 2. Setup schema, default values, and migrators
        JsonObject defaultData = new JsonObject();
        defaultData.addProperty("version", "2.0");
        defaultData.addProperty("enabled", true);
        defaultData.addProperty("maxPlayers", 20);

        JsonSchema schema = null;
        try {
            String schemaJson = "{" +
                    "\"type\":\"object\"," +
                    "\"properties\":{" +
                        "\"version\":{\"type\":\"string\"}," +
                        "\"enabled\":{\"type\":\"boolean\"}," +
                        "\"maxPlayers\":{\"type\":\"number\"}" +
                    "}," +
                    "\"required\":[\"version\",\"enabled\"]" +
                    "}";
            schema = JsonSchema.parse(schemaJson);
        } catch (Exception e) {
            LOGGER.error("Failed to parse configuration schema", e);
        }

        JsonMigrator migrator = new JsonMigrator();
        // Register sequential migration path from 1.0 to 2.0
        migrator.register("1.0", "2.0", oldJson -> {
            LOGGER.info("Migrating configuration shape from 1.0 to 2.0...");
            JsonObject migrated = JsonLib.deepClone(oldJson).getAsJsonObject();
            if (!migrated.has("maxPlayers")) {
                migrated.addProperty("maxPlayers", 20);
            }
            return migrated;
        });

        // 3. Open, migrate, validate, and verify the config file
        try {
            ConfigFile configFile = JaumlConfig.open("jauml", "config", schema, migrator, "2.0", defaultData);
            LOGGER.info("JAUML config loaded successfully. Version: {}", configFile.getString("version"));
            
            // Perform sample reads
            boolean enabled = configFile.getBoolean("enabled", true);
            int maxPlayers = configFile.getInt("maxPlayers", 20);
            LOGGER.info("Active configuration state - enabled: {}, maxPlayers: {}", enabled, maxPlayers);

            // Write back to disk to verify saving is fully operational
            configFile.save();
            LOGGER.info("JAUML configuration startup initialization completed successfully.");
        } catch (Exception e) {
            LOGGER.error("Non-fatal startup error: recoverable config check encountered an issue", e);
        }
    }
}
