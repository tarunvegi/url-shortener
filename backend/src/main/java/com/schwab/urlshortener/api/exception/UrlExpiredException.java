package com.schwab.urlshortener.api.exception;

public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String code) {
        super("Short URL has expired: " + code);
    }
}
