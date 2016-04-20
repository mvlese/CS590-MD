package net.leseonline.sundial;

import android.provider.BaseColumns;

/**
 * Created by mlese on 4/18/2016.
 */
public class LocationContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public LocationContract() {}

    /* Inner class that defines the table contents */
    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME_LOCATION_ID = "locationid";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }}
