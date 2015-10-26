package com.akoscz.youtube.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

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
 * <p/>
 *
 * The VideoViewModel is responsible for manipulating data from the underlying model to be shown
 * in the view.
 * This particular ViewModel exposes two click listeners that are bound to the the view through
 * the android:onClick view attributes.
 */
public class VideoViewModel {

    private static final String YOUTUBE_WATCH_URI = "http://www.youtube.com/watch?v=";
    private Video mVideo;
    private Context mContext;

    public VideoViewModel(Context context) {
        mContext = context;
    }

    public void setVideo(Video video) {
        mVideo = video;
    }

    public String getThumbnailUrl() {
        return mVideo.getSnippet().getThumbnails().getHigh().getUrl();
    }

    public String getTitle() {
        return mVideo.getSnippet().getTitle();
    }

    public String getDescription() {
        return mVideo.getSnippet().getDescription();
    }

    public String getViewCount() {
        return String.format("%,d", mVideo.getStatistics().getViewCount());
    }

    public String getLikeCount() {
        return String.format("%,d", mVideo.getStatistics().getLikeCount());
    }

    public String getDislikeCount() {
        return String.format("%,d", mVideo.getStatistics().getDislikeCount());
    }

    public String getDuration() {
        return mVideo.getContentDetails().getDuration();
    }

    public void onShareClick(View v) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch \"" + getTitle() + "\" on YouTube");
        sendIntent.putExtra(Intent.EXTRA_TEXT, YOUTUBE_WATCH_URI + mVideo.getId());
        sendIntent.setType("text/plain");
        mContext.startActivity(sendIntent);
    }

    public void onPlayVideoClick(View v) {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_WATCH_URI + mVideo.getId())));
    }
}
