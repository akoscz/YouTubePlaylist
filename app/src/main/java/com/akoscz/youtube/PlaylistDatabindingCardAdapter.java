package com.akoscz.youtube;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akoscz.youtube.BR;
import com.akoscz.youtube.model.Playlist;
import com.google.api.services.youtube.model.Video;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class PlaylistDatabindingCardAdapter extends RecyclerView.Adapter<PlaylistDatabindingCardAdapter.BindingHolder> {
    private final YouTubeRecyclerViewFragment.LastItemReachedListener mListener;
    private Playlist mPlaylist;

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    public PlaylistDatabindingCardAdapter(Playlist playlist, YouTubeRecyclerViewFragment.LastItemReachedListener lastItemReachedListener) {
        mPlaylist = playlist;
        mListener = lastItemReachedListener;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.youtube_databinding_video_card, parent, false);
        BindingHolder holder = new BindingHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, final int position) {
        final Video video = mPlaylist.get(position);
        holder.getBinding().setVariable(BR.video, video);
        holder.getBinding().executePendingBindings();

        if (mListener != null) {
            // get the next playlist page if we're at the end of the current page and we have another page to get
            final String nextPageToken = mPlaylist.getNextPageToken();
            if (!isEmpty(nextPageToken) && position == mPlaylist.size() - 1) {
                holder.itemView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onLastItem(position, nextPageToken);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPlaylist.size();
    }

    private boolean isEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }
}