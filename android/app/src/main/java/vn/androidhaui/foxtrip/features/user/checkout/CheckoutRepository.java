package vn.androidhaui.foxtrip.features.user.checkout;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.request.CreateOrderReqDTO;
import vn.androidhaui.foxtrip.models.dto.request.UpsertCartItemRequest;
import vn.androidhaui.foxtrip.models.dto.response.CartResponseDTO;
import vn.androidhaui.foxtrip.models.dto.response.CreateOrderResDTO;
import vn.androidhaui.foxtrip.models.dto.response.PaymentInitResDTO;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class CheckoutRepository {

    public interface OrderCallback {
        void onSuccess(CreateOrderResDTO data);

        void onEmailNotVerified();

        void onError(String message);
    }

    public interface PaymentCallback {
        void onSuccess(String paymentUrl);

        void onError(String message);
    }

    private final ApiService api;

    public CheckoutRepository(Context context) {
        this.api = ApiClient.getInstance(context).getApiService();
    }

    public void createOrder(CreateOrderReqDTO req, OrderCallback callback) {
        api.createOrder(req).enqueue(new Callback<ApiResponse<CreateOrderResDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<CreateOrderResDTO>> call,
                                   Response<ApiResponse<CreateOrderResDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else if (response.code() == 403) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        if (errorBody.contains("EMAIL_NOT_VERIFIED")) {
                            callback.onEmailNotVerified();
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                    callback.onError("Lỗi tạo đơn hàng");
                } else {
                    callback.onError("Lỗi tạo đơn hàng");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CreateOrderResDTO>> call, Throwable t) {
                callback.onError("Lỗi mạng");
            }
        });
    }

    public void initPayment(String orderId, PaymentCallback callback) {
        api.initPayment(orderId).enqueue(new Callback<ApiResponse<PaymentInitResDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentInitResDTO>> call,
                                   Response<ApiResponse<PaymentInitResDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data.paymentUrl);
                } else {
                    callback.onError("Đặt tour đã thành công! Không thể khởi tạo thanh toán, vui lòng thử lại trong mục Đơn hàng.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentInitResDTO>> call, Throwable t) {
                callback.onError("Lỗi mạng");
            }
        });
    }

    public void clearCart(Runnable onDone) {
        api.clearCart().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {
                onDone.run(); // Dù thành công hay thất bại đều báo xong
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                onDone.run();
            }
        });
    }
}
