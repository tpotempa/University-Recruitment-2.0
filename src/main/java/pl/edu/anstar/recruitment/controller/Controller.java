package pl.edu.anstar.recruitment.controller;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import pl.edu.anstar.recruitment.exception.TechnicalException;
import pl.edu.anstar.recruitment.exception.UnauthorizedException;

import org.slf4j.LoggerFactory;

public abstract class Controller {

    @Autowired private HttpServletRequest request;

    public abstract Logger getLogger();
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> unauthorizedExceptionHandler(UnauthorizedException e) {
        getLogger().warn("Handling unauthorized access exception", e);
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<Map<String, String>> technicalExceptionHandler(TechnicalException e) {
        getLogger().warn("Handling technical exception", e);
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingParams(MissingRequestHeaderException ex) {
        String name = ex.getHeaderName();
        getLogger().warn(name + " header is missing");
        Map<String, String> response = new HashMap<>();
        if (name.equals("Authorization")) {
            response.put("status", "error");
            response.put("message", "Authorization header required ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        response.put("status", "error");
        response.put("message", name + " header is missing");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    protected String getServerHost() {
        HttpServletRequest request = getHttpServletRequest();
        return request
                .getRequestURL()
                .substring(0, request.getRequestURL().length() - request.getRequestURI().length());
    }

    protected HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }

    protected HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getResponse();
    }

}