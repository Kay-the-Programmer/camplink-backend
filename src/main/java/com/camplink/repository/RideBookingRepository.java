package com.camplink.repository;

import com.camplink.entity.RideBooking;
import com.camplink.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideBookingRepository extends JpaRepository<RideBooking, String> {

    List<RideBooking> findByStatusOrderByCreatedAtDesc(RideStatus status);

    List<RideBooking> findByPassengerIdOrDriverIdOrderByCreatedAtDesc(
            String passengerId, String driverId);
}
