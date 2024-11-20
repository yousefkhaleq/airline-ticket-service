package com.ykhaleq.airlineticketservice.repository;

import com.ykhaleq.airlineticketservice.model.Seat;
import com.ykhaleq.airlineticketservice.model.SeatingLevel;
import com.ykhaleq.airlineticketservice.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AirplaneLayoutRepositoryTest {

    private AirplaneLayoutRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AirplaneLayoutRepository(new PricingService());
    }

    @Test
    void testInitializeLayout() {
        List<SeatingLevel> levels = repository.getSeatingLevels();
        assertEquals(4, levels.size());

        // Validate seating levels
        SeatingLevel firstClass = levels.get(0);
        assertEquals("First Class", firstClass.getLevelName());
        assertEquals(40, firstClass.getSeats().size()); // Total seats = rows * seatsPerRow

        SeatingLevel business = levels.get(1);
        assertEquals("Business", business.getLevelName());
        assertEquals(90, business.getSeats().size()); // 15 rows x 6 seats per row

        SeatingLevel premiumEconomy = levels.get(2);
        assertEquals("Premium Economy", premiumEconomy.getLevelName());
        assertEquals(120, premiumEconomy.getSeats().size());

        SeatingLevel economy = levels.get(3);
        assertEquals("Economy", economy.getLevelName());
        assertEquals(150, economy.getSeats().size());
    }

    @Test
    void testGetSeatingLevel_ValidLevel() {
        SeatingLevel level = repository.getSeatingLevel("First Class");
        assertNotNull(level);
        assertEquals("First Class", level.getLevelName());
        assertEquals(40, level.getSeats().size());
    }

    @Test
    void testGetSeatingLevel_InvalidLevel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getSeatingLevel("Nonexistent Level");
        });

        assertEquals("Invalid seating level: Nonexistent Level", exception.getMessage());
    }

    @Test
    void testReserveSeat() {
        repository.reserveSeat("First Class");

        SeatingLevel firstClass = repository.getSeatingLevel("First Class");
        long reservedSeats = firstClass.getSeats().stream().filter(Seat::isReserved).count();

        assertEquals(1, reservedSeats);
        assertEquals(1, firstClass.getReservedCount());
    }

    @Test
    void testReserveSeat_NoAvailableSeats() {
        SeatingLevel firstClass = repository.getSeatingLevel("First Class");
        int totalSeats = firstClass.getSeats().size();

        // Reserve all available seats
        for (int i = 0; i < totalSeats; i++) {
            repository.reserveSeat("First Class");
        }

        assertEquals(totalSeats, firstClass.getReservedCount());

        // Attempt to reserve another seat
        repository.reserveSeat("First Class"); // Should not increment reserved count
        assertEquals(totalSeats, firstClass.getReservedCount());
    }

    @Test
    void testGetNextSeatPrice() {
        // Initial price for "First Class"
        double price = repository.getNextSeatPrice("First Class");
        assertEquals(500.0, price); // Check pricing logic for reserved count 0-10

        // Reserve seats and check updated price
        for (int i = 0; i < 11; i++) { // Simulate crossing the 10-reserved threshold
            repository.reserveSeat("First Class");
        }
        price = repository.getNextSeatPrice("First Class");

        assertEquals(1000.0, price); // Check pricing logic for 11-30 reserved seats
    }

    @Test
    void testReserveSeat_InvalidLevel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.reserveSeat("Nonexistent Level");
        });

        assertEquals("Invalid seating level: Nonexistent Level", exception.getMessage());
    }
}
