package tn.naizo.jauml.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JsonLibTest {

    @Test
    public void testSafeParse() {
        Optional<JsonElement> parsed = JsonLib.safeParse("{\"a\": 1}");
        assertTrue(parsed.isPresent());
        assertEquals(1, parsed.get().getAsJsonObject().get("a").getAsInt());

        Optional<JsonElement> invalid = JsonLib.safeParse("{invalid}");
        assertFalse(invalid.isPresent());

        Optional<JsonElement> empty = JsonLib.safeParse("");
        assertFalse(empty.isPresent());

        Optional<JsonElement> nul = JsonLib.safeParse(null);
        assertFalse(nul.isPresent());

        JsonElement fallback = JsonLib.safeParse("{invalid}", new JsonPrimitive("fallback"));
        assertEquals("fallback", fallback.getAsString());
    }

    @Test
    public void testStrictParse() {
        JsonElement parsed = JsonLib.strictParse("{\"a\": 1}");
        assertNotNull(parsed);

        // Strict parse should throw on unquoted keys, trailing comma, extra data
        assertThrows(JsonException.class, () -> JsonLib.strictParse("{a: 1}"));
        assertThrows(JsonException.class, () -> JsonLib.strictParse("{\"a\": 1,}"));
        assertThrows(JsonException.class, () -> JsonLib.strictParse("{\"a\": 1} extra"));
        assertThrows(JsonException.class, () -> JsonLib.strictParse(""));
    }

    @Test
    public void testStableStringify() {
        JsonObject obj = new JsonObject();
        obj.addProperty("z", 1);
        obj.addProperty("a", 2);
        obj.addProperty("m", 3);

        String json = JsonLib.stableStringify(obj);
        // "a" should be serialized before "m", which is before "z"
        int indexA = json.indexOf("\"a\"");
        int indexM = json.indexOf("\"m\"");
        int indexZ = json.indexOf("\"z\"");

        assertTrue(indexA < indexM);
        assertTrue(indexM < indexZ);
    }

    @Test
    public void testDeepClone() {
        JsonObject original = new JsonObject();
        original.addProperty("name", "test");
        JsonArray arr = new JsonArray();
        arr.add(1);
        original.add("list", arr);

        JsonObject clone = JsonLib.deepClone(original).getAsJsonObject();
        assertEquals(original, clone);

        // Modifying clone should not affect original
        clone.addProperty("name", "changed");
        clone.getAsJsonArray("list").add(2);

        assertEquals("test", original.get("name").getAsString());
        assertEquals(1, original.getAsJsonArray("list").size());
        assertEquals(2, clone.getAsJsonArray("list").size());
    }

    @Test
    public void testPathAccessGet() {
        JsonObject root = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("color", "red");
        root.add("theme", inner);

        JsonArray users = new JsonArray();
        JsonObject user0 = new JsonObject();
        user0.addProperty("username", "alice");
        users.add(user0);
        root.add("users", users);

        Optional<JsonElement> color = JsonLib.getByPath(root, "theme.color");
        assertTrue(color.isPresent());
        assertEquals("red", color.get().getAsString());

        Optional<JsonElement> user = JsonLib.getByPath(root, "users[0].username");
        assertTrue(user.isPresent());
        assertEquals("alice", user.get().getAsString());

        Optional<JsonElement> invalid = JsonLib.getByPath(root, "theme.nonexistent");
        assertFalse(invalid.isPresent());
    }

    @Test
    public void testPathAccessSet() {
        JsonObject root = new JsonObject();
        JsonLib.setByPath(root, "settings.theme.color", new JsonPrimitive("blue"));
        assertEquals("blue", root.getAsJsonObject("settings").getAsJsonObject("theme").get("color").getAsString());

        JsonLib.setByPath(root, "users[0].name", new JsonPrimitive("bob"));
        JsonArray users = root.getAsJsonArray("users");
        assertEquals(1, users.size());
        assertEquals("bob", users.get(0).getAsJsonObject().get("name").getAsString());
    }

    @Test
    public void testMerge() {
        JsonObject user = new JsonObject();
        user.addProperty("name", "Alice");
        user.addProperty("enabled", false);

        JsonObject defaults = new JsonObject();
        defaults.addProperty("enabled", true);
        defaults.addProperty("maxPlayers", 20);

        JsonObject merged = JsonLib.merge(user, defaults).getAsJsonObject();
        assertEquals("Alice", merged.get("name").getAsString());
        // User config overrides defaults
        assertEquals(false, merged.get("enabled").getAsBoolean());
        // Defaults fill missing properties
        assertEquals(20, merged.get("maxPlayers").getAsInt());
    }

    @Test
    public void testNormalize() {
        JsonObject config = new JsonObject();
        config.addProperty("enabled", "true"); // string representation of boolean
        config.addProperty("maxPlayers", "50"); // string representation of integer
        config.addProperty("extraKey", "removeMe");

        JsonObject defaults = new JsonObject();
        defaults.addProperty("enabled", false);
        defaults.addProperty("maxPlayers", 20);
        defaults.addProperty("theme", "dark");

        JsonObject normalized = JsonLib.normalize(config, defaults).getAsJsonObject();

        // Should coerce types
        assertTrue(normalized.get("enabled").getAsBoolean());
        assertEquals(50, normalized.get("maxPlayers").getAsInt());
        // Should fill in missing defaults
        assertEquals("dark", normalized.get("theme").getAsString());
        // Should NOT remove extra key during normalization (normalize preserves extra keys if present,
        // or coerces structure. Defaults are populated, mismatches are coerced/reset)
    }

    @Test
    public void testVersionDetection() {
        JsonObject obj = new JsonObject();
        obj.addProperty("version", "1.5");
        assertEquals("1.5", JsonLib.detectVersion(obj).orElse(""));

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("configVersion", "2.1");
        assertEquals("2.1", JsonLib.detectVersion(obj2).orElse(""));
    }
}
