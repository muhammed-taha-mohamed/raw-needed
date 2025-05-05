package com.rawneeded.error.Handelars;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.error.exceptions.AbstractUnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {


    public ResponseEntity<ResponsePayload> error(HttpStatus status, Exception e, String msg) {
        log.error("error message :" + e.getMessage());
        return ResponseEntity.status(status).body(
                ResponsePayload.builder()
                        .error(Map.of("errorMessage", msg))
                        .build()
        );
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<ResponsePayload> handleException(Exception e) {
        return error(INTERNAL_SERVER_ERROR, e, e.getMessage());
    }


    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ResponsePayload> handleRunTimeException(RuntimeException e) {
        return error(INTERNAL_SERVER_ERROR, e,
                e.getMessage());
    }

    @ExceptionHandler({AbstractException.class})
    public ResponseEntity<ResponsePayload> handleCustomException(AbstractException e) {
        return error(INTERNAL_SERVER_ERROR, e,
                e.getMessage());
    }

    @ExceptionHandler({AbstractUnauthorizedException.class})
    public ResponseEntity<ResponsePayload> handleUnauthorizedException(AbstractUnauthorizedException e) {
        return error(UNAUTHORIZED, e,
                e.getMessage());
    }

}
