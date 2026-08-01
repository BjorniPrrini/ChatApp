package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByEmail(String email);
    List<User> findByEmailContainingIgnoreCase(String email);
    List<User> findByNicknameContainingIgnoreCase(String nickname);
    @Query("SELECT u FROM User u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<User> searchByNicknameOrName(@Param("term") String term);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isOnline = false WHERE u.id = :userId")
    void updateUserStatusOffline(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isOnline = true WHERE u.id = :userId")
    void updateUserStatusOnline(@Param("userId") Long userId);
}