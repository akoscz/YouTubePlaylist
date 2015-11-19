package com.akoscz.youtube.module;

import android.content.Context;

import com.akoscz.youtube.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
 */
@Module
public class YouTubeDataApiModule {
    private final Context mContext;

    public YouTubeDataApiModule(Context context) {
        mContext = context;
    }

    @Provides @Singleton
    GsonFactory provideGsonFactory() {
        return new GsonFactory();
    }

    @Provides
    Context provideContext() {
        return mContext;
    }

    @Provides @Singleton
    HttpTransport provideHttpTransport() {
        return AndroidHttp.newCompatibleTransport();
    }

    @Provides @Singleton
    YouTube provideYouTubeDataApi(HttpTransport httpTransport, GsonFactory gsonFactory, Context context) {
        return new YouTube.Builder(httpTransport, gsonFactory, null)
                .setApplicationName(context.getResources().getString(R.string.app_name))
                .build();
    }
}
