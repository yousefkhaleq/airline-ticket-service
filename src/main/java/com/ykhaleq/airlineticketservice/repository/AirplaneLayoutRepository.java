package com.ykhaleq.airlineticketservice.repository;

import com.ykhaleq.airlineticketservice.model.SeatingLevel;
import com.ykhaleq.airlineticketservice.service.PricingService;

import java.util.ArrayList;
import java.util.List;

public class AirplaneLayoutRepository {
    private final List<SeatingLevel> seatingLevels; // Stores all seating levels
    private final PricingService pricingService;   // Handles dynamic pricing logic

    public AirplaneLayoutRepository(PricingService pricingService) {
        this.seatingLevels = new ArrayList<>();
        this.pricingService = pricingService;
        initializeLayout();
    }

    // Initialize the airplane layout
    private void initializeLayout() {
        // First Class
        SeatingLevel firstClass = new SeatingLevel("First Class", 10, 4);
        seatingLevels.add(firstClass);

        // Business
        SeatingLevel business = new SeatingLevel("Business", 15, 6);
        seatingLevels.add(business);

        // Premium Economy
        SeatingLevel premiumEconomy = new SeatingLevel("Premium Economy", 20, 6);
        seatingLevels.add(premiumEconomy);

        // Economy
        SeatingLevel economy = new SeatingLevel("Economy", 25, 6);
        seatingLevels.add(economy);
    }

    // Get all seating levels
    public List<SeatingLevel> getSeatingLevels() {
        return seatingLevels;
    }

    // Get a specific seating level by name
    public SeatingLevel getSeatingLevel(String levelName) {
        return seatingLevels.stream()
                .filter(level -> level.getLevelName().equalsIgnoreCase(levelName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid seating level: " + levelName));
    }

    // Get the price of the next available seat in a given level
    public double getNextSeatPrice(String levelName) {
        SeatingLevel level = getSeatingLevel(levelName);
        int reservedCount = level.getReservedCount();
        return pricingService.calculatePrice(levelName, reservedCount);
    }

    // Reserve a seat in a specific level
    public void reserveSeat(String levelName) {
        SeatingLevel level = getSeatingLevel(levelName);

        // Find the first available seat
        level.getSeats().stream()
                .filter(seat -> !seat.isReserved() && !seat.isHeld())
                .findFirst()
                .ifPresent(seat -> {
                    seat.reserve();
                    level.reserveSeat(); // Update reserved count
                });
    }
}