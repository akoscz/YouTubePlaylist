package com.akoscz.youtube;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
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

import javax.inject.Inject;

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

    // see: https://developers.google.com/youtube/v3/docs/playlistItems/list
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "pageInfo,nextPageToken,items(id,snippet(resourceId/videoId))";
    // see: https://developers.google.com/youtube/v3/docs/videos/list
    private static final String YOUTUBE_VIDEOS_PART = "snippet,contentDetails,statistics"; // video resource properties that the response will include.
    private static final String YOUTUBE_VIDEOS_FIELDS = "items(id,snippet(title,description,thumbnails/high),contentDetails/duration,statistics)"; // selector specifying which fields to include in a partial response.
    // the max number of playlist results to receive per request
    private static final Long YOUTUBE_PLAYLIST_MAX_RESULTS = 10L;
    // number of times to retry network operations
    private static final int RETRY_COUNT = 5;

    private RecyclerView mRecyclerView;
    private PlaylistCardAdapter mAdapter;

    @Inject
    YouTube mYouTubeDataApi;

    @Inject
    Playlist mPlaylist;

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment YouTubeRecyclerViewFragment.
     */
    public static YouTubeRecyclerViewFragment newInstance() {
        YouTubeRecyclerViewFragment fragment = new YouTubeRecyclerViewFragment();
        return fragment;
    }

    public YouTubeRecyclerViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((YouTubePlaylistApplication)getContext().getApplicationContext()).getComponent().inject(this);
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
        RecyclerView.LayoutManager layoutManager;
        if (resources.getBoolean(R.bool.isTablet)) {
            // use a staggered grid layout if we're on a large screen device
            layoutManager = new StaggeredGridLayoutManager(resources.getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL);
        } else {
            // use a linear layout on phone devices
            layoutManager = new LinearLayoutManager(getActivity());
        }

        mRecyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initAdapter(mPlaylist);
        fetchPlaylist();
    }

    private void initAdapter(final Playlist playlist) {
        // create the adapter with our playlist and a callback to handle when we reached the last item
        mAdapter = new PlaylistCardAdapter(playlist,
                        (position, nextPageToken) -> fetchPlaylist());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void fetchPlaylist() {
        final String savedNextPageToken = mPlaylist.getNextPageToken();

        Observable.create((Subscriber<? super PlaylistItemListResponse> subscriber) -> {
            try {
                if (!subscriber.isUnsubscribed()) {
                    PlaylistItemListResponse playlistItemListResponse = mYouTubeDataApi.playlistItems()
                        .list(YOUTUBE_PLAYLIST_PART)
                        .setPlaylistId(mPlaylist.playlistId)
                        .setPageToken(mPlaylist.getNextPageToken())
                        .setFields(YOUTUBE_PLAYLIST_FIELDS)
                        .setMaxResults(YOUTUBE_PLAYLIST_MAX_RESULTS)
                        .setKey(ApiKey.YOUTUBE_API_KEY)
                        .execute();

                    mPlaylist.setNextPageToken(playlistItemListResponse.getNextPageToken());

                    subscriber.onNext(playlistItemListResponse);
                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }
        })
        .flatMap(playlistItemListResponse -> {
            return Observable.from(playlistItemListResponse.getItems()) // emit list on stream
                    .map((PlaylistItem item) -> item.getSnippet().getResourceId().getVideoId()) // map to video ids
                    .toList(); // flatten back to list
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
        .retry(RETRY_COUNT) // retry before we give up
        .filter(videoListItems -> (videoListItems != null && videoListItems.size() != 0))
        .subscribe(videoListItems -> {
            final int startPosition = mPlaylist.size();
            mPlaylist.addAll(videoListItems);
            mAdapter.notifyItemRangeInserted(startPosition, videoListItems.size());
        }, throwable -> {
            // error case, something went wrong.
            Log.d("FetchPlaylist", "Resetting next page token. Error: " + throwable.getMessage(), throwable);
            // reset the next page token
            mPlaylist.setNextPageToken(savedNextPageToken);
        });
    }

    /**
     * Interface used by the {@link PlaylistCardAdapter} to inform us that we reached the last item in the list.
     */
    public interface LastItemReachedListener {
        void onLastItem(int position, String nextPageToken);
    }
}
