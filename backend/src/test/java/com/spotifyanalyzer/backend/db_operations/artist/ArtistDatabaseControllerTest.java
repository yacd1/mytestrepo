package com.spotifyanalyzer.backend.db_operations.artist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArtistDatabaseControllerTest {

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private ArtistDatabaseController artistDatabaseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() throws Exception {
        Artist artist = new Artist("Test Summary", "Test Artist", new Date());
        when(artistService.addArtist(artist)).thenReturn(artist);

        ResponseEntity<Artist> response = artistDatabaseController.register(artist);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(artist, response.getBody());
    }

    @Test
    void testRegisterWithNullArtist() throws Exception {
        Artist artist = null;
        ResponseEntity<?> response = artistDatabaseController.register(artist);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testGetRegisteredArtist() throws Exception {
        List<Artist> artists = Arrays.asList(
                new Artist("Summary1", "Artist1", new Date()),
                new Artist("Summary2", "Artist2", new Date())
        );
        when(artistService.getRegisteredArtists()).thenReturn(artists);

        ResponseEntity<List<Artist>> response = artistDatabaseController.getRegisteredArtist();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(artists, response.getBody());
    }



    @Test
    void testUpdateArtistSummary() throws Exception {
        String artistName = "Test Artist";
        String summary = "Updated Summary";
        Artist updatedArtist = new Artist(summary, artistName, null);
        when(artistService.updateArtistSummary(artistName, summary)).thenReturn(updatedArtist);

        ResponseEntity<Artist> response = artistDatabaseController.updateArtistSummary(artistName, summary);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedArtist, response.getBody());
    }

    @Test
    void testUpdateArtistSummaryWithNullArtist() throws Exception {
        String artistName = "Test Artist";
        String summary = null;
        ResponseEntity<?> response = artistDatabaseController.updateArtistSummary(artistName, summary);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testUpdateArtistSummaryWithNullName() throws Exception {
        String artistName = null;
        String summary = "Updated Summary";
        ResponseEntity<?> response = artistDatabaseController.updateArtistSummary(artistName, summary);
        assertEquals(400, response.getStatusCodeValue());
    }



    @Test
    void testGetArtistByName() throws Exception {
        String artistName = "Test Artist";
        Artist artist = new Artist("Test Summary", artistName, new Date());
        when(artistService.getArtistByName(artistName)).thenReturn(artist);

        ResponseEntity<Artist> response = artistDatabaseController.getArtistByName(artistName);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(artist, response.getBody());
        verify(artistService, times(1)).getArtistByName(artistName);
    }

    @Test
    void testGetArtistByNameWithNullName() throws Exception {
        String artistName = null;
        ResponseEntity<?> response = artistDatabaseController.getArtistByName(artistName);
        assertEquals(400, response.getStatusCodeValue());
    }
}
