package vn.androidhaui.foxtrip.features.user.chatbot;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import vn.androidhaui.foxtrip.models.domain.ChatMessage;
import vn.androidhaui.foxtrip.models.dto.response.ChatResponse;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<ChatResponse> botReply = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChatRepository(application);
    }

    public LiveData<ChatResponse> getBotReply() { return botReply; }
    public LiveData<String> getError() { return error; }

    public void sendMessage(String message, List<ChatMessage> history) {
        repository.sendMessage(message, history, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(ChatResponse response) {
                botReply.postValue(response);
            }

            @Override
            public void onError(String err) {
                error.postValue(err);
            }
        });
    }
}
