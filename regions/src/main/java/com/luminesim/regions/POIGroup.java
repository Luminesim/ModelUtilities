package com.luminesim.regions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A group of (or single) point of interest.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class POIGroup {
    /**
     * The location in/at which these POIs appear.
     */
    @NonNull
    private String locationId;

    /**
     * The type of POI.
     */
    @NonNull
    private final POIType groupType;

    /**
     * If the POIs have employees, whats the minimum they should have?
     */
    private final int minEmployees;

    /**
     * If the POIs have employees, whats the maximum they should have?
     */
    private final int maxEmployees;

    /**
     * If the POIs have attendees (e.g. assisted living, students), whats the minimum they should have?
     */
    private final int minAttendees;

    /**
     * If the POIs have attendees (e.g. assisted living, students), whats the maximum they should have?
     */
    private final int maxAttendees;

    /**
     * The number of POIs in this group.
     */
    private final int number;

    /**
     * A label for the group (e.g. public schools, large businesses, etc.). Not guaranteed to be unique and not to be
     * used as an ID.
     */
    private final String label;

    /**
     * Any citation data.
     */
    private final String citation;

    public enum POIType {
        PrimarySchool,
        SecondarySchool,
        TertiarySchool,
        Workplace,
        AssistedLiving,
        Hospital
    }

}
