package com.spotifyanalyzer.backend.db_operations.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "UserCollection")
public class User {
    @Id
    private String id;

    @Field("spotify_username")
    private String spotifyUsername;


    @Field("artists_minigame_best_time_in_seconds")
    private Long artistsMinigameBestTimeInSeconds;

    @Field("tracks_minigame_best_time_in_seconds")
    private Long tracksMinigameBestTimeInSeconds;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpotifyUsername() {
        return spotifyUsername;
    }

    public void setSpotifyUsername(String spotifyUsername) {
        this.spotifyUsername = spotifyUsername;
    }

    public Long getArtistsMinigameBestTimeInSeconds() {
        return artistsMinigameBestTimeInSeconds;
    }

    public void setArtistsMinigameBestTimeInSeconds(Long artistsMinigameBestTimeInSeconds) {
        this.artistsMinigameBestTimeInSeconds = artistsMinigameBestTimeInSeconds;
    }

    public Long getTracksMinigameBestTimeInSeconds() {
        return tracksMinigameBestTimeInSeconds;
    }
    public void setTracksMinigameBestTimeInSeconds(Long tracksMinigameBestTimeInSeconds) {
        this.tracksMinigameBestTimeInSeconds = tracksMinigameBestTimeInSeconds;
    }


}
