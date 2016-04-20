package net.leseonline.sundial;

/**
 * Created by mlese on 4/19/2016.
 */
public class BasicLocation {
    private float latitude;
    private float longitude;
    private long timestamp;

    public BasicLocation() {
        this.latitude = 0.0f;
        this.longitude = 0.0f;
        this.timestamp = 0L;
    }

    public BasicLocation(float latitude, float longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
