package tn.naizo.jauml.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaTest {

    @Test
    public void testBasicTypeValidation() {
        String schemaJson = "{\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}, \"age\": {\"type\": \"number\"}}}";
        JsonSchema schema = JsonSchema.parse(schemaJson);

        JsonObject valid = new JsonObject();
        valid.addProperty("name", "Bob");
        valid.addProperty("age", 30);
        assertTrue(schema.isValid(valid));

        JsonObject invalidType = new JsonObject();
        invalidType.addProperty("name", "Bob");
        invalidType.addProperty("age", "30"); // string instead of number
        assertFalse(schema.isValid(invalidType));
    }

    @Test
    public void testRequiredFields() {
        String schemaJson = "{\"type\": \"object\", \"required\": [\"id\", \"enabled\"]}";
        JsonSchema schema = JsonSchema.parse(schemaJson);

        JsonObject valid = new JsonObject();
        valid.addProperty("id", "123");
        valid.addProperty("enabled", true);
        assertTrue(schema.isValid(valid));

        JsonObject missingKey = new JsonObject();
        missingKey.addProperty("id", "123");
        assertFalse(schema.isValid(missingKey));
    }

    @Test
    public void testNestedObjects() {
        String schemaJson = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"database\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"host\": {\"type\": \"string\"},\n" +
                "        \"port\": {\"type\": \"number\"}\n" +
                "      },\n" +
                "      \"required\": [\"host\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonSchema schema = JsonSchema.parse(schemaJson);

        JsonObject valid = new JsonObject();
        JsonObject db = new JsonObject();
        db.addProperty("host", "localhost");
        db.addProperty("port", 3306);
        rootAdd(valid, "database", db);
        assertTrue(schema.isValid(valid));

        JsonObject invalidNested = new JsonObject();
        JsonObject invalidDb = new JsonObject();
        invalidDb.addProperty("port", 3306); // missing host
        rootAdd(invalidNested, "database", invalidDb);
        assertFalse(schema.isValid(invalidNested));
    }

    @Test
    public void testArrayItems() {
        String schemaJson = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"tags\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\"type\": \"string\"}\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonSchema schema = JsonSchema.parse(schemaJson);

        JsonObject valid = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("admin");
        tags.add("user");
        valid.add("tags", tags);
        assertTrue(schema.isValid(valid));

        JsonObject invalidTags = new JsonObject();
        JsonArray badTags = new JsonArray();
        badTags.add("admin");
        badTags.add(123); // number instead of string
        invalidTags.add("tags", badTags);
        assertFalse(schema.isValid(invalidTags));
    }

    private void rootAdd(JsonObject root, String key, JsonObject child) {
        root.add(key, child);
    }
}
