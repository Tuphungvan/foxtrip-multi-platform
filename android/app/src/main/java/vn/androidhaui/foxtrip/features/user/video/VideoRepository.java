package vn.androidhaui.foxtrip.features.user.video;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.models.dto.response.TourVideoCardResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiService;

public class VideoRepository {
    private final ApiService apiService;

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public VideoRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    public void getShortVideos(CallbackResult<List<TourVideoCardResponse>> callback) {
        apiService.getShortVideos().enqueue(new Callback<ApiResponse<List<TourVideoCardResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TourVideoCardResponse>>> call, 
                                 @NonNull Response<ApiResponse<List<TourVideoCardResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("Không thể tải danh sách video");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TourVideoCardResponse>>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
