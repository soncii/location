package com.example.location.controllers;

import com.example.location.util.BadRequestException;
import com.example.location.util.ForbidException;
import com.example.location.util.NotFoundException;
import com.example.location.util.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof ForbidException) status = HttpStatus.FORBIDDEN;
        if (ex instanceof BadRequestException) status = HttpStatus.BAD_REQUEST;
        if (ex instanceof NumberFormatException) status = HttpStatus.BAD_REQUEST;
        if (ex instanceof UnauthorizedException) status = HttpStatus.UNAUTHORIZED;
        if (ex instanceof NoSuchElementException) status = HttpStatus.NOT_FOUND;
        if (ex instanceof NotFoundException) status = HttpStatus.NOT_FOUND;
        if (ex instanceof CompletionException && ex.getCause() instanceof ForbidException) status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(ex.getMessage());
    }
}
