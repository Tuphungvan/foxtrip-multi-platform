package haui.foxtrip.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String message;
    private T data;
    private List<ApiError> errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.message = message;
        res.data = data;
        return res;
    }

    public static ApiResponse<Void> success(String message) {
        ApiResponse<Void> res = new ApiResponse<>();
        res.message = message;
        return res;
    }

    public static ApiResponse<Void> failure(String message, List<ApiError> errors) {
        ApiResponse<Void> res = new ApiResponse<>();
        res.message = message;
        res.errors = errors;
        return res;
    }

    public static ApiResponse<Void> failure(String message) {
        return failure(message, null);
    }
}
