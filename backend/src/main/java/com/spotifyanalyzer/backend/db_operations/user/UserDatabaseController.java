package com.spotifyanalyzer.backend.db_operations.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db/users")
public class UserDatabaseController
{
    @Autowired
    private UserService userService;


    //Registers the users
    @PutMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username) throws Exception
    {
        if(username!=null)
        {
            userService.registerUser(username);
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "User registered"));
    }

    //Get all the registered users.
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>>getRegisteredUser() throws Exception
    {
        List<User>users=userService.getRegisteredUsers();
        if(users!=null)
        {
            return new ResponseEntity<>(users,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/topMinigamePlayers")
    public ResponseEntity<List<User>> getTopMinigamePlayers() throws Exception
    {
        List<User> users = userService.getTopMinigamePlayers();
        if (users != null)
        {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/getUserArtistMinigameTime")
    public ResponseEntity<?> getUserArtistMinigameTime(@RequestParam String username) {
        try {
            Long time = userService.getUserArtistMinigameTime(username);
            if (time == null) {
                return new ResponseEntity<>(
                        Map.of("artistMinigameTime", false),
                        HttpStatus.OK
                );
            }
            return new ResponseEntity<>(
                        Map.of("artistMinigameTime", time),
                        HttpStatus.OK
                );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/getUserTrackMinigameTime")
    public ResponseEntity<?> getUserTrackMinigameTime(@RequestParam String username) {
        try {
            Long time = userService.getUserTrackMinigameTime(username);
            System.out.println("Time: " + time);
                if (time == null) {
                    return new ResponseEntity<>(
                            Map.of("trackMinigameTime", false),
                            HttpStatus.OK
                    );
                }

                return new ResponseEntity<>(
                        Map.of("trackMinigameTime", time),
                        HttpStatus.OK
                );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    //update user minigame time
    @PutMapping("/updateArtistMinigameTime")
    public ResponseEntity<?> updateArtistMinigameTime(@RequestParam String username, @RequestParam long newTime) {
        try {
            boolean wasUpdated = userService.updateMinigameTime(username, newTime, "artists");

            return getResponseMessage(wasUpdated);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("/updateTrackMinigameTime")
    public ResponseEntity<?> updateTrackMinigameTime(@RequestParam String username, @RequestParam long newTime) {
        try {
            boolean wasUpdated = userService.updateMinigameTime(username, newTime, "tracks");

            return getResponseMessage(wasUpdated);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private static ResponseEntity<Map<String, Boolean>> getResponseMessage(boolean wasUpdated) {

            return new ResponseEntity<>(
                    Map.of("updated", wasUpdated),
                    HttpStatus.OK

            );
    }

    @DeleteMapping("/deleteBothMinigameScores")
    public ResponseEntity<?> deleteMinigameScore(@RequestParam String username) {
        try {
            boolean deleted = userService.deleteBothMinigameScores(username);

            if (deleted) {
                return new ResponseEntity<>(
                        Map.of("message", "Minigame score deleted successfully"),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        Map.of("message", "Could not delete"),
                        HttpStatus.OK
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("/deleteArtistMinigameScore")
    public ResponseEntity<?> deleteArtistMinigameScore(@RequestParam String username) {
        try {
            boolean deleted = userService.deleteArtistMinigameScore(username);

            if (deleted) {
                return new ResponseEntity<>(
                        Map.of("message", "Artist minigame score deleted successfully"),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        Map.of("message", "No artist score found to delete"),
                        HttpStatus.OK
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("/deleteTrackMinigameScore")
    public ResponseEntity<?> deleteTrackMinigameScore(@RequestParam String username) {
        try {
            boolean deleted = userService.deleteTrackMinigameScore(username);

            if (deleted) {
                return new ResponseEntity<>(
                        Map.of("message", "Track minigame score deleted successfully"),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        Map.of("message", "No Track score found to delete"),
                        HttpStatus.OK
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", "Error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}


