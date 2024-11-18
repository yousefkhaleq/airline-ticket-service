package com.ykhaleq.airlineticketservice.model;

import java.util.ArrayList;
import java.util.List;

public class SeatingLevel {
    private final String levelName;  // Name of the level (e.g., "First Class")
    private final int rows;          // Number of rows in this level
    private final int seatsPerRow;   // Number of seats in each row
    private final List<Seat> seats;  // List of all seats in this level
    private int reservedCount;       // Total number of reserved seats

    public SeatingLevel(String levelName, int rows, int seatsPerRow) {
        this.levelName = levelName;
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
        this.seats = new ArrayList<>();
        this.reservedCount = 0;      // Initialize reserved count to zero
        initializeSeats();
    }

    // Initialize seats
    private void initializeSeats() {
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= seatsPerRow; col++) {
                // Generate a seat number like "1A", "1B", etc.
                String seatNumber = row + Character.toString((char) ('A' + col - 1));
                // Add the seat to the list
                seats.add(new Seat(seatNumber, levelName));
            }
        }
    }

    // Increment the reserved count
    public void reserveSeat() {
        this.reservedCount++;
    }

    // Getters
    public int getReservedCount() {
        return reservedCount;
    }

    public String getLevelName() {
        return levelName;
    }

    public List<Seat> getSeats() {
        return seats;
    }
}
