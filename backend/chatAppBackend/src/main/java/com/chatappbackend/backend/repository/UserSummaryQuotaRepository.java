package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.UserSummaryQuota;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface UserSummaryQuotaRepository extends JpaRepository<UserSummaryQuota, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE UserSummaryQuota u SET u.remainingQuota = u.remainingQuota - 1 WHERE u.userId = :userId AND u.remainingQuota > 0")
    int subtractOneQuota(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE UserSummaryQuota u SET u.remainingQuota = u.remainingQuota + 1 WHERE u.userId = :userId AND u.remainingQuota < 5")
    int addOneQuota(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE UserSummaryQuota u SET u.remainingQuota = 5, u.lastResetDate = :lastDateReseted WHERE u.userId = :userId AND u.lastResetDate < :lastDateReseted")
    int resetQuotaLimit(@Param("userId") Long userId, @Param("lastDateReseted") LocalDate lastDateReseted);
    @Query("SELECT u.remainingQuota FROM UserSummaryQuota u WHERE u.userId = :userId")
    Integer findRemainingQuota(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_summary_quota (user_id, remaining_quota, last_reset_date) VALUES (:userId, 5, :today) ON CONFLICT (user_id) DO NOTHING", nativeQuery = true)
    int insertQuotaIfAbsent(@Param("userId") Long userId, @Param("today") LocalDate today);
}