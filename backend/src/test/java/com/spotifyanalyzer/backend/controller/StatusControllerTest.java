package com.spotifyanalyzer.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatusControllerTest {

    @Test
    void getStatus_ReturnsRunningStatusAndTimestamp() {
        StatusController statusController = new StatusController();

        ResponseEntity<Map<String, String>> response = statusController.getStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Server is running", response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"), "Timestamp should be present");
    }
}