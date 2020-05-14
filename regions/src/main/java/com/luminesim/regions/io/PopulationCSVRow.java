package com.luminesim.regions.io;

import lombok.Data;

/**
 * Row in a population file. Range is [startAge, endAge)
 */
@Data
public class PopulationCSVRow {
    private String locationId;
    private int startAge;
    private int endAge;
    private int count;
    private String segment;
    private String citation;
}
