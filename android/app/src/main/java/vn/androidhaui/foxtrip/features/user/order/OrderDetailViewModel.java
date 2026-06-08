package vn.androidhaui.foxtrip.features.user.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.dto.request.CancelOrderReqDTO;
import vn.androidhaui.foxtrip.models.dto.response.OrderDetailResDTO;

public class OrderDetailViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    private final MutableLiveData<OrderDetailResDTO> order = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasReviewed = new MutableLiveData<>(false);
    private final MutableLiveData<String> paymentUrl = new MutableLiveData<>();

    public OrderDetailViewModel(@NonNull Application application) {
        super(application);
        this.repository = new OrderRepository(application);
    }

    public LiveData<OrderDetailResDTO> getOrder() { return order; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getActionSuccess() { return actionSuccess; }
    public LiveData<Boolean> getHasReviewed() { return hasReviewed; }
    public LiveData<String> getPaymentUrl() { return paymentUrl; }
    public void clearPaymentUrl() { paymentUrl.setValue(null); }
    public void clearMessage() { message.setValue(null); }

    public void loadOrder(String orderId) {
        loading.setValue(true);
        repository.getOrderDetail(orderId, new OrderRepository.DetailCallback() {
            @Override
            public void onSuccess(OrderDetailResDTO data) {
                order.setValue(data);
                if ("COMPLETED".equals(data.status) && data.tour != null && data.tour.tourId != null) {
                    checkReview(data.tour.tourId.toString());
                } else {
                    loading.setValue(false);
                }
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                message.setValue(msg);
            }
        });
    }

    private void checkReview(String tourId) {
        repository.checkReviewExists(tourId, result -> {
            loading.setValue(false);
            hasReviewed.setValue(result);
        });
    }

    public void cancelOrder(String orderId, CancelOrderReqDTO req) {
        loading.setValue(true);
        repository.cancelOrder(orderId, req, new OrderRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                message.setValue("Đã gửi yêu cầu hủy đơn thành công.");
                actionSuccess.setValue(true);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                message.setValue(msg);
            }
        });
    }

    public void initPayment(String orderId) {
        loading.setValue(true);
        repository.initPayment(orderId, new OrderRepository.PaymentCallback() {
            @Override
            public void onSuccess(String url) {
                loading.setValue(false);
                paymentUrl.setValue(url);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                message.setValue(msg);
            }
        });
    }
}
