package vn.androidhaui.foxtrip.features.user.account;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.request.UpdateStaffProfileRequest;
import vn.androidhaui.foxtrip.models.dto.request.UpdateUserProfileRequest;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class AccountRepository {
    private final ApiService api;

    public AccountRepository(@NonNull Context context) {
        api = ApiClient.getInstance(context).getApiService();
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getProfile(final CallbackResult<UserDetailResponse> cb) {
        api.getUserProfile().enqueue(new Callback<ApiResponse<UserDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDetailResponse>> call,
                                   @NonNull Response<ApiResponse<UserDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    cb.onSuccess(response.body().data);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi tải profile"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDetailResponse>> call, @NonNull Throwable t) {
                Log.e("AccountRepo", "getProfile onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void getGuideProfile(final CallbackResult<UserDetailResponse> cb) {
        api.getGuideProfile().enqueue(new Callback<ApiResponse<UserDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserDetailResponse>> call,
                                   @NonNull Response<ApiResponse<UserDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    cb.onSuccess(response.body().data);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi tải profile hướng dẫn viên"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserDetailResponse>> call, @NonNull Throwable t) {
                Log.e("AccountRepo", "getGuideProfile onFailure", t);
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void updateAvatarUrl(String avatarUrl, CallbackResult<Void> cb) {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setAvatarUrl(avatarUrl);
        
        api.updateUserProfile(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi cập nhật avatar"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void updateGuideProfile(UpdateStaffProfileRequest request, CallbackResult<Void> cb) {
        api.updateGuideProfile(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi cập nhật hồ sơ"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }
}

