package vn.androidhaui.foxtrip.features.user.tour;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import vn.androidhaui.foxtrip.models.dto.response.PageData;
import vn.androidhaui.foxtrip.models.dto.response.TourDetailResDTO;
import vn.androidhaui.foxtrip.models.dto.response.TourReviewDTO;

public class TourDetailViewModel extends AndroidViewModel {
    private final TourRepository repository;

    public TourDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new TourRepository(application.getApplicationContext());
    }

    public LiveData<TourDetailResDTO> loadTourDetail(String slug) {
        MutableLiveData<TourDetailResDTO> data = new MutableLiveData<>();
        repository.getTourDetail(slug, data);
        return data;
    }

    public LiveData<PageData<TourReviewDTO>> loadTourReviews(String tourId) {
        MutableLiveData<PageData<TourReviewDTO>> data = new MutableLiveData<>();
        repository.getTourReviews(tourId, data);
        return data;
    }
}
