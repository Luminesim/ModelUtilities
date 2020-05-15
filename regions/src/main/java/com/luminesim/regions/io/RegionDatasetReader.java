package com.luminesim.regions.io;

import com.luminesim.regions.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.simpleflatmapper.csv.CsvParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.luminesim.qa.ContractUtilities.*;
import static java.lang.String.*;

/**
 * Reads region dataset from file.
 */
@NoArgsConstructor
@Slf4j
public class RegionDatasetReader {

    public static String LocationFile = "locations.csv";
    public static String AreaFile = "areas.csv";
    public static String HierarchyFile = "hierarchy.csv";
    public static String LocationAttributesFile = "location_attributes.csv";
    public static String PopulationFile = "populations.csv";
    public static String PopulationAttributesFile = "population_attributes.csv";
    public static String POIGroupsFile = "poi_groups.csv";

    /**
     * Reads the files in the given folder, transforming them into a region dataset.
     *
     * @param folder
     * @return
     * @throws IOException If the folder or its contents could not be read.
     * @pre folder is a valid folder with all required {@link RegionDataset} files.
     * @post dataset has at least one location
     * @post all locations have a {@link GISArea}
     */
    public static RegionDataset read(@NonNull Path folder) throws IOException {

        // Sanity check -- is this a valid folder?
        if (!folder.toFile().isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a directory: " + folder.toAbsolutePath());
        }

        // Read all locations.
        RegionDataset data = new RegionDataset();

        // FIXME: Figure out why the direct mapping isn't working.
        CsvParser
                .skip(1)
                .forEach(
                        folder.toAbsolutePath().resolve(LocationFile).toFile(),
                        row -> data.addLocation(new Location(row[0], row[1])));

        // Set up the hierarchy.
        CsvParser
                .mapTo(LocationHierarchy.class)
                .forEach(
                        folder.toAbsolutePath().resolve(HierarchyFile).toFile(),
                        pair -> data.addChild(pair.getParentId(), pair.getChildId()));

        // Set up regions.
        Map<String, GISArea> areas = data
                .getLocations()
                .stream()
                .map(location -> new GISArea(location.getId()))
                .collect(Collectors.toMap(g -> g.getLocationId(), g -> g));
        // FIXME: Figure out why the direct mapping isn't working.
        CsvParser
                .skip(1)
                .forEach(
                        folder.toAbsolutePath().resolve(AreaFile).toFile(),
                        row -> {
                            // We can't set GIS areas for locations that don't exist.
                            precondition(format(
                                    "Locations %s was found in %s but was not provided in %s.",
                                    row[0], AreaFile, LocationFile),
                                    areas.containsKey(row[0]));

                            // Add the point to the GIS area.
                            areas.get(row[0]).addPoint(Double.valueOf(row[1]), Double.valueOf(row[2]));
                        }
                );


        // Sanity check.
        areas.values().forEach(a -> ensureThat(a + " must be a point or region.", a.isValid()));

        // Add everything to the dataset.
        areas.values().forEach(data::addGISArea);

        // Set up areaattributes.
        CsvParser
                .mapTo(AttributeCSVRow.class)
                .forEach(
                        folder.toAbsolutePath().resolve(LocationAttributesFile).toFile(),
                        row -> {
                            // We can't set attributes for locations that don't exist.
                            precondition(format(
                                    "Locations %s was found in %s but was not provided in %s.",
                                    row.getLocationId(), LocationAttributesFile, LocationFile),
                                    data.hasLocation(row.getLocationId()));

                            // Add the attributes.
                            data.getLocation(row.getLocationId()).setAttribute(row.getAttribute(), row.getValue());
                        }
                );

        // Read populations.
        CsvParser
                .mapTo(PopulationCSVRow.class)
                .forEach(
                        folder.toAbsolutePath().resolve(PopulationFile).toFile(),
                        row -> {
                            // Ensure we don't have duplicates in the dataset.
                            precondition(
                                    () -> String.format(
                                            "Found overlapping segment (%s) & age range (%s-%s) in location %s.",
                                            row.getSegment(),
                                            row.getStartAge(),
                                            row.getEndAge(),
                                            row.getLocationId()),
                                    !data.hasPopulation(row.getLocationId()) || !data.getPopulation(row.getLocationId())
                                            .hasIntersectingPopulation(
                                                    row.getSegment(),
                                                    row.getStartAge(),
                                                    row.getEndAge()));
                            // Set the population.
                            data.setPopulation(
                                    row.getLocationId(),
                                    row.getSegment(),
                                    row.getStartAge(),
                                    row.getEndAge(),
                                    row.getCount());
                        }
                );

        // Sanity check: all locations should contain the population of all descendants.
        data.getLocations().stream().filter(data::hasPopulation).forEach(A -> {
            Population<String> remaining = data.getPopulation(A.getId());
            Set<Location> childrenWithPopulations = data
                    .getDirectSubLocations(A.getId())
                    .stream()
                    .filter(data::hasPopulation)
                    .collect(Collectors.toSet());
            for (Location B : childrenWithPopulations) {
                log.debug("Remaining: {}", remaining);
                log.debug("Removing: {}: {}", B.getName(), data.getPopulation(B));
                if (!remaining.entirelyContains(data.getPopulation(B))) {

                    throw new IllegalStateException(String.format(
                            "Location %s has too few people to support the nested populations in %s. " +
                                    "It encountered a problem with population %s",
                            A, childrenWithPopulations, B)
                    );
                }
                remaining = remaining.excluding(data.getPopulation(B));
            }
        });

        // Set up population attributes.
        CsvParser
                .mapTo(AttributeCSVRow.class)
                .forEach(
                        folder.toAbsolutePath().resolve(PopulationAttributesFile).toFile(),
                        row -> {
                            // We can't set attributes for locations that don't exist.
                            precondition(format(
                                    "Locations %s was found in %s but was not provided in %s.",
                                    row.getLocationId(), PopulationAttributesFile, LocationFile),
                                    data.hasLocation(row.getLocationId()));

                            // Add the attributes.
                            data.getPopulation(row.getLocationId()).setAttribute(row.getAttribute(), row.getValue());
                        }
                );

        // Read POI groups.
        // FIXME: Figure out why the direct mapping isn't working.
        CsvParser
//                .mapTo(POIGroupCSVRow.class)
                .skip(1)
                .forEach(
                        folder.toAbsolutePath().resolve(POIGroupsFile).toFile(),
                        row -> {
                            // We can't create attributes for locations that don't exist.
                            precondition(format(
                                    "Locations %s was found in %s but was not provided in %s.",
                                    row[0], POIGroupsFile, LocationFile),
                                    data.hasLocation(row[0]));

                            // Add the POI group data.
                            data.addPOIGroup(new POIGroup(
                                    row[0],
                                    POIGroup.POIType.valueOf(row[1]),
                                    Integer.valueOf(row[2]),
                                    Integer.valueOf(row[3]),
                                    Integer.valueOf(row[4]),
                                    Integer.valueOf(row[5]),
                                    Integer.valueOf(row[6]),
                                    row[7],
                                    row[8]));
                        }
                );

        // Final check.
        postcondition("Dataset must have at least one location.", !data.getLocations().isEmpty());
        postcondition(
                () -> "Dataset must have a GIS area for each location. The following were missing: " + data.getLocationsWithoutGISAreas(),
                data.getLocationsWithoutGISAreas().isEmpty());

        // Done!
        return data;
    }
}
