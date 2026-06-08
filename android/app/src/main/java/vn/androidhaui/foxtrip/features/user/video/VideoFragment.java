package vn.androidhaui.foxtrip.features.user.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import vn.androidhaui.foxtrip.databinding.FragmentVideoBinding;
import vn.androidhaui.foxtrip.features.user.tour.TourDetailFragment;
import vn.androidhaui.foxtrip.ui.MainActivity;
import vn.androidhaui.foxtrip.R;

public class VideoFragment extends Fragment {

    private FragmentVideoBinding binding;
    private VideoViewModel viewModel;
    private VideoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVideoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        adapter = new VideoAdapter();

        adapter.setOnTourClickListener(video -> {
            if (video.getSlug() != null) {
                ((MainActivity) requireActivity()).loadFragment(TourDetailFragment.newInstance(video.getSlug()), true);
            }
        });

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        viewModel.getVideos().observe(getViewLifecycleOwner(), videos -> {
            if (videos != null && !videos.isEmpty()) {
                adapter.setVideos(videos);
                binding.tvEmpty.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> 
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Play current video
                View view = ((RecyclerView) binding.viewPager.getChildAt(0)).getLayoutManager().findViewByPosition(position);
                if (view != null) {
                    // Let's use a cleaner way to find the holder
                    RecyclerView recyclerView = (RecyclerView) binding.viewPager.getChildAt(0);
                    VideoAdapter.VideoViewHolder activeHolder = (VideoAdapter.VideoViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    if (activeHolder != null) {
                        activeHolder.playVideo();
                    }
                }
            }
        });

        viewModel.loadVideos();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause current video if fragment is hidden
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
