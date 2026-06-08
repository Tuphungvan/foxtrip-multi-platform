package vn.androidhaui.foxtrip.features.guide.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.dto.request.UpdateStaffProfileRequest;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;

public class GuideUpdateProfileViewModel extends AndroidViewModel {
    private final GuideUpdateProfileRepository repo;
    private final MutableLiveData<UserDetailResponse> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public GuideUpdateProfileViewModel(@NonNull Application application) {
        super(application);
        repo = new GuideUpdateProfileRepository(application.getApplicationContext());
    }

    public LiveData<UserDetailResponse> getUser() { return user; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }

    public void loadProfile() {
        loading.postValue(true);
        repo.getUpdateProfile(new GuideUpdateProfileRepository.CallbackResult<>() {
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

    public void updateProfile(UpdateStaffProfileRequest request) {
        loading.postValue(true);
        repo.postUpdateProfile(request, new GuideUpdateProfileRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                updateSuccess.postValue(true);
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
