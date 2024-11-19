package com.ykhaleq.airlineticketservice.service;

import com.ykhaleq.airlineticketservice.model.Seat;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import com.ykhaleq.airlineticketservice.repository.AirplaneLayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SeatServiceTest {
    private PricingService pricingService;
    private SeatService seatService;
    private AirplaneLayoutRepository repository;


    @BeforeEach
    void setUp() {
        repository = new AirplaneLayoutRepository(new PricingService());
        pricingService = new PricingService();
        seatService = new SeatService(repository, 120); // 120 seconds for hold expiration
    }

    @Test
    void testNumSeatsAvailable_AllLevels() {
        List<SeatService.AvailableSeats> result = seatService.numSeatsAvailable(Optional.empty());

        // Verify results for each level
        assertEquals(40, result.stream().filter(r -> r.getLevelName().equals("First Class")).findFirst().get().getAvailableSeats());
        assertEquals(90, result.stream().filter(r -> r.getLevelName().equals("Business")).findFirst().get().getAvailableSeats());
        assertEquals(120, result.stream().filter(r -> r.getLevelName().equals("Premium Economy")).findFirst().get().getAvailableSeats());
        assertEquals(150, result.stream().filter(r -> r.getLevelName().equals("Economy")).findFirst().get().getAvailableSeats());
    }

    @Test
    void testNumSeatsAvailable_FilteredLevels() {
        List<SeatService.AvailableSeats> result = seatService.numSeatsAvailable(Optional.of(new String[]{"First Class", "Economy"}));

        // Verify only filtered levels are returned
        assertEquals(2, result.size());
        assertEquals(40, result.stream().filter(r -> r.getLevelName().equals("First Class")).findFirst().get().getAvailableSeats());
        assertEquals(150, result.stream().filter(r -> r.getLevelName().equals("Economy")).findFirst().get().getAvailableSeats());
    }

    @Test
    void testFindAndHoldSeats_Success() {
        SeatHold seatHold = seatService.findAndHoldSeats(5, "test@example.com");

        assertNotNull(seatHold);
        assertEquals(5, seatHold.getHeldSeats().size());
        assertEquals("test@example.com", seatHold.getCustomerEmail());

        // Ensure the seats are held
        seatHold.getHeldSeats().forEach(seat -> assertTrue(seat.isHeld()));
    }

    @Test
    void testFindAndHoldSeats_InsufficientSeats() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.findAndHoldSeats(50, "test@example.com");
        });

        assertEquals("Not enough available seats to hold.", exception.getMessage());
    }

    @Test
    void testReserveHeldSeats_Success() {
        // Hold some seats
        SeatHold seatHold = seatService.findAndHoldSeats(3, "test@example.com");

        // Reserve the held seats
        String confirmationCode = seatService.reserveHeldSeats(seatHold.getHoldId(), "test@example.com");

        // Validate the confirmation code
        assertNotNull(confirmationCode);
        assertTrue(confirmationCode.startsWith("CONFIRM-"));

        // Ensure the seats are reserved
        seatHold.getHeldSeats().forEach(seat -> assertTrue(seat.isReserved()));
    }

    @Test
    void testReserveHeldSeats_InvalidHoldId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.reserveHeldSeats(999, "test@example.com"); // Invalid hold ID
        });
        assertEquals("Invalid hold ID.", exception.getMessage());
    }

    @Test
    void testReserveHeldSeats_EmailMismatch() {
        // Hold some seats
        SeatHold seatHold = seatService.findAndHoldSeats(3, "test@example.com");

        // Attempt to reserve with a different email
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.reserveHeldSeats(seatHold.getHoldId(), "wrong@example.com");
        });
        assertEquals("Customer email does not match the hold.", exception.getMessage());
    }

    @Test
    void testReserveHeldSeats_ExpiredHold() throws InterruptedException {
        // Hold some seats with a very short expiration time
        SeatService shortExpiryService = new SeatService(repository, 1); // 1 second expiration
        SeatHold seatHold = shortExpiryService.findAndHoldSeats(3, "test@example.com");

        // Wait for the hold to expire
        Thread.sleep(2000);

        // Attempt to reserve the expired hold
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            shortExpiryService.reserveHeldSeats(seatHold.getHoldId(), "test@example.com");
        });
        assertEquals("The hold has expired.", exception.getMessage());
    }

    @Test
    void testReserveBestAvailableSeats_Success() {
        // Reserve 3 seats in First Class within a specific price range
        String confirmationCode = seatService.reserveBestAvailableSeats(3, "test@example.com", 300, 700, Optional.of(new String[]{"First Class"}));

        // Verify confirmation code
        assertNotNull(confirmationCode);
        assertTrue(confirmationCode.startsWith("CONFIRM-DIRECT-"));

        // Verify that 3 seats were reserved
        List<Seat> reservedSeats = repository.getSeatingLevel("First Class").getSeats().stream()
                .filter(Seat::isReserved)
                .toList();
        assertEquals(3, reservedSeats.size());
    }

    @Test
    void testReserveBestAvailableSeats_InsufficientSeats() {
        // Attempt to reserve more seats than are available
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.reserveBestAvailableSeats(50, "test@example.com", 300, 700, Optional.of(new String[]{"First Class"}));
        });

        // Verify exception message
        assertEquals("Not enough available seats within the specified price range.", exception.getMessage());
    }

    @Test
    void testReserveBestAvailableSeats_PriceRangeFilter() {
        // Reserve 2 seats within a narrow price range
        String confirmationCode = seatService.reserveBestAvailableSeats(2, "test@example.com", 500, 500, Optional.of(new String[]{"First Class"}));

        // Verify confirmation code
        assertNotNull(confirmationCode);

        // Verify that only seats within the price range were reserved
        List<Seat> reservedSeats = repository.getSeatingLevel("First Class").getSeats().stream()
                .filter(Seat::isReserved)
                .toList();
        reservedSeats.forEach(seat -> {
            double price = pricingService.calculatePrice("First Class", repository.getSeatingLevel("First Class").getReservedCount());
            assertTrue(price >= 500 && price <= 500);
        });
    }

    @Test
    void testReserveBestAvailableSeats_NoSeatsInPriceRange() {
        // Attempt to reserve seats where no available seats match the price range
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.reserveBestAvailableSeats(3, "test@example.com", 1000, 1200, Optional.of(new String[]{"First Class"}));
        });

        // Verify exception message
        assertEquals("Not enough available seats within the specified price range.", exception.getMessage());
    }

    @Test
    void testReserveBestAvailableSeats_AllLevels() {
        // Reserve 3 seats without filtering by levels
        String confirmationCode = seatService.reserveBestAvailableSeats(3, "test@example.com", 200, 700, Optional.empty());

        // Verify confirmation code
        assertNotNull(confirmationCode);

        // Verify that 3 seats were reserved in total (First Class and possibly others)
        long totalReservedSeats = repository.getSeatingLevels().stream()
                .flatMap(level -> level.getSeats().stream())
                .filter(Seat::isReserved)
                .count();
        assertEquals(3, totalReservedSeats);
    }
}