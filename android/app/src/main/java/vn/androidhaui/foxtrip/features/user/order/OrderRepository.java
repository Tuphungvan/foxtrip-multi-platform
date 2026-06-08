package vn.androidhaui.foxtrip.features.user.order;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.dto.request.CancelOrderReqDTO;
import vn.androidhaui.foxtrip.models.dto.response.MyOrderListItemDTO;
import vn.androidhaui.foxtrip.models.dto.response.OrderDetailResDTO;
import vn.androidhaui.foxtrip.models.dto.response.PageData;
import vn.androidhaui.foxtrip.models.dto.response.PaymentInitResDTO;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class OrderRepository {

    public interface ListCallback {
        void onSuccess(PageData<MyOrderListItemDTO> page);
        void onError();
    }

    public interface DetailCallback {
        void onSuccess(OrderDetailResDTO data);
        void onError(String message);
    }

    public interface BooleanCallback {
        void onResult(boolean result);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface PaymentCallback {
        void onSuccess(String paymentUrl);
        void onError(String message);
    }

    private final ApiService api;

    public OrderRepository(Context context) {
        this.api = ApiClient.getInstance(context).getApiService();
    }

    public void getMyOrders(int page, int size, ListCallback callback) {
        api.getMyOrders(null, page, size).enqueue(new Callback<ApiResponse<PageData<MyOrderListItemDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageData<MyOrderListItemDTO>>> call,
                    Response<ApiResponse<PageData<MyOrderListItemDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageData<MyOrderListItemDTO>>> call, Throwable t) {
                callback.onError();
            }
        });
    }

    public void getOrderDetail(String orderId, DetailCallback callback) {
        api.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<OrderDetailResDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderDetailResDTO>> call,
                    Response<ApiResponse<OrderDetailResDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("Không thể tải chi tiết đơn hàng");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderDetailResDTO>> call, Throwable t) {
                callback.onError("Lỗi kết nối");
            }
        });
    }

    public void checkReviewExists(String tourId, BooleanCallback callback) {
        api.checkReviewExists(tourId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call,
                    Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onResult(response.body().data);
                } else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                callback.onResult(false);
            }
        });
    }

    public void cancelOrder(String orderId, CancelOrderReqDTO req, ActionCallback callback) {
        api.cancelOrder(orderId, req).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                    Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    try {
                        String body = response.errorBody() != null ? response.errorBody().string() : "";
                        org.json.JSONObject err = new org.json.JSONObject(body);
                        callback.onError(err.optString("message", "Lỗi gửi yêu cầu hủy"));
                    } catch (Exception e) {
                        callback.onError("Lỗi gửi yêu cầu hủy");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                callback.onError("Lỗi kết nối mạng");
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
                    callback.onError("Không thể lấy link thanh toán. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentInitResDTO>> call, Throwable t) {
                callback.onError("Lỗi kết nối mạng");
            }
        });
    }
}
