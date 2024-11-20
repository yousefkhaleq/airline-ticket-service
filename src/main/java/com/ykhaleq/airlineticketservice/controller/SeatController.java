package com.ykhaleq.airlineticketservice.controller;

import com.ykhaleq.airlineticketservice.dto.DirectReservationRequest;
import com.ykhaleq.airlineticketservice.dto.ReserveSeatsRequest;
import com.ykhaleq.airlineticketservice.service.SeatService;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import com.ykhaleq.airlineticketservice.dto.SeatHoldRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/seats")
@Validated
public class SeatController {

    private final SeatService seatService;

    @Autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * Get available seats by level.
     */
    @GetMapping("/available")
    public ResponseEntity<List<SeatService.AvailableSeats>> getAvailableSeats(
            @RequestParam(required = false) String[] levelNames) {

        // Call the service method and pass levelNames if provided
        List<SeatService.AvailableSeats> availableSeats = seatService.numSeatsAvailable(Optional.ofNullable(levelNames));
        return ResponseEntity.ok(availableSeats);
    }

    /**
     * Hold seats for a customer.
     */
    @PostMapping("/hold")
    public ResponseEntity<SeatHold> holdSeats(@Valid @RequestBody SeatHoldRequest request) {
        SeatHold seatHold = seatService.findAndHoldSeats(request.getNumSeats(), request.getCustomerEmail());
        return ResponseEntity.ok(seatHold);
    }

    /**
     * Reserve held seats for a customer.
     */
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveHeldSeats(@Valid @RequestBody ReserveSeatsRequest request) {
        String confirmationCode = seatService.reserveHeldSeats(request.getHoldId(), request.getCustomerEmail());
        return ResponseEntity.ok(confirmationCode);
    }

    /**
     * Directly reserve best available seats within a price range.
     */
    @PostMapping("/reserve-direct")
    public ResponseEntity<String> reserveDirect(@Valid @RequestBody DirectReservationRequest request) {
        String confirmationCode = seatService.reserveBestAvailableSeats(
                request.getNumSeats(),
                request.getCustomerEmail(),
                (int) request.getMinPrice(),
                (int) request.getMaxPrice(),
                Optional.ofNullable(request.getLevelNames())
        );
        return ResponseEntity.ok(confirmationCode);
    }
}
