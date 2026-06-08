package vn.androidhaui.foxtrip.features.user.video;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemShortMiniBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourVideoCardResponse;
import vn.androidhaui.foxtrip.utils.YouTubeUtils;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<TourVideoCardResponse> videoList = new ArrayList<>();
    private OnTourClickListener listener;

    public interface OnTourClickListener {
        void onTourClick(TourVideoCardResponse video);
    }

    public void setOnTourClickListener(OnTourClickListener listener) {
        this.listener = listener;
    }

    public void setVideos(List<TourVideoCardResponse> videos) {
        this.videoList = videos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShortMiniBinding binding = ItemShortMiniBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VideoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(videoList.get(position));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // Pause video when it's not visible
        holder.pauseVideo();
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        final ItemShortMiniBinding binding;
        YouTubePlayer activePlayer;
        String videoId;
        boolean isInitialized = false;

        public VideoViewHolder(@NonNull ItemShortMiniBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // CRITICAL: Initialize ONLY ONCE here
            binding.youtubePlayerView.initialize(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    activePlayer = youTubePlayer;
                    isInitialized = true;
                    if (videoId != null) {
                        youTubePlayer.cueVideo(videoId, 0);
                    }
                }
            });
        }

        void bind(TourVideoCardResponse video) {
            binding.tvTourName.setText(video.getTitle());
            binding.tvTourPrice.setText(String.format("%,.0fđ", video.getFinalPrice()));
            
            Glide.with(itemView.getContext())
                    .load(video.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivTour);

            this.videoId = YouTubeUtils.extractVideoId(video.getShortId());

            // If already initialized, update the video
            if (isInitialized && activePlayer != null && videoId != null) {
                activePlayer.cueVideo(videoId, 0);
            }
            
            binding.tourCard.setOnClickListener(v -> {
                if (listener != null) listener.onTourClick(video);
            });
        }

        public void playVideo() {
            if (activePlayer != null && isInitialized) {
                activePlayer.play();
            }
        }

        public void pauseVideo() {
            if (activePlayer != null && isInitialized) {
                activePlayer.pause();
            }
        }
    }
}
