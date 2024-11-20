package com.ykhaleq.airlineticketservice.controller;

import com.ykhaleq.airlineticketservice.service.SeatService;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import org.springframework.beans.factory.annotation.Autowired;
import com.ykhaleq.airlineticketservice.dto.SeatHoldRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/seats")
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
    public ResponseEntity<SeatHold> holdSeats(@RequestBody SeatHoldRequest request) {

        // Delegate the seat hold logic to the service
        SeatHold seatHold = seatService.findAndHoldSeats(request.getNumSeats(), request.getCustomerEmail());
        return ResponseEntity.ok(seatHold);
    }


    /**
     * Reserve held seats for a customer.
     */
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveSeats(
            @RequestParam int holdId,
            @RequestParam String customerEmail) {

        // Delegate the reservation logic to the service
        String confirmationCode = seatService.reserveHeldSeats(holdId, customerEmail);
        return ResponseEntity.ok(confirmationCode);
    }

    /**
     * Directly reserve best available seats within a price range.
     */
    @PostMapping("/reserve-direct")
    public ResponseEntity<String> reserveSeatsDirect(
            @RequestParam int numSeats,
            @RequestParam double maxPrice,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String[] levelNames){

        // Delegate the reservation logic to the service
        String confirmationCode = seatService.reserveBestAvailableSeats(
                numSeats,
                customerEmail,
                (int) minPrice,
                (int) maxPrice,
                Optional.ofNullable(levelNames)
        );
        return ResponseEntity.ok(confirmationCode);
    }
}
