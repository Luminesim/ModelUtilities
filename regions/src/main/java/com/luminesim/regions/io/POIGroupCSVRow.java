package com.luminesim.regions.io;

import com.luminesim.regions.POIGroup;
import lombok.*;

import java.util.List;

/**
 * Places of interest with similar attributes as represented in a CSV file.
 */
@Data
public class POIGroupCSVRow {
    /**
     * The location in/at which these POIs appear.
     */
    private String locationId;

    /**
     * The type of POI.
     */
    private POIGroup.POIType groupType;

    /**
     * If the POIs have employees, whats the minimum they should have?
     */
    private int minEmployees;


    /**
     * If the POIs have employees, whats the maximum they should have?
     */
    private int maxEmployees;

    /**
     * If the POIs have attendees (e.g. assisted living, students), whats the minimum they should have?
     */
    private int minAttendees;

    /**
     * If the POIs have attendees (e.g. assisted living, students), whats the maximum they should have?
     */
    private int maxAttendees;

    /**
     * The number of POIs in this group.
     */
    private int number;

    /**
     * A label for the group (e.g. public schools, large businesses, etc.). Not guaranteed to be unique and not to be
     * used as an ID.
     */
    private String label;

    /**
     * Any citation information.
     */
    private String citation;

    /**
     * Converts the row to a {@link POIGroup}
     * @return
     */
    public POIGroup toPOIGroup() {
        return POIGroup
                .builder()
                .citation(citation)
                .groupType(groupType)
                .label(label)
                .locationId(locationId)
                .minAttendees(minAttendees)
                .maxAttendees(maxAttendees)
                .minEmployees(minEmployees)
                .maxEmployees(maxEmployees)
                .number(number)
                .build();
    }
}
