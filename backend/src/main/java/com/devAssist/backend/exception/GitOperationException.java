package com.devAssist.backend.exception;

public class GitOperationException extends DevAssistException {

    public GitOperationException(String message) {
        super(message);
    }

    public GitOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}