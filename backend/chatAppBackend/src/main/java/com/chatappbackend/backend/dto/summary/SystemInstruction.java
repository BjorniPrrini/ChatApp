package com.chatappbackend.backend.dto.summary;

import com.chatappbackend.backend.dto.embedding.Part;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemInstruction {
    private List<Part> parts;
}