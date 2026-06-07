package com.camplink.entity;

public enum UserRole {
    BUYER, SELLER, RIDER, DRIVER, ADMIN;

    /// True for roles that require admin verification before they can operate.
    public boolean isProvider() {
        return this == SELLER || this == RIDER || this == DRIVER;
    }

    /// Riders and drivers operate a vehicle, so must supply vehicle + NRC details.
    public boolean requiresVehicleInfo() {
        return this == RIDER || this == DRIVER;
    }
}
