package com.carparking.algorithm;

/**
 * EPIC 01 / EPIC 02 – Location & Distance Module
 *
 * Reusable utility class that implements the Haversine formula to calculate
 * the great-circle distance (in km) between two GPS coordinates.
 *
 * Formula reference: https://en.wikipedia.org/wiki/Haversine_formula
 */
public final class HaversineUtil {

    /** Earth's mean radius in kilometres */
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Utility class – prevent instantiation
    private HaversineUtil() {}

    /**
     * Calculate the distance between two GPS points using the Haversine formula.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in kilometres (double, always >= 0)
     */
    public static double distanceKm(double lat1, double lon1,
                                    double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1))
                 * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check whether a slot is within a given radius from the driver's location.
     *
     * @param driverLat  Driver's latitude
     * @param driverLon  Driver's longitude
     * @param slotLat    Slot's latitude
     * @param slotLon    Slot's longitude
     * @param radiusKm   Search radius in kilometres
     * @return true if the slot is within the radius
     */
    public static boolean isWithinRadius(double driverLat, double driverLon,
                                         double slotLat,   double slotLon,
                                         double radiusKm) {
        return distanceKm(driverLat, driverLon, slotLat, slotLon) <= radiusKm;
    }
}
