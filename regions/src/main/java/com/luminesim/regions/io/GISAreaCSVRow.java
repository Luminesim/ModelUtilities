package com.luminesim.regions.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;

/**
 * A (location, latitude, longitude) tuple.
 */
@Data
public class GISAreaCSVRow {

    /**
     * The ID of the location to which this area is tied.
     */
    @NonNull
    public String id;
    public double latitude;
    public double longitude;
}
