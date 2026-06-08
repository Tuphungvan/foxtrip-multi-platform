package vn.androidhaui.foxtrip.features.user.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.dto.request.UpdateStaffProfileRequest;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;

public class AccountViewModel extends AndroidViewModel {
    private final AccountRepository repo;
    private final MutableLiveData<UserDetailResponse> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public AccountViewModel(@NonNull Application application) {
        super(application);
        repo = new AccountRepository(application.getApplicationContext());
    }

    public LiveData<UserDetailResponse> getUser() { return user; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void loadProfile() {
        loading.postValue(true);
        repo.getProfile(new AccountRepository.CallbackResult<>() {
            @Override
            public void onSuccess(UserDetailResponse result) {
                loading.postValue(false);
                user.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void loadGuideProfile() {
        loading.postValue(true);
        repo.getGuideProfile(new AccountRepository.CallbackResult<>() {
            @Override
            public void onSuccess(UserDetailResponse result) {
                loading.postValue(false);
                user.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void updateAvatarUrl(String avatarUrl) {
        loading.postValue(true);
        repo.updateAvatarUrl(avatarUrl, new AccountRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                message.postValue("Cập nhật avatar thành công");
                loadProfile(); // Reload profile
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void updateGuideProfile(UpdateStaffProfileRequest request) {
        loading.postValue(true);
        repo.updateGuideProfile(request, new AccountRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                message.postValue("Cập nhật hồ sơ thành công");
                loadGuideProfile();
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void clearMessage() { message.setValue(null); }
}
