package com.chatappbackend.backend.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class BlockedUserId implements Serializable {
    private Long blockerId;
    private Long blockedId;
}