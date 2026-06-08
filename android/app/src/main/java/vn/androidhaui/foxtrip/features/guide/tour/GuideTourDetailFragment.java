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
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import vn.androidhaui.foxtrip.features.user.tour.ItineraryAdapter;
import vn.androidhaui.foxtrip.models.dto.response.ItineraryItemResDTO;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentGuideTourDetailBinding;

public class GuideTourDetailFragment extends Fragment {

    private FragmentGuideTourDetailBinding binding;
    private GuideTourViewModel viewModel;
    private PassengersAdapter passengersAdapter;
    private ItineraryAdapter itineraryAdapter;
    private String tourId;
    private String slug;

    public static GuideTourDetailFragment newInstance(String tourId, String slug) {
        GuideTourDetailFragment fragment = new GuideTourDetailFragment();
        Bundle args = new Bundle();
        args.putString("tourId", tourId);
        args.putString("slug", slug);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tourId = getArguments().getString("tourId");
            slug = getArguments().getString("slug");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideTourDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GuideTourViewModel.class);

        setupRecyclerViews();
        observeViewModel();

        viewModel.loadTourDetail(slug);
        viewModel.loadPassengers(tourId);

        binding.btnScanQR.setOnClickListener(v -> startScanning());
    }

    private void setupRecyclerViews() {
        // Hành khách
        passengersAdapter = new PassengersAdapter();
        binding.recyclerPassengers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerPassengers.setAdapter(passengersAdapter);

        // Lịch trình — tái sử dụng ItineraryAdapter của user (đã có nút Google Maps)
        itineraryAdapter = new ItineraryAdapter(requireContext());
        binding.recyclerItinerary.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerItinerary.setAdapter(itineraryAdapter);
    }

    private void observeViewModel() {
        viewModel.getTourDetail().observe(getViewLifecycleOwner(), tour -> {
            if (tour == null) return;

            // Tên tour
            binding.txtTourName.setText(tour.name != null ? tour.name : "Tên Tour");

            // Banner ảnh full-width
            if (tour.thumbnailUrl != null && !tour.thumbnailUrl.isEmpty()) {
                Glide.with(this)
                        .load(tour.thumbnailUrl)
                        .centerCrop()
                        .into(binding.imgTourBanner);
            }

            // Thời gian
            if (tour.startDate != null && tour.endDate != null) {
                binding.txtTime.setText("Từ " + DateTimeUtils.toDisplayDate(tour.startDate)
                        + " → " + DateTimeUtils.toDisplayDate(tour.endDate));
            } else {
                binding.txtTime.setVisibility(View.GONE);
            }

            // Mô tả
            binding.txtDescription.setText(
                    (tour.description != null && !tour.description.isEmpty())
                            ? tour.description : "Không có mô tả.");

            // Lịch trình — dùng cùng logic ItineraryAdapter.DisplayItem như bên user
            if (tour.itineraries != null && !tour.itineraries.isEmpty()) {
                List<ItineraryAdapter.DisplayItem> displayItems = new ArrayList<>();
                int lastDay = -1;
                for (ItineraryItemResDTO item : tour.itineraries) {
                    if (item.dayNumber != null && item.dayNumber != lastDay) {
                        displayItems.add(ItineraryAdapter.DisplayItem.header(item.dayNumber));
                        lastDay = item.dayNumber;
                    }
                    displayItems.add(ItineraryAdapter.DisplayItem.activity(item));
                }
                itineraryAdapter.submitList(displayItems);
                binding.recyclerItinerary.setVisibility(View.VISIBLE);
                binding.tvNoItinerary.setVisibility(View.GONE);
            } else {
                binding.recyclerItinerary.setVisibility(View.GONE);
                binding.tvNoItinerary.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getPassengers().observe(getViewLifecycleOwner(), passengers -> {
            if (passengers == null || passengers.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                passengersAdapter.reloadData(null);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                passengersAdapter.reloadData(passengers);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        viewModel.getCheckInSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Check-in thành công!", Toast.LENGTH_SHORT).show();
                viewModel.resetCheckInStatus();
                viewModel.loadPassengers(tourId);
            }
        });
    }

    private void startScanning() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.guide_fragment_container, QRScannerFragment.newInstance(tourId))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
