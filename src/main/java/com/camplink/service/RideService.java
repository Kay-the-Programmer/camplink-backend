package com.camplink.service;

import com.camplink.dto.RideBookingRequest;
import com.camplink.dto.RideBookingResponse;
import com.camplink.dto.RidePricesDto;
import com.camplink.entity.*;
import com.camplink.exception.AppException;
import com.camplink.repository.RideBookingRepository;
import com.camplink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideBookingRepository rideRepo;
    private final UserRepository userRepo;
    private final SettingsService settingsService;
    private final NotificationService notificationService;

    @Transactional
    public RideBookingResponse create(String passengerId, RideBookingRequest req) {
        User passenger = userRepo.findById(passengerId)
                .orElseThrow(() -> AppException.notFound("Passenger not found"));

        int seats = (req.getSeats() == null || req.getSeats() < 1) ? 1 : req.getSeats();
        if (req.getType() == RideType.SCHEDULED && req.getScheduledAt() == null) {
            throw AppException.badRequest("scheduledAt is required for a scheduled ride");
        }

        RideBooking ride = RideBooking.builder()
                .id(UUID.randomUUID().toString())
                .passenger(passenger)
                .fromLocation(req.getFrom())
                .toLocation(req.getTo())
                .direction(req.getDirection())
                .dropOff(req.getDropOff())
                .type(req.getType())
                .scheduledAt(req.getType() == RideType.SCHEDULED ? req.getScheduledAt() : null)
                .seats(seats)
                .note(req.getNote())
                .status(RideStatus.PENDING)
                .fare(estimateFare(req.getDirection(), req.getDropOff(), seats))
                .build();

        // saveAndFlush so @CreationTimestamp is populated before we serialise.
        return RideBookingResponse.from(rideRepo.saveAndFlush(ride));
    }

    public List<RideBookingResponse> pending() {
        return rideRepo.findByStatusOrderByCreatedAtDesc(RideStatus.PENDING)
                .stream().map(RideBookingResponse::from).toList();
    }

    public List<RideBookingResponse> mine(String userId) {
        return rideRepo.findByPassengerIdOrDriverIdOrderByCreatedAtDesc(userId, userId)
                .stream().map(RideBookingResponse::from).toList();
    }

    @Transactional
    public RideBookingResponse accept(String rideId, String driverId) {
        RideBooking ride = load(rideId);
        if (ride.getStatus() != RideStatus.PENDING) {
            throw AppException.badRequest("This ride is no longer available");
        }
        if (ride.getPassenger().getId().equals(driverId)) {
            throw AppException.badRequest("You cannot accept your own ride");
        }
        User driver = userRepo.findById(driverId)
                .orElseThrow(() -> AppException.notFound("Driver not found"));
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        RideBooking saved = rideRepo.save(ride);

        // Tell the passenger their ride was accepted.
        notificationService.push(
                ride.getPassenger().getId(),
                NotificationType.RIDE_ACCEPTED,
                "Ride accepted",
                driver.getFullName() + " accepted your ride to " + ride.getToLocation() + ".",
                ride.getId());

        return RideBookingResponse.from(saved);
    }

    @Transactional
    public RideBookingResponse complete(String rideId, String callerId) {
        RideBooking ride = load(rideId);
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(callerId)) {
            throw AppException.forbidden("Only the assigned driver can complete this ride");
        }
        ride.setStatus(RideStatus.COMPLETED);
        RideBooking saved = rideRepo.save(ride);

        notificationService.push(
                ride.getPassenger().getId(),
                NotificationType.RIDE_COMPLETED,
                "Ride completed",
                "Your ride to " + ride.getToLocation() + " is complete. Safe travels!",
                ride.getId());

        return RideBookingResponse.from(saved);
    }

    @Transactional
    public RideBookingResponse cancel(String rideId, String callerId) {
        RideBooking ride = load(rideId);
        boolean isPassenger = ride.getPassenger().getId().equals(callerId);
        boolean isDriver = ride.getDriver() != null && ride.getDriver().getId().equals(callerId);
        if (!isPassenger && !isDriver) {
            throw AppException.forbidden("Only the passenger or driver can cancel this ride");
        }
        ride.setStatus(RideStatus.CANCELLED);
        RideBooking saved = rideRepo.save(ride);

        // Notify whichever party did NOT cancel (if a driver was assigned).
        if (ride.getDriver() != null) {
            String recipient = isPassenger
                    ? ride.getDriver().getId()
                    : ride.getPassenger().getId();
            notificationService.push(
                    recipient,
                    NotificationType.RIDE_CANCELLED,
                    "Ride cancelled",
                    (isPassenger ? ride.getPassenger().getFullName() : ride.getDriver().getFullName())
                            + " cancelled the ride to " + ride.getToLocation() + ".",
                    ride.getId());
        }

        return RideBookingResponse.from(saved);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private RideBooking load(String rideId) {
        return rideRepo.findById(rideId)
                .orElseThrow(() -> AppException.notFound("Ride not found"));
    }

    /// Per-seat fare comes from the admin-configured ride prices.
    private BigDecimal estimateFare(RouteDirection direction, DropOff dropOff, int seats) {
        RidePricesDto p = settingsService.getRidePrices();
        double perSeat = direction == RouteDirection.CAMPUS_TO_TOWN
                ? p.getCampusToTown()
                : dropOff == DropOff.ACROSS
                    ? p.getTownToAcross()
                    : p.getTownToInsideCampus();
        return BigDecimal.valueOf(perSeat * seats);
    }
}
