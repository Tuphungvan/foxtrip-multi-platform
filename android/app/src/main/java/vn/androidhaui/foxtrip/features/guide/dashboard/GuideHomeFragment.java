package vn.androidhaui.foxtrip.features.guide.dashboard;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentGuideHomeBinding;
import vn.androidhaui.foxtrip.features.guide.tour.GuideTourDetailFragment;
import vn.androidhaui.foxtrip.features.guide.tour.GuideTourViewModel;
import vn.androidhaui.foxtrip.features.guide.tour.GuideToursAdapter;
import vn.androidhaui.foxtrip.features.user.account.AccountViewModel;
import vn.androidhaui.foxtrip.features.user.account.CloudinaryUploadHelper;
import vn.androidhaui.foxtrip.features.user.auth.AuthViewModel;
import vn.androidhaui.foxtrip.models.dto.request.UpdateStaffProfileRequest;

public class GuideHomeFragment extends Fragment {

    private FragmentGuideHomeBinding binding;
    private GuideTourViewModel tourViewModel;
    private AccountViewModel accountViewModel;
    private AuthViewModel authViewModel;
    private GuideToursAdapter adapter;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private CloudinaryUploadHelper uploadHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tourViewModel = new ViewModelProvider(this).get(GuideTourViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        uploadHelper = new CloudinaryUploadHelper(requireContext());

        setupRecyclerView();
        setupImagePicker();
        setupListeners();
        observeViewModel();

        accountViewModel.loadGuideProfile();
        tourViewModel.loadMyTours();
    }

    private void setupRecyclerView() {
        adapter = new GuideToursAdapter(tour -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.guide_fragment_container, GuideTourDetailFragment.newInstance(tour.id.toString(), tour.slug))
                    .addToBackStack(null)
                    .commit();
        });
        binding.recyclerTours.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTours.setAdapter(adapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatar(uri);
                    }
                }
        );
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> authViewModel.logout());
        
        // Click info text -> Go to update screen
        binding.layoutInfo.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.guide_fragment_container, new GuideUpdateProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Click avatar -> Change avatar
        binding.cardAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void observeViewModel() {
        accountViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvUsername.setText(user.getUsername());
                binding.tvEmail.setText(user.getEmail());
                
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .into(binding.imgAvatar);
                } else {
                    binding.imgAvatar.setImageResource(R.drawable.ic_avatar);
                }
            }
        });

        accountViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                accountViewModel.clearMessage();
            }
        });

        tourViewModel.getTours().observe(getViewLifecycleOwner(), tours -> {
            if (tours == null || tours.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                adapter.reloadData(null);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.reloadData(tours);
            }
        });

        tourViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void uploadAvatar(Uri uri) {
        Toast.makeText(requireContext(), "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
        Glide.with(this).load(uri).into(binding.imgAvatar);

        uploadHelper.uploadAvatar(uri, new CloudinaryUploadHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UpdateStaffProfileRequest req = new UpdateStaffProfileRequest();
                        req.setAvatarUrl(imageUrl);
                        accountViewModel.updateGuideProfile(req);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Lỗi upload: " + error, Toast.LENGTH_SHORT).show();
                        accountViewModel.loadGuideProfile();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
