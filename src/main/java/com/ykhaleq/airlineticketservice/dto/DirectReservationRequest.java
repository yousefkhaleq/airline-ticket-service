package com.ykhaleq.airlineticketservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class DirectReservationRequest {

    @Min(value = 1, message = "Number of seats must be greater than 0.")
    private int numSeats;

    @Min(value = 1, message = "Maximum price must be greater than 0.")
    private double maxPrice;

    private double minPrice;

    private String[] levelNames;

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

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public String[] getLevelNames() {
        return levelNames;
    }

    public void setLevelNames(String[] levelNames) {
        this.levelNames = levelNames;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
