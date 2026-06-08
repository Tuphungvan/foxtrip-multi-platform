package vn.androidhaui.foxtrip.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import okhttp3.ResponseBody;

public class ApiResponse<T> {
    public String message;
    public T data;
    public List<ApiError> errors;

    // Ghép tất cả thông báo lỗi lại thành một chuỗi
    public String getDisplayError(String fallback) {
        StringBuilder sb = new StringBuilder();

        // 1. Ưu tiên message tổng quát từ Backend
        if (message != null && !message.isEmpty()) {
            sb.append(message);
        }

        // 2. Nếu có lỗi chi tiết từng field, ghép thêm vào
        if (errors != null && !errors.isEmpty()) {
            if (sb.length() > 0)
                sb.append("\n"); // Xuống dòng nếu đã có message tổng
            for (int i = 0; i < errors.size(); i++) {
                sb.append(errors.get(i).message);
                if (i < errors.size() - 1)
                    sb.append("\n"); // Ngăn cách các lỗi bằng dấu xuống dòng
            }
        }

        return sb.length() > 0 ? sb.toString() : fallback;
    }

    // Static helper for backward compatibility in repositories
    public static <T> String getDisplayError(retrofit2.Response<ApiResponse<T>> response, String fallback) {
        if (response != null && response.body() != null) {
            return response.body().getDisplayError(fallback);
        }
        ApiResponse<?> err = parseError(response != null ? response.errorBody() : null);
        return err != null ? err.getDisplayError(fallback) : fallback;
    }

    // Parse nội dung JSON từ errorBody của Retrofit khi gặp lỗi HTTP 4xx/5xx
    public static ApiResponse<?> parseError(ResponseBody errorBody) {
        if (errorBody == null)
            return null;
        try {
            return new Gson().fromJson(errorBody.charStream(),
                    new TypeToken<ApiResponse<Void>>() {
                    }.getType());
        } catch (Exception e) {
            return null;
        }
    }

    public static class ApiError {
        public String field;
        public String message;
    }
}
