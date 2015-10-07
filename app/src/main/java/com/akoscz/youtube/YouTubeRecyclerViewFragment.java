package com.akoscz.youtube;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akoscz.youtube.model.Playlist;
import com.akoscz.youtube.model.PlaylistItem;
import com.akoscz.youtube.model.Video;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private LinearLayoutManager mLayoutManager;
    private PlaylistCardAdapter mAdapter;
    private boolean mIsLoading;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param playlistId The YouTube Playlist ID parameter string
     * @return A new instance of fragment YouTubeRecyclerViewFragment.
     */
    public static YouTubeRecyclerViewFragment newInstance(String playlistId) {
        YouTubeRecyclerViewFragment fragment = new YouTubeRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_YOUTUBE_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    public YouTubeRecyclerViewFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        Picasso.with(getActivity()).setIndicatorsEnabled(BuildConfig.DEBUG);

        View rootView = inflater.inflate(R.layout.youtube_recycler_view_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.youtube_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());

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

        // if we have a saved playlist, ensure the adapter is initialized
        if (mPlaylist != null) {
            initAdapter(mPlaylist);
        } else {
            // otherwise start loading the first page of our playlist
            new GetYouTubePlaylistAsyncTask() {
                @Override
                public void onPostExecute(JSONObject result) {
                    if (result == null) return;
                    handlePlaylistResult(result);
                }
            }.execute(mPlaylistId, null);
        }
    }

    private void initAdapter(final Playlist mPlaylist) {
        // create the adapter with our playlist and a callback to handle when we reached the last item
        mAdapter = new PlaylistCardAdapter(mPlaylist, new LastItemReachedListener() {
            @Override
            public void onLastItem(int position, String nextPageToken) {
                new GetYouTubePlaylistAsyncTask() {
                    @Override
                    public void onPostExecute(JSONObject result) {
                        handlePlaylistResult(result);
                    }
                }.execute(mPlaylistId, nextPageToken);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void handlePlaylistResult(JSONObject result) {
        try {
            if (mPlaylist == null) {
                mPlaylist = new Playlist(result);
                initAdapter(mPlaylist);
            }

            final Playlist.Page page = mPlaylist.addPage(result);
            final int itemsPerPage = page.items.size();
            final int pageNumberBase = page.pageNumber * itemsPerPage;

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
                        for (int i = 0; i < itemsPerPage; i++) {
                            playlistItem = page.items.get(i);
                            playlistItem.video = new Video(resultItems.getJSONObject(i));
                            // make sure the UI gets updated for the item
                            mAdapter.notifyItemChanged(pageNumberBase + i);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute(page);

            mAdapter.notifyItemRangeInserted(pageNumberBase, pageNumberBase + itemsPerPage);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Interface used by the {@link PlaylistCardAdapter} to inform us that we reached the last item in the list.
     */
    public interface LastItemReachedListener {
        void onLastItem(int position, String nextPageToken);
    }
}
