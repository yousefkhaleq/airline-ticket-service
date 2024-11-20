package com.ykhaleq.airlineticketservice.service;

import com.ykhaleq.airlineticketservice.exception.InvalidRequestException;
import com.ykhaleq.airlineticketservice.exception.SeatHoldNotFoundException;
import com.ykhaleq.airlineticketservice.model.Seat;
import com.ykhaleq.airlineticketservice.model.SeatHold;
import com.ykhaleq.airlineticketservice.model.SeatingLevel;
import com.ykhaleq.airlineticketservice.repository.AirplaneLayoutRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeatService {

    private final AirplaneLayoutRepository layoutRepository;
    private final ConcurrentHashMap<Integer, SeatHold> ticketHolds;
    private final PricingService pricingService;
    private int holdIdCounter = 1;
    private final int holdExpirationSeconds;

    public SeatService(AirplaneLayoutRepository layoutRepository,
                       PricingService pricingService,
                       @Value("${seat.hold.expiration.seconds}") int holdExpirationSeconds) {
        this.layoutRepository = layoutRepository;
        this.ticketHolds = new ConcurrentHashMap<>();
        this.holdExpirationSeconds = holdExpirationSeconds;
        this.pricingService = pricingService;
    }

    /**
     * Get the number of available seats by level.
     * @param levelNames Optional array of level names to filter by.
     * @return List of available seats with their level and count.
     */
    public List<AvailableSeats> numSeatsAvailable(Optional<String[]> levelNames) {
        //Using Optional<String[]> for levelNames instead of a nullable array avoids null pointer issues
        List<AvailableSeats> result = new ArrayList<>();
        List<SeatingLevel> levels = layoutRepository.getSeatingLevels();

        // Filter levels if specific names are provided
        if (levelNames.isPresent()) {
            List<String> levelFilter = List.of(levelNames.get());
            levels = levels.stream()
                    .filter(level -> levelFilter.contains(level.getLevelName()))
                    .toList();
        }

        // Count available seats in each level
        for (SeatingLevel level : levels) {
            long availableSeats = level.getSeats().stream()
                    .filter(seat -> !seat.isHeld() && !seat.isReserved())
                    .count();

            result.add(new AvailableSeats(level.getLevelName(), availableSeats));
        }

        return result;
    }

    /**
     * Find and hold the best available seats in First Class.
     * The SeatHold Object is not an array due to only having First Class Seating
     *
     * @param numSeats      The number of seats to hold.
     * @param customerEmail The customer's email address.
     * @return A SeatHold object containing the held seats.
     */
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        SeatingLevel firstClassLevel = layoutRepository.getSeatingLevel("First Class");
        List<Seat> availableSeats = new ArrayList<>();

        // Collect available seats without modifying their state
        for (Seat seat : firstClassLevel.getSeats()) {
            if (!seat.isHeld() && !seat.isReserved()) {
                availableSeats.add(seat);
            }
            if (availableSeats.size() == numSeats) {
                break;
            }
        }

        // If not enough seats are available, throw an exception
        if (availableSeats.size() < numSeats) {
            throw new IllegalArgumentException("Not enough available seats to hold.");
        }

        // Mark the seats as held only after validation
        for (Seat seat : availableSeats) {
            seat.hold(); // Mark seat as held
        }

        // Create a SeatHold and store it
        int holdId = holdIdCounter++;
        SeatHold seatHold = new SeatHold(holdId, availableSeats, customerEmail, holdExpirationSeconds);
        ticketHolds.put(holdId, seatHold);

        return seatHold;
    }

    /**
     *	Commit seats held for a specific first class customer
     *
     *	@param holdId the seat hold identifier
     *	@param customerEmail the email address of the customer to which the seat hold is assigned
     *	@return a reservation confirmation code
     */
    public String reserveHeldSeats(int holdId, String customerEmail) {
        // Retrieve the hold
        SeatHold seatHold = ticketHolds.get(holdId);

        // Validate the hold
        if (seatHold == null) {
            throw new SeatHoldNotFoundException("Invalid hold ID.");
        }
        if (!seatHold.getCustomerEmail().equals(customerEmail)) {
            throw new InvalidRequestException("Customer email does not match the hold.");
        }
        if (seatHold.isExpired()) {
            // Release the seats if the hold is expired
            seatHold.getHeldSeats().forEach(Seat::release);
            ticketHolds.remove(holdId);
            throw new IllegalStateException("The hold has expired.");
        }

        // Reserve the seats
        seatHold.getHeldSeats().forEach(Seat::reserve);

        // Remove the hold from active holds
        ticketHolds.remove(holdId);

        // Generate a confirmation code
        return "CONFIRM-" + holdId + "-" + System.currentTimeMillis();
    }

    /**
     Reserve seats for business, premium economy, and economy level customers
     *
     *	@param numSeats the number of seats to reserve
     *	@param customerEmail the email address of the customer to which the seat hold is assigned
     *	@param levelNames an array of level name to limit the reservation, it is optional
     *	If not provided, find the best available level seats in the price range
     *	@param minPrice the minimum price
     *	@param maxPrice the maximum price
     *	@return a reservation confirmation code
     */
    public String reserveBestAvailableSeats(
            int numSeats,
            String customerEmail,
            int minPrice,
            int maxPrice,
            Optional<String[]> levelNames
    ) {
        List<Seat> availableSeats = new ArrayList<>();

        // Retrieve and filter levels if specific names are provided
        List<SeatingLevel> levels = layoutRepository.getSeatingLevels();
        if (levelNames.isPresent()) {
            List<String> levelFilter = List.of(levelNames.get());
            levels = levels.stream()
                    .filter(level -> levelFilter.contains(level.getLevelName()))
                    .toList();
        }

        // Collect all available seats within the price range
        for (SeatingLevel level : levels) {
            List<Seat> levelSeats = level.getSeats().stream()
                    .filter(seat -> !seat.isHeld() && !seat.isReserved())
                    .filter(seat -> {
                        double price = pricingService.calculatePrice(level.getLevelName(), level.getReservedCount());
                        return price >= minPrice && price <= maxPrice;
                    })
                    .toList();
            availableSeats.addAll(levelSeats);
        }

        // Ensure there are enough seats available
        if (availableSeats.size() < numSeats) {
            throw new IllegalArgumentException("Not enough available seats within the specified price range.");
        }

        // Reserve the best available seats
        List<Seat> seatsToReserve = availableSeats.subList(0, numSeats);
        seatsToReserve.forEach(Seat::reserve);

        // Generate and return confirmation code
        return "CONFIRM-DIRECT-" + System.currentTimeMillis();
    }

    // Nested DTO for returning available seats
    public static class AvailableSeats {
        private final String levelName;
        private final long availableSeats;

        public AvailableSeats(String levelName, long availableSeats) {
            this.levelName = levelName;
            this.availableSeats = availableSeats;
        }

        public String getLevelName() {
            return levelName;
        }

        public long getAvailableSeats() {
            return availableSeats;
        }
    }
}