package com.akoscz.youtube.model;

import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
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
 */
public class Playlist extends ArrayList<Video>{
    public final String playlistId;
    private String mNextPageToken;

    public Playlist(String id) {
        playlistId = id;
    }

    public void setNextPageToken(String nextPageToken) {
        mNextPageToken = nextPageToken;
    }

    public String getNextPageToken() {
        return mNextPageToken;
    }
}
