package com.camplink.dto;

import com.camplink.entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RideBookingResponse {
    private String id;
    private String passengerId;
    private String passengerName;
    private String from;
    private String to;
    private RouteDirection direction;
    private DropOff dropOff;
    private RideType type;
    private LocalDateTime scheduledAt;
    private int seats;
    private String note;
    private RideStatus status;
    private String driverId;
    private String driverName;
    private String driverPhone;
    private BigDecimal fare;
    private LocalDateTime createdAt;

    public static RideBookingResponse from(RideBooking r) {
        RideBookingResponse d = new RideBookingResponse();
        d.id            = r.getId();
        d.passengerId   = r.getPassenger().getId();
        d.passengerName = r.getPassenger().getFullName();
        d.from          = r.getFromLocation();
        d.to            = r.getToLocation();
        d.direction     = r.getDirection();
        d.dropOff       = r.getDropOff();
        d.type          = r.getType();
        d.scheduledAt   = r.getScheduledAt();
        d.seats         = r.getSeats();
        d.note          = r.getNote();
        d.status        = r.getStatus();
        if (r.getDriver() != null) {
            d.driverId    = r.getDriver().getId();
            d.driverName  = r.getDriver().getFullName();
            d.driverPhone = r.getDriver().getPhone();
        }
        d.fare          = r.getFare();
        d.createdAt     = r.getCreatedAt();
        return d;
    }
}
