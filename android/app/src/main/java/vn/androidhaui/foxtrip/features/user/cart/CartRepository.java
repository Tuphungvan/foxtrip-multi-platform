package vn.androidhaui.foxtrip.features.user.cart;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.request.UpsertCartItemRequest;
import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;
import vn.androidhaui.foxtrip.models.dto.response.CartResponseDTO;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class CartRepository {

    private final ApiService apiService;

    public CartRepository(Context context) {
        apiService = ApiClient.getInstance(context).getApiService();
    }

    /** Lấy giỏ hàng. Trả về null nếu lỗi mạng, empty list nếu rỗng. */
    public LiveData<List<CartItemDTO>> getCart() {
        MutableLiveData<List<CartItemDTO>> data = new MutableLiveData<>();
        apiService.getCart().enqueue(new Callback<ApiResponse<CartResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponseDTO>> call,
                                   @NonNull Response<ApiResponse<CartResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    data.postValue(response.body().data.safeItems());
                } else {
                    data.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponseDTO>> call, @NonNull Throwable t) {
                data.postValue(null); // null = lỗi kết nối
            }
        });
        return data;
    }

    /**
     * Thêm / cập nhật / xóa item trong giỏ.
     * quantity=0 → backend sẽ xóa item.
     * Trả về null nếu lỗi; empty list nếu 401 (chưa đăng nhập).
     *
     * @return LiveData<List> — null = lỗi mạng/server, empty = 401 chưa đăng nhập
     */
    public LiveData<List<CartItemDTO>> upsertItem(String tourId, int quantity) {
        MutableLiveData<List<CartItemDTO>> data = new MutableLiveData<>();
        apiService.upsertCartItem(new UpsertCartItemRequest(tourId, quantity))
                .enqueue(new Callback<ApiResponse<CartResponseDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<CartResponseDTO>> call,
                                           @NonNull Response<ApiResponse<CartResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            data.postValue(response.body().data.safeItems());
                        } else if (response.code() == 401) {
                            data.postValue(new ArrayList<>()); // empty = dấu hiệu 401
                        } else {
                            data.postValue(null); // null = lỗi khác
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<CartResponseDTO>> call, @NonNull Throwable t) {
                        data.postValue(null);
                    }
                });
        return data;
    }

    /** Xóa toàn bộ giỏ hàng */
    public LiveData<Boolean> clearCart() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        apiService.clearCart().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                result.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                result.postValue(false);
            }
        });
        return result;
    }
}
