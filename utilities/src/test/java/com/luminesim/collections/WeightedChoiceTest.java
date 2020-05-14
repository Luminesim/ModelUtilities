package com.luminesim.collections;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link WeightedChoice}
 */
public class WeightedChoiceTest {

    private Random rng = new Random(System.currentTimeMillis());
    private final int SampleSize = 1000;
    private final double Tol = 0.1;
    /**
     * Ensures that {@link WeightedChoice#pickRandom()} can correctly
     * handle a single choice. This is an easy
     * edge case to get wrong.
     */
    @Test
    public void noChoices_shouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeightedChoice<Boolean>(rng, Collections.emptyList(), t -> 1.0),
                "Should reject an empty collection");
    }

    /**
     * Ensures that {@link WeightedChoice#pickRandom()} can correctly
     * handle a single choice. This is an easy
     * edge case to get wrong.
     */
    @Test
    public void unaryChoice_shouldProduceCorrectDistribution() {
        // Set up to draw true with arbitrary weight.
        double tWeight = 999;
        WeightedChoice<Boolean> choice = new WeightedChoice<>(rng,
                Arrays.asList(true),
                t -> tWeight
        );

        // Draw a lot.
        double tCount = 0;
        double fCount = 0;
        for (int i = 0; i < SampleSize; i += 1) {
            if (choice.pickRandom() == true) {
                tCount += 1;
            } else {
                fCount += 1;
            }
        }

        // This should simply work and produce true always.
        assertEquals(
                1,
                tCount / (tCount + fCount),
                "Should see exactly the correct number of Trues.");
    }

    /**
     * Ensures that {@link WeightedChoice#pickRandom()} can correctly
     * handle binary choices with known distribution. This is an easy
     * edge case to get wrong.
     */
    @Test
    public void binaryChoice_shouldProduceCorrectDistribution() {
        // Set up to draw true more than false.
        double tWeight = 0.75;
        double fWeight = 0.25;
        WeightedChoice<Boolean> choice = new WeightedChoice<>(rng,
                Arrays.asList(true, false),
                t -> t ? tWeight : fWeight
        );

        // Draw a lot.
        double tCount = 0;
        double fCount = 0;
        for (int i = 0; i < SampleSize; i += 1) {
            if (choice.pickRandom() == true) {
                tCount += 1;
            } else {
                fCount += 1;
            }
        }

        // Expect the known ratio.
        assertEquals(
                tWeight,
                tCount / (tCount + fCount),
                Tol,
                "Should see roughly the correct number of Trues.");
        assertEquals(
                fWeight,
                fCount / (tCount + fCount),
                Tol,
                "Should see roughly the correct number of Trues.");
    }
}
