package com.luminesim.qa;

import lombok.NonNull;

/**
 * Indicates that a method precondition has failed.
 */
public class InvalidPreconditionException extends RuntimeException {
    public InvalidPreconditionException(@NonNull String message) {
        super(message);
    }
}
