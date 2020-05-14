package com.luminesim.regions;

import com.google.common.primitives.Doubles;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

import static com.luminesim.qa.ContractUtilities.precondition;

/**
 * A GIS location. This can either be a point (one lat/lon pair) or a fenced region (at least three points).
 *
 * @apiNote The area's interface is designed to be easily compatible with AnyLogic APIs as it is often integrated into
 * that tool.
 */
@RequiredArgsConstructor
@Slf4j
public class GISArea {

    /**
     * The ID of the location to which this area is tied.
     */
    @Getter
    private final String locationId;

    /**
     * The latitudes in the GIS region.
     * There may be exactly one or more than three.
     */
    private ArrayList<Double> latitudes = new ArrayList<Double>(1);

    /**
     * The longitudes in the GIS region.
     * There may be exactly one or more than three.
     */
    private ArrayList<Double> longitudes = new ArrayList<Double>(1);

    /**
     * The path of the area.
     */
    private Path2D path = new Path2D.Double();

    /**
     * @return True, if the location is a point and not a region.
     */
    public boolean isPoint() {
        // Sanity check
        assertValidity();

        return latitudes.size() == 1;
    }

    /**
     * @return True, if the location is a region and not a point.
     */
    public boolean isRegion() {
        // Sanity check
        assertValidity();

        return latitudes.size() >= 3;
    }

    /**
     *
     * @return
     *  An array of {lat1, lon1, lat2, lon2, ... latn, lonn}
     * @apiNote
     *  For compatibility with AnyLogic
     */
    public double[] getLatLonPairs() {
        double[] pairs = new double[latitudes.size() * 2];
        for (int i = 0; i < latitudes.size(); i += 1) {
            pairs[2*i] = latitudes.get(i);
            pairs[2*i+1] = longitudes.get(i);
        }
        return pairs;
    }

    /**
     * Ensures this is a valid GIS location.
     */
    private void assertValidity() {
        if (latitudes.size() != longitudes.size()) {
            throw new IllegalStateException("GIS location needs equal number of lat/lon points.");
        }
        if (!(latitudes.size() == 1 || latitudes.size() >= 3)) {
            throw new IllegalStateException("GIS location needs either one point or more than two.");
        }
    }

    /**
     * Adds a point to the area.
     *
     * @param latitude
     * @param longitude
     */
    public void addPoint(double latitude, double longitude) {

        // Update the path.
        if (latitudes.isEmpty()) {
            path.moveTo(latitude, longitude);
        }
        else {
            path.lineTo(latitude, longitude);
        }

        latitudes.add(latitude);
        longitudes.add(longitude);


    }

    /**
     * @return True, if the area has enough points to be a point or region.
     */
    public boolean isValid() {
        return isPoint() || isRegion();
    }

    /**
     * @param latLonAction An action to perform with each point.
     */
    public void forEach(@NonNull BiConsumer<Double, Double> latLonAction) {

        // Sanity check.
        precondition("GIS area is not a valid area. Does it have any points?", isValid());

        // Iterate.
        for (int i = 0; i < latitudes.size(); i += 1) {
            latLonAction.accept(latitudes.get(i), longitudes.get(i));
        }
    }

    public double[] getLatitudes() {
        return Doubles.toArray(latitudes);
    }

    public double[] getLongitudes() {
        return Doubles.toArray(longitudes);
    }

    public boolean contains(double lat, double lon) {
        Area us = new Area(this.path);
        return us.contains(lat, lon);
    }

    /**
     * @return
     *  The area of the shape.
     * @see <a href="https://arachnoid.com/area_irregular_polygon/index.html">The website</a> from which this algorithm was derived.
     */
    public double area() {

        int n = latitudes.size();
        double[] x = getLatitudes();
        double[] y = getLongitudes();
        double sumA = 0;
        for (int i = 0; i < n-1; i += 1) {
            sumA += (x[i]*y[i+1]);
        }

        double sumB = 0;
        for (int i = 0; i < n-1; i += 1) {
            sumB += x[i+1]*y[i];
        }

        return 0.5 * Math.abs(sumA + x[n-1]*y[0] - sumB - x[0]*y[n-1]);
    }

    public boolean intersects(GISArea other) {
        // Four cases:
        // 1. We're both points.
        if (this.isPoint() && other.isPoint()) {
            return this.latitudes.equals(other.latitudes) && this.longitudes.equals(other.longitudes);
        }
        // 2. We're a point, they're not.
        else if (this.isPoint() && other.isRegion()) {
            Area them = new Area(other.path);
            return them.contains(this.latitudes.get(0), this.longitudes.get(0));
        }
        // 3. We're a region, they're a point.
        else if (this.isRegion() && other.isPoint()) {
            Area us = new Area(this.path);
            return us.contains(other.latitudes.get(0), other.longitudes.get(0));
        }
        // 4. We're both regions.
        else {
            // Make the shapes for both areas.
            // Take the intersect
            // If an empty shape, no intersection.
            Area us = new Area(path);
            Area them = new Area(other.path);
            us.intersect(them);
            return !us.isEmpty();
        }
    }
}
