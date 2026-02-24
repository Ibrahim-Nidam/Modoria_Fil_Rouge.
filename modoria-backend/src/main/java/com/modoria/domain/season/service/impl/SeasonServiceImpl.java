package com.modoria.domain.season.service.impl;

import com.modoria.domain.season.entity.Season;
import com.modoria.domain.season.dto.response.SeasonResponse;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.season.mapper.SeasonMapper;
import com.modoria.domain.season.repository.SeasonRepository;
import com.modoria.domain.season.service.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Implementation of SeasonService for season operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonMapper seasonMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "current-season")
    public SeasonResponse getCurrent() {
        log.debug("Fetching current season");
        Month currentMonth = LocalDate.now().getMonth();

        return seasonRepository.findCurrentSeason(currentMonth.ordinal())
                .map(season -> {
                    SeasonResponse response = seasonMapper.toResponse(season);
                    response.setCurrent(true);
                    return response;
                })
                .orElseGet(() -> {
                    Season defaultSeason = getDefaultSeasonForMonth(currentMonth);
                    SeasonResponse response = seasonMapper.toResponse(defaultSeason);
                    response.setCurrent(true);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse getById(Long id) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.season(id));
        return seasonMapper.toResponse(season);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeasonResponse> getAll() {
        return seasonMapper.toResponseList(
                seasonRepository.findByIsActiveTrueOrderByDisplayOrderAsc());
    }

    @Override
    @Transactional
    public SeasonResponse create(String name, String displayName, int startMonth, int endMonth) {
        log.info("Creating season: {}", name);
        Season season = Season.builder()
                .name(name.toUpperCase())
                .displayName(displayName)
                .startMonth(Month.of(startMonth))
                .endMonth(Month.of(endMonth))
                .isActive(true)
                .build();

        season = seasonRepository.save(season);
        log.info("Season created with ID: {}", season.getId());
        return seasonMapper.toResponse(season);
    }

    @Override
    @Transactional
    public SeasonResponse update(Long id, String displayName, String description) {
        log.info("Updating season: {}", id);
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.season(id));

        if (displayName != null)
            season.setDisplayName(displayName);
        if (description != null)
            season.setDescription(description);

        season = seasonRepository.save(season);
        return seasonMapper.toResponse(season);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting season: {}", id);
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.season(id));
        seasonRepository.delete(season);
        log.info("Season {} deleted", id);
    }

    private Season getDefaultSeasonForMonth(Month month) {
        String seasonName;
        Month startMonth;
        Month endMonth;

        switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> {
                seasonName = Season.WINTER;
                startMonth = Month.DECEMBER;
                endMonth = Month.FEBRUARY;
            }
            case MARCH, APRIL, MAY -> {
                seasonName = Season.SPRING;
                startMonth = Month.MARCH;
                endMonth = Month.MAY;
            }
            case JUNE, JULY, AUGUST -> {
                seasonName = Season.SUMMER;
                startMonth = Month.JUNE;
                endMonth = Month.AUGUST;
            }
            default -> {
                seasonName = Season.AUTUMN;
                startMonth = Month.SEPTEMBER;
                endMonth = Month.NOVEMBER;
            }
        }

        return Season.builder()
                .name(seasonName)
                .displayName(seasonName.charAt(0) + seasonName.substring(1).toLowerCase())
                .startMonth(startMonth)
                .endMonth(endMonth)
                .isActive(true)
                .build();
    }
}
