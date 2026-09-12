package com.carfinder.nlq.exception;

/** Thrown when the LLM response can't be parsed into a valid {@code VehicleFilter}. */
public class LlmParsingException extends RuntimeException {

    public LlmParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmParsingException(String message) {
        super(message);
    }
}
