package vn.androidhaui.foxtrip.features.user.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.regex.Pattern;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentRegisterBinding;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    // Regex patterns — compile 1 lần duy nhất khi App khởi động
    private static final Pattern EMAIL_PATTERN    = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN    = Pattern.compile("^[0-9]{10}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#^()+\\-={}|:;<>,.?/~`]).{8,}$");

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // Lưu tạm khi đang chờ API trả về để điều hướng OTP
    private String pendingEmail;
    private boolean pendingSendOtp;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        setupGoogleSignIn();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        observeViewModel();
        setupClickListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private void observeViewModel() {
        // Khi backend yêu cầu sign-out Google cục bộ
        viewModel.getShouldSignOutGoogle().observe(getViewLifecycleOwner(), shouldSignOut -> {
            if (shouldSignOut != null && shouldSignOut) signOutGoogle();
        });

        // Đăng nhập thành công → điều hướng (MainActivity sẽ tự động redirect Guide nếu cần)
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onLoginSuccess();
            }
        });

        // Vô hiệu hóa nút khi đang gọi API
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) binding.btnRegister.setEnabled(!isLoading);
        });

        // Kết quả đăng ký
        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (success) {
                if (pendingSendOtp && pendingEmail != null) {
                    VerifyEmailFragment otpFragment =
                            VerifyEmailFragment.newInstance(pendingEmail, "", "", "");
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(otpFragment, false);
                    }
                } else {
                    toast("Đăng ký thành công! Vui lòng đăng nhập.");
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new LoginFragment(), false);
                    }
                }
                viewModel.clearRegisterSuccess();
            }
        });

        // Hiện lỗi từ backend rồi clear để không bị lặp khi xoay màn
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                toast(msg);
                viewModel.clearMessage();
            }
        });
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String username = getText(binding.etUsername);
            String email    = getText(binding.etEmail);
            String password = getText(binding.etPassword);
            String phone    = getText(binding.etPhone);

            if (isInvalid(username,null,"Nhập đầy đủ thông tin bắt buộc")) return;
            if (isInvalid(email,null,"Nhập đầy đủ thông tin bắt buộc")) return;
            if (isInvalid(password,null,"Nhập đầy đủ thông tin bắt buộc")) return;
            if (isInvalid(phone,null,"Nhập đầy đủ thông tin bắt buộc")) return;
            if (username.length() < 8 || username.length() > 50) {
                toast("Tên đăng nhập phải từ 8 đến 50 ký tự"); return;
            }
            if (email.length() < 10 || email.length() > 50 || !EMAIL_PATTERN.matcher(email).matches()) {
                toast("Email không hợp lệ (10-50 ký tự)"); return;
            }
            if (isInvalid(phone, PHONE_PATTERN,"Số điện thoại phải có đúng 10 chữ số")) return;
            if (isInvalid(password, PASSWORD_PATTERN,"Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, thường, số và ký tự đặc biệt")) return;

            pendingEmail = email;
            pendingSendOtp = binding.cbSendOtp.isChecked();
            viewModel.setRegisteredEmail(email);
            viewModel.register(username, email, password, phone, pendingSendOtp);
        });

        binding.btnGoogleRegister.setOnClickListener(v -> promptGoogleAccountSelection());

        binding.tvGoLogin.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new LoginFragment(), false);
            }
        });
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGoogleSignInResult(
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData()))
        );
    }

    private void promptGoogleAccountSelection() {
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(),
                task -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void signOutGoogle() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                Log.d(TAG, "Google Sign-Out cục bộ thành công.");
                viewModel.resetShouldSignOutGoogle();
            });
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            String idToken = completedTask.getResult(ApiException.class).getIdToken();
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken);
            } else {
                toast("Không thể lấy Google ID Token");
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed: " + e.getStatusCode(), e);
            toast("Đăng ký Google thất bại: " + e.getStatusCode());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Kiểm tra field, hiện Toast và trả true nếu có lỗi */
    private boolean isInvalid(String text, Pattern pattern, String errorMsg) {
        if (TextUtils.isEmpty(text) || (pattern != null && !pattern.matcher(text).matches())) {
            toast(errorMsg);
            return true;
        }
        return false;
    }

    private String getText(android.widget.EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}