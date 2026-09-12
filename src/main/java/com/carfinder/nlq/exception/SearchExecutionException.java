package com.carfinder.nlq.exception;

/** Thrown when the Elasticsearch query itself fails to execute. */
public class SearchExecutionException extends RuntimeException {

    public SearchExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
