package vn.androidhaui.foxtrip.features.user.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon;
import java.util.List;

import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemMessageBotBinding;
import vn.androidhaui.foxtrip.databinding.ItemMessageUserBinding;
import vn.androidhaui.foxtrip.features.user.tour.TourCardAdapter;
import vn.androidhaui.foxtrip.features.user.tour.TourDetailFragment;
import vn.androidhaui.foxtrip.models.domain.ChatMessage;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOT = 0;
    private static final int TYPE_USER = 1;

    private final List<ChatMessage> messages;
    private Markwon markwon;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (markwon == null) {
            markwon = Markwon.builder(parent.getContext())
                .usePlugin(TablePlugin.create(parent.getContext()))
                .usePlugin(StrikethroughPlugin.create())
                .build();
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            ItemMessageUserBinding binding = ItemMessageUserBinding.inflate(inflater, parent, false);
            return new UserHolder(binding);
        } else {
            ItemMessageBotBinding binding = ItemMessageBotBinding.inflate(inflater, parent, false);
            return new BotHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof UserHolder) {
            ItemMessageUserBinding binding = ((UserHolder) holder).binding;
            binding.txtMessage.setText(msg.getMessage());
        } else {
            BotHolder h = (BotHolder) holder;
            ItemMessageBotBinding binding = h.binding;
            markwon.setMarkdown(binding.txtMessage, msg.getMessage());
            
            // Render Tours
            if (msg.getTours() != null && !msg.getTours().isEmpty()) {
                binding.recyclerRelatedTours.setVisibility(View.VISIBLE);
                binding.recyclerRelatedTours.setLayoutManager(new LinearLayoutManager(h.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                TourCardAdapter tourAdapter = new TourCardAdapter(h.itemView.getContext());
                tourAdapter.setHorizontal(true);
                tourAdapter.submitList(msg.getTours());
                tourAdapter.setOnClickListener(tour -> {
                    if (h.itemView.getContext() instanceof MainActivity) {
                        ((MainActivity) h.itemView.getContext()).loadFragment(
                                TourDetailFragment.newInstance(tour.slug), true
                        );
                    }
                });
                binding.recyclerRelatedTours.setAdapter(tourAdapter);
            } else {
                binding.recyclerRelatedTours.setVisibility(View.GONE);
            }

            // Render Locations
            if (msg.getLocations() != null && !msg.getLocations().isEmpty()) {
                binding.recyclerRelatedLocations.setVisibility(View.VISIBLE);
                binding.recyclerRelatedLocations.setLayoutManager(new LinearLayoutManager(h.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                LocationCardAdapter locationAdapter = new LocationCardAdapter(msg.getLocations(), loc -> {
                    // Navigate to location detail if exists
                });
                binding.recyclerRelatedLocations.setAdapter(locationAdapter);
            } else {
                binding.recyclerRelatedLocations.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        final ItemMessageUserBinding binding;
        UserHolder(@NonNull ItemMessageUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class BotHolder extends RecyclerView.ViewHolder {
        final ItemMessageBotBinding binding;
        BotHolder(@NonNull ItemMessageBotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
