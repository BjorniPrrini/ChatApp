package com.chatappbackend.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "user_summary_quota")
@Data
public class UserSummaryQuota {
    @Id
    private Long userId;

    @NotNull
    @ColumnDefault("5")
    @Column(name = "remaining_quota", nullable = false)
    private Integer remainingQuota;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "last_reset_date", nullable = false)
    private LocalDate lastResetDate;
}