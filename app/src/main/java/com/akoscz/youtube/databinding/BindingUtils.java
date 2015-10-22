package com.akoscz.youtube.databinding;

import android.databinding.BindingAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

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
public class BindingUtils {
    @BindingAdapter({"app:imageUrl", "app:placeHolder"})
    public static void loadImage(ImageView view, String url, int placeHolder) {
        RequestCreator requestCreator =
                Picasso.with(view.getContext()).load(url);
        if (placeHolder != 0) {
            requestCreator.placeholder(placeHolder);
        }
        requestCreator.into(view);
    }

    @BindingAdapter({"app:videoDuration"})
    public static void parseDuration(TextView view, String duration) {
        boolean hasSeconds = duration.indexOf('S') > 0;
        boolean hasMinutes = duration.indexOf('M') > 0;

        String s;
        if (hasSeconds) {
            s = duration.substring(2, duration.length() - 1);
        } else {
            s = duration.substring(2, duration.length());
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

        view.setText(minutes + ":" + seconds);
    }
}