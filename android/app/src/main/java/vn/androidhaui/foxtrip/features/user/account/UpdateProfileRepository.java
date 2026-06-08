package vn.androidhaui.foxtrip.features.user.account;

import android.content.Context;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.request.UpdateUserProfileRequest;
import vn.androidhaui.foxtrip.models.dto.request.VerifyOtpRequest;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class UpdateProfileRepository {
    private final ApiService api;

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public UpdateProfileRepository(Context ctx) {
        api = ApiClient.getInstance(ctx).getApiService();
    }

    public void getUpdateProfile(CallbackResult<UserDetailResponse> cb) {
        api.getUserProfile().enqueue(new Callback<ApiResponse<UserDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDetailResponse>> call,
                                   @NonNull Response<ApiResponse<UserDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    cb.onSuccess(response.body().data);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi tải thông tin"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDetailResponse>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void postUpdateProfile(UpdateUserProfileRequest request, CallbackResult<Void> cb) {
        api.updateUserProfile(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Cập nhật thất bại"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void verifyEmailLoggedIn(String otp, CallbackResult<Void> cb) {
        VerifyOtpRequest request = new VerifyOtpRequest(otp);
        api.verifyEmailLoggedIn(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Xác thực thất bại"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void resendOtpLoggedIn(CallbackResult<Void> cb) {
        api.resendOtpLoggedIn().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Gửi OTP thất bại"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}

