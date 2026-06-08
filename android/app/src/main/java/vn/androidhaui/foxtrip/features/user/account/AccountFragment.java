package vn.androidhaui.foxtrip.features.user.account;

import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentAccountBinding;
import vn.androidhaui.foxtrip.features.user.auth.LoginFragment;
import vn.androidhaui.foxtrip.features.user.order.OrdersFragment;
import vn.androidhaui.foxtrip.models.dto.response.UserDetailResponse;
import vn.androidhaui.foxtrip.features.user.auth.AuthViewModel;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AccountViewModel profileViewModel;
    private AuthViewModel authViewModel;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private CloudinaryUploadHelper uploadHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        uploadHelper = new CloudinaryUploadHelper(requireContext());

        authViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                ((MainActivity) requireActivity()).loadFragment(new LoginFragment(), false);
            }
        });

        profileViewModel.getUser().observe(getViewLifecycleOwner(), this::renderUser);

        profileViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                profileViewModel.clearMessage();
            }
        });

        // Load profile khi mở fragment
        profileViewModel.loadProfile();

        // Option placeholders
        binding.optionOrders.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .addToBackStack(null)
                    .commit()
        );

        binding.optionUpdate.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UpdateProfileFragment())
                    .addToBackStack(null)
                    .commit()
        );

        // Khởi tạo image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatar(uri);
                    }
                }
        );

        // Click vào avatar để chọn ảnh
        binding.imgAvatar.setOnClickListener(v ->
            imagePickerLauncher.launch("image/*")
        );
    }

    private void renderUser(UserDetailResponse user) {
        if (user == null) {
            binding.tvUsername.setText("Khách");
            binding.tvEmail.setText("Bạn chưa đăng nhập");
            binding.imgAvatar.setImageResource(R.drawable.ic_avatar);
            
            // Hide private options
            binding.optionOrders.setVisibility(View.GONE);
            binding.optionUpdate.setVisibility(View.GONE);
            
            // Change button to Login
            binding.btnLogout.setText("Đăng nhập");
            binding.btnLogout.setOnClickListener(v -> 
                ((MainActivity) requireActivity()).loadFragment(new LoginFragment(), false)
            );
            return;
        }

        // Show private options
        binding.optionOrders.setVisibility(View.VISIBLE);
        binding.optionUpdate.setVisibility(View.VISIBLE);

        // Change button to Logout
        binding.btnLogout.setText(R.string.btn_logout);
        binding.btnLogout.setOnClickListener(v -> authViewModel.logout());

        binding.tvUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        binding.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        String avatar = user.getAvatarUrl();
        if (avatar == null || avatar.isEmpty()) {
            binding.imgAvatar.setImageResource(R.drawable.ic_avatar);
        } else {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.ic_avatar)
                    .into(binding.imgAvatar);
        }
    }

    private void uploadAvatar(Uri uri) {
        Toast.makeText(requireContext(), "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
        
        // Hiển thị ảnh preview ngay lập tức
        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_avatar)
                .into(binding.imgAvatar);

        uploadHelper.uploadAvatar(uri, new CloudinaryUploadHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Upload thành công!", Toast.LENGTH_SHORT).show();
                    // Cập nhật avatar URL vào profile
                    profileViewModel.updateAvatarUrl(imageUrl);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Lỗi upload: " + error, Toast.LENGTH_SHORT).show();
                    // Reload lại avatar cũ
                    profileViewModel.loadProfile();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
