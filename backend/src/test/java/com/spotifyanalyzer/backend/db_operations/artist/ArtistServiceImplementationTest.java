package com.spotifyanalyzer.backend.db_operations.artist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArtistServiceImplementationTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistServiceImplementation artistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddArtist_ValidArtist() throws Exception {
        Artist artist = new Artist("Test Summary", "Test Artist", new Date());
        when(artistRepository.save(artist)).thenReturn(artist);

        Artist result = artistService.addArtist(artist);

        assertNotNull(result);
        assertEquals(artist, result);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void testAddArtist_NullArtist() {
        Exception exception = assertThrows(Exception.class, () -> artistService.addArtist(null));
        assertEquals("User is null", exception.getMessage());
        verify(artistRepository, never()).save(any());
    }

    @Test
    void testGetRegisteredArtists_ValidArtists() throws Exception {
        List<Artist> artists = Arrays.asList(
                new Artist("Summary1", "Artist1", new Date()),
                new Artist("Summary2", "Artist2", new Date())
        );
        when(artistRepository.findAll()).thenReturn(artists);

        List<Artist> result = artistService.getRegisteredArtists();

        assertNotNull(result);
        assertEquals(artists, result);
        verify(artistRepository, times(1)).findAll();
    }

    @Test
    void testGetRegisteredArtists_NoArtistsFound() {
        when(artistRepository.findAll()).thenReturn(Arrays.asList());

        Exception exception = assertThrows(Exception.class, () -> artistService.getRegisteredArtists());
        assertEquals("No artists found", exception.getMessage());
        verify(artistRepository, times(1)).findAll();
    }

    @Test
    void testUpdateArtistSummary_ValidArtist() throws Exception {
        String artistName = "Test Artist";
        String summary = "Updated Summary";
        Artist artist = new Artist("Old Summary", artistName, new Date());
        when(artistRepository.findByArtistName(artistName)).thenReturn(artist);
        when(artistRepository.save(artist)).thenReturn(artist);

        Artist result = artistService.updateArtistSummary(artistName, summary);

        assertNotNull(result);
        assertEquals(summary, result.getSummary());
        verify(artistRepository, times(1)).findByArtistName(artistName);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void testUpdateArtistSummary_ArtistNotFound() {
        String artistName = "Nonexistent Artist";
        String summary = "Updated Summary";
        when(artistRepository.findByArtistName(artistName)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> artistService.updateArtistSummary(artistName, summary));
        assertEquals("Artist not found", exception.getMessage());
        verify(artistRepository, times(1)).findByArtistName(artistName);
        verify(artistRepository, never()).save(any());
    }

    @Test
    void testGetArtistByName_ValidArtist() throws Exception {
        String artistName = "Test Artist";
        Artist artist = new Artist("Test Summary", artistName, new Date());
        when(artistRepository.findByArtistName(artistName)).thenReturn(artist);

        Artist result = artistService.getArtistByName(artistName);

        assertNotNull(result);
        assertEquals(artist, result);
        verify(artistRepository, times(1)).findByArtistName(artistName);
    }

    @Test
    void testGetArtistByName_ArtistNotFound() {
        String artistName = "Nonexistent Artist";
        when(artistRepository.findByArtistName(artistName)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> artistService.getArtistByName(artistName));
        assertEquals("Artist not found", exception.getMessage());
        verify(artistRepository, times(1)).findByArtistName(artistName);
    }
}
