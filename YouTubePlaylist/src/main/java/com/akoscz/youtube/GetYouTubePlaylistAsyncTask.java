package com.akoscz.youtube;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.etag.CacheRequest;
import com.github.kevinsawicki.etag.EtagCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
public abstract class GetYouTubePlaylistAsyncTask extends AsyncTask<String, Void, JSONObject> {
    private static final String TAG = "GetYouTubePlaylistAsyncTask";

    private static final int YOUTUBE_PLAYLIST_MAX_RESULTS = 10;
    private static final String YOUTUBE_PLAYLISTITEMS_URL = "https://www.googleapis.com/youtube/v3/playlistItems";
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "etag,pageInfo,nextPageToken,items(id,snippet(title,description,position,thumbnails(medium,high),resourceId/videoId))";

    public GetYouTubePlaylistAsyncTask() {
        mUriBuilder = Uri.parse(YOUTUBE_PLAYLISTITEMS_URL).buildUpon();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        final String playlistId = params[0];
        if (playlistId == null || playlistId.length() == 0) {
            return null;
        }

        if (params.length == 2) {
            final String nextPageToken = params[1];
            if (nextPageToken != null) {
                mUriBuilder.appendQueryParameter("pageToken", nextPageToken);
            }
        }

        mUriBuilder.appendQueryParameter("playlistId", playlistId)
                .appendQueryParameter("part", YOUTUBE_PLAYLIST_PART)
                .appendQueryParameter("maxResults", Integer.toString(YOUTUBE_PLAYLIST_MAX_RESULTS))
                .appendQueryParameter("fields", YOUTUBE_PLAYLIST_FIELDS)
                .appendQueryParameter("key", ApiKey.YOUTUBE_API_KEY);

        final String result = doGetUrl(mUriBuilder.build().toString());
        if (result == null) {
            Log.e(TAG, "Failed to get playlist");
            return null;
        } else {
            //Log.i(TAG, result);
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonObject;
    }

    protected Uri.Builder mUriBuilder;

    public abstract EtagCache getEtagCache();

    public String doGetUrl(String url) {
        Log.d(TAG, url);

        CacheRequest request = CacheRequest.get(url, getEtagCache());
//        Log.d(TAG, "Response was " + request.body());

        StringBuilder builder = new StringBuilder();
        InputStream is = request.stream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (request.cached()) {
            Log.d(TAG, "Cache hit");
        } else {
            Log.d(TAG, "Cache miss");
        }

        return builder.toString();
    }
}
