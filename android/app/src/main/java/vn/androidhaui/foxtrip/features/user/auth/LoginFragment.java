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

import vn.androidhaui.foxtrip.ui.MainActivity;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentLoginBinding;

import java.util.Arrays;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    // Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupGoogleSignIn();
        return binding.getRoot();
    }

    private void setupGoogleSignIn() {
        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Launcher để nhận kết quả từ Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
        );
    }

    private void promptGoogleAccountSelection() {
        // Sign out khỏi Google trước khi sign in để buộc chọn tài khoản
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            // Sau khi sign out, bắt đầu sign in
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void signOutGoogle() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut()
                    .addOnCompleteListener(requireActivity(), task -> {
                        Log.d(TAG, "Google Sign-Out cục bộ thành công.");
                        // Reset cờ báo hiệu để tránh gọi lại nhiều lần
                        viewModel.resetShouldSignOutGoogle();
                    });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewModel.getShouldSignOutGoogle().observe(getViewLifecycleOwner(), shouldSignOut -> {
            if (shouldSignOut != null && shouldSignOut) {
                signOutGoogle();
            }
        });

        // Observe user — sau khi login thành công (MainActivity sẽ tự động chuyển hướng sang GuideActivity nếu role là GUIDE)
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onLoginSuccess();
            }
        });

        // Observe message
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Login thông thường
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(), "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        // ✅ Login với Google
        binding.btnGoogleLogin.setOnClickListener(v ->
            promptGoogleAccountSelection()
        );

        binding.tvForgotPassword.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new ForgotPasswordFragment(), false);
            }
        });

        // Go to Register
        binding.tvGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new RegisterFragment(), false);
            }
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken != null) {
                Log.d(TAG, "Google Sign-In success, sending idToken to backend");
                viewModel.loginWithGoogle(idToken);
            } else {
                Toast.makeText(requireContext(), "Không thể lấy Google ID Token", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed: " + e.getStatusCode(), e);
            Toast.makeText(requireContext(), "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}