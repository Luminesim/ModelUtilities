package com.luminesim.regions;

import lombok.NonNull;

import java.util.function.Function;

/**
 * Something with an arbitrary number of attributes.
 */
public interface HasAttributes {
    /**
     * Gets the attribute.
     * @param name
     * @param <T>
     * @return
     */
    <T> T getAttribute(@NonNull String name, Function<String, T> transform);

    /**
     *
     * @param name
     * @return
     *  True if the instance has an attribute with the given name.
     */
    boolean hasAttribute(@NonNull String name);

    /**
     * Gets the boolean value of the attribute according to {@link Boolean#valueOf(String)},
     * defaulting to FALSE if not present.
     * @param name
     * @return
     */
    default boolean getBoolean(@NonNull String name) {
        return getAttribute(name, Boolean::valueOf);
    }
    default String getString(@NonNull String name) {
        return getAttribute(name, x -> x);
    }

    /**
     * Gets the boolean value of the attribute according to {@link Double#valueOf(String)},
     * defaulting to 0 if not present.
     * @param name
     * @return
     * @throws Exception if attribute is not numeric.
     */
    default double getNumber(@NonNull String name) {
        return getAttribute(name, s -> hasAttribute(name) ? Double.valueOf(s) : 0);
    }

    /**
     * Gets the integer value of the attribute according to {@link Double#valueOf(String)}
     * and cast to an integer, defaulting to 0 if not present.
     * @param name
     * @return
     * @throws Exception if attribute is not numeric.
     */
    default int getInteger(@NonNull String name) {
        return (getAttribute(name, s -> hasAttribute(name) ? Double.valueOf(s) : 0)).intValue();
    }

    /**
     * Sets the attribute.
     * @param name
     * @pre value != null
     * @pre name != null
     */
    void setAttribute(@NonNull String name, @NonNull String value);
}
