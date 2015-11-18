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
 *
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
