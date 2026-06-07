package com.camplink.service;

import com.camplink.dto.RidePricesDto;
import com.camplink.entity.RidePricesEntity;
import com.camplink.repository.RidePricesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final RidePricesRepository repo;

    /// Current fares, or sensible defaults before an admin has saved any.
    /// Read-only — does not write on first access.
    public RidePricesDto getRidePrices() {
        return repo.findById(RidePricesEntity.SINGLETON_ID)
                .map(SettingsService::toDto)
                .orElseGet(SettingsService::defaults);
    }

    @Transactional
    public RidePricesDto updateRidePrices(RidePricesDto dto) {
        RidePricesEntity e = repo.findById(RidePricesEntity.SINGLETON_ID)
                .orElseGet(() -> RidePricesEntity.builder()
                        .id(RidePricesEntity.SINGLETON_ID).build());
        e.setCampusToTown(dto.getCampusToTown());
        e.setTownToAcross(dto.getTownToAcross());
        e.setTownToInsideCampus(dto.getTownToInsideCampus());
        return toDto(repo.save(e));
    }

    private static RidePricesDto toDto(RidePricesEntity e) {
        RidePricesDto d = new RidePricesDto();
        d.setCampusToTown(e.getCampusToTown());
        d.setTownToAcross(e.getTownToAcross());
        d.setTownToInsideCampus(e.getTownToInsideCampus());
        return d;
    }

    /// Matches RidePrices.defaults on the Flutter side.
    private static RidePricesDto defaults() {
        RidePricesDto d = new RidePricesDto();
        d.setCampusToTown(20.0);
        d.setTownToAcross(15.0);
        d.setTownToInsideCampus(25.0);
        return d;
    }
}
