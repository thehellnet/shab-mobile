package org.thehellnet.shab.protocol.bean;

import java.util.Locale;

/**
 * Created by sardylan on 19/07/16.
 */
public class Position {

    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private double angle;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Lat: %.06f - Long: %.06f - Alt: %.06f - Speed: %.06f - Angle: %.06f",
                latitude, longitude, altitude, speed, angle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;

        Position position = (Position) o;

        if (Double.compare(position.getLatitude(), getLatitude()) != 0) return false;
        if (Double.compare(position.getLongitude(), getLongitude()) != 0) return false;
        if (Double.compare(position.getAltitude(), getAltitude()) != 0) return false;
        if (Double.compare(position.getSpeed(), getSpeed()) != 0) return false;
        return Double.compare(position.getAngle(), getAngle()) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getLatitude());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLongitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getAltitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getSpeed());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getAngle());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
