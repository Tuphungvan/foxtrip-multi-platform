package vn.androidhaui.foxtrip.features.user.home;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentHomeBinding;
import vn.androidhaui.foxtrip.features.user.auth.AuthViewModel;
import vn.androidhaui.foxtrip.features.user.auth.LoginFragment;
import vn.androidhaui.foxtrip.features.user.tour.TourCardAdapter;
import vn.androidhaui.foxtrip.features.user.tour.TourDetailFragment;
import vn.androidhaui.foxtrip.ui.MainActivity;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import vn.androidhaui.foxtrip.databinding.DialogFilterBinding;
import vn.androidhaui.foxtrip.utils.ProvinceUtils;
import vn.androidhaui.foxtrip.utils.PriceTextWatcher;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.util.Date;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel vm;
    private AuthViewModel authViewModel;
    private TourCardAdapter adapterDiscounted;
    private TourCardAdapter adapterUpcoming;
    private TourCardAdapter adapterSearch;

    // Filter states
    private String selectedProvince = null;
    private String selectedCategory = null;
    private Double priceMin = null;
    private Double priceMax = null;
    private Date startDate = null;
    private Date endDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupAuthCheck();
        setupRecycler();
        setupSearch();
        setupFilter();
        setupHideKeyboard();

        binding.btnToggleView.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeMapFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 1. Observe Home Data
        vm.getUpcomingToursLive().observe(getViewLifecycleOwner(), tours -> {
            if (Boolean.FALSE.equals(vm.getIsSearchingLive().getValue())) {
                adapterUpcoming.submitList(tours);
            }
        });

        vm.getDiscountedToursLive().observe(getViewLifecycleOwner(), currentDiscounts -> {
            if (Boolean.FALSE.equals(vm.getIsSearchingLive().getValue())) {
                adapterDiscounted.submitList(currentDiscounts);
            }
        });

        // 2. Observe Search Results
        vm.getSearchResultLive().observe(getViewLifecycleOwner(), tours -> {
            if (Boolean.TRUE.equals(vm.getIsSearchingLive().getValue())) {
                if (tours != null) {
                    if (tours.isEmpty()) {
                        Toast.makeText(requireContext(), "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                    }
                    adapterSearch.submitList(new java.util.ArrayList<>(tours));
                }
            }
        });

        // 3. Observe Search State change
        vm.getIsSearchingLive().observe(getViewLifecycleOwner(), isSearching -> {
            if (isSearching) {
                binding.layoutNormalMode.setVisibility(View.GONE);
                binding.layoutSearchMode.setVisibility(View.VISIBLE);
            } else {
                binding.layoutNormalMode.setVisibility(View.VISIBLE);
                binding.layoutSearchMode.setVisibility(View.GONE);
                binding.searchInput.setText("");
                selectedProvince = null;
                selectedCategory = null;
                priceMin = null;
                priceMax = null;
                startDate = null;
                endDate = null;
                // Refresh home lists
                adapterUpcoming.submitList(vm.getUpcomingToursLive().getValue());
                adapterDiscounted.submitList(vm.getDiscountedToursLive().getValue());
            }
        });

        binding.btnSeeAll.setOnClickListener(v -> {
            vm.searchTours("", null, null, null, null, null, null, false);
        });

        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Check if scrolling near bottom
            if (scrollY >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight() - 100)) {
                if (Boolean.TRUE.equals(vm.getIsSearchingLive().getValue())) {
                    vm.loadMore();
                }
            }
        });
    }

    private void setupAuthCheck() {
        authViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                binding.layoutNotLoggedIn.setVisibility(View.VISIBLE);
                binding.btnLoginHome.setOnClickListener(
                        v -> ((MainActivity) requireActivity()).loadFragment(new LoginFragment(), false));
            } else {
                binding.layoutNotLoggedIn.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecycler() {
        // 1. Discounted
        adapterDiscounted = new TourCardAdapter(requireContext());
        adapterDiscounted.setOnClickListener(tour -> openDetail(tour.slug));
        binding.rvDiscounted.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvDiscounted.setAdapter(adapterDiscounted);

        // 2. Upcoming
        adapterUpcoming = new TourCardAdapter(requireContext());
        adapterUpcoming.setOnClickListener(tour -> openDetail(tour.slug));
        binding.rvUpcoming.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvUpcoming.setAdapter(adapterUpcoming);

        // 3. Search
        adapterSearch = new TourCardAdapter(requireContext());
        adapterSearch.setOnClickListener(tour -> openDetail(tour.slug));
        binding.rvSearch.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvSearch.setAdapter(adapterSearch);
    }

    private void openDetail(String slug) {
        ((MainActivity) requireActivity()).loadFragment(TourDetailFragment.newInstance(slug), false);
    }

    private void setupSearch() {
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                hideKeyboard(v);
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String q = binding.searchInput.getText().toString().trim();
        if (q.isEmpty() && selectedProvince == null && selectedCategory == null && priceMin == null
                && priceMax == null) {
            vm.clearSearch();
        } else {
            String startStr = DateTimeUtils.formatForBackend(startDate);
            String endStr = DateTimeUtils.formatForBackend(endDate);
            vm.searchTours(q, selectedProvince, selectedCategory, priceMin, priceMax, startStr, endStr, false);
        }
    }

    private void setupFilter() {
        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogFilterBinding dfb = DialogFilterBinding.inflate(getLayoutInflater());
        dialog.setContentView(dfb.getRoot());

        // Restore current states
        if (selectedProvince != null)
            dfb.btnChooseProvince.setText(ProvinceUtils.getProvinceDisplay(selectedProvince));
        if (selectedCategory != null)
            dfb.btnChooseCategory.setText(ProvinceUtils.getCategoryDisplay(selectedCategory));
        if (priceMin != null)
            dfb.etPriceMin.setText(String.valueOf(priceMin.intValue()));
        if (priceMax != null)
            dfb.etPriceMax.setText(String.valueOf(priceMax.intValue()));
        if (startDate != null)
            dfb.btnStartDate.setText(DateTimeUtils.toDisplayDate(startDate));
        if (endDate != null)
            dfb.btnEndDate.setText(DateTimeUtils.toDisplayDate(endDate));

        dfb.btnChooseProvince.setOnClickListener(v -> {
            List<String> displayNames = ProvinceUtils.getAllProvinceDisplays();
            String[] items = displayNames.toArray(new String[0]);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn tỉnh thành")
                    .setItems(items, (d, which) -> {
                        String selectedDisplay = items[which];
                        selectedProvince = ProvinceUtils.getProvinceEnum(selectedDisplay);
                        dfb.btnChooseProvince.setText(selectedDisplay);
                    }).show();
        });

        dfb.etPriceMin.addTextChangedListener(new PriceTextWatcher(dfb.etPriceMin));
        dfb.etPriceMax.addTextChangedListener(new PriceTextWatcher(dfb.etPriceMax));

        dfb.btnChooseCategory.setOnClickListener(v -> {
            List<String> displayNames = ProvinceUtils.getAllCategoryDisplays();
            String[] items = displayNames.toArray(new String[0]);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn loại hình")
                    .setItems(items, (d, which) -> {
                        String selectedDisplay = items[which];
                        selectedCategory = ProvinceUtils.getCategoryEnum(selectedDisplay);
                        dfb.btnChooseCategory.setText(selectedDisplay);
                    }).show();
        });

        dfb.btnStartDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày bắt đầu")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                startDate = new Date(selection);
                dfb.btnStartDate.setText(DateTimeUtils.toDisplayDate(startDate));
            });
            picker.show(getChildFragmentManager(), "START_DATE_PICKER");
        });

        dfb.btnEndDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày kết thúc")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                endDate = new Date(selection);
                dfb.btnEndDate.setText(DateTimeUtils.toDisplayDate(endDate));
            });
            picker.show(getChildFragmentManager(), "END_DATE_PICKER");
        });

        dfb.btnClearFilter.setOnClickListener(v -> {
            selectedProvince = null;
            selectedCategory = null;
            priceMin = null;
            priceMax = null;
            startDate = null;
            endDate = null;
            dfb.btnChooseProvince.setText(R.string.btn_selected_province);
            dfb.btnChooseCategory.setText(R.string.btn_selected_category);
            dfb.btnStartDate.setText(R.string.btn_start);
            dfb.btnEndDate.setText(R.string.btn_end);
            dfb.etPriceMin.setText("");
            dfb.etPriceMax.setText("");
            vm.clearSearch();
            dialog.dismiss();
        });

        dfb.btnApplyFilter.setOnClickListener(v -> {
            String pMinStr = PriceTextWatcher.getCleanString(dfb.etPriceMin.getText().toString().trim());
            String pMaxStr = PriceTextWatcher.getCleanString(dfb.etPriceMax.getText().toString().trim());
            priceMin = pMinStr.isEmpty() ? null : Double.valueOf(pMinStr);
            priceMax = pMaxStr.isEmpty() ? null : Double.valueOf(pMaxStr);

            performSearch();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupHideKeyboard() {
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focus = requireActivity().getCurrentFocus();
                if (focus instanceof EditText) {
                    focus.clearFocus();
                    hideKeyboard(focus);
                }
            }
            v.performClick();
            return false;
        });
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        if (imm != null)
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
