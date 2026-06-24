package com.chatappbackend.backend.repository;

import com.chatappbackend.backend.entity.BlockedUser;
import com.chatappbackend.backend.entity.BlockedUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, BlockedUserId> {

}