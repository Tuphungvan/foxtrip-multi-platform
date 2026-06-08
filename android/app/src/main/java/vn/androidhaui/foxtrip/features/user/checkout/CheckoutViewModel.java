package vn.androidhaui.foxtrip.features.user.checkout;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.dto.request.AddonItemDTO;
import vn.androidhaui.foxtrip.models.dto.request.AddonsWrapperDTO;
import vn.androidhaui.foxtrip.models.dto.request.CreateOrderReqDTO;
import vn.androidhaui.foxtrip.models.dto.response.CreateOrderResDTO;

import java.util.ArrayList;
import java.util.List;

public class CheckoutViewModel extends AndroidViewModel {

    public enum Event { EMAIL_NOT_VERIFIED }

    private final CheckoutRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<CreateOrderResDTO> orderCreated = new MutableLiveData<>();
    private final MutableLiveData<String> paymentUrl = new MutableLiveData<>();
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cartCleared = new MutableLiveData<>();

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CheckoutRepository(application);
    }

    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<CreateOrderResDTO> getOrderCreated() { return orderCreated; }
    public LiveData<String> getPaymentUrl() { return paymentUrl; }
    public LiveData<Event> getEvent() { return event; }
    public LiveData<Boolean> getCartCleared() { return cartCleared; }

    public void clearPaymentUrl() { paymentUrl.setValue(null); }
    public void clearEvent() { event.setValue(null); }

    /** Gọi sau khi WebView thanh toán xong: xóa giỏ hàng trên server và thông báo UI */
    public void clearCartAfterPayment() {
        repository.clearCart(() -> cartCleared.postValue(true));
    }

    public void createOrder(String name, String phone, String email,
                            String tourId, int quantity,
                            List<AddonPickerAdapter.SelectedAddon> selectedAddons) {
        loading.setValue(true);

        List<AddonItemDTO> addonItems = new ArrayList<>();
        for (AddonPickerAdapter.SelectedAddon sa : selectedAddons) {
            addonItems.add(new AddonItemDTO(sa.addon.id, sa.quantity));
        }
        AddonsWrapperDTO wrapper = new AddonsWrapperDTO(addonItems);
        CreateOrderReqDTO req = new CreateOrderReqDTO(true, name, phone, email, tourId, quantity, wrapper);

        repository.createOrder(req, new CheckoutRepository.OrderCallback() {
            @Override
            public void onSuccess(CreateOrderResDTO data) {
                loading.setValue(false);
                orderCreated.setValue(data);
            }

            @Override
            public void onEmailNotVerified() {
                loading.setValue(false);
                event.setValue(Event.EMAIL_NOT_VERIFIED);
            }

            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void initPayment(String orderId) {
        loading.setValue(true);
        repository.initPayment(orderId, new CheckoutRepository.PaymentCallback() {
            @Override
            public void onSuccess(String url) {
                loading.setValue(false);
                paymentUrl.setValue(url);
            }

            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }
}
