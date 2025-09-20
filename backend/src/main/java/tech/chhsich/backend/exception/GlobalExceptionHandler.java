package tech.chhsich.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns a 404 Not Found response with a structured JSON body.
     *
     * The response body (in insertion order) contains:
     * - timestamp: current LocalDateTime
     * - status: 404
     * - error: "Not Found"
     * - message: the exception's message
     * - path: request.getDescription(false)
     *
     * @return ResponseEntity containing the constructed body and HTTP 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DuplicateResourceException by returning a 409 Conflict HTTP response with a structured JSON body.
     *
     * The response body preserves field order and contains:
     * - "timestamp": the time the error was produced
     * - "status": HTTP status code (409)
     * - "error": short reason phrase ("Conflict")
     * - "message": the exception message
     * - "path": request description obtained from the WebRequest
     *
     * @param ex the DuplicateResourceException whose message is placed in the response "message" field
     * @param request the current web request; used to populate the response "path" field
     * @return a ResponseEntity whose body is a LinkedHashMap with the fields above and whose status is 409 Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles IllegalArgumentException across controllers and returns an HTTP 400 response.
     *
     * <p>Builds a JSON-compatible response body with the following fields (in insertion order):
     * <ul>
     *   <li>timestamp: current LocalDateTime</li>
     *   <li>status: 400</li>
     *   <li>error: "Bad Request"</li>
     *   <li>message: the exception's message</li>
     *   <li>path: value from {@code request.getDescription(false)}</li>
     * </ul>
     *
     * @param request used to extract the request description for the {@code path} field
     * @return a ResponseEntity whose body is the described map and whose status is 400 BAD_REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MethodArgumentNotValidException (Spring Boot validation errors) and returns a structured 400 response.
     *
     * <p>The response body includes:
     * <ul>
     *   <li>timestamp: current LocalDateTime</li>
     *   <li>status: 400</li>
     *   <li>error: "Bad Request"</li>
     *   <li>message: "Validation failed"</li>
     *   <li>errors: array of field-specific error objects</li>
     *   <li>path: request description</li>
     * </ul>
     * </p>
     *
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @param request the current web request
     * @return a ResponseEntity with structured validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Validation failed");

        // Extract field validation errors
        List<Map<String, Object>> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            Map<String, Object> errorDetail = new LinkedHashMap<>();
            errorDetail.put("field", fieldError.getField());
            errorDetail.put("defaultMessage", fieldError.getDefaultMessage());
            errorDetail.put("rejectedValue", fieldError.getRejectedValue());
            errors.add(errorDetail);
        });
        body.put("errors", errors);
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles uncaught RuntimeException instances and returns a structured 500 Internal Server Error response.
     *
     * The response body is a LinkedHashMap with insertion order:
     * timestamp (LocalDateTime.now()), status (500), error ("Internal Server Error"),
     * message ("An unexpected error occurred"), and path (derived from the provided WebRequest).
     * The original exception message is not exposed to clients.
     *
     * @param ex the RuntimeException that was thrown
     * @param request the current web request; used to populate the response "path" field
     * @return a ResponseEntity containing the response body and HttpStatus.INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred");
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}