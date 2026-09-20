package com.chatappbackend.backend.entity;

import jakarta.persistence.*;

import lombok.Data;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "message_embedding")
@Data
public class MessageEmbedding {
    @Id
    private Long messageId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "message_id")
    private Message message;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding")
    @Array(length = 768)
    private float[] embedding;
}