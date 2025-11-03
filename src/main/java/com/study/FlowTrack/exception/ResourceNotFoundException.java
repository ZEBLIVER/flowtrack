package com.study.FlowTrack.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException() {
        super("The resource was not found");
    }
}
