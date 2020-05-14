package com.luminesim.regions;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.luminesim.qa.ContractUtilities.precondition;
import static java.lang.Integer.max;
import static java.lang.Math.random;

/**
 * A population of people.
 */
public class Population<SegmentType> implements HasAttributes {

    /**
     * The numbers of people in the population.
     */
    private Table<SegmentType, Range<Integer>, Integer> numbers = HashBasedTable.create();


    /**
     * Attributes of the population.
     */
    private Map<String, String> attributes = new HashMap<>();

    /**
     *
     * @return
     *  An empty population.
     */
    public static Population<?> empty() {
        return new Population<>();
    }

    /**
     *
     * @return
     *  A new population excluding the given populations. If the other population has more individuals than this
     *  population for any segments, the number of individuals for those segments will be zero. The resulting
     *  population will have all of this instance's age ranges, i.e., if other is more specific than this, the amounts
     *  will be removed from this's cruder breakdowns.
     */
    public Population<SegmentType> excluding(@NonNull Population<SegmentType> B) {
        // Put all our segments into the new result.
        Population<SegmentType> result = new Population<>();
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : numbers.cellSet()) {
            SegmentType segment = cell.getRowKey();
            int start = cell.getColumnKey().lowerEndpoint();
            int end = cell.getColumnKey().upperEndpoint();
            int number = getCount(segment, start, end);
            result.put(segment, start, end, number);
        }
        result.attributes = new HashMap<>(this.attributes);

        // Begin to reduce.
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : numbers.cellSet()) {

            // Is there a match or overlap? If so, don't include them.
            SegmentType segment = cell.getRowKey();
            int start = cell.getColumnKey().lowerEndpoint();
            int end = cell.getColumnKey().upperEndpoint();
            int number = result.getCount(segment, start, end);
            int reduction = B.getCount(segment, start, end);
            result.put(segment, start, end, max(0, number - reduction));
        }
        return result;
    }

    /**
     *
     * @param B
     * @return
     *  True, if the population entirely contains the other population.
     */
    public boolean entirelyContains(@NonNull Population<SegmentType> B) {
        // Setup.
        Population<SegmentType> A = this;

        // All of B's segments must overlap A's.
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : B.numbers.cellSet()) {
            SegmentType segment = cell.getRowKey();
            int start = cell.getColumnKey().lowerEndpoint();
            int end = cell.getColumnKey().upperEndpoint();
            if (!A.hasIntersectingPopulation(segment, start, end)) {
                return false;
            }
        }

        // For each of our segments, decrement when we see something from B. If any are negative, we don't entirely contain.

        // Put all our segments into the new result.
        Population<SegmentType> result = new Population<>();
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : numbers.cellSet()) {
            SegmentType segment = cell.getRowKey();
            int start = cell.getColumnKey().lowerEndpoint();
            int end = cell.getColumnKey().upperEndpoint();
            int number = getCount(segment, start, end);
            result.put(segment, start, end, number);
        }
        // Start subtracting.
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : numbers.cellSet()) {

            // Is there a match or overlap? If so, don't include them.
            SegmentType segment = cell.getRowKey();
            int start = cell.getColumnKey().lowerEndpoint();
            int end = cell.getColumnKey().upperEndpoint();
            int number = result.getCount(segment, start, end);
            int reduction = B.getCount(segment, start, end);
            if (number - reduction < 0) {
                return false;
            }
            result.put(segment, start, end, max(0, number - reduction));
        }
        return true;
    }

    /**
     *
     * @return
     *  True, if there is no one in the population.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     *
     * @return
     *  The total number of people in this population.
     */
    public int size() {
        return numbers.values().stream().mapToInt(x -> x).sum();
    }

    /**
     * Sets the number of people into the population.
     *
     * @param segment
     */
    public void put(@NonNull SegmentType segment, int startAgeInclusive, int endAgeExclusive, int count) {

        // Sanity check.
        precondition("Start age must be non-negative.", startAgeInclusive >= 0);
        precondition("Start age must <= end age.", startAgeInclusive <= endAgeExclusive);
        precondition("Cound must be non-negative.", count >= 0);

        numbers.put(segment, Range.closedOpen(startAgeInclusive, endAgeExclusive), count);
    }

    /**
     * @return The number of people in the age range, rounding down.
     * @pre start age <= end age
     */
    public int getCount(int startAgeInclusive, int endAgeExclusive) {

        // Sanity check.
        precondition("Start age must be non-negative.", startAgeInclusive >= 0);
        precondition("Start age must <= end age.", startAgeInclusive <= endAgeExclusive);

        // Tally up.
        double total = 0;
        Range<Integer> them = Range.closedOpen(startAgeInclusive, endAgeExclusive);
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : numbers.cellSet()) {
            SegmentType ourType = cell.getRowKey();
            total += getContribution(them, cell, ourType);
        }
        return (int)total;
    }

    /**
     * @return The number of people in the age range, rounding down.
     */
    public int getCount(@NonNull SegmentType segment, int startAgeInclusive, int endAgeExclusive) {

        // Sanity check.
        precondition("Start age must be non-negative.", startAgeInclusive >= 0);
        precondition("Start age must <= end age.", startAgeInclusive <= endAgeExclusive);

        double total = 0;
        Range<Integer> them = Range.closedOpen(startAgeInclusive, endAgeExclusive);
        for (Table.Cell<SegmentType, Range<Integer>, Integer> cell : numbers.cellSet()) {
            // Move on if not the right segment.
            SegmentType ourType = cell.getRowKey();
            if (!ourType.equals(segment)) {
                continue;
            }
            total += getContribution(them, cell, ourType);
        }
        return (int)total;
    }

    private double getContribution(@NonNull Range<Integer> them,
                                   @NonNull Table.Cell<SegmentType, Range<Integer>, Integer> cell,
                                   @NonNull SegmentType ourType) {
        double total = 0;
        Range<Integer> us = cell.getColumnKey();
        // Five cases:
        // 0. No overlap at all.
        if (!us.isConnected(them)) {
            total = 0;
        }
        // 1. The range is exactly us.
        else if (us.intersection(them).equals(us)) {
            total = numbers.get(ourType, us);
        }
        // 2. They enclose us
        else if (them.encloses(us)) {
            total = numbers.get(ourType, us);
        }
        // 3. We enclose them
        else if (us.encloses(them)) {
            double theirSpan = them.upperEndpoint() - them.lowerEndpoint();
            double ourSpan = us.upperEndpoint() - us.lowerEndpoint();
            double fractionToAdd = theirSpan / ourSpan;
            total = fractionToAdd * numbers.get(ourType, us);
        }
        // 4. There is overlap.
        else {
            double ourNormalSpan = us.upperEndpoint() - us.lowerEndpoint();
            Range<Integer> intersection = us.intersection(them);
            double intersectionSpan = intersection.upperEndpoint() - intersection.lowerEndpoint();
            double fractionToAdd = intersectionSpan / ourNormalSpan;
            total = fractionToAdd * numbers.get(ourType, us);
        }
        return total;
    }

    /**
     *
     * @param segment
     * @param startAgeInclusive
     * @param endAgeExclusive
     * @return
     *  True if there is a population already intersecting this age range and segment.
     * @apiNote
     *  Useful for determining if duplicate entries have been made when encoding a population.
     */
    public boolean hasIntersectingPopulation(@NonNull SegmentType segment, int startAgeInclusive, int endAgeExclusive) {

        // Sanity check.
        precondition("Start age must be non-negative.", startAgeInclusive >= 0);
        precondition("Start age must <= end age.", startAgeInclusive <= endAgeExclusive);

        Range<Integer> them = Range.closedOpen(startAgeInclusive, endAgeExclusive);
        return numbers.row(segment).keySet().stream().anyMatch(us -> us.isConnected(them) && !us.intersection(them).isEmpty());
    }

    /**
     * Allows the population to be iterated through and processed.
     * @param action The action to perform.
     * @pre action not null
     */
    public void forEachSegment(@NonNull Consumer<Segment> action) {
        numbers.cellSet().forEach(cell -> action.accept(new Segment(
                cell.getRowKey(),
                cell.getColumnKey().lowerEndpoint(),
                cell.getColumnKey().upperEndpoint(),
                cell.getValue()
        )));
    }

    /**
     * Gets the attribute, using a default if not present.
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getAttribute(@NonNull String name, Function<String, T> transform) {
        return transform.apply(attributes.get(name));
    }

    @Override
    public boolean hasAttribute(@NonNull String name) {
        return attributes.containsKey(name);
    }

    /**
     * Sets the attribute.
     * @param name
     * @param attribute
     */
    public void setAttribute(@NonNull String name, @NonNull String attribute) {
        attributes.put(name, attribute);
    }

    /**
     * A segment of a population.
     * @apiNote
     *  Used for forEachSegment statements.
     */
    @Getter
    @AllArgsConstructor
    public class Segment {
        private SegmentType segment;
        private int startAgeInclusive;
        private int endAgeExclusive;
        private int count;

        /**
         *
         * @return
         *  A random age within the segment.
         * @apiNote Convenience method.
         */
        public double getAge() {
            return random() * (endAgeExclusive - startAgeInclusive) + startAgeInclusive;
        }
    }
}
