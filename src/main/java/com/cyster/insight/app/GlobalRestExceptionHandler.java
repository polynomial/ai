package com.cyster.insight.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(RestException.class)
    public ResponseEntity<String> handleCustomException(RestException exception) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return ResponseEntity
                .status(exception.getStatusCode())
                .body(objectMapper.writeValueAsString(exception));
        } catch (JsonProcessingException e) {
            System.out.println("RestException: " + exception.toString());
            exception.printStackTrace(System.out);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{ \"message\": \"error converting error to json response\"}");
        }
    }

}