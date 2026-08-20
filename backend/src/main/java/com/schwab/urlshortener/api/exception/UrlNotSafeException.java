package com.schwab.urlshortener.api.exception;

public class UrlNotSafeException extends RuntimeException {
    public UrlNotSafeException(String reason) {
        super("URL flagged as potentially unsafe: " + reason);
    }
}
