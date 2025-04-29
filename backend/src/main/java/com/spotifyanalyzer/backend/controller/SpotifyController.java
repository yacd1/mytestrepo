package com.spotifyanalyzer.backend.controller;

import com.spotifyanalyzer.backend.authservice.ArtistDetails;
import com.spotifyanalyzer.backend.authservice.JsonUtil;
import com.spotifyanalyzer.backend.authservice.PythonService;
import com.spotifyanalyzer.backend.dto.SpotifyAuthResponse;
import com.spotifyanalyzer.backend.authservice.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final PythonService pythonService;

    @Value("${python.service.url}")
    private String pythonServiceUrl;

    @Autowired
    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
        this.pythonService = new PythonService();
    }

    private String getValidAccessToken(HttpSession session) {
        String accessToken = (String) session.getAttribute("spotify_access_token");

        if (accessToken == null) {
            return null;
        }

        // check if token is expired
        Long expiryTime = (Long) session.getAttribute("spotify_token_expiry");
        if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
            // if the tooken is expired, attempt to refresh
            String refreshToken = (String) session.getAttribute("spotify_refresh_token");
            if (refreshToken != null) {
                try {
                    SpotifyAuthResponse refreshResponse = spotifyService.refreshAccessToken(refreshToken);
                    session.setAttribute("spotify_access_token", refreshResponse.getAccessToken());

                    long newExpiryTime = System.currentTimeMillis() + (refreshResponse.getExpiresIn() * 1000);
                    session.setAttribute("spotify_token_expiry", newExpiryTime);

                    accessToken = refreshResponse.getAccessToken();
                } catch (Exception e) {
                    System.out.println("Failed to refresh token: " + e.getMessage());
                    return null; // Token refresh failed
                }
            }
        }

        return accessToken;
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String authUrl = spotifyService.getAuthorisationUrl();
        //System.out.println("generated spotify auth URL: " + authUrl);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @PostMapping("/token-exchange")
    public ResponseEntity<?> exchangeToken(@RequestParam String code, HttpSession session) {
        try {
            //System.out.println("received token exchange request with code: " + code.substring(0, 5) + "...");

            // exchange our code for the token
            SpotifyAuthResponse authResponse = spotifyService.exchangeCodeForToken(code);

            System.out.println("Token exchange successful!");

            // store the retrieved token in our sesesion
            session.setAttribute("spotify_access_token", authResponse.getAccessToken());
            session.setAttribute("spotify_refresh_token", authResponse.getRefreshToken());

            // find our token expiry time
            long expiryTime = System.currentTimeMillis() + (authResponse.getExpiresIn() * 1000);
            session.setAttribute("spotify_token_expiry", expiryTime);

            //System.out.println("Session ID: " + session.getId());
            System.out.println("Token stored in session");

            // create response with token details
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", authResponse.getAccessToken());
            response.put("expires_in", authResponse.getExpiresIn());
            response.put("token_type", authResponse.getTokenType());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("token exchange error: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "authentication_failed",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkAuthStatus(HttpSession session) {
        String accessToken = (String) session.getAttribute("spotify_access_token");
        //System.out.println("Checking auth status. Session ID: " + session.getId());
        //System.out.println("Access token present: " + (accessToken != null));

        if (accessToken != null) {
            try {
                Map<String, Object> profile = spotifyService.getUserProfile(accessToken);
                return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "profile", profile
                ));
            } catch (Exception e) {
                System.out.println("error verifying token: " + e.getMessage());
                return ResponseEntity.ok(Map.of("authenticated", false));
            }
        }

        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    // add some actual endpoints that we know the spotify API has. these are what are called from our frontend feel free to add more
    @GetMapping("/data/top-artists")
    public ResponseEntity<?> getTopArtists(
            @RequestParam(value = "time_range", defaultValue = "medium_term") String timeRange,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            HttpSession session) {

        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "not authenticated with Spotify"));
        }

        try {
            // making the actual request to the spotify API
            Object topArtists = spotifyService.makeSpotifyRequest(
                    "/me/top/artists?time_range=" + timeRange + "&limit=" + limit,
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Object.class);

            return ResponseEntity.ok(topArtists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to fetch top artists",
                            "message", e.getMessage()));
        }
    }

    // endpoint for getting the user's most listened to tracks from spotify
    @GetMapping("/data/top-tracks")
    public ResponseEntity<?> getTopTracks(
            @RequestParam(value = "time_range", defaultValue = "medium_term") String timeRange,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            HttpSession session) {

        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "not authenticated with Spotify"));
        }

        try {
            // make the request
            Object topTracks = spotifyService.makeSpotifyRequest(
                    "/me/top/tracks?time_range=" + timeRange + "&limit=" + limit,
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Object.class);

            return ResponseEntity.ok(topTracks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch top tracks",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/data/fake-recommendations")
    public ResponseEntity<?> getFakeRecommendations(HttpSession session) {
        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated with Spotify"));
        }

        try {
            // Spotify's recommendations endpoint is deprecated
            //taking users most recent listen - searching it up and taking the suggestions from the search instead
            Map<String, Object> recent = spotifyService.makeSpotifyRequest(
                    "/me/player/recently-played?limit=1",
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Map.class
            );

            List<Map<String, Object>> items = (List<Map<String, Object>>) recent.get("items");
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No recently played tracks found"));
            }

            Map<String, Object> track = (Map<String, Object>) items.get(0).get("track");
            String trackName = (String) track.get("name");
            Map<String, Object> artist = ((List<Map<String, Object>>) track.get("artists")).get(0);
            String artistName = (String) artist.get("name");

            // where i search using track name + artist
            String query = URLEncoder.encode(trackName + " " + artistName, StandardCharsets.UTF_8);
            String endpoint = "/search?q=" + query + "&type=track&limit=10";

            Map<String, Object> searchResults = spotifyService.makeSpotifyRequest(
                    endpoint,
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Map.class
            );

            // Removing the original track from the list
            Map<String, Object> tracks = (Map<String, Object>) searchResults.get("tracks");
            List<Map<String, Object>> allItems = (List<Map<String, Object>>) tracks.get("items");

            //the below is used to remove any tracks with the same id OR the same name
            String originalId = (String) track.get("id");
            String originalName = ((String) track.get("name")).trim().toLowerCase();
            List<Map<String, Object>> filtered = allItems.stream()
                    .filter(t ->
                            !originalId.equals(t.get("id"))&&
                                    !originalName.equals(((String) t.get("name")).trim().toLowerCase()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(filtered);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate recommendations", "details", e.getMessage()));
        }
    }

    //endpoint for top genre does not exist - extracting it from top artists
    @GetMapping("/data/top-genre")
    public ResponseEntity<?> getTopGenre(HttpSession session) {
        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated with Spotify"));
        }

        try {
            Map<String, Object> topArtists = spotifyService.makeSpotifyRequest(
                    "/me/top/artists?limit=20",
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Map.class
            );

            List<Map<String, Object>> items = (List<Map<String, Object>>) topArtists.get("items");
            Map<String, Integer> genreCounts = new HashMap<>();

            for (Map<String, Object> artist : items) {
                Object genreObj = artist.get("genres");
                if (genreObj instanceof List<?> genres) {
                    for (Object g : genres) {
                        if (g instanceof String genre) {
                            genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
                        }
                    }
                }
            }

            String topGenre = genreCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");

            return ResponseEntity.ok(Map.of("topGenre", topGenre));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch top genre", "details", e.getMessage()));
        }
    }

    // end point for getting the tracks a user has JUST listened too
    @GetMapping("/data/recently-played")
    public ResponseEntity<?> getRecentlyPlayed(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            HttpSession session) {

        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated with Spotify"));
        }

        try {
            // call Spotify API for recently played - I can change limit to change the numebr displayed
            Object recentlyPlayed = spotifyService.makeSpotifyRequest(
                    "/me/player/recently-played?limit=" + limit,
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Object.class);

            return ResponseEntity.ok(recentlyPlayed);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch recently played tracks",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("data/search")
    public ResponseEntity<?> getSearch(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "limit", defaultValue = "5") String limit,
            @RequestParam(value = "type", defaultValue = "") String type,
            HttpSession session) {

        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "not authenticated with Spotify"));
        }

        try {
            // make the request
            Object searchResponse = spotifyService.makeSpotifyRequest(
                    "/search?q=" + q + "&type=" + type + "&limit=" + limit,
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Object.class);

            return ResponseEntity.ok(searchResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/data/artist-info")
    public ResponseEntity<?> getArtistInfo(
            @RequestParam(value = "artist_id") String artistID,
            HttpSession session) {
        System.out.println("Getting artist info for: " + artistID);

        String accessToken = getValidAccessToken(session);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "not authenticated with Spotify"));
        }

        try {
            // making the actual request to the spotify API
            Object artistInfo = spotifyService.makeSpotifyRequest(
                    "/artists/" + artistID,
                    HttpMethod.GET,
                    accessToken,
                    null,
                    Object.class);

            return ResponseEntity.ok(artistInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to fetch top artists",
                            "message", e.getMessage()));
        }
    }

    @GetMapping("/data/artist-summary")
    public ResponseEntity<?> getArtistSummary(
            @RequestParam(value = "artistName") String artistName){

        System.out.println("Getting artist summary for: " + artistName);

        try {
            ArtistDetails artistDetails = new ArtistDetails();
            artistDetails.setArtistName(artistName);

            String artistDetailJSON = JsonUtil.convertObjectToJson(artistDetails);
            System.out.println("Sending artist summary request to python service" + artistDetailJSON);

            String artistSummary = pythonService.sendPOST(artistDetailJSON, pythonServiceUrl+"/artistSummary");
            System.out.println(artistSummary);
            return ResponseEntity.ok(artistSummary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to fetch artist summary",
                            "message", e.getMessage()));
        }



    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.removeAttribute("spotify_access_token");
        session.removeAttribute("spotify_refresh_token");
        session.removeAttribute("spotify_token_expiry");
        return ResponseEntity.ok().build();
    }
}