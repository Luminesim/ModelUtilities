package com.luminesim.health;

import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * A person.
 */
public interface Person {

    /**
     * Adds a need to the person, if it has not already been added.
     *
     * @see Need#equals(Object)
     */
    void addNeed(@NonNull Need need);

    /**
     * Resolves the person's need.
     *
     * @param need
     * @precondition Person has this need.
     */
    default void resolveNeed(@NonNull Need need) {
    }

    /**
     * Resolves the person's need.
     *
     * @param name
     * @precondition Person has this need.
     */
    default void resolveNeed(@NonNull String name) {
        Need need = getNeed(name);
        resolveNeed(need);
    }

    default Set<Need> getNeeds() {
        return Collections.emptySet();
    }

    /**
     * @return
     *  The first need with the given name.
     * @pre {@link #hasNeed(String)}
     */
    default Need getNeed(@NonNull String name) {
        return getNeeds().stream().filter(n -> n.getName().equals(name)).findFirst().get();
    }

    /**
     * @param name
     * @return True, if the person has a need with the given name.
     */
    default boolean hasNeed(@NonNull String name) {
        return getNeeds().stream().anyMatch(n -> n.getName().equals(name));
    }
}
