package vn.androidhaui.foxtrip.features.user.home;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.response.TourCardResponse;
import vn.androidhaui.foxtrip.models.dto.response.MarkerResponse;
import vn.androidhaui.foxtrip.models.dto.response.PageData;
import vn.androidhaui.foxtrip.models.dto.response.LocationResponse;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class HomeRepository {
    private final ApiService apiService;

    public HomeRepository(Context context) {
        apiService = ApiClient.getInstance(context).getApiService();
    }

    private <T> void handleResponse(MutableLiveData<T> out, Response<ApiResponse<T>> response) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> body = response.body();
            if (body.data != null) {
                out.postValue(body.data);
            } else {
                out.postValue(null);
            }
        } else {
            android.util.Log.w("API_RESPONSE", "failed code=" + response.code());
            out.postValue(null);
        }
    }

    private <T> void handleFailure(MutableLiveData<T> out, Throwable t) {
        android.util.Log.e("API_RESPONSE", "error=" + t.getMessage(), t);
        out.postValue(null);
    }

    public void getUpcomingTours(MutableLiveData<List<TourCardResponse>> out) {
        apiService.getUpcomingTours(20).enqueue(new Callback<ApiResponse<List<TourCardResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TourCardResponse>>> call,
                    @NonNull Response<ApiResponse<List<TourCardResponse>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TourCardResponse>>> call, @NonNull Throwable t) {
                handleFailure(out, t);
            }
        });
    }

    public void getDiscountedTours(MutableLiveData<List<TourCardResponse>> out) {
        apiService.getDiscountedTours(20).enqueue(new Callback<ApiResponse<List<TourCardResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TourCardResponse>>> call,
                    @NonNull Response<ApiResponse<List<TourCardResponse>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TourCardResponse>>> call, @NonNull Throwable t) {
                handleFailure(out, t);
            }
        });
    }

    public void searchTours(String query, String province, String category, Double priceFrom, Double priceTo, String startDate, String endDate, int page, MutableLiveData<List<TourCardResponse>> out) {
        Log.d("HOME_REPO", "searchTours query: q=" + query + ", province=" + province + ", page=" + page);
        // Đảm bảo size cố định (ví dụ 20) hoặc truyền từ bên ngoài
        apiService.searchTours(query, province, category, priceFrom, priceTo, startDate, endDate, page, 20).enqueue(new Callback<ApiResponse<PageData<TourCardResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageData<TourCardResponse>>> call, @NonNull Response<ApiResponse<PageData<TourCardResponse>>> response) {
                // In ra JSON thô để debug
                String rawJson = new com.google.gson.Gson().toJson(response.body());
                Log.d("HOME_REPO", "Raw JSON Response: " + rawJson);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageData<TourCardResponse>> body = response.body();
                    if (body.data != null && body.data.items != null) {
                        List<TourCardResponse> content = body.data.items;
                        Log.d("HOME_REPO", "Search Success. Items found: " + content.size());
                        out.postValue(content);
                    } else {
                        Log.w("HOME_REPO", "Data or Content is NULL. JSON mismatch?");
                        out.postValue(new ArrayList<>()); // Trả về list rỗng thay vì null để Fragment hiện Toast
                    }
                } else {
                    Log.w("HOME_REPO", "Search Failed. Code: " + response.code());
                    out.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PageData<TourCardResponse>>> call, @NonNull Throwable t) {
                Log.e("HOME_REPO", "Network Error: " + t.getMessage());
                handleFailure((MutableLiveData) out, t);
            }
        });
    }

    public void getDiscoveryMap(MutableLiveData<List<MarkerResponse>> out) {
        apiService.getDiscoveryMap().enqueue(new Callback<ApiResponse<List<MarkerResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<MarkerResponse>>> call,
                    @NonNull Response<ApiResponse<List<MarkerResponse>>> response) {
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<MarkerResponse>>> call, @NonNull Throwable t) {
                handleFailure(out, t);
            }
        });
    }

    public void getLocationDetail(String id, MutableLiveData<LocationResponse> out, MutableLiveData<List<MarkerResponse>> mapOut) {
        apiService.getLocationDetail(id).enqueue(new Callback<ApiResponse<LocationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LocationResponse>> call,
                    @NonNull Response<ApiResponse<LocationResponse>> response) {
                if (response.code() == 404) {
                    // Tự động làm mới bản đồ nếu địa điểm không còn tồn tại
                    getDiscoveryMap(mapOut);
                }
                handleResponse(out, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LocationResponse>> call, @NonNull Throwable t) {
                handleFailure(out, t);
            }
        });
    }
}
