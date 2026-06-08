package vn.androidhaui.foxtrip.features.user.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application.getApplicationContext());
    }

    public LiveData<String> getMessage() { return message; }

    public LiveData<List<CartItemDTO>> loadCart() {
        return repository.getCart();
    }

    /** Tăng / Giảm / Cập nhật số lượng. quantity=0 → xóa item */
    public LiveData<List<CartItemDTO>> upsertItem(String tourId, int quantity) {
        return repository.upsertItem(tourId, quantity);
    }

    public LiveData<Boolean> clearCart() {
        return repository.clearCart();
    }
}
