package com.ykhaleq.airlineticketservice.service;

import com.ykhaleq.airlineticketservice.model.Seat;
import com.ykhaleq.airlineticketservice.model.SeatingLevel;
import com.ykhaleq.airlineticketservice.repository.AirplaneLayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AirlineTicketServiceTest {
    private AirplaneLayoutRepository repository;
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        // Initialize the PricingService and Repository before each test
        pricingService = new PricingService();
        repository = new AirplaneLayoutRepository(pricingService);
    }

    // Test: Seat Initialization and State Management
    @Test
    void testSeatInitializationAndState() {
        Seat seat = new Seat("1A", "First Class");

        assertEquals("1A", seat.getSeatNumber());
        assertEquals("First Class", seat.getLevel());
        assertFalse(seat.isHeld());
        assertFalse(seat.isReserved());

        seat.hold();
        assertTrue(seat.isHeld());
        assertFalse(seat.isReserved());

        seat.reserve();
        assertFalse(seat.isHeld());
        assertTrue(seat.isReserved());
    }

    // Test: SeatingLevel Initialization
    @Test
    void testSeatingLevelInitialization() {
        SeatingLevel level = new SeatingLevel("Business", 2, 3); // 2 rows, 3 seats per row
        List<Seat> seats = level.getSeats();

        assertEquals(6, seats.size()); // Total seats = 2 rows * 3 seats per row
        assertEquals("1A", seats.get(0).getSeatNumber());
        assertEquals("2C", seats.get(5).getSeatNumber());
    }

    // Test: Reserved Count in SeatingLevel
    @Test
    void testReservedCountInSeatingLevel() {
        SeatingLevel level = new SeatingLevel("Economy", 3, 2); // 3 rows, 2 seats per row
        assertEquals(0, level.getReservedCount());

        // Reserve some seats
        level.getSeats().get(0).reserve();
        level.reserveSeat();
        assertEquals(1, level.getReservedCount());

        level.getSeats().get(1).reserve();
        level.reserveSeat();
        assertEquals(2, level.getReservedCount());
    }

    // Test: PricingService Logic
    @Test
    void testPricingService() {
        // First Class Pricing
        assertEquals(500.0, pricingService.calculatePrice("First Class", 0));
        assertEquals(500.0, pricingService.calculatePrice("First Class", 10));
        assertEquals(1000.0, pricingService.calculatePrice("First Class", 20));
        assertEquals(1600.0, pricingService.calculatePrice("First Class", 31));

        // Business Pricing
        assertEquals(350.0, pricingService.calculatePrice("Business", 10));
        assertEquals(450.0, pricingService.calculatePrice("Business", 50));

        // Economy Pricing (Flat)
        assertEquals(200.0, pricingService.calculatePrice("Economy", 0));
        assertEquals(200.0, pricingService.calculatePrice("Economy", 100));
    }

    // Test: AirplaneLayoutRepository Initialization
    @Test
    void testAirplaneLayoutRepositoryInitialization() {
        List<SeatingLevel> levels = repository.getSeatingLevels();

        assertEquals(4, levels.size()); // 4 seating levels
        assertEquals("First Class", levels.get(0).getLevelName());
        assertEquals(10 * 4, levels.get(0).getSeats().size()); // 10 rows * 4 seats per row

        assertEquals("Business", levels.get(1).getLevelName());
        assertEquals(15 * 6, levels.get(1).getSeats().size()); // 15 rows * 6 seats per row
    }

    // Test: Reserve Seat and Dynamic Pricing
    @Test
    void testReserveSeatAndDynamicPricing() {
        // Reserve seats in First Class
        repository.reserveSeat("First Class");
        repository.reserveSeat("First Class");

        // Price should still be $500 for the first 10 reserved seats
        assertEquals(500.0, repository.getNextSeatPrice("First Class"));

        // Simulate 10 total reserved seats
        for (int i = 11; i < 30; i++) {
            repository.reserveSeat("First Class");
        }
        assertEquals(1000.0, repository.getNextSeatPrice("First Class")); // Price jumps after 10 reservations

        // Simulate 30 total reserved seats
        for (int i = 31; i < 50; i++) {
            repository.reserveSeat("First Class");
        }
        assertEquals(1600.0, repository.getNextSeatPrice("First Class")); // Price jumps after 30 reservations
    }
}
