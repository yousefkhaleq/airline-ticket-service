package com.ykhaleq.airlineticketservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ReserveSeatsRequest {

    @Min(value = 1, message = "Hold ID must be greater than 0.")
    private int holdId;

    @NotBlank(message = "Customer email is required.")
    @Email(message = "Invalid email format.")
    private String customerEmail;

    // Getters and setters
    public int getHoldId() {
        return holdId;
    }

    public void setHoldId(int holdId) {
        this.holdId = holdId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
