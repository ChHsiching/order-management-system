package tech.chhsich.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a ResourceNotFoundException with the specified detail message.
     *
     * @param message descriptive message explaining which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a ResourceNotFoundException for a missing resource identified by a specific field and value.
     *
     * <p>The exception message is formatted as: "<code>&lt;resource&gt; not found with &lt;field&gt;: '&lt;value&gt;'</code>".</p>
     *
     * @param resource the type or name of the resource that was not found
     * @param field the field used to look up the resource (e.g., "id", "email")
     * @param value the value of the field that failed to match any existing resource
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}