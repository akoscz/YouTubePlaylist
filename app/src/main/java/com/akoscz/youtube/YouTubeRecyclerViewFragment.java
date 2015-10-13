package com.akoscz.youtube;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akoscz.youtube.model.Playlist;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

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
 * YouTubeRecyclerViewFragment contains a RecyclerView that shows a list of YouTube video cards.
 * <p/>
 */
public class YouTubeRecyclerViewFragment extends Fragment {
    // the fragment initialization parameter
    private static final String ARG_YOUTUBE_PLAYLIST_ID = "YOUTUBE_PLAYLIST_ID";
    // key used in the saved instance bundle to persist the playlist
    private static final String KEY_SAVED_INSTANCE_PLAYLIST = "SAVED_INSTANCE_PLAYLIST";

    private String mPlaylistId;
    private RecyclerView mRecyclerView;
    private Playlist mPlaylist;
    private RecyclerView.LayoutManager mLayoutManager;
    private PlaylistCardAdapter mAdapter;
    private YouTube mYouTubeDataApi;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @param youTubeDataApi
     * @param playlistId The YouTube Playlist ID parameter string
     * @return A new instance of fragment YouTubeRecyclerViewFragment.
     */
    public static YouTubeRecyclerViewFragment newInstance(YouTube youTubeDataApi, String playlistId) {
        YouTubeRecyclerViewFragment fragment = new YouTubeRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_YOUTUBE_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        fragment.setYouTubeDataApi(youTubeDataApi);
        return fragment;
    }

    public YouTubeRecyclerViewFragment() {
        // Required empty public constructor
    }

    public void setYouTubeDataApi(YouTube api) {
        mYouTubeDataApi = api;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlaylistId = getArguments().getString(ARG_YOUTUBE_PLAYLIST_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = new Gson().toJson(mPlaylist);
        outState.putString(KEY_SAVED_INSTANCE_PLAYLIST, json);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // set the Picasso debug indicator only for debug builds
        Picasso.with(getActivity()).setIndicatorsEnabled(BuildConfig.DEBUG);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.youtube_recycler_view_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.youtube_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        Resources resources = getResources();
        if (resources.getBoolean(R.bool.isTablet)) {
            // use a staggered grid layout if we're on a large screen device
            mLayoutManager = new StaggeredGridLayoutManager(resources.getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL);
        } else {
            // use a linear layout on phone devices
            mLayoutManager = new LinearLayoutManager(getActivity());
        }

        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // restore the playlist after an orientation change
        if (savedInstanceState != null) {
            mPlaylist = new Gson().fromJson(savedInstanceState.getString(KEY_SAVED_INSTANCE_PLAYLIST), Playlist.class);
        }

        // if we have a saved playlist, use it to populate the UI right away
        if (mPlaylist != null) {
            initAdapter(mPlaylist);
        } else {
            // otherwise create an empty playlist
            mPlaylist = new Playlist(mPlaylistId);
            // populate an empty UI
            initAdapter(mPlaylist);
            // and start fetching the playlist contents
            new GetPlaylistAsyncTask(mYouTubeDataApi) {
                @Override
                public void onPostExecute(Pair<String, List<Video>> result) {
                    handleGetPlaylistResult(mPlaylist, result);
                }
            }.execute(mPlaylist.playlistId, mPlaylist.getNextPageToken());
        }
    }

    private void initAdapter(final Playlist playlist) {
        // create the adapter with our playlist and a callback to handle when we reached the last item
        mAdapter = new PlaylistCardAdapter(mPlaylist, new LastItemReachedListener() {
            @Override
            public void onLastItem(int position, String nextPageToken) {
                new GetPlaylistAsyncTask(mYouTubeDataApi) {
                    @Override
                    public void onPostExecute(Pair<String, List<Video>> result) {
                        handleGetPlaylistResult(playlist, result);
                    }
                }.execute(playlist.playlistId, playlist.getNextPageToken());
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void handleGetPlaylistResult(Playlist playlist, Pair<String, List<Video>> result) {
        if (result == null) return;
        final int positionStart = playlist.size();
        playlist.setNextPageToken(result.first);
        playlist.addAll(result.second);
        mAdapter.notifyItemRangeInserted(positionStart, result.second.size());
    }

    /**
     * Interface used by the {@link PlaylistCardAdapter} to inform us that we reached the last item in the list.
     */
    public interface LastItemReachedListener {
        void onLastItem(int position, String nextPageToken);
    }
}
