package com.ykhaleq.airlineticketservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykhaleq.airlineticketservice.dto.SeatHoldRequest;
import com.ykhaleq.airlineticketservice.dto.ReserveSeatsRequest;
import com.ykhaleq.airlineticketservice.dto.DirectReservationRequest;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import com.ykhaleq.airlineticketservice.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class SeatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @Autowired
    private ObjectMapper objectMapper;

    private SeatHold seatHold;

    @BeforeEach
    public void setUp() {
        seatHold = new SeatHold(1, new ArrayList<>(), "test@example.com", 120);
        Mockito.when(seatService.findAndHoldSeats(any(Integer.class), any(String.class)))
                .thenReturn(seatHold);

        Mockito.when(seatService.reserveHeldSeats(any(Integer.class), any(String.class)))
                .thenReturn("CONFIRM-12345");

        Mockito.when(seatService.reserveBestAvailableSeats(
                any(Integer.class),
                any(String.class),
                any(Integer.class),
                any(Integer.class),
                any(Optional.class))
        ).thenReturn("CONFIRM-DIRECT-67890");
    }

    @Test
    public void testHoldSeats_ValidRequest() throws Exception {
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(3);
        request.setCustomerEmail("test@example.com");

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value(1));
    }

    @Test
    public void testHoldSeats_InvalidRequest() throws Exception {
        SeatHoldRequest request = new SeatHoldRequest();
        request.setNumSeats(0); // Invalid number of seats
        request.setCustomerEmail("test@example.com"); // Invalid email

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testReserveHeldSeats_ValidRequest() throws Exception {
        ReserveSeatsRequest request = new ReserveSeatsRequest();
        request.setHoldId(1);
        request.setCustomerEmail("test@example.com");

        mockMvc.perform(post("/seats/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("CONFIRM-12345"));
    }

    @Test
    public void testReserveHeldSeats_InvalidRequest() throws Exception {
        ReserveSeatsRequest request = new ReserveSeatsRequest();
        request.setHoldId(0); // Invalid holdId
        request.setCustomerEmail(""); // Blank email

        mockMvc.perform(post("/seats/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testReserveDirect_ValidRequest() throws Exception {
        DirectReservationRequest request = new DirectReservationRequest();
        request.setNumSeats(3);
        request.setMaxPrice(1000);
        request.setCustomerEmail("test@example.com");
        request.setLevelNames(new String[]{"First Class"});

        mockMvc.perform(post("/seats/reserve-direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("CONFIRM-DIRECT-67890"));
    }

    @Test
    public void testReserveDirect_InvalidRequest() throws Exception {
        DirectReservationRequest request = new DirectReservationRequest();
        request.setNumSeats(0); // Invalid number of seats
        request.setMaxPrice(-100); // Invalid max price
        request.setCustomerEmail(""); // Blank email

        mockMvc.perform(post("/seats/reserve-direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}