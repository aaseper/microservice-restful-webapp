package eolopark.planner.controller.exception;

import eolopark.planner.model.ApiRestResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    /* Methods */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiRestResponse> handleResourceNotFoundException(ResourceNotFoundException e,
                                                                           HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiRestResponse> handleAccessDeniedException(AccessDeniedException e,
                                                                       HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.FORBIDDEN.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<Object> handleExceptionOrRuntimeException(Exception e, HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiRestResponse> handleIllegalArgumentException(IllegalArgumentException e,
                                                                          HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiRestResponse> handleNoSuchElementException(NoSuchElementException e,
                                                                        HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), "The requested resource could not be " +
                "found", HttpStatus.NOT_FOUND.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiRestResponse> handleHttpClientErrorException(HttpClientErrorException e,
                                                                          HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.TOO_MANY_REQUESTS);
    }
}
