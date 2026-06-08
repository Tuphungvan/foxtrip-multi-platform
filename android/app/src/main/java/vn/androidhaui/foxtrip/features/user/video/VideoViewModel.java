package vn.androidhaui.foxtrip.features.user.video;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import vn.androidhaui.foxtrip.models.dto.response.TourVideoCardResponse;

public class VideoViewModel extends AndroidViewModel {
    private final VideoRepository repo;
    private final MutableLiveData<List<TourVideoCardResponse>> videos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public VideoViewModel(@NonNull Application application) {
        super(application);
        this.repo = new VideoRepository(application);
    }

    public LiveData<List<TourVideoCardResponse>> getVideos() { return videos; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadVideos() {
        loading.setValue(true);
        repo.getShortVideos(new VideoRepository.CallbackResult<List<TourVideoCardResponse>>() {
            @Override
            public void onSuccess(List<TourVideoCardResponse> result) {
                loading.postValue(false);
                if (result != null) {
                    Collections.shuffle(result);
                }
                videos.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }
}
