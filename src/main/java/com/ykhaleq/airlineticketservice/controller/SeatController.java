package com.ykhaleq.airlineticketservice.controller;

import com.ykhaleq.airlineticketservice.service.SeatService;
import com.ykhaleq.airlineticketservice.service.SeatService.AvailableSeats;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import org.springframework.beans.factory.annotation.Autowired;
import com.ykhaleq.airlineticketservice.dto.SeatHoldRequest;
import jakarta.validation.Valid;
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
    public List<AvailableSeats> getAvailableSeats(@RequestParam Optional<String[]> levelNames) {
        return seatService.numSeatsAvailable(levelNames);
    }

    /**
     * Hold seats for a customer.
     */
    @PostMapping("/hold")
    public SeatHold holdSeats(@Valid @RequestBody SeatHoldRequest request) {
        return seatService.findAndHoldSeats(request.getNumSeats(), request.getCustomerEmail());
    }

    /**
     * Reserve held seats for a customer.
     */
    @PostMapping("/reserve")
    public String reserveSeats(
            @RequestParam int holdId,
            @RequestParam String customerEmail
    ) {
        return seatService.reserveHeldSeats(holdId, customerEmail);
    }

    /**
     * Directly reserve best available seats within a price range.
     */
    @PostMapping("/reserve-direct")
    public String reserveBestSeats(
            @RequestParam int numSeats,
            @RequestParam String customerEmail,
            @RequestParam int minPrice,
            @RequestParam int maxPrice,
            @RequestParam Optional<String[]> levelNames
    ) {
        return seatService.reserveBestAvailableSeats(numSeats, customerEmail, minPrice, maxPrice, levelNames);
    }
}
