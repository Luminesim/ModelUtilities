package com.luminesim.regions;


import com.luminesim.regions.io.POIGroupCSVRow;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.stream.Collectors;

import static com.luminesim.qa.ContractUtilities.*;

/**
 * A full region dataset.
 * @implNote
 *  This acts as a facade, allowing quick access to all aspects of the region.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public class RegionDataset {
    /**
     * The locations in the dataset and any hierarchical nature within specifying where people, objects, etc.
     * should be placed. E.g. A location without any hierarchy randomly distributes the items within;
     * a location with subdivisions for schools, businesses, residential areas, etc. should allocate those populations
     * accordingly.
     */
    private DirectedAcyclicGraph<Location, Object> hierarchy = new DirectedAcyclicGraph<>(Object.class);
    private Map<Location, Set<Location>> directSublocations = new HashMap<>();

    /**
     * The locations (points or areas of interest) in the region.
     */
    private Map<String, Location> locations = new HashMap<>();

    /**
     * The area each location occupies in the region.
     */
    private Map<String, GISArea> areas = new HashMap<>();

    /**
     * The populations in the region.
     */
    private Map<String, Population<String>> populations = new HashMap<>();

    /**
     * The POI groups in the region.
     */
    private Map<String, List<POIGroup>> poiGroups = new HashMap<>();

    /**
     * Adds a location to the dataset.
     *
     * @param location
     * @pre location must not be in the dataset
     * @pre location not null
     */
    public void addLocation(@NonNull Location location) {
        Location existingEntry = locations.put(location.getId(), location);
        hierarchy.addVertex(getLocation(location.getId()));
        directSublocations.put(location, new HashSet<>());

        // Sanity check.
        ensureThat("Location must be unique in the dataset but ID " + location.getId() + " was found multiple times.", existingEntry == null);
        postcondition("Vertex must be recorded.", hierarchy.containsVertex(getLocation(location.getId())));
    }

    /**
     * @param id
     * @return The location associated with the ID.
     * @pre location with ID exists in dataset
     */
    public Location getLocation(@NonNull String id) {

        // Sanity check.
        assertLocationExists(id);

        // Return.
        return locations.get(id);
    }

    private void assertLocationExists(@NonNull String id) {
        precondition(() -> "Location " + id + " must be in the dataset.", hasLocation(id));
    }

    /**
     * Links locations as a child and parent.
     *
     * @param parentId
     * @param childId
     * @pre parent and child known to dataset
     * @pre parent is not the child
     * @pre parent does not already have this as a direct child
     * @post dataset must remain a directed acyclic graph
     */
    public void addChild(String parentId, String childId) {
        // Sanity check.
        precondition(() -> "Parent location " + parentId + " not contained in dataset.", hasLocation(parentId));
        precondition(() -> "Child location " + childId + " not contained in dataset.", hasLocation(childId));
        precondition(() -> "Child location " + childId + " and parent " + parentId + " are the same", !parentId.equals(childId));
        precondition(() -> "Child location " + childId + " already belongs to parent " + parentId, !directSublocations.get(getLocation(parentId)).contains(getLocation(childId)));

        // Add the edge.
        hierarchy.addEdge(getLocation(parentId), getLocation(childId));
        directSublocations.get(getLocation(parentId)).add(getLocation(childId));
        postcondition("Parent vertex must still be recorded.", hierarchy.containsVertex(getLocation(parentId)));
        postcondition("Child vertex must still be recorded.", hierarchy.containsVertex(getLocation(childId)));
    }

    /**
     * @return The locations in the dataset.
     */
    public Collection<Location> getLocations() {
        return locations.values();
    }

    /**
     * Adds a GIS area to the dataset.
     *
     * @param gisArea
     * @pre gisArea must belong to a location in the dataset.
     * @pre location must not already have a {@link GISArea}
     */
    public void addGISArea(@NonNull GISArea gisArea) {
        // Sanity check.
        precondition(() -> "GIS Area " + gisArea + "must belong to a location.", hasLocation(gisArea.getLocationId()));
        precondition(() -> "Location " + locations.get(gisArea.getLocationId()) + " was given more than one location.", !areas.containsKey(gisArea.getLocationId()));

        // Record.
        areas.put(gisArea.getLocationId(), gisArea);
    }

    /**
     * @return Any locations without a {@link GISArea}
     */
    public Collection<Location> getLocationsWithoutGISAreas() {
        return locations
                .values()
                .stream()
                .filter(l -> !areas.containsKey(l.getId()))
                .collect(Collectors.toList());
    }

    /**
     * @param id
     * @return True if the dataset has a location with the given ID.
     */
    public boolean hasLocation(@NonNull String id) {
        return locations.containsKey(id);
    }

    /**
     * @return All (indirect and direct) sublocations for the given location.
     * @pre location with given ID in dataset.
     */
    public Collection<Location> getAllSubLocations(@NonNull String id) {
        return new HashSet<>(hierarchy.getDescendants(getLocation(id)));
    }

    /**
     * @return All (direct) sublocations for the given location.
     * @pre location with given ID in dataset.
     */
    public Collection<Location> getDirectSubLocations(@NonNull String id) {
        return new HashSet<>(hierarchy.getDescendants(getLocation(id)));
    }
    /**
     *
     * @param id
     * @return
     *  The GIS area associated with the location.
     * @pre location has a GIS area set
     */
    public GISArea getArea(@NonNull String id) {
        precondition(() -> "Location must have an associated GIS location: " + id, areas.containsKey(id));
        return areas.get(id);
    }

    /**
     *
     * @return
     *  The GIS areas in the region.
     */
    public Collection<GISArea> getAreas() {
        return areas.values();
    }

    /**
     *
     * @param locationId
     * @param startAgeInclusive
     * @param endAgeExclusive
     * @return
     *  The number of people in the location in the age range (rounded down), or zero if it has no population.
     *  This number INCLUDES people in lower levels of the hierarchy.
     * @pre location is known
     * @pre 0 <= startAge <= endAge
     */
    public int getTotalPopulationSize(String locationId, int startAgeInclusive, int endAgeExclusive) {
        // Sanity check
        assertLocationExists(locationId);
        precondition("Start age must be non-negative", startAgeInclusive >= 0);
        precondition("End age must be at least the start age.", startAgeInclusive <= endAgeExclusive);

        // Return the population size.
        if (populations.containsKey(locationId)) {
            return populations.get(locationId).getCount(startAgeInclusive, endAgeExclusive);
        }
        else {
            return 0;
        }
    }
    /**
     *
     * @param locationId
     * @param startAgeInclusive
     * @param endAgeExclusive
     * @return
     *  The number of people in the location in the age range (rounded down), or zero if it has no population.
     *  This number EXCLUDES people in lower levels of the hierarchy.
     * @pre location is known
     * @pre 0 <= startAge <= endAge
     */
    public int getExclusivePopulationSize(String locationId, int startAgeInclusive, int endAgeExclusive) {
        return getExclusivePopulation(locationId).getCount(startAgeInclusive, endAgeExclusive);
        /*// Sanity check
        assertLocationExists(locationId);
        precondition("Start age must be non-negative", startAgeInclusive >= 0);
        precondition("End age must be at least the start age.", startAgeInclusive <= endAgeExclusive);

        // Find all lower levels of the hierarchy.
        Location us = getLocation(locationId);
        Set<Location> children = hierarchy.getDescendants(us);

        // Return the population size.
        if (populations.containsKey(locationId)) {

            // Sum up the population held by our children.
            int peopleInSublevels = children
                    .stream()
                    .filter(loc -> hasPopulation(loc))
                    .map(Location::getId)
                    .mapToInt(id -> populations.get(id).getCount(startAgeInclusive, endAgeExclusive))
                    .sum();

            int ourPeople = populations.get(locationId).getCount(startAgeInclusive, endAgeExclusive);
            if (peopleInSublevels > ourPeople) {
                String summary = children
                        .stream()
                        .filter(loc -> hasPopulation(loc))
                        .map(loc -> String.format(
                                "(%s/%s: %s)",
                                loc.getId(),
                                loc.getName(),
                                populations.get(loc.getId()).getCount(startAgeInclusive, endAgeExclusive)))
                        .collect(Collectors.joining(", "));
                throw new IllegalStateException(String.format(
                        "Location %s has %s people in age range %s-%s, but this is less than the total from all subpopulations (%s from %s).",
                        locationId, ourPeople, startAgeInclusive, endAgeExclusive, peopleInSublevels, summary
                ));
            }
            return ourPeople - peopleInSublevels;
        }
        else {
            return 0;
        }*/
    }

    /**
     * Sets the number of people in the dataset for the given age range and population segment.
     * @param segment
     * @param count
     * @pre location is known
     * @pre 0 <= startAge <= endAge
     * @pre count >= 0
     */
    public void setPopulation(String locationId, String segment, int startAgeInclusive, int endAgeExclusive, int count) {
        // Sanity check
        assertLocationExists(locationId);
        precondition("Start age must be non-negative.", startAgeInclusive >= 0);
        precondition("End age must be at least the start age.", startAgeInclusive <= endAgeExclusive);
        precondition("Count must be non-negative", count >= 0);

        // Set the population.
        populations.computeIfAbsent(locationId, x -> new Population<>());
        populations.get(locationId).put(segment, startAgeInclusive, endAgeExclusive, count);
    }

    /**
     * Convenience method.
     * @see #getExclusivePopulation(String)
     */
    public Population<String> getExclusivePopulation(@NonNull Location location) {
        return getExclusivePopulation(location.getId());
    }

    /**
     *
     * @param location
     * @return
     *  The population associated with the given location, EXCLUDING any sub-populations.
     * @implNote
     *  Assumes that all direct children are entirely contained within this population.
     *  E.g. if A(100 youth, 100 elderly) contains B and C, B + C = (<= 100 youth, <= 100 elderly)
     * @pre location known
     * @pre location has a population set.
     */
    public Population<String> getExclusivePopulation(@NonNull String location) {
        // Preconditions
        assertLocationExists(location);
        precondition(() -> location + " does not have a population set to it.", populations.containsKey(location));

        // Remove all other populations that fall under the jurisdiction of the given population.
        // First: if there are no subpopulations, just quit.
        Set<String> childrenWithPopulations = hierarchy
                .outgoingEdgesOf(getLocation(location))
                .stream()
                .map(edge -> hierarchy.getEdgeTarget(edge))
                .filter(this::hasPopulation)
                .map(Location::getId)
                .collect(Collectors.toSet());
        if (childrenWithPopulations.isEmpty()) {
            return populations.get(location);
        }
        // Otherwise, reduce down.
        else {
            return childrenWithPopulations
                    .stream()
                    .map(id -> populations.get(id))
                    .reduce(populations.get(location), Population::excluding);
        }
    }

    /**
     *
     * @param location
     * @return
     *  The population associated with the given location, including any sub-populations.
     * @pre location known
     * @pre location has a population set.
     */
    public Population<String> getPopulation(@NonNull String location) {
        // Preconditions
        assertLocationExists(location);
        precondition(() -> location + " does not have a population set to it.", populations.containsKey(location));
        return populations.get(location);
    }

    /**
     * Convenience method.
     */
    public Population<String> getPopulation(@NonNull Location location) {
        return getPopulation(location.getId());
    }

    /**
     *
     * @param locationId
     * @return
     *  True, if there is a population associated with the given location.
     */
    public boolean hasPopulation(@NonNull String locationId) {
        return populations.containsKey(locationId);
    }

    /**
     *
     * @return
     *  True, if there is a population associated with the given location.
     */
    public boolean hasPopulation(@NonNull Location location) {
        return populations.containsKey(location.getId());
    }

    public void addPOIGroup(@NonNull POIGroup group) {
        // Preconditions
        assertLocationExists(group.getLocationId());

        // Add.
        poiGroups.computeIfAbsent(group.getLocationId(), x -> new LinkedList<>());
        poiGroups.get(group.getLocationId()).add(group);
    }

    public List<POIGroup> getPOIGroups(@NonNull Location location) {
        return getPOIGroups(location.getId());
    }

    public List<POIGroup> getPOIGroups(@NonNull String locationId) {
        return poiGroups.getOrDefault(locationId, Collections.emptyList());
    }
}
