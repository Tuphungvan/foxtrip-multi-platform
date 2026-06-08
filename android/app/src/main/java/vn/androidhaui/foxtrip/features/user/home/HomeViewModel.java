package vn.androidhaui.foxtrip.features.user.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.models.dto.response.LocationResponse;
import vn.androidhaui.foxtrip.models.dto.response.TourCardResponse;
import vn.androidhaui.foxtrip.models.dto.response.MarkerResponse;

public class HomeViewModel extends AndroidViewModel {
    private final HomeRepository repo;

    private final MutableLiveData<List<TourCardResponse>> upcomingToursLive = new MutableLiveData<>();
    private final MutableLiveData<List<TourCardResponse>> discountedToursLive = new MutableLiveData<>();
    private final MutableLiveData<List<MarkerResponse>> discoveryMapLive = new MutableLiveData<>();
    private final MutableLiveData<List<TourCardResponse>> searchResultLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearchingLive = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoadingMoreLive = new MutableLiveData<>(false);

    // Pagination & Current Filter state
    private int currentPage = 0;
    private boolean isLastPage = false;
    private String currentQuery, currentProvince, currentCategory, currentStartDate, currentEndDate;
    private Double currentPriceFrom, currentPriceTo;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repo = new HomeRepository(application.getApplicationContext());
        loadData();
    }

    public void loadData() {
        repo.getUpcomingTours(upcomingToursLive);
        repo.getDiscountedTours(discountedToursLive);
        repo.getDiscoveryMap(discoveryMapLive);
    }

    public LiveData<List<TourCardResponse>> getUpcomingToursLive() {
        return upcomingToursLive;
    }

    public LiveData<List<TourCardResponse>> getDiscountedToursLive() {
        return discountedToursLive;
    }

    public LiveData<List<MarkerResponse>> getDiscoveryMapLive() {
        return discoveryMapLive;
    }

    public LiveData<List<TourCardResponse>> getSearchResultLive() {
        return searchResultLive;
    }

    public LiveData<Boolean> getIsSearchingLive() {
        return isSearchingLive;
    }

    public LiveData<Boolean> getIsLoadingMoreLive() {
        return isLoadingMoreLive;
    }

    public void searchTours(String q, String province, String category, Double priceFrom, Double priceTo, String startDate, String endDate, boolean isLoadMore) {
        if (isLoadMore) {
            if (isLastPage || Boolean.TRUE.equals(isLoadingMoreLive.getValue())) return;
            currentPage++;
            isLoadingMoreLive.setValue(true);
        } else {
            currentPage = 0;
            isLastPage = false;
            isSearchingLive.setValue(true);
            // Save current filters for loadMore
            currentQuery = q; currentProvince = province; currentCategory = category;
            currentPriceFrom = priceFrom; currentPriceTo = priceTo;
            currentStartDate = startDate; currentEndDate = endDate;
        }

        repo.searchTours(q, province, category, priceFrom, priceTo, startDate, endDate, currentPage, new MutableLiveData<List<TourCardResponse>>() {
            @Override
            public void postValue(List<TourCardResponse> value) {
                isLoadingMoreLive.postValue(false);
                if (value == null || value.isEmpty()) {
                    isLastPage = true;
                    if (!isLoadMore) searchResultLive.postValue(new ArrayList<>());
                    return;
                }
                
                if (isLoadMore) {
                    List<TourCardResponse> currentList = searchResultLive.getValue();
                    if (currentList == null) currentList = new ArrayList<>();
                    else currentList = new ArrayList<>(currentList);
                    currentList.addAll(value);
                    searchResultLive.postValue(currentList);
                } else {
                    searchResultLive.postValue(value);
                }
                
                if (value.size() < 20) { // Giả sử size=20 như trong Repo
                    isLastPage = true;
                }
            }
        });
    }

    public void loadMore() {
        if (Boolean.TRUE.equals(isSearchingLive.getValue())) {
            searchTours(currentQuery, currentProvince, currentCategory, currentPriceFrom, currentPriceTo, currentStartDate, currentEndDate, true);
        }
    }

    public void clearSearch() {
        isSearchingLive.setValue(false);
        searchResultLive.setValue(null);
    }

    public LiveData<LocationResponse> getLocationDetail(String id) {
        MutableLiveData<LocationResponse> result = new MutableLiveData<>();
        repo.getLocationDetail(id, result, discoveryMapLive);
        return result;
    }
}
