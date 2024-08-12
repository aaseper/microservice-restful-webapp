package eolopark.server.controller.exception;

public class HttpClientErrorException extends RuntimeException {
    public HttpClientErrorException (String message) {
        super(message);
    }
}
