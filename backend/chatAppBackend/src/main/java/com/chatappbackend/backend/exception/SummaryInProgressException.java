package com.chatappbackend.backend.exception;

public class SummaryInProgressException extends RuntimeException {
    public SummaryInProgressException(String message) {
        super(message);
    }
}