package com.schwab.urlshortener.api.exception;

public class DuplicateAliasException extends RuntimeException {
    public DuplicateAliasException(String message) {
        super(message);
    }
}
