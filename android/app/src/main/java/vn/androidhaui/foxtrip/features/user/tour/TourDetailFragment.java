package vn.androidhaui.foxtrip.features.user.tour;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
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

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.bumptech.glide.Glide;

import vn.androidhaui.foxtrip.features.user.cart.CartSharedViewModel;
import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;
import vn.androidhaui.foxtrip.utils.ProvinceUtils;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.features.user.cart.CartRepository;
import vn.androidhaui.foxtrip.ui.MainActivity;
import vn.androidhaui.foxtrip.databinding.FragmentTourDetailBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourDetailResDTO;
import vn.androidhaui.foxtrip.models.dto.response.ItineraryItemResDTO;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

public class TourDetailFragment extends Fragment {

    private static final String ARG_SLUG = "slug";
    private String slug;
    private FragmentTourDetailBinding binding;
    private TourDetailViewModel viewModel;
    private ItineraryAdapter itineraryAdapter;
    private MediaPagerAdapter mediaPagerAdapter;
    private TourReviewAdapter reviewAdapter;
    private TourDetailResDTO currentTour;   // lưu để btnAddToCart dùng
    private CartRepository cartRepository;  // gọi PUT /api/cart/item

    public static TourDetailFragment newInstance(String slug) {
        TourDetailFragment fragment = new TourDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SLUG, slug);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTourDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TourDetailViewModel.class);
        cartRepository = new CartRepository(requireContext());
        if (getArguments() != null) slug = getArguments().getString(ARG_SLUG);

        setupItineraryRecycler();
        setupMediaPager();
        setupReviewRecycler();
        loadTourDetail();

        final int[] quantity = {1};
        binding.tvQuantity.setText(String.valueOf(quantity[0]));

        binding.btnIncrease.setOnClickListener(v -> {
            try {
                String availableStr = binding.tvAvailableSlots.getText().toString();
                int available = Integer.parseInt(availableStr);
                if (quantity[0] < available) {
                    quantity[0]++;
                    binding.tvQuantity.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(requireContext(), "Đã đạt giới hạn số chỗ còn trống", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                // Nếu chưa load xong hoặc lỗi, mặc định cho phép tăng nếu text chưa có
                if (binding.tvAvailableSlots.getText().toString().isEmpty()) {
                     quantity[0]++;
                     binding.tvQuantity.setText(String.valueOf(quantity[0]));
                }
            }
        });

        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                binding.tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            // Kiểm tra đăng nhập
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
            if (prefs.getString("accessToken", null) == null) {
                Toast.makeText(requireContext(),
                        "Vui lòng đăng nhập để thêm vào giỏ hàng",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentTour == null || currentTour.id == null) {
                Toast.makeText(requireContext(), "Tour không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tính số lượng mới (cộng dồn nếu đã có trong giỏ hàng)
            int finalQuantity = quantity[0];
            CartSharedViewModel cartSharedViewModel = new ViewModelProvider(requireActivity()).get(CartSharedViewModel.class);
            if (cartSharedViewModel.getCartItems().getValue() != null) {
                for (CartItemDTO item : cartSharedViewModel.getCartItems().getValue()) {
                    if (currentTour.id.equals(item.tourId)) {
                        finalQuantity += item.getSafeQuantity();
                        break;
                    }
                }
            }

            binding.btnAddToCart.setEnabled(false);
            cartRepository.upsertItem(currentTour.id, finalQuantity).observe(getViewLifecycleOwner(), items -> {
                binding.btnAddToCart.setEnabled(true);
                if (items != null) {
                    Toast.makeText(requireContext(), "✓ Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại giỏ hàng chung
                    cartSharedViewModel.setCartItems(new ArrayList<>(items));
                    // Update badge ở bottom nav
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge(items.size());
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi kết nối hoặc đạt giới hạn số lượng", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnBack.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack()
        );
    }
    
    private void setupItineraryRecycler() {
        itineraryAdapter = new ItineraryAdapter(requireContext());
        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvItinerary.setAdapter(itineraryAdapter);
    }

    private void setupMediaPager() {
        mediaPagerAdapter = new MediaPagerAdapter(requireContext());
        binding.mediaPager.setAdapter(mediaPagerAdapter);
    }

    private void setupReviewRecycler() {
        reviewAdapter = new TourReviewAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void loadTourDetail() {
        viewModel.loadTourDetail(slug).observe(getViewLifecycleOwner(), tour -> {
            if (tour == null) {
                Toast.makeText(requireContext(), "Không thể tải chi tiết tour", Toast.LENGTH_SHORT).show();
            } else {
                currentTour = tour;
                bindTourData(tour);
                if (tour.id != null) {
                    loadTourReviews(tour.id);
                }
            }
        });
    }

    private void loadTourReviews(String tourId) {
        viewModel.loadTourReviews(tourId).observe(getViewLifecycleOwner(), pageData -> {
            if (pageData != null && pageData.items != null && !pageData.items.isEmpty()) {
                reviewAdapter.setItems(pageData.items);
                binding.rvReviews.setVisibility(View.VISIBLE);
                binding.tvNoReviews.setVisibility(View.GONE);
                if (pageData.totalItems != null && pageData.totalItems > 5) {
                    binding.tvShowAllReviews.setVisibility(View.VISIBLE);
                }
            } else {
                binding.rvReviews.setVisibility(View.GONE);
                binding.tvNoReviews.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindTourData(TourDetailResDTO tour) {
        binding.tvTourName.setText(tour.name != null ? tour.name : "");
        
        // Review Status Header
        double avgRating = tour.averageRating != null ? tour.averageRating : 5.0;
        binding.layoutReviewHeader.setVisibility(View.VISIBLE);
        binding.tvAverageRatingBig.setText(String.format("%.1f", avgRating));
        binding.tvTotalReviewCount.setText(String.format("%d đánh giá", tour.reviewCount != null ? tour.reviewCount : 0));
        
        if (avgRating >= 4.0) {
            binding.ivRatingStatus.setImageResource(R.drawable.fantastic);
            binding.tvRatingText.setText("Tuyệt vời");
        } else {
            binding.ivRatingStatus.setImageResource(R.drawable.good);
            binding.tvRatingText.setText("Tốt");
        }

        binding.tvRatingDetail.setText(String.format("★ %.1f", avgRating));
        binding.tvReviewCount.setText(String.format("(%d đánh giá)", tour.reviewCount != null ? tour.reviewCount : 0));
        
        // Aggregate images from thumbnail and itinerary
        List<String> allImages = new ArrayList<>();
        if (tour.thumbnailUrl != null) allImages.add(tour.thumbnailUrl);
        if (tour.itineraries != null) {
            for (ItineraryItemResDTO item : tour.itineraries) {
                if (item.locationImageUrl != null && !item.locationImageUrl.isEmpty() && !allImages.contains(item.locationImageUrl)) {
                    allImages.add(item.locationImageUrl);
                }
            }
        }
        mediaPagerAdapter.submitList(allImages);

        binding.tvDescription.setText(tour.description != null ? tour.description : "");

        double price = tour.price != null ? tour.price : 0;
        double discount = tour.discount != null ? tour.discount : 0;
        double finalPrice = tour.finalPrice != null ? tour.finalPrice : price;

        if (discount > 0) {
            binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvOriginalPrice.setPaintFlags(
                    binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            binding.tvOriginalPrice.setText(String.format("%,.0f VND", price));
        } else {
            binding.tvOriginalPrice.setVisibility(View.GONE);
        }

        binding.tvFinalPrice.setText(String.format("%,.0f VND", finalPrice));

        if (tour.startDate != null && tour.endDate != null) {
            binding.tvTime.setText("Từ " + DateTimeUtils.toDisplayDate(tour.startDate) +
                    " - " + DateTimeUtils.toDisplayDate(tour.endDate));
        }

        binding.tvCategory.setText(ProvinceUtils.getCategoryDisplay(tour.category));
        binding.tvProvince.setText(ProvinceUtils.getProvinceDisplay(tour.province));
        binding.tvRegion.setText("N/A"); // Remove region as it's not in new DTO
        binding.tvAvailableSlots.setText(tour.availableSlots != null ? tour.availableSlots.toString() : "0");

        // Bind Guide data
        if (tour.guide != null) {
            binding.layoutGuide.setVisibility(View.VISIBLE);
            
            String name = tour.guide.fullName != null ? tour.guide.fullName : tour.guide.username;
            binding.tvGuideName.setText("Tên: " + (name != null ? name : "N/A"));
            
            binding.tvGuidePhone.setText("SĐT: " + (tour.guide.phoneNumber != null ? tour.guide.phoneNumber : "N/A"));
            binding.tvGuideEmail.setText("Email: " + (tour.guide.email != null ? tour.guide.email : "N/A"));
        } else {
            binding.layoutGuide.setVisibility(View.GONE);
        }
        
        if (tour.itineraries != null && !tour.itineraries.isEmpty()) {
            List<ItineraryAdapter.DisplayItem> displayItems = new ArrayList<>();
            int lastDay = -1;
            for (ItineraryItemResDTO item : tour.itineraries) {
                if (item.dayNumber != lastDay) {
                    displayItems.add(ItineraryAdapter.DisplayItem.header(item.dayNumber));
                    lastDay = item.dayNumber;
                }
                displayItems.add(ItineraryAdapter.DisplayItem.activity(item));
            }
            itineraryAdapter.submitList(displayItems);
        }

        itineraryAdapter.setOnLocationClickListener(locationId -> {
            // Show location info (using the existing bottom sheet logic if possible)
            // For now, we just perform a selection on the map to show the popup
            for (ItineraryItemResDTO item : tour.itineraries) {
                if (locationId.equals(item.locationId)) {
                    // Logic to show popup or navigate to location detail
                    // Here we can show a Toast or integrate with a BottomSheet
                    Toast.makeText(requireContext(), "Địa điểm: " + item.locationName, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        });
        setupMapboxItinerary(tour.itineraries);
    }

    private void setupMapboxItinerary(List<ItineraryItemResDTO> itinerary) {
        MapView mapView = binding.mapViewItinerary;
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            Bitmap tourIcon = drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_blue));
            if (tourIcon != null) style.addImage("icon-tour", tourIcon);
        });
        
        AnnotationPlugin plugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        PointAnnotationManager pointAnnotationManager = (PointAnnotationManager) plugin.createAnnotationManager(AnnotationType.PointAnnotation, new AnnotationConfig());
        
        Point firstPoint = null;

        for (ItineraryItemResDTO item : itinerary) {
            if (item.lat != null && item.lng != null) {
                Point point = Point.fromLngLat(item.lng, item.lat);
                if (firstPoint == null) firstPoint = point;
                
                PointAnnotationOptions options = new PointAnnotationOptions()
                        .withPoint(point)
                        .withTextField("Ngày " + item.dayNumber + ": " + (item.locationName != null ? item.locationName : ""))
                        .withTextSize(12.0)
                        .withTextColor("#2196F3") // Blue
                        .withTextHaloColor("#FFFFFF")
                        .withTextHaloWidth(1.2)
                        .withIconImage("icon-tour")
                        .withIconSize(1.0f)
                        .withTextOffset(new ArrayList<Double>() {{ add(0.0); add(2.5); }});
                
                pointAnnotationManager.create(options);
            }
        }
        
        if (firstPoint != null) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(firstPoint)
                .zoom(10.0)
                .build());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
