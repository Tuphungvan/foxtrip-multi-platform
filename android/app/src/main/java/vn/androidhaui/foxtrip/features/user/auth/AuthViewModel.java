package vn.androidhaui.foxtrip.features.user.auth;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.androidhaui.foxtrip.ui.MainActivity;
import vn.androidhaui.foxtrip.models.domain.User;
import vn.androidhaui.foxtrip.features.user.chatbot.ChatbotFragment;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repo;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> shouldSignOutGoogle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> forgotPasswordSuccess = new MutableLiveData<>();
    private String registeredEmail;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repo = new AuthRepository(application.getApplicationContext());
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getShouldSignOutGoogle() {
        return shouldSignOutGoogle;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<Boolean> getOtpSuccess() {
        return otpSuccess;
    }

    public LiveData<Boolean> getForgotPasswordSuccess() {
        return forgotPasswordSuccess;
    }

    public void clearRegisterSuccess() {
        registerSuccess.setValue(null);
    }

    public void clearOtpSuccess() {
        otpSuccess.setValue(null);
    }

    public void clearForgotPasswordSuccess() {
        forgotPasswordSuccess.setValue(null);
    }

    public String getRegisteredEmail() {
        return registeredEmail;
    }

    public void setRegisteredEmail(String email) {
        this.registeredEmail = email;
    }

    public void resetShouldSignOutGoogle() {
        shouldSignOutGoogle.setValue(false);
    }

    public void register(String username, String email, String password,
            String phone, boolean sendOtp) {
        loading.postValue(true);
        repo.register(username, email, password, phone, sendOtp, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                registerSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void verifyEmail(String email, String otp) {
        loading.postValue(true);
        repo.verifyEmail(email, otp, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                message.postValue(result);
                otpSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void login(String email, String password) {
        loading.postValue(true);
        repo.login(email, password, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                message.postValue("Đăng nhập thành công");
                user.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        loading.postValue(true);
        repo.loginWithGoogle(idToken, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(User result) {
                loading.postValue(false);
                message.postValue("Đăng nhập thành công");
                user.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
                if (error != null && error.contains("Tài khoản bị vô hiệu")) {
                    shouldSignOutGoogle.postValue(true);
                }
            }
        });
    }

    public void logout() {
        repo.logout(new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                performLocalLogout(result);
            }

            @Override
            public void onError(String error) {
                // Vẫn thực hiện logout local nếu call API thất bại
                performLocalLogout("Đã đăng xuất");
            }
        });
    }

    private void performLocalLogout(String msg) {
        message.postValue(msg);
        user.postValue(null);

        shouldSignOutGoogle.postValue(true);

        ChatbotFragment.clearChatHistory(getApplication());

        Intent intent = new Intent(getApplication(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getApplication().startActivity(intent);
    }

    public void loadSession() {
        User savedUser = repo.loadUserFromPrefs();
        user.setValue(savedUser);
    }

    public void sendOTP(String email) {
        loading.postValue(true);
        repo.sendOTP(email, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                message.postValue(result);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void forgotPassword(String email) {
        loading.postValue(true);
        repo.forgotPassword(email, new AuthRepository.CallbackResult<>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                message.postValue(result);
                forgotPasswordSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
            }
        });
    }

    public void clearMessage() {
        message.setValue(null);
    }
}