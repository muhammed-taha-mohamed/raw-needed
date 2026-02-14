package com.rawneeded.error.Handelars;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.error.exceptions.AbstractUnauthorizedException;
import com.rawneeded.error.exceptions.AccountInactiveException;
import com.rawneeded.error.exceptions.ExistingSessionException;
import com.rawneeded.error.exceptions.NoSearchesQuotaException;
import com.rawneeded.error.exceptions.PlanLimitExceededException;
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

    @ExceptionHandler({PlanLimitExceededException.class})
    public ResponseEntity<ResponsePayload> handlePlanLimitExceededException(PlanLimitExceededException e) {
        return error(BAD_REQUEST, e, e.getMessage());
    }

    @ExceptionHandler({AccountInactiveException.class})
    public ResponseEntity<ResponsePayload> handleAccountInactiveException(AccountInactiveException e) {
        return error(FORBIDDEN, e, e.getMessage());
    }

    @ExceptionHandler({NoSearchesQuotaException.class})
    public ResponseEntity<ResponsePayload> handleNoSearchesQuotaException(NoSearchesQuotaException e) {
        log.warn("No searches quota: {}", e.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
                ResponsePayload.builder()
                        .error(Map.of(
                                "errorMessage", e.getMessage(),
                                "errorCode", NoSearchesQuotaException.ERROR_CODE))
                        .build()
        );
    }

    @ExceptionHandler({ExistingSessionException.class})
    public ResponseEntity<ResponsePayload> handleExistingSessionException(ExistingSessionException e) {
        log.warn("Existing session for user: {}", e.getMessage());
        return ResponseEntity.status(513).body(
                ResponsePayload.builder()
                        .error(Map.of(
                                "errorMessage", e.getMessage(),
                                "errorCode", ExistingSessionException.ERROR_CODE))
                        .build()
        );
    }

}
