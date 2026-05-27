package com.camplink.dto;

import com.camplink.entity.ShoppingRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ShoppingRequestResponse {
    private String id;
    private String requesterId;
    private String requesterName;
    private String title;
    private List<ShoppingRequestItemDto> items;
    private String deliveryHostel;
    private String deliveryRoom;
    private BigDecimal budget;
    private String note;
    private String status;
    private String runnerId;
    private String runnerName;
    private BigDecimal runnerFee;
    private LocalDateTime createdAt;

    public static ShoppingRequestResponse from(ShoppingRequest r) {
        ShoppingRequestResponse d = new ShoppingRequestResponse();
        d.id = r.getId();
        d.requesterId = r.getRequester().getId();
        d.requesterName = r.getRequester().getFullName();
        d.title = r.getTitle();
        d.items = r.getItems().stream().map(ShoppingRequestItemDto::from).collect(Collectors.toList());
        d.deliveryHostel = r.getDeliveryHostel();
        d.deliveryRoom = r.getDeliveryRoom();
        d.budget = r.getBudget();
        d.note = r.getNote();
        d.status = r.getStatus().name();
        if (r.getRunner() != null) {
            d.runnerId = r.getRunner().getId();
            d.runnerName = r.getRunner().getFullName();
        }
        d.runnerFee = r.getRunnerFee();
        d.createdAt = r.getCreatedAt();
        return d;
    }
}
