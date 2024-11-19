package com.ykhaleq.airlineticketservice.exception;

public class SeatHoldNotFoundException extends RuntimeException {
    public SeatHoldNotFoundException(String message) {
        super(message);
    }
}
