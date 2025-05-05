package com.rawneeded.error.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class AbstractUnauthorizedException extends RuntimeException {

    public AbstractUnauthorizedException(String message) {
        super(message);
    }
}
