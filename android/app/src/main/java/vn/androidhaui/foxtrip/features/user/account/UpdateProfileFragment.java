package vn.androidhaui.foxtrip.features.user.account;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.databinding.FragmentUpdateProfileBinding;
import vn.androidhaui.foxtrip.features.user.auth.AuthViewModel;
import vn.androidhaui.foxtrip.models.dto.request.UpdateUserProfileRequest;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;

public class UpdateProfileFragment extends Fragment {

    private FragmentUpdateProfileBinding binding;
    private UpdateProfileViewModel viewModel;
    private AuthViewModel authViewModel;

    private String originalEmail = "";
    private String originalPassword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdateProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UpdateProfileViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Tải thông tin ban đầu
        viewModel.loadProfile();
        viewModel.getUser().observe(getViewLifecycleOwner(), this::renderUser);

        // Quan sát thông báo
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        // Nút lưu
        binding.btnSave.setOnClickListener(v -> saveProfile());

        // Nút Verify Email
        binding.btnVerifyEmail.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new VerifyEmailLoggedInFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Khi cập nhật thành công
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                String newEmail = getText(binding.inputEmail);
                String newPassword = getText(binding.inputPassword);

                boolean emailChanged = !TextUtils.equals(originalEmail, newEmail);
                boolean passwordChanged = !TextUtils.isEmpty(newPassword);

                if (emailChanged || passwordChanged) {
                    Toast.makeText(requireContext(),
                            "Bạn đã thay đổi thông tin đăng nhập. Hệ thống sẽ đăng xuất...",
                            Toast.LENGTH_LONG).show();
                    authViewModel.logout();
                } else {
                    Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void renderUser(UserDetailResponse user) {
        if (user == null) return;

        binding.inputUsername.setText(user.getUsername());
        binding.inputEmail.setText(user.getEmail());
        binding.inputPhone.setText(user.getPhoneNumber());
        binding.inputAddress.setText(""); // Backend không có address trong UserDetailResponse
        binding.inputPassword.setText("");

        // Lưu email gốc
        originalEmail = user.getEmail() != null ? user.getEmail() : "";

        // Hiện nút Verify Email nếu chưa xác thực
        if (user.getIsVerified() != null && !user.getIsVerified()) {
            binding.btnVerifyEmail.setVisibility(View.VISIBLE);
        } else {
            binding.btnVerifyEmail.setVisibility(View.GONE);
        }

        // Email là read-only (không cho sửa)
        binding.inputEmail.setEnabled(false);
        binding.inputEmail.setFocusable(false);
    }

    private void saveProfile() {
        String username = getText(binding.inputUsername);
        String email = getText(binding.inputEmail);
        String phone = getText(binding.inputPhone);
        String password = getText(binding.inputPassword);

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setUsername(username);
        request.setPhoneNumber(phone);
        request.setPassword(TextUtils.isEmpty(password) ? null : password);

        viewModel.updateProfile(request);
    }

    private String getText(android.widget.EditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
