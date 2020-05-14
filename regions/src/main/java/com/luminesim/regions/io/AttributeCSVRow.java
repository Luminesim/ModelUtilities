package com.luminesim.regions.io;

import lombok.Data;

/**
 * A row in a CSV file describing a attribute of an entity, e.g. Sex, Age Band, type of dwelling, etc.
 */
@Data
public class AttributeCSVRow {
    public String locationId;
    public String attribute;
    public String value;
}
