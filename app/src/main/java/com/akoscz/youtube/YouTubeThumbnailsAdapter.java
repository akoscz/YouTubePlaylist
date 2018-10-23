package com.akoscz.youtube;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.squareup.picasso.Picasso;

import java.util.List;

public class YouTubeThumbnailsAdapter extends RecyclerView.Adapter<YouTubeThumbnailsAdapter.ViewHolder> {

    private final Context _context;
    private final List<Video> _playlistVideos;

    @SuppressWarnings("WeakerAccess")
    public YouTubeThumbnailsAdapter(Context context, List<Video> playlistVideos) {
        _context = context;
        _playlistVideos = playlistVideos;
    }

    @NonNull
    @Override
    public YouTubeThumbnailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(_context);
        View v = inflater.inflate(R.layout.youtube_video_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Video video = _playlistVideos.get(position);
        final VideoSnippet videoSnippet = video.getSnippet();

        Picasso.with(_context)
                .load(videoSnippet.getThumbnails().getMedium().getUrl())
                .placeholder(R.drawable.video_placeholder)
                .into(holder._youTubeThumbnail);
    }

    @Override
    public int getItemCount() {
        return _playlistVideos.size();
    }

    @SuppressWarnings("WeakerAccess")
    static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView _youTubeThumbnail;

        public ViewHolder(View v) {
            super(v);
            _youTubeThumbnail = v.findViewById(R.id.youtube_thumbnail);
        }

    }

}
