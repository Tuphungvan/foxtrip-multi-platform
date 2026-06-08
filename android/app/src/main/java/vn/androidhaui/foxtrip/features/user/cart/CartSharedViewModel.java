package vn.androidhaui.foxtrip.features.user.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;
public class CartSharedViewModel extends AndroidViewModel {

    private final MutableLiveData<List<CartItemDTO>> cartItems = new MutableLiveData<>();

    public CartSharedViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCartItems(List<CartItemDTO> items) {
        cartItems.setValue(items);
    }

    public LiveData<List<CartItemDTO>> getCartItems() {
        return cartItems;
    }
    
    private final MutableLiveData<CartItemDTO> checkoutItem = new MutableLiveData<>();
    
    public void setCheckoutItem(CartItemDTO item) {
        checkoutItem.setValue(item);
    }

    public LiveData<CartItemDTO> getCheckoutItem() {
        return checkoutItem;
    }

    /** Gọi sau khi thanh toán thành công để xóa item khỏi giỏ và reset badge */
    public void clearCheckoutItem() {
        checkoutItem.setValue(null);
        cartItems.setValue(null);
    }
}
