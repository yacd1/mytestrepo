package com.spotifyanalyzer.backend.db_operations.artist;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

class ArtistTest {

    @Test
    void testDefaultConstructor() {
        Artist artist = new Artist();
        assertNull(artist.getId());
        assertNull(artist.getSummary());
        assertNull(artist.getArtistName());
        assertNull(artist.getUpdate_date());
    }

    @Test
    void testParameterizedConstructor() {
        String summary = "Test Summary";
        String artistName = "Test Artist";
        Date updateDate = new Date();

        Artist artist = new Artist(summary, artistName, updateDate);

        assertNull(artist.getId()); // ID is not set in the constructor
        assertEquals(summary, artist.getSummary());
        assertEquals(artistName, artist.getArtistName());
        assertEquals(updateDate, artist.getUpdate_date());
    }

    @Test
    void testSettersAndGetters() {
        Artist artist = new Artist();

        String id = "123";
        String summary = "Updated Summary";
        String artistName = "Updated Artist";
        Date updateDate = new Date();

        artist.setId(id);
        artist.setSummary(summary);
        artist.setArtistName(artistName);
        artist.setUpdate_date(updateDate);

        assertEquals(id, artist.getId());
        assertEquals(summary, artist.getSummary());
        assertEquals(artistName, artist.getArtistName());
        assertEquals(updateDate, artist.getUpdate_date());
    }
}