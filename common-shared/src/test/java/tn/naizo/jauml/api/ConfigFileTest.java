package tn.naizo.jauml.api;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigFileTest {

    @TempDir
    public Path tempDir;

    @BeforeEach
    public void setUp() {
        TestPlatformProvider.setTempDir(tempDir);
    }

    @Test
    public void testBasicSaveAndLoad() {
        ConfigFile config = JaumlConfig.open("sub", "test_config");
        config.set("key1", "value1");
        config.set("key2", 123);
        config.set("key3", true);
        config.save();

        assertTrue(config.exists());

        config.reload();
        assertEquals("value1", config.getString("key1"));
        assertEquals(123, config.getInt("key2", 0));
        assertTrue(config.getBoolean("key3", false));
    }

    @Test
    public void testCoercionAndNormalization() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("version", "1.0");
        defaults.addProperty("enabled", false);
        defaults.addProperty("count", 10);

        ConfigFile config = JaumlConfig.open("sub", "coercion_config");
        config.setDefaultData(defaults);
        
        config.set("enabled", "true");
        config.set("count", "50");
        config.save();

        config.reload();
        assertTrue(config.getBoolean("enabled"));
        assertEquals(50, config.getInt("count", 0));
    }

    @Test
    public void testConfigMigration() {
        JsonMigrator migrator = new JsonMigrator();
        migrator.register("1.0", "2.0", old -> {
            JsonObject upgraded = JsonLib.deepClone(old).getAsJsonObject();
            upgraded.addProperty("newKey", "migratedValue");
            return upgraded;
        });

        JsonObject defaults = new JsonObject();
        defaults.addProperty("version", "2.0");
        defaults.addProperty("newKey", "defaultValue");

        ConfigFile preConfig = JaumlConfig.open("sub", "migrate_config");
        preConfig.set("version", "1.0");
        preConfig.set("oldKey", "oldValue");
        preConfig.save();

        ConfigFile config = JaumlConfig.open("sub", "migrate_config", null, migrator, "2.0", defaults);

        assertEquals("2.0", config.getString("version"));
        assertEquals("migratedValue", config.getString("newKey"));
        assertEquals("oldValue", config.getString("oldKey"));
    }

    @Test
    public void testCorruptedConfigRecovery() throws IOException {
        Path configPath = tempDir.resolve("sub").resolve("corrupt_config.json");
        Files.createDirectories(configPath.getParent());
        Files.write(configPath, "invalid json {[[}".getBytes());

        JsonObject defaults = new JsonObject();
        defaults.addProperty("recovered", true);

        ConfigFile config = JaumlConfig.open("sub", "corrupt_config", null, null, null, defaults);

        assertTrue(config.getBoolean("recovered"));

        Path backupPath = tempDir.resolve("sub").resolve("corrupt_config.json.bak");
        assertTrue(Files.exists(backupPath));
        assertEquals("invalid json {[[}", new String(Files.readAllBytes(backupPath)));
    }
}
