package vn.androidhaui.foxtrip.features.guide.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import vn.androidhaui.foxtrip.databinding.FragmentGuideUpdateProfileBinding;
import vn.androidhaui.foxtrip.models.dto.request.UpdateStaffProfileRequest;

public class GuideUpdateProfileFragment extends Fragment {

    private FragmentGuideUpdateProfileBinding binding;
    private GuideUpdateProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideUpdateProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GuideUpdateProfileViewModel.class);

        observeViewModel();
        setupListeners();
        
        viewModel.loadProfile();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.inputPhone.setText(user.getPhoneNumber());
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void saveProfile() {
        String phone = binding.inputPhone.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateStaffProfileRequest request = new UpdateStaffProfileRequest();
        request.setPhoneNumber(phone);
        if (!password.isEmpty()) {
            request.setPassword(password);
        }

        viewModel.updateProfile(request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
