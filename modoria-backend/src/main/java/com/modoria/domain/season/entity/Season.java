package com.modoria.domain.season.entity;

import com.modoria.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Month;

/**
 * Season entity for seasonal product categorization.
 * UI/theming is handled by the frontend - this only contains season metadata.
 */
@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Season extends BaseEntity {

    public static final String SPRING = "SPRING";
    public static final String SUMMER = "SUMMER";
    public static final String AUTUMN = "AUTUMN";
    public static final String WINTER = "WINTER";

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "start_month", nullable = false)
    private Month startMonth;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "end_month", nullable = false)
    private Month endMonth;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;
}


