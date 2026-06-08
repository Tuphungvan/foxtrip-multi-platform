package vn.androidhaui.foxtrip.features.user.tour;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.response.PageData;
import vn.androidhaui.foxtrip.models.dto.response.TourDetailResDTO;
import vn.androidhaui.foxtrip.models.dto.response.TourReviewDTO;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class TourRepository {
    private final ApiService apiService;

    public TourRepository(Context context) {
        apiService = ApiClient.getInstance(context).getApiService();
    }

    public void getTourDetail(String slug, MutableLiveData<TourDetailResDTO> data) {
        apiService.getTourDetail(slug).enqueue(new Callback<ApiResponse<TourDetailResDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<TourDetailResDTO>> call, @NonNull Response<ApiResponse<TourDetailResDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.postValue(response.body().data);
                } else {
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<TourDetailResDTO>> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
    }

    public void getTourReviews(String tourId, MutableLiveData<PageData<TourReviewDTO>> data) {
        apiService.getTourReviews(tourId, 0, 5).enqueue(new Callback<ApiResponse<PageData<TourReviewDTO>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageData<TourReviewDTO>>> call, @NonNull Response<ApiResponse<PageData<TourReviewDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.postValue(response.body().data);
                } else {
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PageData<TourReviewDTO>>> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
    }
}
