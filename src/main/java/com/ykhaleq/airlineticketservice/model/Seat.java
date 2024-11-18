package com.ykhaleq.airlineticketservice.model;

public class Seat {
    private final String seatNumber; // Unique identifier for the seat (e.g., "1A")
    private final String level;      // Seating level (e.g., "First Class")
    private boolean held;            // Whether the seat is temporarily held
    private boolean reserved;        // Whether the seat is reserved

    // Constructor: Initialize seat with seat number and level
    public Seat(String seatNumber, String level) {
        this.seatNumber = seatNumber;
        this.level = level;
        this.held = false;           // Default: not held
        this.reserved = false;       // Default: not reserved
    }

    // Getters
    public String getSeatNumber() {
        return seatNumber;
    }

    public String getLevel() {
        return level;
    }

    public boolean isHeld() {
        return held;
    }

    public boolean isReserved() {
        return reserved;
    }

    // Mark the seat as held
    public void hold() {
        this.held = true;
    }

    // Release the hold on the seat
    public void release() {
        this.held = false;
    }

    // Mark the seat as reserved
    public void reserve() {
        this.held = false;           // Clear any hold
        this.reserved = true;        // Mark as reserved
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatNumber='" + seatNumber + '\'' +
                ", level='" + level + '\'' +
                ", held=" + held +
                ", reserved=" + reserved +
                '}';
    }
}