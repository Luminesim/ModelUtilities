package com.luminesim.regions;

import static com.luminesim.regions.POIGroup.POIType.SecondarySchool;
import static com.luminesim.regions.POIGroup.POIType.Workplace;
import static org.junit.jupiter.api.Assertions.*;

import com.luminesim.regions.io.RegionDatasetReader;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests {@link RegionDataset} and related classes.
 */
public class RegionDatasetTest {

    /**
     * Ensures that a valid dataset can be read and that a spot check of its data shows it has been
     * loaded correctly.
     *
     * @throws Throwable
     */
    @Test
    void loadValidDataset_shouldContainExpectedData() throws Throwable {
        // Read the dataset
        RegionDataset data = RegionDatasetReader.read(
                Paths.get(ClassLoader.getSystemResource("UnitySKData").toURI()));

        // Ensure that everything in the dataset is there that we expect.
        assertEquals(
                set("CensusRegion-RoundValleyNo410", "CensusRegion-Unity", "Unity-LutherPlace", "Unity-UCHS"),
                data.getLocations().stream().map(Location::getId).collect(Collectors.toSet()),
                "Must load all IDs exactly.");
        assertEquals(
                set("Round Valley No 410", "Unity", "Luther Place", "UCHS"),
                data.getLocations().stream().map(Location::getName).collect(Collectors.toSet()),
                "Must load all names exactly.");

        // Spot check generated locations
        assertEquals(
                "Unity-LutherPlace",
                data.getLocation("Unity-LutherPlace").getId(),
                "IDs returned must match those of the ID provided.");
        assertEquals(
                "Luther Place",
                data.getLocation("Unity-LutherPlace").getName(),
                "Names returned must match those of the ID provided.");

        // Spot check attributes.
        assertEquals(
                true,
                data.getLocation("Unity-UCHS").getBoolean("IsHighSchool"));
        assertEquals(
                false,
                data.getLocation("CensusRegion-Unity").getBoolean("SupportsFarms"));
        assertEquals(
                true,
                data.getLocation("CensusRegion-Unity").getBoolean("SupportsUrbanDetachedUnits"));

        // Spot check hiearchy
        assertEquals(
                set(data.getLocation("Unity-UCHS"), data.getLocation("Unity-LutherPlace")),
                data.getAllSubLocations("CensusRegion-Unity"),
                "Indirect sublocations not correctly mapped at top level");
        assertEquals(
                set(),
                data.getAllSubLocations("Unity-UCHS"),
                "Indirect sublocations not correctly mapped at terminal level.");
        assertEquals(
                set(),
                data.getAllSubLocations("CensusRegion-RoundValleyNo410"),
                "Sublocations should not exist for Round Valley, even though it geographically contains Unity, as the relationship has not been defined.");


        // Spot check locations.
        assertTrue(data.getArea("Unity-UCHS").isPoint(), "UCHS must be a point location.");
        assertTrue(data.getArea("CensusRegion-RoundValleyNo410").isRegion(), "Round Valley must be a region location.");
        assertEquals(
                new Point2D.Double(52.44050321, -109.1532727),
                new Point2D.Double(data.getArea("Unity-UCHS").getLatitudes()[0], data.getArea("Unity-UCHS").getLongitudes()[0]),
                "UCHS has wrong point location.");
        assertArrayEquals(
                new double[]{52.668713, 52.668297, 52.404338, 52.405595},
                data.getArea("CensusRegion-RoundValleyNo410").getLatitudes(),
                "Round Valley region has wrong latitudes.");
        assertArrayEquals(
                new double[]{-109.460515, -109.026555, -109.025181, -109.457081},
                data.getArea("CensusRegion-RoundValleyNo410").getLongitudes(),
                "Round Valley region has wrong latitudes.");

        // Ensure intersection is working.
        assertTrue(
                data.getArea("CensusRegion-RoundValleyNo410").intersects(data.getArea("CensusRegion-Unity")),
                "Round Valley and Unity should intersect (hierarchical direction).");
        assertTrue(
                data.getArea("CensusRegion-Unity").intersects(data.getArea("CensusRegion-RoundValleyNo410")),
                "Round Valley and Unity should intersect (reverse direction).");
        assertFalse(
                data.getArea("Unity-LutherPlace").intersects(data.getArea("Unity-UCHS")),
                "Disconnected points should not intersect");
        assertTrue(
                data.getArea("Unity-UCHS").intersects(data.getArea("Unity-UCHS")),
                "UCHS should intersect with itself.");


        // Spot check populations with hierarchy in mind
        assertEquals(
                1015,
                data.getTotalPopulationSize("CensusRegion-Unity", 40, 80),
                "Unity has wrong number of people in age range when including sublocations.");
        assertEquals(
                985,
                data.getExclusivePopulationSize("CensusRegion-Unity", 40, 80),
                "Unity has wrong number of people in age range when excluding sublocations.");
        assertEquals(
                data.getExclusivePopulationSize("CensusRegion-Unity", 0, 120),
                data.getExclusivePopulation("CensusRegion-Unity").size(),
                "Exclusive population should return correct numbers regardless of method used to access.");
        assertEquals(
                12,
                data.getTotalPopulationSize("CensusRegion-RoundValleyNo410", 70, 73),
                "Round Valley has wrong number of people in age range.");
        assertEquals(
                0,
                data.getTotalPopulationSize("Unity-UCHS", 0, 100),
                "UCHS should have no population.");
        assertEquals(
                35,
                data.getTotalPopulationSize("Unity-LutherPlace", 0, 120),
                "Luther Place needs to report its entire population size (total pop size test)");
        assertEquals(
                35,
                data.getTotalPopulationSize("Unity-LutherPlace", 0, 120),
                "Luther Place needs to report its entire population size (exclusive pop size test, which should be unchanged from total pop size)");

        // Spot check POI groups
        List<POIGroup> unityBusinesses = data.getPOIGroups("CensusRegion-Unity");
        assertEquals(
                set(0, 10),
                set(unityBusinesses, POIGroup::getMaxEmployees),
                "Businesses have wrong maximum number of employees.");
        assertEquals(
                set(0, 1),
                set(unityBusinesses, POIGroup::getMinEmployees),
                "Businesses have wrong minimum number of employees.");
        assertEquals(
                set(0, 0),
                set(unityBusinesses, POIGroup::getMaxAttendees),
                "Businesses have wrong maximum number of attendees.");
        assertEquals(
                set(0, 0),
                set(unityBusinesses, POIGroup::getMinAttendees),
                "Businesses have wrong minimum number of attendees.");
        assertEquals(
                set(0, 0),
                set(unityBusinesses, POIGroup::getMinAttendees),
                "Businesses have wrong type");
        assertEquals(
                set(Workplace, Workplace),
                set(unityBusinesses, POIGroup::getGroupType),
                "Businesses have wrong type");
        assertEquals(
                set(500, 100),
                set(unityBusinesses, POIGroup::getNumber),
                "Businesses have wrong numbers");
        POIGroup uchs = data.getPOIGroups("Unity-UCHS").get(0);
        assertEquals(SecondarySchool, uchs.getGroupType(), "School type is wrong.");
        assertEquals(100, uchs.getMinAttendees(), "School min attendees are wrong.");
        assertEquals(200, uchs.getMaxAttendees(), "School max attendees are wrong.");
        assertEquals(1, uchs.getNumber(), "School numbers are wrong.");


        // Spot check population attributes.
        assertTrue(data.getPopulation("CensusRegion-RoundValleyNo410").getBoolean("Is Rural"));
        assertTrue(data.getPopulation("CensusRegion-Unity").getBoolean("Is Urban"));
        assertEquals(1000, data.getPopulation("CensusRegion-Unity").getNumber("Number of Partnered Men"));
    }

    private <T> Set<T> set(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    private <T, U> Set<U> set(Collection<T> items, Function<T, U> transform) {
        return items.stream().map(transform).collect(Collectors.toSet());
    }
}
