package com.luminesim.health;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * An entity that can fulfill a need for another entity.
 */
public interface CanFulfillNeed<T> {
    /**
     * @param target
     * @param need
     * @return
     *  True, if the entity can fulfill the needs for the given target.
     */
    boolean canFulfillNeeds(T target, Collection<Need> need);

    /**
     *
     * @param target
     * @param needs
     * @param timeUnit
     * @return
     *  The estimated time until the entity can fulfill the needs of the given targer.
     */
    double estimatedTimeToFulfillNeeds(T target, Collection<Need> needs, TimeUnit timeUnit);
}
