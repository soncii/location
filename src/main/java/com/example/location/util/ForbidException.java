package com.example.location.util;

public class ForbidException extends RuntimeException {

    public ForbidException() {

        super("Access Denied");
    }
}
