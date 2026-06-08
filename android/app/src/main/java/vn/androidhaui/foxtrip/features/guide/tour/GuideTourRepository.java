package vn.androidhaui.foxtrip.features.guide.tour;

import android.content.Context;
import androidx.annotation.NonNull;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.response.PageData;
import vn.androidhaui.foxtrip.models.dto.response.PassengerResDTO;
import vn.androidhaui.foxtrip.models.dto.response.TourDetailResDTO;
import vn.androidhaui.foxtrip.models.dto.response.TourListResDTO;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class GuideTourRepository {
    private final ApiService api;

    public GuideTourRepository(@NonNull Context context) {
        api = ApiClient.getInstance(context).getApiService();
    }

    public interface CallbackResult<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchMyTours(int page, int size, final CallbackResult<PageData<TourListResDTO>> cb) {
        api.getGuideTours(page, size).enqueue(new Callback<ApiResponse<PageData<TourListResDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageData<TourListResDTO>>> call, Response<ApiResponse<PageData<TourListResDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body().data);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi lấy danh sách tour"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageData<TourListResDTO>>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void fetchTourDetail(String slug, final CallbackResult<TourDetailResDTO> cb) {
        api.getTourDetail(slug).enqueue(new Callback<ApiResponse<TourDetailResDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<TourDetailResDTO>> call, Response<ApiResponse<TourDetailResDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body().data);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi lấy chi tiết tour"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TourDetailResDTO>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void fetchPassengers(String tourId, final CallbackResult<List<PassengerResDTO>> cb) {
        api.getGuidePassengers(tourId).enqueue(new Callback<ApiResponse<List<PassengerResDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PassengerResDTO>>> call, Response<ApiResponse<List<PassengerResDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body().data);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi lấy danh sách hành khách"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PassengerResDTO>>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void checkIn(String tourId, String orderCode, final CallbackResult<Void> cb) {
        api.checkIn(tourId, orderCode).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(ApiResponse.getDisplayError(response, "Lỗi check-in"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
