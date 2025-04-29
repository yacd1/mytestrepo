package com.spotifyanalyzer.backend.db_operations.user;

import java.util.List;

public interface UserService
{
    public void registerUser(String username) throws Exception;
    public List<User> getRegisteredUsers() throws Exception;
    public List<User> getTopMinigamePlayers() throws Exception;
    public boolean updateMinigameTime(String username, long newTime, String typeOfGame) throws Exception;
    public boolean deleteBothMinigameScores(String username) throws Exception;
    public Long getUserArtistMinigameTime(String username) throws Exception;
    public Long getUserTrackMinigameTime(String username) throws Exception;
    public boolean deleteArtistMinigameScore(String username) throws Exception;
    public boolean deleteTrackMinigameScore(String username) throws Exception;
}

