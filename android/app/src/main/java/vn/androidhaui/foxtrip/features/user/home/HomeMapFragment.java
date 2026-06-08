package vn.androidhaui.foxtrip.features.user.home;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.compass.CompassPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentHomeMapBinding;
import vn.androidhaui.foxtrip.databinding.LocationPopupBinding;
import vn.androidhaui.foxtrip.databinding.TourPopupBinding;
import vn.androidhaui.foxtrip.databinding.ItemSearchSuggestionBinding;
import vn.androidhaui.foxtrip.features.user.tour.TourDetailFragment;
import vn.androidhaui.foxtrip.models.dto.response.MarkerResponse;

public class HomeMapFragment extends Fragment {
    private FragmentHomeMapBinding binding;
    private HomeViewModel vm;
    private PointAnnotationManager pointAnnotationManager;
    private List<MarkerResponse> allMarkers = new ArrayList<>();
    private SearchSuggestionAdapter suggestionAdapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private boolean isStyleLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        vm.loadData(); // Đảm bảo làm mới dữ liệu mỗi khi vào bản đồ

        setupMap();
        setupSearch();

        binding.btnBackToList.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        vm.getDiscoveryMapLive().observe(getViewLifecycleOwner(), markers -> {
            if (markers != null) {
                this.allMarkers = markers;
                if (isStyleLoaded) {
                    plotMarkers(markers);
                }
            }
        });
    }

    private void setupSearch() {
        suggestionAdapter = new SearchSuggestionAdapter(new ArrayList<>(), m -> {
            binding.etSearch.setText("");
            binding.cvSuggestions.setVisibility(View.GONE);
            zoomToMarker(m);
        });
        binding.rvSuggestions.setAdapter(suggestionAdapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            binding.cvSuggestions.setVisibility(View.GONE);
        });
    }

    private List<String> mapQueryToLocationTypes(String query) {
        List<String> types = new ArrayList<>();
        String q = query.toLowerCase().trim();
        
        if (q.contains("ăn") || q.contains("nhà hàng") || q.contains("restaurant") || q.contains("food") || q.contains("phở") || q.contains("bún")) {
            types.add("RESTAURANT");
        }
        if (q.contains("cà phê") || q.contains("cafe") || q.contains("coffee") || q.contains("trà") || q.contains("nước")) {
            types.add("CAFE");
        }
        if (q.contains("khách sạn") || q.contains("hotel") || q.contains("resort") || q.contains("homestay") || q.contains("ngủ") || q.contains("chỗ nghỉ")) {
            types.add("HOTEL");
            types.add("HOMESTAY");
            types.add("RESORT");
        }
        if (q.contains("tham quan") || q.contains("du lịch") || q.contains("attraction") || q.contains("di tích")) {
            types.add("ATTRACTION");
            types.add("MUSEUM");
            types.add("PARK");
        }
        if (q.contains("giải trí") || q.contains("vui chơi") || q.contains("entertainment")) {
            types.add("ENTERTAINMENT");
        }
        return types;
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            binding.cvSuggestions.setVisibility(View.GONE);
            return;
        }

        List<String> mappedTypes = mapQueryToLocationTypes(query);
        List<MarkerResponse> filtered = new ArrayList<>();
        
        for (MarkerResponse m : allMarkers) {
            boolean matchName = m.title != null && m.title.toLowerCase().contains(query.toLowerCase());
            boolean matchType = false;
            
            if (m.type != null) {
                String typeStr = m.type.toUpperCase();
                for (String t : mappedTypes) {
                    if (typeStr.contains(t)) {
                        matchType = true;
                        break;
                    }
                }
            }
            
            if (matchName || matchType) {
                filtered.add(m);
            }
        }

        // Sort markers by priority descending
        java.util.Collections.sort(filtered, (m1, m2) -> {
            int p1 = m1.priority == null ? 0 : m1.priority;
            int p2 = m2.priority == null ? 0 : m2.priority;
            return Integer.compare(p2, p1);
        });

        if (!filtered.isEmpty()) {
            suggestionAdapter.updateData(filtered);
            binding.cvSuggestions.setVisibility(View.VISIBLE);
        } else {
            binding.cvSuggestions.setVisibility(View.GONE);
        }
    }

    private void zoomToMarker(MarkerResponse m) {
        binding.mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(m.lng, m.lat))
                .zoom(16.0)
                .build());

        if ("TOUR".equals(m.type) || (m.type != null && m.type.contains("TOUR"))) {
            showTourBottomSheet(m);
        } else {
            showLocationBottomSheet(m.id);
        }
    }

    private void setupMap() {
        MapView mapView = binding.mapView;
        
        // Cấu hình Compass của Mapbox xuống dưới
        CompassPlugin compassPlugin = mapView.getPlugin(Plugin.MAPBOX_COMPASS_PLUGIN_ID);
        if (compassPlugin != null) {
            compassPlugin.setEnabled(true);
            compassPlugin.updateSettings(settings -> {
                settings.setPosition(Gravity.BOTTOM | Gravity.END);
                settings.setMarginRight(20f);
                settings.setMarginBottom(300f); // Đưa lên trên nút back một chút
                return null;
            });
        }

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            style.addImage("icon-tour", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_blue)));
            style.addImage("icon-info", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_red)));
            style.addImage("icon-location", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)));
            style.addImage("icon-cafe", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_cafe)));
            style.addImage("icon-hotel", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_hotel)));
            style.addImage("icon-restaurant", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant)));
            style.addImage("icon-museum", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_museum)));
            style.addImage("icon-park", drawableToBitmap(ContextCompat.getDrawable(requireContext(), R.drawable.ic_park)));
            
            isStyleLoaded = true;
            
            AnnotationPlugin plugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
            if (plugin != null) {
                pointAnnotationManager = (PointAnnotationManager) plugin.createAnnotationManager(AnnotationType.PointAnnotation, new AnnotationConfig());
                if (!allMarkers.isEmpty()) {
                    plotMarkers(allMarkers);
                }
            }
        });

        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(Point.fromLngLat(108.2772, 14.0583))
                .zoom(5.0)
                .build());

        mapView.getMapboxMap().addOnCameraChangeListener(cameraChanged -> {
            if (isStyleLoaded) {
                plotMarkers(allMarkers);
            }
        });
    }

    private void plotMarkers(List<MarkerResponse> markers) {
        if (pointAnnotationManager == null || !isStyleLoaded) return;
        pointAnnotationManager.deleteAll();

        double currentZoom = binding.mapView.getMapboxMap().getCameraState().getZoom();

        for (MarkerResponse m : markers) {
            Point point = Point.fromLngLat(m.lng, m.lat);
            
            String typeStr = (m.type != null) ? m.type.toUpperCase().trim() : "AD";
            boolean isTour = typeStr.contains("TOUR");
            
            float iconSize = isTour ? 1.2f : (currentZoom >= 14.5 ? 1.2f : 0.4f);
            
            PointAnnotationOptions options = new PointAnnotationOptions()
                    .withPoint(point)
                    .withIconSize(iconSize);

            boolean shouldShowText = isTour || currentZoom >= 15.0;

            if (shouldShowText) {
                options.withTextField(m.title)
                        .withTextSize(11.0)
                        .withTextColor("#333333")
                        .withTextHaloColor("#FFFFFF")
                        .withTextHaloWidth(1.0)
                        .withTextOffset(new ArrayList<Double>() {{ add(0.0); add(2.2); }});
            } else {
                options.withTextField(""); 
            }

            if (isTour) {
                options.withIconImage("icon-tour");
            } else {
                if (typeStr.contains("CAFE")) options.withIconImage("icon-cafe");
                else if (typeStr.contains("HOTEL") || typeStr.contains("HOMESTAY") || typeStr.contains("RESORT")) options.withIconImage("icon-hotel");
                else if (typeStr.contains("RESTAURANT") || typeStr.contains("FOOD")) options.withIconImage("icon-restaurant");
                else if (typeStr.contains("MUSEUM")) options.withIconImage("icon-museum");
                else if (typeStr.contains("PARK")) options.withIconImage("icon-park");
                else if (typeStr.contains("ATTRACTION")) options.withIconImage("icon-location");
                else options.withIconImage("icon-info");
            }
            
            pointAnnotationManager.create(options);
        }

        pointAnnotationManager.addClickListener(annotation -> {
            for (MarkerResponse m : markers) {
                if (Double.compare(annotation.getPoint().latitude(), m.lat) == 0 &&
                        Double.compare(annotation.getPoint().longitude(), m.lng) == 0) {
                    if ("TOUR".equals(m.type) || (m.type != null && m.type.contains("TOUR"))) {
                        showTourBottomSheet(m);
                    } else {
                        showLocationBottomSheet(m.id);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private void showTourBottomSheet(MarkerResponse m) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        TourPopupBinding b = TourPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(b.getRoot());

        b.tvTourTitle.setText(m.title);
        
        if (m.averageRating == null || m.averageRating == 0) {
            b.tvTourRating.setText("★ 5.0 (Mặc định)");
        } else {
            b.tvTourRating.setText(String.format(Locale.getDefault(), "★ %.1f", m.averageRating));
        }

        if (m.discount != null && m.discount > 0) {
            double finalPrice = m.price * (1 - m.discount / 100.0);
            b.tvTourOldPrice.setVisibility(View.VISIBLE);
            b.tvTourOldPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", m.price));
            b.tvTourOldPrice.setPaintFlags(b.tvTourOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            b.tvTourPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", finalPrice));
        } else {
            b.tvTourOldPrice.setVisibility(View.GONE);
            b.tvTourPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", m.price));
        }

        Glide.with(this).load(m.thumbnailUrl).placeholder(R.drawable.ic_image_placeholder).transform(new CenterCrop()).into(b.ivTour);

        b.btnViewDetail.setOnClickListener(v -> {
            dialog.dismiss();
            Fragment detail = TourDetailFragment.newInstance(m.slug != null ? m.slug : m.id);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detail)
                    .addToBackStack(null)
                    .commit();
        });
        dialog.show();
    }

    private void showLocationBottomSheet(String locationId) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LocationPopupBinding b = LocationPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(b.getRoot());

        vm.getLocationDetail(locationId).observe(getViewLifecycleOwner(), loc -> {
            if (loc == null) {
                Toast.makeText(requireContext(), "Địa điểm không tồn tại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            b.tvLocationName.setText(loc.name);
            b.tvLocationAddress.setText(loc.address);
            b.tvLocationType.setText(loc.type);

            if (loc.contactPhone != null && !loc.contactPhone.isEmpty()) {
                b.llLocationPhone.setVisibility(View.VISIBLE);
                b.tvLocationPhone.setText(loc.contactPhone);
            } else {
                b.llLocationPhone.setVisibility(View.GONE);
            }

            Glide.with(this).load(loc.imageUrl).placeholder(R.drawable.ic_image_placeholder).transform(new CenterCrop()).into(b.ivLocation);
            
            b.btnDirections.setOnClickListener(v -> {
                dialog.dismiss();
                if (loc.lat != null && loc.lng != null) {
                    android.net.Uri uri = android.net.Uri.parse("geo:" + loc.lat + "," + loc.lng + "?q=" + loc.lat + "," + loc.lng + "(" + android.net.Uri.encode(loc.name) + ")");
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        android.net.Uri fallback = android.net.Uri.parse("https://maps.google.com/?q=" + loc.lat + "," + loc.lng);
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, fallback));
                    }
                } else {
                    Toast.makeText(requireContext(), "Không có thông tin tọa độ", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();
        
        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 48;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 48;
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onStart() { super.onStart(); if (binding != null) binding.mapView.onStart(); }
    @Override
    public void onStop() { super.onStop(); if (binding != null) binding.mapView.onStop(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); if (binding != null) binding.mapView.onLowMemory(); }
    @Override
    public void onDestroyView() { 
        super.onDestroyView(); 
        if (binding != null) {
            binding.mapView.onDestroy();
        }
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null; 
    }

    private static class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {
        private List<MarkerResponse> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(MarkerResponse item);
        }

        SearchSuggestionAdapter(List<MarkerResponse> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void updateData(List<MarkerResponse> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSearchSuggestionBinding binding = ItemSearchSuggestionBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MarkerResponse item = items.get(position);
            ItemSearchSuggestionBinding binding = holder.binding;
            binding.tvTitle.setText(item.title);
            
            String typeStr = (item.type != null) ? item.type.toUpperCase().trim() : "AD";
            if (typeStr.contains("TOUR")) {
                binding.ivIcon.setImageResource(R.drawable.ic_location_blue);
            } else {
                if (typeStr.contains("CAFE")) binding.ivIcon.setImageResource(R.drawable.ic_cafe);
                else if (typeStr.contains("HOTEL") || typeStr.contains("HOMESTAY") || typeStr.contains("RESORT")) binding.ivIcon.setImageResource(R.drawable.ic_hotel);
                else if (typeStr.contains("RESTAURANT") || typeStr.contains("FOOD")) binding.ivIcon.setImageResource(R.drawable.ic_restaurant);
                else if (typeStr.contains("MUSEUM")) binding.ivIcon.setImageResource(R.drawable.ic_museum);
                else if (typeStr.contains("PARK")) binding.ivIcon.setImageResource(R.drawable.ic_park);
                else if (typeStr.contains("ATTRACTION")) binding.ivIcon.setImageResource(R.drawable.ic_location);
                else binding.ivIcon.setImageResource(R.drawable.ic_location_red);
            }
            if (item.priority != null && item.priority > 0) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                if (item.priority == 3) {
                    binding.tvBadge.setText("HOT");
                    binding.tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF5252")));
                } else if (item.priority == 2) {
                    binding.tvBadge.setText("NICE");
                    binding.tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFB300")));
                } else {
                    binding.tvBadge.setText("OK");
                    binding.tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                }
            } else {
                binding.tvBadge.setVisibility(View.GONE);
            }

            if (item.address != null && !item.address.isEmpty()) {
                binding.tvAddress.setVisibility(View.VISIBLE);
                binding.tvAddress.setText(item.address);
            } else {
                binding.tvAddress.setVisibility(View.GONE);
            }
            
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemSearchSuggestionBinding binding;

            ViewHolder(ItemSearchSuggestionBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}