package tn.naizo.jauml.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A lightweight JSON Schema validator supporting type checks, required keys,
 * nested objects, and array item validation.
 */
public final class JsonSchema {

    private final JsonObject schemaObject;

    public JsonSchema(JsonObject schemaObject) {
        if (schemaObject == null) {
            throw new IllegalArgumentException("Schema object cannot be null");
        }
        this.schemaObject = schemaObject;
    }

    /**
     * Parses a JSON Schema from a string representation.
     */
    public static JsonSchema parse(String jsonSchema) throws JsonException {
        if (jsonSchema == null || jsonSchema.trim().isEmpty()) {
            throw new JsonException("Schema string cannot be null or empty");
        }
        try {
            JsonElement el = JsonParser.parseString(jsonSchema);
            if (!el.isJsonObject()) {
                throw new JsonException("Schema must be a valid JSON Object");
            }
            return new JsonSchema(el.getAsJsonObject());
        } catch (Exception e) {
            throw new JsonException("Failed to parse JSON schema: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the provided JsonElement against this schema.
     * Throws a JsonException if validation fails.
     */
    public void validate(JsonElement data) throws JsonException {
        validate(data, schemaObject, "#");
    }

    /**
     * Returns true if the provided JsonElement is valid according to this schema, false otherwise.
     */
    public boolean isValid(JsonElement data) {
        try {
            validate(data);
            return true;
        } catch (JsonException e) {
            return false;
        }
    }

    private void validate(JsonElement data, JsonObject schema, String path) throws JsonException {
        if (schema == null) {
            return;
        }

        // 1. Check type
        if (schema.has("type")) {
            String expectedType = schema.get("type").getAsString();
            if (!checkType(data, expectedType)) {
                throw new JsonException("Validation failed at " + path + ": expected type '" + expectedType + "', but found '" + getActualType(data) + "'");
            }
        }

        // 2. Check object properties
        if (data != null && data.isJsonObject()) {
            JsonObject obj = data.getAsJsonObject();

            // Validate required properties
            if (schema.has("required")) {
                JsonArray required = schema.getAsJsonArray("required");
                for (JsonElement req : required) {
                    String reqKey = req.getAsString();
                    if (!obj.has(reqKey) || obj.get(reqKey).isJsonNull()) {
                        throw new JsonException("Validation failed at " + path + ": missing required property '" + reqKey + "'");
                    }
                }
            }

            // Validate nested properties
            if (schema.has("properties")) {
                JsonObject properties = schema.getAsJsonObject("properties");
                for (String key : properties.keySet()) {
                    if (obj.has(key)) {
                        validate(obj.get(key), properties.getAsJsonObject(key), path + "." + key);
                    }
                }
            }
        }

        // 3. Validate array items
        if (data != null && data.isJsonArray() && schema.has("items")) {
            JsonArray arr = data.getAsJsonArray();
            JsonObject itemsSchema = schema.getAsJsonObject("items");
            for (int i = 0; i < arr.size(); i++) {
                validate(arr.get(i), itemsSchema, path + "[" + i + "]");
            }
        }
    }

    private boolean checkType(JsonElement data, String type) {
        if (data == null || data.isJsonNull()) {
            return "null".equals(type);
        }
        switch (type) {
            case "string":
                return data.isJsonPrimitive() && data.getAsJsonPrimitive().isString();
            case "number":
                return data.isJsonPrimitive() && data.getAsJsonPrimitive().isNumber();
            case "boolean":
                return data.isJsonPrimitive() && data.getAsJsonPrimitive().isBoolean();
            case "array":
                return data.isJsonArray();
            case "object":
                return data.isJsonObject();
            case "null":
                return data.isJsonNull();
            default:
                return true;
        }
    }

    private String getActualType(JsonElement data) {
        if (data == null || data.isJsonNull()) {
            return "null";
        }
        if (data.isJsonObject()) {
            return "object";
        }
        if (data.isJsonArray()) {
            return "array";
        }
        if (data.isJsonPrimitive()) {
            var prim = data.getAsJsonPrimitive();
            if (prim.isString()) return "string";
            if (prim.isNumber()) return "number";
            if (prim.isBoolean()) return "boolean";
        }
        return "unknown";
    }
}
