package com.akoscz.youtube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

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
 * <p/>
 * <p/>
 * YouTubeListViewFragment which contains a list view of YouTube video cards
 */
public class YouTubeListViewFragment extends Fragment {
    private static final String TAG = "YouTubeListViewFragment";

    private static final String YOUTUBE_PLAYLIST = "PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0";
    private static final String PLAYLIST_KEY = "PLAYLIST_KEY";
    private ListView mListView;
    private Playlist mPlaylist;
    private PlaylistAdapter mAdapter;

    public YouTubeListViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Picasso.with(getActivity()).setIndicatorsEnabled(BuildConfig.DEBUG);

        View rootView = inflater.inflate(R.layout.youtube_fragment, container, false);

        mListView = (ListView) rootView.findViewById(R.id.youtube_listview);

        // restore the playlist after an orientation change
        if (savedInstanceState != null) {
            mPlaylist = new Gson().fromJson(savedInstanceState.getString(PLAYLIST_KEY), Playlist.class);
        }

        // ensure the adapter and listview are initialized
        if (mPlaylist != null) {
            initListAdapter(mPlaylist);
        }

        // start loading the first page of our playlist
        new GetYouTubePlaylistAsyncTask() {
            @Override
            public void onPostExecute(JSONObject result) {
                if (result == null) return;

                handlePlaylistResult(result);
            }
        }.execute(YOUTUBE_PLAYLIST, null);

        return rootView;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = new Gson().toJson(mPlaylist);
        outState.putString(PLAYLIST_KEY, json);
    }

    private void initListAdapter(Playlist playlist) {
        mAdapter = new PlaylistAdapter(playlist);
        mListView.setAdapter(mAdapter);
    }

    private void handlePlaylistResult(JSONObject result) {
        try {
            if (mPlaylist == null) {
                mPlaylist = new Playlist(result);
                initListAdapter(mPlaylist);
            }

            final Playlist.Page page = mPlaylist.addPage(result);

            // fetch all the video details for the current page of Playlist Items
            new GetYouTubeVideoAsyncTask() {

                @Override
                public void onPostExecute(JSONObject result) {
                    if (result == null) {
                        return;
                    }

                    try {
                        JSONArray resultItems = result.getJSONArray("items");
                        PlaylistItem playlistItem;
                        for (int i = 0; i < page.items.size(); i++) {
                            playlistItem = page.items.get(i);
                            playlistItem.video = new Video(resultItems.getJSONObject(i));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // make sure the UI gets updated
                    mAdapter.notifyDataSetChanged();
                }
            }.execute(page);

            if (!mAdapter.setIsLoading(false)) {
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected class PlaylistAdapter extends BaseAdapter {
        private final DecimalFormat formatter = new DecimalFormat("#,###,###");
        private final LayoutInflater mInflater;
        private Playlist mPlaylist;
        private boolean mIsLoading = false;

        PlaylistAdapter(Playlist playlist) {
            mPlaylist = playlist;
            mInflater = getLayoutInflater(null);
        }

        /**
         * @param isLoading
         * @return True if the adapter was notified that data set has changed, false otherwise
         */
        public boolean setIsLoading(boolean isLoading) {
            if (mIsLoading != isLoading) {
                mIsLoading = isLoading;
                notifyDataSetChanged();
                return true;
            }
            return false;
        }

        @Override
        public int getCount() {
            return mPlaylist.getCount() + (mIsLoading ? 1 : 0);
        }

        @Override
        public PlaylistItem getItem(int i) {
            return mPlaylist.getItem(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (mIsLoading && position == (getCount() - 1)) {
                return mInflater.inflate(R.layout.youtube_video_list_item_loading, null, false);
            }

            ViewHolder viewHolder;

            if (convertView == null || convertView.getTag() == null) {

                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.youtube_video_list_item, null, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.video_title);
                viewHolder.description = (TextView) convertView.findViewById(R.id.video_description);
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                viewHolder.share = (ImageView) convertView.findViewById(R.id.video_share);
                viewHolder.shareText = (TextView) convertView.findViewById(R.id.video_share_text);
                viewHolder.duration = (TextView) convertView.findViewById(R.id.video_dutation_text);
                viewHolder.viewCount= (TextView) convertView.findViewById(R.id.video_view_count);
                viewHolder.likeCount = (TextView) convertView.findViewById(R.id.video_like_count);
                viewHolder.dislikeCount = (TextView) convertView.findViewById(R.id.video_dislike_count);
                convertView.setTag(viewHolder);
            }

            viewHolder = (ViewHolder) convertView.getTag();

            final PlaylistItem item = getItem(position);
            viewHolder.title.setText(item.title);
            viewHolder.description.setText(item.description);

            // load the video thumbnail image
            Picasso.with(getActivity())
                    .load(item.thumbnailUrl)
                    .into(viewHolder.thumbnail);

            // set the click listener to play the video
            viewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + item.videoId)));

                }
            });

            // create and set the click listener for both the share icon and share text
            View.OnClickListener shareClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch \"" + item.title + "\" on YouTube");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + item.videoId);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            };
            viewHolder.share.setOnClickListener(shareClickListener);
            viewHolder.shareText.setOnClickListener(shareClickListener);

            if (item.video != null) {
                // set the video duration text
                viewHolder.duration.setText(item.video.duration);
                // set the video statistics
                viewHolder.viewCount.setText(formatter.format(item.video.viewCount));
                viewHolder.likeCount.setText(formatter.format(item.video.likeCount));
                viewHolder.dislikeCount.setText(formatter.format(item.video.dislikeCount));
            }

            // get the next playlist page if we're at the end of the current page and we have another page to get
            final String nextPageToken = mPlaylist.getNextPageToken(position);
            if (!isEmpty(nextPageToken) && position == getCount() - 1) {
                new GetYouTubePlaylistAsyncTask() {
                    @Override
                    public void onPostExecute(JSONObject result) {
                        handlePlaylistResult(result);
                    }
                }.execute(YOUTUBE_PLAYLIST, nextPageToken);

                setIsLoading(true);
            }

            return convertView;
        }

        private boolean isEmpty(String s) {
            if (s == null || s.length() == 0) {
                return true;
            }
            return false;
        }

        class ViewHolder {
            ImageView thumbnail;
            TextView title;
            TextView description;
            ImageView share;
            TextView shareText;
            TextView duration;
            TextView viewCount;
            TextView likeCount;
            TextView dislikeCount;
        }
    }
}
