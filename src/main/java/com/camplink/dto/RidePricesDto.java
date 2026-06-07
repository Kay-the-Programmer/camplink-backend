package com.camplink.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/// Request and response body for ride fares. All three fares are per seat in ZMW.
@Data
public class RidePricesDto {
    @Positive(message = "campusToTown must be greater than 0")
    private double campusToTown;

    @Positive(message = "townToAcross must be greater than 0")
    private double townToAcross;

    @Positive(message = "townToInsideCampus must be greater than 0")
    private double townToInsideCampus;
}
