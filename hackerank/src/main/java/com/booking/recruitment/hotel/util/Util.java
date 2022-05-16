package com.booking.recruitment.hotel.util;

import com.booking.recruitment.hotel.model.Hotel;

public class Util {

    public static int CompareHotelRatings(Double r1, Double r2) {
        if (r1 == null ^ r2 == null) {
            return (r1 == null) ? -1 : 1;
        }

        if (r1 == null && r2 == null) {
            return 0;
        }
        return r1.compareTo(r2);
    }

    public static int compareHotelDistance(Hotel h1, Hotel h2) {
        double cityCentreLatitude = h1.getCity().getCityCentreLatitude();
        double cityCentreLongitude = h1.getCity().getCityCentreLongitude();

        double h1Latitude = h1.getLatitude();
        double h1Longitude = h1.getLongitude();

        double h2Latitude = h2.getLatitude();
        double h2Longitude = h2.getLongitude();

        double distanceFromH1 = calculateDistance(cityCentreLatitude, cityCentreLongitude, h1Latitude, h1Longitude);
        double distanceFromH2 = calculateDistance(cityCentreLatitude, cityCentreLongitude, h2Latitude, h2Longitude);

        return (int) (distanceFromH1 - distanceFromH2);
    }

    /**
     * Method to calculate the distance between two spatial entities using haversine
     * formula
     *
     * @param lat1 - Latitude of first entity
     * @param lon1 - Longitude of first entity
     * @param lat2 - Latitude of second entity
     * @param lon2 - Longitude of second entity
     * @return Distance between the provided two entities
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

}