package com.camplink.dto;

import com.camplink.entity.ServiceListing;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceListingResponse {
    private String id;
    private String providerId;
    private String providerName;
    private String providerPhone;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String priceNote;
    private boolean available;
    private LocalDateTime createdAt;

    public static ServiceListingResponse from(ServiceListing s) {
        ServiceListingResponse d = new ServiceListingResponse();
        d.id            = s.getId();
        d.providerId    = s.getProvider().getId();
        d.providerName  = s.getProvider().getFullName();
        d.providerPhone = s.getProvider().getPhone();
        d.title         = s.getTitle();
        d.description   = s.getDescription();
        d.category      = s.getCategory();
        d.price         = s.getPrice();
        d.priceNote     = s.getPriceNote();
        d.available     = s.isAvailable();
        d.createdAt     = s.getCreatedAt();
        return d;
    }
}
