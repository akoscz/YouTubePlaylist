package com.akoscz.youtube.component;

import com.akoscz.youtube.model.Playlist;
import com.akoscz.youtube.module.PlaylistModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 *
 */
@Singleton
@Component(modules = {PlaylistModule.class})
public interface PlaylistComponent {

    Playlist providePlaylist();
}
