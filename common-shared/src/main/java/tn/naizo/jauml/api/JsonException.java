package tn.naizo.jauml.api;

/**
 * Exception thrown when a JSON operations fails, such as parsing errors,
 * schema validation errors, or migration failures.
 */
public class JsonException extends RuntimeException {

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
