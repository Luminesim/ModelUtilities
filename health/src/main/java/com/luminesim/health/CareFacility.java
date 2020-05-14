package com.luminesim.health;

import lombok.NonNull;

import java.util.function.Consumer;

/**
 * A facility that can fulfill the needs of {@link Person}s.
 */
public interface CareFacility<T extends Person> extends CanFulfillNeed<T> {
    /**
     * Admits the person. The person may not receive care in a timely fashion
     * (or at all) if the facility cannot service their needs.
     *
     * @param person
     */
    void admit(@NonNull T person, @NonNull Consumer<T> onDischarge);

    /**
     * @see #admit(Person, Consumer)
     */
    default void admit(@NonNull T person) {
        admit(person, x -> {});
    }
}
