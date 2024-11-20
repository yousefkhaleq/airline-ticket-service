package com.ykhaleq.airlineticketservice.controller;

import com.ykhaleq.airlineticketservice.dto.SeatHoldRequest;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import com.ykhaleq.airlineticketservice.service.SeatService;
import com.ykhaleq.airlineticketservice.service.SeatService.AvailableSeats;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SeatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- /available Endpoint ---
    @Test
    public void testGetAvailableSeats_AllLevels() throws Exception {
        List<AvailableSeats> mockResponse = List.of(
                new AvailableSeats("First Class", 10),
                new AvailableSeats("Economy", 50)
        );
        Mockito.when(seatService.numSeatsAvailable(Optional.empty())).thenReturn(mockResponse);

        mockMvc.perform(get("/seats/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].levelName").value("First Class"))
                .andExpect(jsonPath("$[0].availableSeats").value(10));
    }

    // --- /hold Endpoint ---
    @Test
    public void testHoldSeats_ValidRequest() throws Exception {
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(3);
        request.setCustomerEmail("test@example.com");

        SeatHold mockSeatHold = new SeatHold(1, List.of(), "test@example.com", 120);
        Mockito.when(seatService.findAndHoldSeats(3, "test@example.com")).thenReturn(mockSeatHold);

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value(1))
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"));
    }

    @Test
    public void testHoldSeats_InvalidSeatCount() throws Exception {
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(0);
        request.setCustomerEmail("test@example.com");

        Mockito.when(seatService.findAndHoldSeats(0, "test@example.com"))
                .thenThrow(new IllegalArgumentException("Number of seats must be greater than 0."));

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Number of seats must be greater than 0."));
    }

    // --- /reserve Endpoint ---
    @Test
    public void testReserveSeats_ValidHold() throws Exception {
        Mockito.when(seatService.reserveHeldSeats(1, "test@example.com")).thenReturn("CONFIRM-1");

        mockMvc.perform(post("/seats/reserve")
                        .param("holdId", "1")
                        .param("customerEmail", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("CONFIRM-1"));
    }

    @Test
    public void testReserveSeats_InvalidHoldId() throws Exception {
        Mockito.when(seatService.reserveHeldSeats(999, "test@example.com"))
                .thenThrow(new IllegalArgumentException("Seat hold with ID 999 not found."));

        mockMvc.perform(post("/seats/reserve")
                        .param("holdId", "999")
                        .param("customerEmail", "test@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Seat hold with ID 999 not found."));
    }

    // --- /reserve-direct Endpoint ---
    @Test
    public void testReserveSeatsDirect_ValidRequest() throws Exception {
        Mockito.when(seatService.reserveBestAvailableSeats(3, "test@example.com", 0, 1000, Optional.empty()))
                .thenReturn("CONFIRM-DIRECT-1");

        mockMvc.perform(post("/seats/reserve-direct")
                        .param("numSeats", "3")
                        .param("maxPrice", "1000")
                        .param("customerEmail", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("CONFIRM-DIRECT-1")));
    }

    @Test
    public void testReserveSeatsDirect_InvalidSeatCount() throws Exception {
        Mockito.when(seatService.reserveBestAvailableSeats(0, "test@example.com", 0, 1000, Optional.empty()))
                .thenThrow(new IllegalArgumentException("Number of seats must be greater than 0."));

        mockMvc.perform(post("/seats/reserve-direct")
                        .param("numSeats", "0")
                        .param("maxPrice", "1000")
                        .param("customerEmail", "test@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Number of seats must be greater than 0."));
    }
}
