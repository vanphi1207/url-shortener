package me.ihqqq.url_shortener.exception;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.url_shortener.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Object>> handlingException(Exception exception) {
        log.error("Exception: ", exception);
        return build(ErrorCode.UNCATEGORIZED_EXCEPTION, null);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Object>> handlingAppException(AppException exception) {
        return build(exception.getErrorCode(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse<Object>> handlingNoResource() {
        return build(ErrorCode.RESOURCE_NOT_FOUND, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Object>> handlingUnreadableBody() {
        return build(ErrorCode.INVALID_REQUEST_BODY, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Object>> handlingTypeMismatch() {
        return build(ErrorCode.INVALID_KEY, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            attributes = exception.getBindingResult().getAllErrors().getFirst()
                    .unwrap(ConstraintViolation.class)
                    .getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException e) {
            log.warn("Message validate không phải ErrorCode: {}", enumKey);
        }

        return build(errorCode, attributes);
    }

    private ResponseEntity<ApiResponse<Object>> build(ErrorCode errorCode, Map<String, Object> attributes) {
        String message = attributes == null
                ? errorCode.getMessage()
                : mapAttribute(errorCode.getMessage(), attributes);

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }

    private static String mapAttribute(String message, Map<String, Object> attributes) {
        String result = message;
        for (var entry : attributes.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}