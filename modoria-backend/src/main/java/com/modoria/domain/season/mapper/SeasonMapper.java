package com.modoria.domain.season.mapper;

import com.modoria.domain.season.entity.Season;
import com.modoria.domain.season.dto.response.SeasonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Simple mapper for Season entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SeasonMapper {

    @Mapping(target = "current", constant = "false")
    SeasonResponse toResponse(Season season);

    List<SeasonResponse> toResponseList(List<Season> seasons);
}


