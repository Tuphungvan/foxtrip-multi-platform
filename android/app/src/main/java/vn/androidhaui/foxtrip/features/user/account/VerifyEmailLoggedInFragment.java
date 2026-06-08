package vn.androidhaui.foxtrip.features.user.account;

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

public class VerifyEmailLoggedInFragment extends Fragment {

    private FragmentVerificationBinding binding;
    private UpdateProfileViewModel viewModel;

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

        viewModel = new ViewModelProvider(requireActivity()).get(UpdateProfileViewModel.class);

        observeViewModel();

        // Gửi OTP ngay khi vào màn hình
        viewModel.resendOtpLoggedIn();

        // Nút xác thực
        binding.btnVerifyOTP.setOnClickListener(v -> {
            String code = binding.etOtpCode.getText().toString().trim();
            if (code.length() != 6) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.verifyEmailLoggedIn(code);
        });

        // Nút gửi lại
        binding.btnResendOTP.setOnClickListener(v -> viewModel.resendOtpLoggedIn());
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

        viewModel.getVerifySuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                // Quay lại màn hình trước
                requireActivity().getSupportFragmentManager().popBackStack();
                viewModel.clearVerifySuccess();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
