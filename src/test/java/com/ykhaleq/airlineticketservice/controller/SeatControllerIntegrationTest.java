package com.ykhaleq.airlineticketservice.controller;

import com.ykhaleq.airlineticketservice.dto.SeatHoldRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SeatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For serializing request bodies to JSON

    @Test
    void testHoldSeats_ValidRequest() throws Exception {
        // Create a valid SeatHoldRequest
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(3);
        request.setCustomerEmail("test@example.com");

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Serialize request to JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").isNumber())
                .andExpect(jsonPath("$.heldSeats").isArray())
                .andExpect(jsonPath("$.heldSeats[0]").isString())
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"));
    }

    @Test
    void testHoldSeats_InvalidSeatCount() throws Exception {
        // Create an invalid SeatHoldRequest (numSeats = 0)
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(0);
        request.setCustomerEmail("test@example.com");

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Serialize request to JSON
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Number of seats must be at least 1."));
    }

    @Test
    void testHoldSeats_InvalidEmail() throws Exception {
        // Create an invalid SeatHoldRequest (invalid email format)
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(3);
        request.setCustomerEmail("invalid_email");

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Serialize request to JSON
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format."));
    }

    @Test
    void testHoldSeats_MissingEmail() throws Exception {
        // Create an invalid SeatHoldRequest (missing email)
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(3);
        request.setCustomerEmail("");

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Serialize request to JSON
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer email is required."));
    }

    @Test
    void testHoldSeats_ExceedingAvailableSeats() throws Exception {
        // Create an invalid SeatHoldRequest (numSeats > available seats)
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(999);
        request.setCustomerEmail("test@example.com");

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Serialize request to JSON
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not enough available seats to hold."));
    }
}
