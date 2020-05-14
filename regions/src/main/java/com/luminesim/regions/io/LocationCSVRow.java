package com.luminesim.regions.io;

import lombok.Data;

@Data
public class LocationCSVRow {

    /**
     * The location's ID.
     */
    public String id;

    /**
     * The name of the location.
     */
    public String name;
}
