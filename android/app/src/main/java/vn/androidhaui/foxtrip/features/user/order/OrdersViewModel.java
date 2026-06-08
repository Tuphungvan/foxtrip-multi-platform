package vn.androidhaui.foxtrip.features.user.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.models.dto.response.MyOrderListItemDTO;
import vn.androidhaui.foxtrip.models.dto.response.PageData;

public class OrdersViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    private final MutableLiveData<List<MyOrderListItemDTO>> orders = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> paymentUrl = new MutableLiveData<>();

    private int tabMode = 0;
    private int currentPage = 0;
    private boolean isLastPage = false;
    private boolean isFetching = false;

    public OrdersViewModel(@NonNull Application application) {
        super(application);
        this.repository = new OrderRepository(application);
    }

    public LiveData<List<MyOrderListItemDTO>> getOrders() { return orders; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getPaymentUrl() { return paymentUrl; }
    public void clearPaymentUrl() { paymentUrl.setValue(null); }

    public void setTabMode(int mode) {
        this.tabMode = mode;
        loadOrders(true);
    }

    public void loadOrders(boolean isRefresh) {
        if (isFetching) return;
        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
            loading.setValue(true);
        } else {
            if (isLastPage) return;
        }

        isFetching = true;
        repository.getMyOrders(currentPage, 20, new OrderRepository.ListCallback() {
            @Override
            public void onSuccess(PageData<MyOrderListItemDTO> page) {
                loading.setValue(false);
                isFetching = false;
                List<MyOrderListItemDTO> items = page.items;
                if (items == null || items.isEmpty()) {
                    isLastPage = true;
                    if (isRefresh) orders.setValue(new ArrayList<>());
                    return;
                }

                List<MyOrderListItemDTO> filtered = new ArrayList<>();
                for (MyOrderListItemDTO item : items) {
                    String st = item.status;
                    if (tabMode == 0) {
                        if ("PENDING".equals(st) || "PAID".equals(st) || "PROCESSING".equals(st)) {
                            filtered.add(item);
                        }
                    } else {
                        if ("COMPLETED".equals(st) || "CANCEL_REQUESTED".equals(st)
                                || "REFUND_PENDING".equals(st) || "REFUNDED".equals(st)
                                || "CANCELLED".equals(st)) {
                            filtered.add(item);
                        }
                    }
                }

                List<MyOrderListItemDTO> current = isRefresh ? new ArrayList<>() : orders.getValue();
                if (current == null) current = new ArrayList<>();
                // Chống trùng ID
                for (MyOrderListItemDTO newItem : filtered) {
                    boolean exists = false;
                    for (MyOrderListItemDTO old : current) {
                        if (newItem.orderId != null && newItem.orderId.equals(old.orderId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) current.add(newItem);
                }
                orders.setValue(current);

                currentPage++;
                if (currentPage >= page.totalPages) isLastPage = true;

                if (filtered.isEmpty() && !isLastPage) loadOrders(false);
            }

            @Override
            public void onError() {
                loading.setValue(false);
                isFetching = false;
                if (isRefresh) orders.setValue(null);
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
            public void onError(String message) {
                loading.setValue(false);
            }
        });
    }
}
