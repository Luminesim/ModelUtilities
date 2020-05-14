package com.luminesim.regions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link Population}
 */
public class PopulationTest {

    /**
     * Removes populations from an existing population. The result should be a left anti-join, i.e.,
     * A - B.
     */
    @Test
    public void excludePopulationSegments_shouldReturnLeftAntiJoin() {
        Population<String> A = new Population<>();
        A.put("All", 0, 20, 40);
        A.put("All", 20, 40, 40);
        A.put("All", 40, 60, 40);
        A.put("All", 60, 80, 40);
        A.put("All", 80, 100, 40);


        Population<String> B = new Population<>();
        B.put("All", 0, 30, 15);
        B.put("All", 40, 60, 500);
        B.put("All", 80, 90, 5);
        B.put("All", 90, 100, 15);

        // Ensure left-anti join.
        Population<String> result = A.excluding(B);
        assertEquals(30, result.getCount(0, 20), "Wrong number of people remaining (simple subtraction, first half).");
        assertEquals(35, result.getCount(20, 40), "Wrong number of people remaining (simple subtraction, other half).");
        assertEquals(0, result.getCount(40, 60), "Wrong number of people remaining (large population subtraction).");
        assertEquals(40, result.getCount(60, 80), "Wrong number of people remaining (no subtraction due to zero count).");
        assertEquals(20, result.getCount(80, 100), "Wrong number of people remaining (multi-subtraction).");

        // Ensure no modification to original collection.
        for (int s = 0, e = 20; e <= 100; s += 20, e += 20) {
            assertEquals(40, A.getCount(s, e), "A should be unchanged.");
        }
    }

    /**
     * Subtract a large A from B: it should be empty afterwards.
     */
    @Test
    public void subtractLargePopulation_shouldResultInEmptyPopulation() {

        Population<String> A = new Population<>();
        A.put("All", 0, 20, 40);
        A.put("All", 20, 40, 40);
        A.put("All", 40, 60, 40);
        A.put("All", 60, 80, 40);


        Population<String> B = new Population<>();
        B.put("All", 0, 20, 10);
        B.put("All", 20, 40, 10);

        assertTrue(B.excluding(A).isEmpty(), "Should result in an empty population!");
    }

    /**
     * If A entirely contains B, that should be detectable.
     */
    @Test
    public void testEntirelyContains_shouldDetectAEntirelyContainingB() {

        Population<String> A = new Population<>();
        A.put("All", 0, 20, 40);
        A.put("All", 20, 40, 40);
        A.put("All", 40, 60, 40);
        A.put("All", 60, 80, 40);


        Population<String> B = new Population<>();
        B.put("All", 0, 10, 10);
        B.put("All", 10, 15, 10);
        B.put("All", 20, 40, 10);

        assertTrue(A.entirelyContains(B), "A should contain B!");
    }

    /**
     * If A does not entirely contain B, that should be detectable.
     */
    @Test
    public void testEntirelyContains_shouldDetectANotEntirelyContainingB() {

        Population<String> A = new Population<>();
        A.put("All", 0, 20, 40);
        A.put("All", 20, 40, 40);
        A.put("All", 40, 60, 40);
        A.put("All", 60, 80, 40);


        Population<String> B = new Population<>();
        B.put("All", 0, 10, 30);
        B.put("All", 10, 20, 30);
        B.put("All", 40, 60, 10);
        B.put("All", 60, 80, 10);

        assertFalse(A.entirelyContains(B), "A should NOT contain B!");
    }
}
