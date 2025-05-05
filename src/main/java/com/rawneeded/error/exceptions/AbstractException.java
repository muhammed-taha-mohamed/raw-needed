package com.rawneeded.error.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class AbstractException extends RuntimeException {

    public AbstractException(String message) {
        super(message);
    }
}
