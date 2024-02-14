package com.cyster.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(RestException.class)
    public ResponseEntity<String> handleCustomException(RestException exception) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            logger.error("RestException: " + exception.toString(), exception);

            return ResponseEntity
                .status(exception.getStatusCode())
                .body(objectMapper.writeValueAsString(exception));
        } catch (JsonProcessingException exception2) {
            logger.error("RestException failed converting to json: " + exception.toString(), exception);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{ \"message\": \"error converting error to json response\"}");
        }
    }

}