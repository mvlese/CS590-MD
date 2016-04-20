package net.leseonline.sundial;

import java.util.ArrayDeque;
import android.location.Location;

/**
 * This class provides the one and only instance of LocationDeque.
 */
@Deprecated
public class LocationDeque extends ArrayDeque<Location> {

    private static LocationDeque _this = null;
    private LocationDeque() {
    }

    public static LocationDeque handle() {
        if (_this == null) {
            _this = new LocationDeque();
        }

        return _this;
    }
}
