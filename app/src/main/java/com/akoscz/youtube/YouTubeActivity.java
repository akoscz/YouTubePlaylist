package com.akoscz.youtube;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ie.moses.caimito.net.InternetUtils;

import static ie.moses.caimito.android.ToastUtils.toast;
import static ie.moses.caimito.collections.CollectionUtils.list;

public class YouTubeActivity extends AppCompatActivity {

    //see: https://developers.google.com/youtube/v3/docs/videos/list
    private static final String YOUTUBE_VIDEOS_PART = "snippet"; // video resource properties that the response will include.
    private static final String YOUTUBE_VIDEOS_FIELDS = "items(snippet(thumbnails/medium))"; // selector specifying which fields to include in a partial response.

    private YouTubeThumbnailsAdapter _youTubeThumbnailsAdapter;

    @Override
    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_activity);

        boolean isInternetAvailable = InternetUtils.isInternetAvailable(this);
        if (!isInternetAvailable) {
            toast(this, "No Internet Connection Detected");
        }

        final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        final GsonFactory jsonFactory = new GsonFactory();
        final Resources resources = getResources();
        final String appName = resources.getString(R.string.app_name);
        final YouTube youTube = new YouTube.Builder(httpTransport, jsonFactory, null)
                .setApplicationName(appName)
                .build();

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final List<Video> playlistVideos = new ArrayList<>();

        _youTubeThumbnailsAdapter = new YouTubeThumbnailsAdapter(this, playlistVideos);
        recyclerView.setAdapter(_youTubeThumbnailsAdapter);

        new Thread() {
            @Override
            public void run() {
                List<String> videoIds = list("ksl4kS6GMe8", "7554FltLnRE");

                try {
                    VideoListResponse videoListResponse = youTube.videos()
                            .list(YOUTUBE_VIDEOS_PART)
                            .setFields(YOUTUBE_VIDEOS_FIELDS)
                            .setKey("AIzaSyCxFRhzzHte-lL5a22Md9x7VxSZK0Y14DQ")
                            .setId(TextUtils.join(",", videoIds)).execute();

                    if (videoListResponse != null) {
                        final List<Video> result = videoListResponse.getItems();
                        final int positionStart = playlistVideos.size();
                        playlistVideos.addAll(result);

                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                _youTubeThumbnailsAdapter.notifyItemRangeInserted(positionStart, result.size());
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}