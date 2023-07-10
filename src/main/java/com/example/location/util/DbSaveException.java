package com.example.location.util;

public class DbSaveException extends RuntimeException {

    public DbSaveException() {

        super("Could not save to database");
    }
}
