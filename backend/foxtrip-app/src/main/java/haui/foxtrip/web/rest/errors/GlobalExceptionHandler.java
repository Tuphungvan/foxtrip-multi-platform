package haui.foxtrip.web.rest.errors;

import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.common.response.ApiError;
import haui.foxtrip.common.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        log.warn("Business error message={} status={}", ex.getMessage(), status.value());
        return buildError(status, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ.", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ.", errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu.", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ.", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadJson(HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ.", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Chưa xác thực.", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Không có quyền truy cập.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("Unhandled error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống.", null);
    }

    private ResponseEntity<ApiResponse<Void>> buildError(HttpStatus status, String message, List<ApiError> errors) {
        ApiResponse<Void> body = ApiResponse.failure(message, errors);
        return ResponseEntity.status(status).body(body);
    }
}
