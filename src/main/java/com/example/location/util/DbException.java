package com.example.location.util;

public class DbException extends RuntimeException {

    public DbException() {

        super("Could not interact with database");
    }
}
