package com.example.location.util;

public class UnauthorizedException extends RuntimeException {


    public UnauthorizedException() {

        super("Email or password is incorrect");
    }
}
