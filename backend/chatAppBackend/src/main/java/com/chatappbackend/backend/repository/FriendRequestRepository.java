package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, String status);
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    @Query("SELECT fr FROM FriendRequest fr WHERE (fr.sender.id = :userId OR fr.receiver.id = :userId) AND fr.status = 'accepted'")
    List<FriendRequest> findAcceptedFriendships(@Param("userId") Long userId);
    @Query("SELECT COUNT(fr) > 0 FROM FriendRequest fr WHERE ((fr.sender.id = :userId AND fr.receiver.id = :otherId) OR (fr.sender.id = :otherId AND fr.receiver.id = :userId)) AND fr.status = 'accepted'")
    boolean areFriends(@Param("userId") Long userId, @Param("otherId") Long otherId);
}