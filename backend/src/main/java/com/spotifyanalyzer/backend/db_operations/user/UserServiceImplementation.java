package com.spotifyanalyzer.backend.db_operations.user;

import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImplementation implements UserService
{
    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Synchronized
    public void registerUser(String username){
        // Check if the user already exists
        User existingUser = getUserFromUsername(username);
        if (existingUser != null) {
            // User already exists, no need to register again
            return;
        }
        User user = new User();
        user.setSpotifyUsername(username);
        user.setArtistsMinigameBestTimeInSeconds(null);
        user.setTracksMinigameBestTimeInSeconds(null);
        userRepository.save(user);
    }

    @Override
    public List<User> getRegisteredUsers() throws Exception
    {
        List<User>users=userRepository.findAll();
        if(!users.isEmpty())
        {
            return users;
        }
        throw new Exception("User is null");
    }

    @Override
    public List<User> getTopMinigamePlayers() throws Exception
    {
        List<User> users = userRepository.findTop5ByOrderByArtistsMinigameBestTimeInSecondsAsc();
        if (users != null)
        {
            return users;
        }
        throw new Exception("No top minigame players found");
    }

    @Override
    public boolean deleteBothMinigameScores(String username){
        User user = getUserFromUsername(username);
        if (user == null) return false;
        user.setArtistsMinigameBestTimeInSeconds(null);
        user.setTracksMinigameBestTimeInSeconds(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean updateMinigameTime(String username, long newTime, String typeOfGame){
        User user = getUserFromUsername(username);
        if (user == null) return false;
        if (typeOfGame.equals("artists")) {
            // Check if the new time is better than the existing best time
            if (user.getArtistsMinigameBestTimeInSeconds() == null || newTime < user.getArtistsMinigameBestTimeInSeconds()) {
                user.setArtistsMinigameBestTimeInSeconds(newTime);
                userRepository.save(user);
                return true;
            }
        } else if (typeOfGame.equals("tracks")) {
            if (user.getTracksMinigameBestTimeInSeconds() == null || newTime < user.getTracksMinigameBestTimeInSeconds()) {
                user.setTracksMinigameBestTimeInSeconds(newTime);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public Long getUserArtistMinigameTime(String username){
        User user = getUserFromUsername(username);
        if (user == null) return null;
        return user.getArtistsMinigameBestTimeInSeconds();
        
    }

    @Override
    public Long getUserTrackMinigameTime(String username){
        User user = getUserFromUsername(username);
        if (user == null) return null;
        return user.getTracksMinigameBestTimeInSeconds();
    }

    @Override
    public boolean deleteArtistMinigameScore(String username) {
        User user = getUserFromUsername(username);
        if (user == null) return false;
        user.setArtistsMinigameBestTimeInSeconds(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean deleteTrackMinigameScore(String username) {
        User user = getUserFromUsername(username);
        if (user == null) return false;
        user.setTracksMinigameBestTimeInSeconds(null);
        userRepository.save(user);
        return true;
    }


    private User getUserFromUsername(String username) {
        List<User> users = userRepository.findAllBySpotifyUsername(username);
        if (users.isEmpty()) {
            return null;
        }
        return users.getFirst();
    }
}
