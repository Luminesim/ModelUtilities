package com.luminesim.qa;

import lombok.NonNull;

/**
 * Indicates that a method postcondition has failed.
 */
public class InvalidPostconditionException extends RuntimeException {
    public InvalidPostconditionException(@NonNull String s) {
        super(s);
    }
}
