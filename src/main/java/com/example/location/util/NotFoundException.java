package com.example.location.util;

public class NotFoundException extends RuntimeException{

    public NotFoundException(String message) {

        super(message+ " not found");
    }

}
