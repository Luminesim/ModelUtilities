package com.luminesim.collections;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Allows weighted selection from a collection.
 * @param <T>
 */
@Slf4j
public class WeightedChoice <T> {

    private TreeMap<Double, T> weightedThings = new TreeMap<>();
    private Random rng;

    /**
     * Assigns a weight to each item. Items with non-positive weight are excluded.
     * @param items
     * @param weight
     * @pre items.size() >= 1
     */
    public WeightedChoice(@NonNull Random rng, @NonNull Collection<T> items, @NonNull Function<T, Double> weight) {

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one item to choose from.");
        }

        this.rng = rng;
        double totalWeight = 0;
        for (T next : items) {
            double w = weight.apply(next);
            if (w > 0) {
                totalWeight += w;
            }
            weightedThings.put(totalWeight, next);
        }
    }

    /**
     * @return
     *  A random value, weighted according to the preset weights.
     */
    public T pickRandom() {
        double raw = rng.nextDouble() * weightedThings.lastKey();
        double key = weightedThings.ceilingKey(raw);
        return weightedThings.get(key);
    }
}
