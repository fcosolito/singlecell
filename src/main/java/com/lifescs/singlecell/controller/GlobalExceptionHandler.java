package com.lifescs.singlecell.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler(Exception.class)
    // public ResponseEntity handleGenericException(Exception e) {
    // return new ResponseEntity<>("Exception caught in controller: " +
    // e.getMessage(), HttpStatus.OK);
    // }

}
