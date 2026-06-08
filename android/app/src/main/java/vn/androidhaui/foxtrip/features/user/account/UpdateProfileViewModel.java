package vn.androidhaui.foxtrip.features.user.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.models.dto.request.UpdateUserProfileRequest;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;

public class UpdateProfileViewModel extends AndroidViewModel {
    private final UpdateProfileRepository repo;
    private final MutableLiveData<UserDetailResponse> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    private final MutableLiveData<Boolean> verifySuccess = new MutableLiveData<>();

    public UpdateProfileViewModel(@NonNull Application application) {
        super(application);
        repo = new UpdateProfileRepository(application.getApplicationContext());
    }

    public LiveData<UserDetailResponse> getUser() { return user; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<Boolean> getVerifySuccess() { return verifySuccess; }

    public void loadProfile() {
        loading.postValue(true);
        repo.getUpdateProfile(new UpdateProfileRepository.CallbackResult<>() {
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

    public void updateProfile(UpdateUserProfileRequest request) {
        loading.postValue(true);
        repo.postUpdateProfile(request, new UpdateProfileRepository.CallbackResult<>() {
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

    public void verifyEmailLoggedIn(String otp) {
        loading.postValue(true);
        repo.verifyEmailLoggedIn(otp, new UpdateProfileRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                verifySuccess.postValue(true);
                message.postValue("Xác thực thành công");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void resendOtpLoggedIn() {
        loading.postValue(true);
        repo.resendOtpLoggedIn(new UpdateProfileRepository.CallbackResult<>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                message.postValue("Đã gửi lại mã OTP");
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void clearMessage() { message.setValue(null); }
    public void clearVerifySuccess() { verifySuccess.setValue(null); }
}
