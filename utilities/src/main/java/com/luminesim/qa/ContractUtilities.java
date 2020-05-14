package com.luminesim.qa;

import lombok.NonNull;

import java.util.function.Supplier;

/**
 * Contains utility functions for specifying method contracts.
 */
public class ContractUtilities {
    /**
     * @throws InvalidPreconditionException when the given condition is false
     */
    public static void precondition(@NonNull String message, boolean isTrue) {
        if (!isTrue) {
            throw new InvalidPreconditionException(message);
        }
    }

    /**
     * @throws InvalidPreconditionException when the given condition is false
     */
    public static void precondition(@NonNull Supplier<String> message, boolean isTrue) {
        if (!isTrue) {
            throw new InvalidPreconditionException(message.get());
        }
    }
    /**
     * @throws InvalidPostconditionException when the given condition is false
     */
    public static void postcondition(@NonNull String message, boolean isTrue) {
        if (!isTrue) {
            throw new InvalidPostconditionException(message);
        }
    }

    /**
     * @throws InvalidPostconditionException when the given condition is false
     */
    public static void postcondition(@NonNull Supplier<String> message, boolean isTrue) {
        if (!isTrue) {
            throw new InvalidPostconditionException(message.get());
        }
    }

    /**
     * @throws IllegalStateException when the given condition is false
     */
    public static void ensureThat(@NonNull String message, boolean isTrue) {
        if (!isTrue) {
            throw new IllegalStateException(message);
        }
    }
}
