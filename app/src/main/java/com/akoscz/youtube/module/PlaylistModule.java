package com.akoscz.youtube.module;

import com.akoscz.youtube.model.Playlist;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class PlaylistModule {

    private final String mPlaylistId;

    public PlaylistModule(String playlistId) {
        mPlaylistId = playlistId;
    }

    @Provides
    Playlist providesPlaylist() {
        return new Playlist(mPlaylistId);
    }

    @Provides @Singleton
    String providesPlaylistId() {
        return mPlaylistId;
    }
}
