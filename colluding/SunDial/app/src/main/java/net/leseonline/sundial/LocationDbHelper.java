package net.leseonline.sundial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.ArrayList;

/**
 * Created by mlese on 4/18/2016.
 */
public class LocationDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Locations.db";

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LocationContract.LocationEntry.TABLE_NAME + " (" +
                    LocationContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                    LocationContract.LocationEntry.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    LocationContract.LocationEntry.COLUMN_NAME_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    LocationContract.LocationEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LocationContract.LocationEntry.TABLE_NAME;

    public static void addLocation(LocationDbHelper helper, Location location) {
        // Gets the data repository in write mode
        SQLiteDatabase db = helper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationContract.LocationEntry.COLUMN_NAME_LATITUDE, location.getLatitude());
        values.put(LocationContract.LocationEntry.COLUMN_NAME_LONGITUDE, location.getLongitude());
        values.put(LocationContract.LocationEntry.COLUMN_NAME_TIMESTAMP, System.currentTimeMillis() / 1000L);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                LocationContract.LocationEntry.TABLE_NAME,
                null,
                values);

        fixTableSize(helper);
    }

    public static long getLocationsCount(LocationDbHelper helper) {
        SQLiteDatabase db = helper.getReadableDatabase();
        long numRows = DatabaseUtils.queryNumEntries(db, LocationContract.LocationEntry.TABLE_NAME);
        return numRows;
    }

    public static BasicLocation peekFirstLocation(LocationDbHelper helper) {
        BasicLocation loc = null;
        ArrayList<BasicLocation> locations = getLocations(helper, true);
        if (locations.size() == 1) {
            loc = locations.get(0);
        }

        return loc;
    }

    private static void fixTableSize(LocationDbHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            String table = LocationContract.LocationEntry.TABLE_NAME;
            while (getLocationsCount(helper) > 10) {
                String where = "_ID in (select MIN(_ID) from " + table + ")";
                db.delete(table, where, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ArrayList<BasicLocation> getLocations(LocationDbHelper helper) {
        return getLocations(helper, false);
    }

    private static ArrayList<BasicLocation> getLocations(LocationDbHelper helper, boolean justFirst) {
        SQLiteDatabase db = helper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LocationContract.LocationEntry._ID,
                LocationContract.LocationEntry.COLUMN_NAME_LONGITUDE,
                LocationContract.LocationEntry.COLUMN_NAME_LATITUDE,
                LocationContract.LocationEntry.COLUMN_NAME_TIMESTAMP
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                LocationContract.LocationEntry.COLUMN_NAME_TIMESTAMP + " DESC";

        Cursor cursor = db.query(
                LocationContract.LocationEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<BasicLocation> locations = new ArrayList<BasicLocation>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(LocationContract.LocationEntry._ID));
            float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_LATITUDE));
            float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_LONGITUDE));
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_TIMESTAMP));
            BasicLocation location = new BasicLocation(latitude, longitude, timestamp);
            locations.add(location);
            if (justFirst) {
                break;
            }
            cursor.moveToNext();
        }
        cursor.close();

        return locations;
    }

}
