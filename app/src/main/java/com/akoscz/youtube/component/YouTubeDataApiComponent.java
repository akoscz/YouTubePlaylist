package com.akoscz.youtube.component;

import com.akoscz.youtube.module.YouTubeDataApiModule;
import com.google.api.services.youtube.YouTube;

import javax.inject.Singleton;

import dagger.Component;

/**
 *
 */
@Singleton
@Component(modules = {YouTubeDataApiModule.class})
public interface YouTubeDataApiComponent {

    YouTube provideYouTubeDataApi();

}
