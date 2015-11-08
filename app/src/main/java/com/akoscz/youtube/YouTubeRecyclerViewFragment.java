package com.akoscz.youtube;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akoscz.youtube.model.Playlist;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    // see: https://developers.google.com/youtube/v3/docs/playlistItems/list
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "pageInfo,nextPageToken,items(id,snippet(resourceId/videoId))";
    // see: https://developers.google.com/youtube/v3/docs/videos/list
    private static final String YOUTUBE_VIDEOS_PART = "snippet,contentDetails,statistics"; // video resource properties that the response will include.
    private static final String YOUTUBE_VIDEOS_FIELDS = "items(id,snippet(title,description,thumbnails/high),contentDetails/duration,statistics)"; // selector specifying which fields to include in a partial response.
    // the max number of playlist results to receive per request
    private static final Long YOUTUBE_PLAYLIST_MAX_RESULTS = 10L;

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
        setRetainInstance(true);
        if (getArguments() != null) {
            mPlaylistId = getArguments().getString(ARG_YOUTUBE_PLAYLIST_ID);
        }
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

        // if we have a playlist in our retained fragment, use it to populate the UI
        if (mPlaylist != null) {
            initAdapter(mPlaylist);
        } else {
            // otherwise create an empty playlist
            mPlaylist = new Playlist(mPlaylistId);
            // populate an empty UI
            initAdapter(mPlaylist);
            // and start fetching the playlist contents
            fetchPlaylist(mPlaylist, mYouTubeDataApi);
        }
    }

    private void initAdapter(final Playlist playlist) {
        // create the adapter with our playlist and a callback to handle when we reached the last item
        mAdapter = new PlaylistCardAdapter(playlist,
                        (position, nextPageToken) -> fetchPlaylist(playlist, mYouTubeDataApi));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void fetchPlaylist(final Playlist playlist, final YouTube youTubeDataApi) {
        Observable.create((Subscriber<? super PlaylistItemListResponse> subscriber) -> {
            try {
                if(!subscriber.isUnsubscribed()) {
                    PlaylistItemListResponse playlistItemListResponse = youTubeDataApi.playlistItems()
                        .list(YOUTUBE_PLAYLIST_PART)
                        .setPlaylistId(playlist.playlistId)
                        .setPageToken(playlist.getNextPageToken())
                        .setFields(YOUTUBE_PLAYLIST_FIELDS)
                        .setMaxResults(YOUTUBE_PLAYLIST_MAX_RESULTS)
                        .setKey(ApiKey.YOUTUBE_API_KEY)
                        .execute();

                    playlist.setNextPageToken(playlistItemListResponse.getNextPageToken());

                    subscriber.onNext(playlistItemListResponse);
                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        })
        .map(playlistItemListResponse -> {
            List<String> videoIds = new ArrayList();

            // pull out the video id's from the playlist page
            for (PlaylistItem item : playlistItemListResponse.getItems()) {
                videoIds.add(item.getSnippet().getResourceId().getVideoId());
            }

            return videoIds;
        })
        .map(videoIdsList -> {
            VideoListResponse videoListResponse = null;
                // get details of the videos on this playlist page
            try {
                videoListResponse = mYouTubeDataApi.videos()
                    .list(YOUTUBE_VIDEOS_PART)
                    .setFields(YOUTUBE_VIDEOS_FIELDS)
                    .setKey(ApiKey.YOUTUBE_API_KEY)
                    .setId(TextUtils.join(",", videoIdsList))
                    .execute();
            } catch (IOException e) {
                // don't swallow the exception.  Let it propagate up to the subscriber
                throw new RuntimeException(e);
            }

            return videoListResponse.getItems();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(videoListItems -> {
            if (videoListItems == null) return;

            final int positionStart = playlist.size();
            playlist.addAll(videoListItems);
            mAdapter.notifyItemRangeInserted(positionStart, videoListItems.size());
        });
    }

    /**
     * Interface used by the {@link PlaylistCardAdapter} to inform us that we reached the last item in the list.
     */
    public interface LastItemReachedListener {
        void onLastItem(int position, String nextPageToken);
    }
}
