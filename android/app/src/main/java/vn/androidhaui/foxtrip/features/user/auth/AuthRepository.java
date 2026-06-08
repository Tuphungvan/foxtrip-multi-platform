package vn.androidhaui.foxtrip.features.user.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.domain.User;
import vn.androidhaui.foxtrip.models.dto.request.ForgotPasswordRequest;
import vn.androidhaui.foxtrip.models.dto.request.RegisterRequest;
import vn.androidhaui.foxtrip.models.dto.request.ResendOtpRequest;
import vn.androidhaui.foxtrip.models.dto.request.VerifyEmailRequest;
import vn.androidhaui.foxtrip.models.dto.request.LoginRequest;
import vn.androidhaui.foxtrip.models.dto.response.AuthResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AuthRepository {
    private final ApiService api;
    private final SharedPreferences prefs;

    public AuthRepository(@NonNull Context context) {
        api = ApiClient.getInstance(context).getApiService();
        prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);

        void onError(String error);
    }

    // ── Authentication ───────────────────────────────────────────────────────

    public void register(String username, String email, String password,
            String phone, boolean sendOtp, CallbackResult<String> cb) {
        RegisterRequest body = new RegisterRequest(email, password, username, phone, sendOtp);
        api.register(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body().message != null ? response.body().message : "Đăng ký thành công");
                } else {
                    cb.onError(extractError(response, "Đăng ký thất bại"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void login(String email, String password, CallbackResult<User> cb) {
        LoginRequest body = new LoginRequest(email, password);
        api.login(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call,
                    @NonNull Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body().data;
                    String role = authData != null ? authData.role : null;
                    if ("ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role)) {
                        cb.onError("Tác vụ không khả dụng trên thiết bị mobile");
                        return;
                    }
                    User user = parseUserFromData(authData);
                    if (user != null) {
                        cb.onSuccess(user);
                        return;
                    }
                }
                cb.onError(extractError(response, "Đăng nhập thất bại"));
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void loginWithGoogle(String idToken, CallbackResult<User> cb) {
        Map<String, String> body = new HashMap<>();
        body.put("idToken", idToken);

        api.loginWithGoogle(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call,
                    @NonNull Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body().data;
                    String role = authData != null ? authData.role : null;
                    if ("ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role)) {
                        cb.onError("Tác vụ không khả dụng trên thiết bị mobile");
                        return;
                    }
                    User user = parseUserFromData(authData);
                    if (user != null) {
                        cb.onSuccess(user);
                        return;
                    }
                }
                cb.onError(extractError(response, "Đăng nhập Google thất bại"));
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void logout(CallbackResult<String> cb) {
        String refreshToken = prefs.getString("refreshToken", null);
        if (refreshToken == null) {
            clearTokens();
            cb.onSuccess("Đã đăng xuất");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("refreshToken", refreshToken);

        api.logout(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                clearTokens();
                cb.onSuccess("Đã đăng xuất");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                clearTokens();
                cb.onSuccess("Đã đăng xuất (ngoại tuyến)");
            }
        });
    }

    public User loadUserFromPrefs() {
        String token = getAccessToken();
        if (token == null)
            return null;

        String role = prefs.getString("userRole", null);
        if ("ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role)) {
            clearTokens(); // Xoá session không hợp lệ
            return null;
        }

        User user = new User();
        user.role = role;
        return user;
    }

    // ── OTP & Email Verification ──────────────────────────────────────────────

    public void sendOTP(String email, CallbackResult<String> cb) {
        ResendOtpRequest request = new ResendOtpRequest(email);
        api.resendOTP(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body().message != null ? response.body().message : "Đã gửi mã OTP");
                } else {
                    cb.onError(extractError(response, "Không thể gửi OTP"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void verifyEmail(String email, String otp, CallbackResult<String> cb) {
        VerifyEmailRequest request = new VerifyEmailRequest(email, otp);
        api.verifyEmail(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(
                            response.body().message != null ? response.body().message : "Xác thực email thành công.");
                } else {
                    cb.onError(extractError(response, "Mã OTP không hợp lệ"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void forgotPassword(String email, CallbackResult<String> cb) {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        api.forgotPassword(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                    @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body().message != null ? response.body().message
                            : "Đã gửi yêu cầu đặt lại mật khẩu.");
                } else {
                    cb.onError(extractError(response, "Không thể gửi yêu cầu quên mật khẩu"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private void saveTokens(String accessToken, String refreshToken, Long expiresIn, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        if (accessToken != null)
            editor.putString("accessToken", accessToken);
        if (refreshToken != null)
            editor.putString("refreshToken", refreshToken);
        if (expiresIn != null)
            editor.putLong("expiresIn", expiresIn);
        if (role != null)
            editor.putString("userRole", role);
        editor.apply();
    }

    private String getAccessToken() {
        return prefs.getString("accessToken", null);
    }

    public void clearTokens() {
        prefs.edit().remove("accessToken").remove("refreshToken").remove("expiresIn").apply();
    }

    private String extractError(Response<?> response, String fallback) {
        if (response.body() != null) {
            ApiResponse<?> body = (ApiResponse<?>) response.body();
            return body.getDisplayError(fallback);
        }
        ApiResponse<?> err = ApiResponse.parseError(response.errorBody());
        return err != null ? err.getDisplayError(fallback) : fallback;
    }

    private User parseUserFromData(AuthResponse data) {
        if (data == null)
            return null;
            
        if ("ADMIN".equalsIgnoreCase(data.role) || "SUPER_ADMIN".equalsIgnoreCase(data.role)) {
            clearTokens();
            return null;
        }

        saveTokens(data.accessToken, data.refreshToken, data.expiresIn, data.role);

        User user = new User();
        user.role = data.role;
        // id và các thông tin khác có thể chưa có trong AuthResponse mới,
        // nhưng role là quan trọng nhất lúc này cho flow.
        return user;
    }
}