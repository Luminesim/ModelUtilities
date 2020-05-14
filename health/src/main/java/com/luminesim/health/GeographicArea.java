package com.luminesim.health;

/**
 * A geographic area.
 */
public interface GeographicArea {
    /**
     *
     * @return
     *  The geofence of the geographic area.
     *  A collection of lat,lon,lat,lon... points
     */
    double[] getGeofence();
}
