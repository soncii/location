package com.example.location.util;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {

        super(message);
    }

    public BadRequestException() {

        super("Authorization header is missing");
    }
}
