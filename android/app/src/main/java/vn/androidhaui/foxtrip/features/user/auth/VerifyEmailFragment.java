package vn.androidhaui.foxtrip.features.user.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.databinding.FragmentVerificationBinding;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class VerifyEmailFragment extends Fragment {

    private FragmentVerificationBinding binding;
    private AuthViewModel viewModel;
    private String email;
    private String username;

    public static VerifyEmailFragment newInstance(String email, String username,
                                                 String phoneNumber, String address) {
        VerifyEmailFragment fragment = new VerifyEmailFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString("email");
            username = getArguments().getString("username");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng chung AuthViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        observeViewModel();

        // Lấy email từ ViewModel (được set từ lúc Register)
        email = viewModel.getRegisteredEmail();

        // Gửi OTP ngay khi vào màn hình
        if (email != null && !email.isEmpty()) {
            viewModel.sendOTP(email);
        }

        // Nút xác thực
        binding.btnVerifyOTP.setOnClickListener(v -> {
            String code = binding.etOtpCode.getText().toString().trim();
            if (code.length() != 6) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.verifyEmail(email, code);
        });

        // Nút gửi lại
        binding.btnResendOTP.setOnClickListener(v -> {
            if (email != null && !email.isEmpty()) {
                viewModel.sendOTP(email);
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy Email đăng ký", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.btnVerifyOTP.setEnabled(!isLoading);
            }
        });

        viewModel.getOtpSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Xác thực thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new LoginFragment(), false); // false = ẩn bottom nav
                }
                viewModel.clearOtpSuccess();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}