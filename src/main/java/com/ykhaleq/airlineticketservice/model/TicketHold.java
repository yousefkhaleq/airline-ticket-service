package com.ykhaleq.airlineticketservice.model;

import java.time.LocalDateTime;
import java.util.List;

public class TicketHold {
    private final int holdId;                // Unique hold identifier
    private final List<Seat> heldSeats;     // List of seats held
    private final String customerEmail;     // Customer's email
    private final LocalDateTime expirationTime; // Expiration time for the hold

    public TicketHold(int holdId, List<Seat> heldSeats, String customerEmail, int holdDurationSeconds) {
        this.holdId = holdId;
        this.heldSeats = heldSeats;
        this.customerEmail = customerEmail;
        this.expirationTime = LocalDateTime.now().plusSeconds(holdDurationSeconds);
    }

    // Check if the hold has expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    // Getters
    public int getHoldId() {
        return holdId;
    }

    public List<Seat> getHeldSeats() {
        return heldSeats;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    @Override
    public String toString() {
        return "TicketHold{" +
                "holdId=" + holdId +
                ", heldSeats=" + heldSeats +
                ", customerEmail='" + customerEmail + '\'' +
                ", expirationTime=" + expirationTime +
                '}';
    }
}