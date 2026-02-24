package com.modoria.domain.season.service;

import com.modoria.domain.season.dto.response.SeasonResponse;

import java.util.List;

/**
 * Service interface for season operations.
 */
public interface SeasonService {

    SeasonResponse getCurrent();

    SeasonResponse getById(Long id);

    List<SeasonResponse> getAll();

    SeasonResponse create(String name, String displayName, int startMonth, int endMonth);

    SeasonResponse update(Long id, String displayName, String description);

    void delete(Long id);
}




