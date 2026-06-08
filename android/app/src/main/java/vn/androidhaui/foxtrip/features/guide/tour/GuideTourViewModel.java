package vn.androidhaui.foxtrip.features.guide.tour;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import vn.androidhaui.foxtrip.models.dto.response.PageData;
import vn.androidhaui.foxtrip.models.dto.response.PassengerResDTO;
import vn.androidhaui.foxtrip.models.dto.response.TourDetailResDTO;
import vn.androidhaui.foxtrip.models.dto.response.TourListResDTO;

public class GuideTourViewModel extends AndroidViewModel {
    private final GuideTourRepository repository;

    private final MutableLiveData<List<TourListResDTO>> tours = new MutableLiveData<>();
    private final MutableLiveData<TourDetailResDTO> tourDetail = new MutableLiveData<>();
    private final MutableLiveData<List<PassengerResDTO>> passengers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> checkInSuccess = new MutableLiveData<>();

    public GuideTourViewModel(@NonNull Application application) {
        super(application);
        repository = new GuideTourRepository(application);
    }

    public LiveData<List<TourListResDTO>> getTours() { return tours; }
    public LiveData<TourDetailResDTO> getTourDetail() { return tourDetail; }
    public LiveData<List<PassengerResDTO>> getPassengers() { return passengers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getCheckInSuccess() { return checkInSuccess; }

    public void loadMyTours() {
        loading.setValue(true);
        repository.fetchMyTours(0, 100, new GuideTourRepository.CallbackResult<PageData<TourListResDTO>>() {
            @Override
            public void onSuccess(PageData<TourListResDTO> result) {
                loading.setValue(false);
                tours.setValue(result.items);
            }

            @Override
            public void onError(String err) {
                loading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void loadTourDetail(String tourId) {
        loading.setValue(true);
        repository.fetchTourDetail(tourId, new GuideTourRepository.CallbackResult<TourDetailResDTO>() {
            @Override
            public void onSuccess(TourDetailResDTO result) {
                loading.setValue(false);
                tourDetail.setValue(result);
            }

            @Override
            public void onError(String err) {
                loading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void loadPassengers(String tourId) {
        loading.setValue(true);
        repository.fetchPassengers(tourId, new GuideTourRepository.CallbackResult<List<PassengerResDTO>>() {
            @Override
            public void onSuccess(List<PassengerResDTO> result) {
                loading.setValue(false);
                passengers.setValue(result);
            }

            @Override
            public void onError(String err) {
                loading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void checkIn(String tourId, String orderCode) {
        loading.setValue(true);
        repository.checkIn(tourId, orderCode, new GuideTourRepository.CallbackResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.setValue(false);
                checkInSuccess.setValue(true);
                // Reload passengers to reflect status change
                loadPassengers(tourId);
            }

            @Override
            public void onError(String err) {
                loading.setValue(false);
                error.setValue(err);
                checkInSuccess.setValue(false);
            }
        });
    }

    public void clearError() { error.setValue(null); }
    public void resetCheckInStatus() { checkInSuccess.setValue(null); }
}
