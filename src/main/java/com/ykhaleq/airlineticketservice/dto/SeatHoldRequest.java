package com.ykhaleq.airlineticketservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SeatHoldRequest {

    @Min(value = 1, message = "Number of seats must be at least 1.")
    private int numSeats;

    @NotBlank(message = "Customer email is required.")
    @Email(message = "Invalid email format.")
    private String customerEmail;

    // Getters and setters
    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}