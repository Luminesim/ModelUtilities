package com.luminesim.regions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link GISArea}
 */
public class GISAreaTest {

    /**
     * Ensures that the area of an irregular polygon (like a town's residential district)
     * can be calculated.
     * Example derived from https://en.wikipedia.org/wiki/Shoelace_formula
     */
    @Test
    public void irregularPolygon_ensureAreaCorrect() {
        GISArea area = new GISArea("Test");
        area.addPoint(3,4);
        area.addPoint(5,11);
        area.addPoint(12,8);
        area.addPoint(9,5);
        area.addPoint(5,6);
        assertEquals(30, area.area(), "Area must be correct.");
    }
}
