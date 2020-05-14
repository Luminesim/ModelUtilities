package com.luminesim.health;

/**
 * A care need.
 */
public interface Need {
    String getName();

    /**
     * @return
     *  True, if the need requires a person's sex to be female, e.g. pregnancy.
     */
    default boolean requiresSexFemale() { return false; }

    /**
     * @return
     *  True, if the need requires a person's sex to be male, e.g. prostate cancer.
     */
    default boolean requiresSexMale() { return false; }
}
