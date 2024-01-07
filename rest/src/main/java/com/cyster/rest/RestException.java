package com.cyster.rest;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

// TODO remove suppressed from json which is final

public class RestException extends Exception {
    private HttpStatus statusCode;
    private String message;

    public RestException(HttpStatus statusCode, String message) {
        super();
        this.statusCode = statusCode;
        this.message = message;
    }

    public RestException(HttpStatus statusCode, String message, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
        this.message = message;
    }

    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    public String getMessage() {
        return this.message;
    }

    @JsonIgnore
    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

}