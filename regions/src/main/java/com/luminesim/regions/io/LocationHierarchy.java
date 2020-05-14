package com.luminesim.regions.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

/**
 * Captures a location hierarchy relationship.
 */
@Data
public class LocationHierarchy {

    /**
     * The parent location's ID
     */
    String parentId;

    /**
     * The child location's ID.
     */
    String childId;
}
