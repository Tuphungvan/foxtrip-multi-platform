package vn.androidhaui.foxtrip.features.guide.tour;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentGuideToursBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourListResDTO;

public class GuideToursFragment extends Fragment {

    private FragmentGuideToursBinding binding;
    private GuideTourViewModel viewModel;
    private GuideToursAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideToursBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GuideTourViewModel.class);

        setupRecyclerView();
        observeViewModel();

        viewModel.loadMyTours();
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

    private void observeViewModel() {
        viewModel.getTours().observe(getViewLifecycleOwner(), tours -> {
            if (tours == null || tours.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                adapter.reloadData(null);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.reloadData(tours);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
