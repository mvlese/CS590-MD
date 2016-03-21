package net.leseonline.sundial;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mlese on 3/20/2016.
 */
public class LocationSupervisor {
    private final String TAG = "LocationSupervisor";
    private LocationManager mLocationManager;
    private static LocationSupervisor _this = null;

    private LocationSupervisor() {
    }

    public static LocationSupervisor getHandle() {
        if (_this == null) {
            _this = new LocationSupervisor();
        }
        return _this;
    }

    public void setLocationManager(LocationManager locationManager) {
        mLocationManager = locationManager;
    }

    public Location getLocation() {
        Location location = null;
        try {
            if (mLocationManager != null) {
                List<String> providers = mLocationManager.getAllProviders();
                Iterator<String> iter = providers.iterator();
                while ((location == null) && (iter.hasNext() == true)) {
                    String s = iter.next();
                    location = mLocationManager.getLastKnownLocation(s);
                }
            }
        } catch (SecurityException ex) {
            Log.d(TAG, "Location permissions has NOT been granted.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return location;
    }

}
