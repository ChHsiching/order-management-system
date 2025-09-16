package tech.chhsich.backend.exception;

public class DuplicateResourceException extends RuntimeException {
    /**
     * Constructs a DuplicateResourceException with the specified detail message.
     *
     * @param message the detail message describing the duplicate resource condition
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Creates a DuplicateResourceException with a standardized message indicating a duplicate field value for a resource.
     *
     * The resulting message has the form: "<resource> already exists with <field>: '<value>'".
     *
     * @param resource name of the resource type (e.g., "User")
     * @param field    name of the field that is duplicated (e.g., "email")
     * @param value    the duplicate value found for the field
     */
    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resource, field, value));
    }
}