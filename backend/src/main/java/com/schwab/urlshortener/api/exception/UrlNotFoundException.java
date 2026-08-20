package com.schwab.urlshortener.api.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String code) {
        super("Short URL not found: " + code);
    }
}
