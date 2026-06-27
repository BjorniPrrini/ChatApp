package com.chatappbackend.backend.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class MessageDeleteId implements Serializable {
    private Long message;
    private Long user;
}