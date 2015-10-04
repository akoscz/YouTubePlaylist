package com.akoscz.youtube;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    //see: https://developers.google.com/youtube/v3/docs/playlistItems/list
    private static final String YOUTUBE_PLAYLISTITEMS_URL = "https://www.googleapis.com/youtube/v3/playlistItems";
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "etag,pageInfo,nextPageToken,items(id,snippet(title,description,position,thumbnails(medium,high),resourceId/videoId))";

    private static OkHttpClient client = new OkHttpClient();

    private final Uri.Builder mUriBuilder;

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

        final String url = mUriBuilder.build().toString();
        //Log.d(TAG, url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;
        String result;
        try {
            response = client.newCall(request).execute();
            result = response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (result == null) {
            Log.e(TAG, "Failed to get playlist");
            return null;
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
}
