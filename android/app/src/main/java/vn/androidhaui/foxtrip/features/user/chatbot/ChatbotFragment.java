package vn.androidhaui.foxtrip.features.user.chatbot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.databinding.FragmentChatbotBinding;
import vn.androidhaui.foxtrip.features.user.auth.LoginFragment;
import vn.androidhaui.foxtrip.models.domain.ChatMessage;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class ChatbotFragment extends Fragment {

    private FragmentChatbotBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatViewModel viewModel;

    private static final String PREFS_CHAT = "chat_prefs";
    private static final String KEY_CHAT_HISTORY = "chat_history";
    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatbotBinding.inflate(inflater, container, false);
        
        if (checkLogin()) {
            binding.layoutChatContent.setVisibility(View.VISIBLE);
            binding.layoutLoginRequired.setVisibility(View.GONE);
            setupRecycler();
            setupViewModel();
            setupListeners();
            loadLocalHistory();
        } else {
            binding.layoutChatContent.setVisibility(View.GONE);
            binding.layoutLoginRequired.setVisibility(View.VISIBLE);
            binding.btnLogin.setOnClickListener(v -> {
                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).loadFragment(new LoginFragment(), false);
                }
            });
        }
        
        return binding.getRoot();
    }

    private boolean checkLogin() {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return prefs.getString("accessToken", null) != null;
    }

    private void setupRecycler() {
        adapter = new ChatAdapter(messages);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerChat.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.getBotReply().observe(getViewLifecycleOwner(), reply -> {
            if (reply != null) {
                ChatMessage botMsg = new ChatMessage(reply.getMessage(), false, System.currentTimeMillis(), reply.getTours(), reply.getLocations());
                messages.add(botMsg);
                adapter.notifyItemInserted(messages.size() - 1);
                binding.recyclerChat.scrollToPosition(messages.size() - 1);

                saveLocalHistory();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) addMessage("❌ " + err, false);
        });
    }

    private void setupListeners() {
        binding.btnSend.setOnClickListener(v -> sendMessage());

        binding.inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String msg = binding.inputMessage.getText() != null
                ? binding.inputMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(msg)) return;

        addMessage(msg, true);
        binding.inputMessage.setText("");

        viewModel.sendMessage(msg, new ArrayList<>(messages));
        saveLocalHistory();
    }

    private void addMessage(String message, boolean isUser) {
        messages.add(new ChatMessage(message, isUser, System.currentTimeMillis()));
        adapter.notifyItemInserted(messages.size() - 1);
        binding.recyclerChat.scrollToPosition(messages.size() - 1);
    }

    private void saveLocalHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_CHAT, Context.MODE_PRIVATE);
        String json = gson.toJson(messages);
        prefs.edit().putString(KEY_CHAT_HISTORY, json).apply();
    }

    private void loadLocalHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_CHAT, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CHAT_HISTORY, null);
        if (json != null) {
            List<ChatMessage> history = gson.fromJson(json, new TypeToken<List<ChatMessage>>(){}.getType());
            if (history != null) {
                messages.clear();
                messages.addAll(history);
                adapter.notifyDataSetChanged();
                binding.recyclerChat.scrollToPosition(messages.size() - 1);
            }
        }
    }

    public static void clearChatHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CHAT, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_CHAT_HISTORY).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
