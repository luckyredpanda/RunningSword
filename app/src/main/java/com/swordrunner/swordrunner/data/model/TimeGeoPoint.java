package com.swordrunner.swordrunner.data.model;

import org.osmdroid.util.GeoPoint;

public class TimeGeoPoint extends GeoPoint {

    private long recordedAt = 0L;

    public TimeGeoPoint(double aLatitude, double aLongitude) {
        super(aLatitude, aLongitude);
    }

    public TimeGeoPoint(GeoPoint p, long date) {
        super(p);
        recordedAt = date;
    }
}
