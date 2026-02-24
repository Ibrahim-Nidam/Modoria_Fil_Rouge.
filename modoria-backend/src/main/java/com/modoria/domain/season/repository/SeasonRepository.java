package com.modoria.domain.season.repository;

import com.modoria.domain.season.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Season entity operations.
 */
@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {

    Optional<Season> findByName(String name);

    boolean existsByName(String name);

    List<Season> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query(value = "SELECT * FROM seasons s WHERE s.is_active = true AND " +
            "((s.start_month <= :monthOrdinal AND s.end_month >= :monthOrdinal) OR " +
            "(s.start_month > s.end_month AND (s.start_month <= :monthOrdinal OR s.end_month >= :monthOrdinal)))", nativeQuery = true)
    Optional<Season> findCurrentSeason(@Param("monthOrdinal") int monthOrdinal);
}


