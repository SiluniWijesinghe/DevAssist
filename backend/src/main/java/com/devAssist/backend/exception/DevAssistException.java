package com.devAssist.backend.exception;

public class DevAssistException extends RuntimeException {

    public DevAssistException(String message) {
        super(message);
    }

    public DevAssistException(String message, Throwable cause) {
        super(message, cause);
    }
}
