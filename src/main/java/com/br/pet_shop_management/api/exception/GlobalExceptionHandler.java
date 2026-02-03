package com.br.pet_shop_management.api.exception;

import com.br.pet_shop_management.application.exception.DomainRuleException;
import com.br.pet_shop_management.application.exception.InvalidInputException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ApiError(status.value(), status.name(), message, request.getRequestURI(), OffsetDateTime.now())
        );
    }

    // 400 - Entrada inválida / pré-condições de request
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiError> handleInvalidInput(InvalidInputException ex, HttpServletRequest request) {
        log.warn("Invalid input: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 409 - Regra de negócio / conflito de estado
    @ExceptionHandler(DomainRuleException.class)
    public ResponseEntity<ApiError> handleDomainRule(DomainRuleException ex, HttpServletRequest request) {
        log.warn("Domain rule violation: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 404 - Não encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entity not found: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 400 - Validação Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation error: path={}, msg={}", request.getRequestURI(), message);
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 - JSON malformado / enum inválido / tipo errado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed JSON request.";

        Throwable root = getRootCause(ex);
        if (root != null) {
            String rootMsg = root.getMessage();
            if (rootMsg != null && rootMsg.toLowerCase().contains("cannot deserialize value of type")) {
                message = "Invalid value type in request body (check enums/dates/numbers).";
            }
        }

        log.warn("Unreadable JSON: path={}, msg={}", request.getRequestURI(), message);
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    // 409 - Integridade dos dados (unique, FK, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Data integrity violation.";

        log.warn("Data integrity violation: path={}, msg={}, root={}",
                request.getRequestURI(),
                message,
                safeRootMessage(ex));

        return buildError(HttpStatus.CONFLICT, message, request);
    }

    // 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error: path={}", request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error.", request);
    }

    private Throwable getRootCause(Throwable t) {
        Throwable result = t;
        while (result != null && result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }

    private String safeRootMessage(Throwable t) {
        Throwable root = getRootCause(t);
        if (root == null) return "";
        String msg = root.getMessage();
        return msg == null ? "" : msg;
    }
}
