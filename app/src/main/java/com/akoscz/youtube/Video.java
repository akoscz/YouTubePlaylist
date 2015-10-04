package com.akoscz.youtube;

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
 */
public class Video {
    public final String duration;
    public final long viewCount;
    public final long likeCount;
    public final long dislikeCount;
    public final long favoriteCount;
    public final long commentCount;

    public Video(JSONObject json) throws JSONException {
        JSONObject contentDetails = json.getJSONObject("contentDetails");
        final String rawDuration = contentDetails.getString("duration");
        duration = parseDuration(rawDuration);

        JSONObject statistics = json.optJSONObject("statistics");
        if (statistics == null) {
            viewCount = 0;
            likeCount = 0;
            favoriteCount = 0;
            dislikeCount = 0;
            commentCount = 0;
        } else {
            viewCount = statistics.optLong("viewCount", 0);
            likeCount = statistics.optLong("likeCount", 0);
            dislikeCount = statistics.optLong("dislikeCount", 0);
            favoriteCount = statistics.optLong("favoriteCount", 0);
            commentCount = statistics.optLong("commentCount", 0);
        }
    }

    private String parseDuration(String in) {
        boolean hasSeconds = in.indexOf('S') > 0;
        boolean hasMinutes = in.indexOf('M') > 0;

        String s;
        if (hasSeconds) {
            s = in.substring(2, in.length() - 1);
        } else {
            s = in.substring(2, in.length());
        }

        String minutes = "0";
        String seconds = "00";

        if (hasMinutes && hasSeconds) {
            String[] split = s.split("M");
            minutes = split[0];
            seconds = split[1];
        } else if (hasMinutes) {
            minutes = s.substring(0, s.indexOf('M'));
        } else if (hasSeconds) {
            seconds = s;
        }

        // pad seconds with a 0 if less than 2 digits
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        return minutes + ":" + seconds;
    }
}