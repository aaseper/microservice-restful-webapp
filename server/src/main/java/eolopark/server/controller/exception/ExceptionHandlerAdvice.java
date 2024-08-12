package eolopark.server.controller.exception;

import eolopark.server.controller.LogController;
import eolopark.server.model.dto.ApiRestResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

@Order (Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    private final LogController logController;

    public ExceptionHandlerAdvice (LogController logController) {
        this.logController = logController;
    }

    /* Methods */
    private boolean isWebError (HttpServletRequest request) {
        return request.getHeader("Accept").contains("text/html");
    }

    @ExceptionHandler (ResourceNotFoundException.class)
    public ResponseEntity<ApiRestResponse> handleResourceNotFoundException (ResourceNotFoundException e,
                                                                            HttpServletRequest request,
                                                                            HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/404");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler (TimeoutException.class)
    public ResponseEntity<ApiRestResponse> handleTimeoutException (TimeoutException e, HttpServletRequest request) {
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.REQUEST_TIMEOUT.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler (InsufficientAuthenticationException.class)
    public ResponseEntity<ApiRestResponse> handleForbidden (InsufficientAuthenticationException e,
                                                            HttpServletRequest request, HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/403");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.FORBIDDEN.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler (AccessDeniedException.class)
    public ResponseEntity<ApiRestResponse> handleAccessDeniedException (AccessDeniedException e,
                                                                        HttpServletRequest request,
                                                                        HttpServletResponse response) throws IOException {
        if (isWebError(request))
            try {response.sendRedirect("/403");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.FORBIDDEN.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler (BadCredentialsException.class)
    public ResponseEntity<ApiRestResponse> handleUnauthorized (BadCredentialsException e, HttpServletRequest request,
                                                               HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/403");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler ({Exception.class, RuntimeException.class})
    public ResponseEntity<Object> handleExceptionOrRuntimeException (Exception e, HttpServletRequest request,
                                                                     HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/error");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        logController.error("API RESPONSE");
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler (IllegalArgumentException.class)
    public ResponseEntity<ApiRestResponse> handleIllegalArgumentException (IllegalArgumentException e,
                                                                           HttpServletRequest request,
                                                                           HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/400");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler (NoSuchElementException.class)
    public ResponseEntity<ApiRestResponse> handleNoSuchElementException (NoSuchElementException e,
                                                                         HttpServletRequest request,
                                                                         HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/404");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), "The requested resource could " +
                "not be " + "found", HttpStatus.NOT_FOUND.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler (HttpClientErrorException.class)
    public ResponseEntity<ApiRestResponse> handleHttpClientErrorException (HttpClientErrorException e,
                                                                           HttpServletRequest request,
                                                                           HttpServletResponse response) {
        if (isWebError(request))
            try {response.sendRedirect("/429");} catch (IOException ex) {e.printStackTrace();} finally {
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
            }
        ApiRestResponse apiRestResponse = new ApiRestResponse(request.getRequestURI(), e.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiRestResponse, HttpStatus.TOO_MANY_REQUESTS);
    }
}
