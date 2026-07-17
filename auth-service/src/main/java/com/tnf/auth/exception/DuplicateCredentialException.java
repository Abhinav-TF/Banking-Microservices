package com.tnf.auth.exception;

public class DuplicateCredentialException extends RuntimeException {

    public DuplicateCredentialException(String message) {
        super(message);
    }
}
