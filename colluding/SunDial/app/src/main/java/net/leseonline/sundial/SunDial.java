package net.leseonline.sundial;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.text.DecimalFormat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import net.leseonline.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SunDial extends Activity implements SensorEventListener {
    private SunDialView mSunDialView;
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Location mLocation;
    private LatitudeDialog setLatDialog;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float mAzimuthAngle = Float.MIN_VALUE;
    private boolean mSendLocations = false;

    private static final int SEND_LOCATIONS = 7;
    static final int RECEIVE_LOCATIONS = 8;
    private static final String TAG = "Compass";
    private static final String LAT = "Latitude";
    private static final int DIALOG_SET_LAT = 1;
    private static final int MENU_SET_LAT = 1;
    private static final int MENU_SET_MINE = 2;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;
    //    private ArrayDeque<Location> mLocations = new ArrayDeque<Location>();
    private Thread mWorker;
    private Object mLockObject = new Object();
    private boolean isRemoteBound = false;
    private Messenger remoteMessgener = null;
    private LocationDbHelper mDbHelper;

    private enum State {
        IDLE,
        SETUP,
        USE,
        USE_GOOGLE
    }

    ;
    private State mState = State.IDLE;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDbHelper = new LocationDbHelper(this);
        mSunDialView = new SunDialView(this);
        mState = State.SETUP;
        checkForLocationPermissions();

        setContentView(mSunDialView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Iterator<Sensor> i = sensors.iterator();
        while (i.hasNext()) {
            Sensor s = i.next();
            Log.d(TAG, s.getName());
        }
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mWorker = new Thread() {
            int counter = 0;

            public void run() {
                boolean interrupted = false;
                while (!interrupted) {
                    try {
                        if ((counter % 10) == 0) {
                            getLocation();
                        }
                        counter++;
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        interrupted = true;
                    }
                }
            }
        };

        startRemoteService();
        mWorker.start();
    }

    @Override
    public void onDestroy() {
        mWorker.interrupt();
        try {
            mWorker.join(1000);
        } catch (InterruptedException ex) {
        }

//        try {
//            Intent mIntent = new Intent(this, LocationService.class);
//            stopService(mIntent);
//        } catch (Exception ex) { }

        if (remoteServiceConnection != null && isRemoteBound) {
            unbindService(remoteServiceConnection);
        }
        super.onDestroy();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SET_LAT:
                setLatDialog = new LatitudeDialog(this);
                setLatDialog.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        Double d = setLatDialog.getLatitude();
                        if (d != 0.0) {
                            Log.d(LAT, "Lat Changed (" + d.toString() + ")");
                            mSunDialView.setLatitude(d);
                            setLatitudeLabel(d);
                        }
                    }
                });
                break;
            default:
                setLatDialog = null;
                break;
        }
        return setLatDialog;
    }

    // Creates the menu items
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SET_MINE, 0, "Set My Location")
                .setIcon(android.R.drawable.ic_menu_mylocation);
        menu.add(0, MENU_SET_LAT, 0, "Set Latitude")
                .setIcon(android.R.drawable.ic_menu_compass);
        return true;
    }

    // Handles item selections
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SET_LAT:
                this.showDialog(DIALOG_SET_LAT);
                return true;
            case MENU_SET_MINE:
                mState = State.USE;
                checkForLocationPermissions();
                return true;
            default:
                break;
        }
        return false;
    }

    private void getLocation() {
        // Get the current location.
        // If different than the most recent in the deque, add to the deque and send
        // to the remote service if there are 10 or more in the deque.
        LocationSupervisor ls = LocationSupervisor.getHandle();
        Location location = ls.getLocation();
        if (location != null) {
            if (LocationDbHelper.getLocationsCount(mDbHelper) == 0) {
                LocationDbHelper.addLocation(mDbHelper, location);
            } else {
                BasicLocation first = LocationDbHelper.peekFirstLocation(mDbHelper);
                // If difference between the most recent and the new is greater than
                // 0.001 for either that lat or long, add the location.
                if (first != null && ((Math.abs(first.getLatitude() - location.getLatitude()) > 0.001) ||
                        (Math.abs(first.getLongitude() - location.getLongitude()) > 0.001))) {
//                    Toast.makeText(getApplicationContext(), "Adding Location", Toast.LENGTH_SHORT).show();
                    LocationDbHelper.addLocation(mDbHelper, location);
                }
            }
        }
        synchronized (mLockObject) {
            if (mSendLocations) {
                mSendLocations = false;
                sendLocations();
            }
        }
    }

    private void checkForLocationPermissions() {
        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //given that Manifest.permission.READ_CONTACTS and Manifest.permission.WRITE_CONTACTS
            //are from the same permission group , on theory it is enough to check for only one of them
            //yet permission groups are subject to change therefore it is s good idea to check
            //for both permissions

            // Contacts permissions have not been granted.
            Log.i(TAG, "Location permissions has NOT been granted. Requesting permissions.");
            ActivityCompat.requestPermissions(SunDial.this, PERMISSIONS_LOCATION, 1);
        } else {
            // Location permissions have been granted. Show the contacts fragment.
            Log.i(TAG,
                    "Location permissions have already been granted. Displaying contact details.");
            if (mLocationManager == null) {
                mLocationManager = (LocationManager)
                        this.getSystemService(Context.LOCATION_SERVICE);
                LocationSupervisor.getHandle().setLocationManager(mLocationManager);
            }
            switch (mState) {
                case IDLE:
                    break;
                case SETUP:
                    List<String> providers = mLocationManager.getAllProviders();
                    Iterator<String> iter = providers.iterator();
                    Location location = null;
                    while (location == null && iter.hasNext() == true) {
                        try {
                            String s = iter.next();
                            LocationProvider lp = mLocationManager.getProvider(s);
                            mLocationManager.requestLocationUpdates(s, 1000, (float) 1000.0,
                                    new LocationListener() {

                                        @Override
                                        public void onLocationChanged(Location location) {
                                            // TODO Auto-generated method stub
                                            if (location != null) {
                                                setLocation(location);
                                            }
                                        }

                                        @Override
                                        public void onProviderDisabled(String provider) {
                                            // TODO Auto-generated method stub
                                        }

                                        @Override
                                        public void onProviderEnabled(String provider) {
                                            // TODO Auto-generated method stub
                                        }

                                        @Override
                                        public void onStatusChanged(String provider,
                                                                    int status, Bundle extras) {
                                            // TODO Auto-generated method stub
                                        }
                                    });
                            location = mLocationManager.getLastKnownLocation(s);
                            setLocation(location);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                    break;
                case USE:
                    LocationSupervisor ls = LocationSupervisor.getHandle();
                    location = ls.getLocation();
                    setLocation(location);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permissions approved.");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "Location permissions denied.");
                }
                break;
            }
            default:
                break;
        }
    }

    private void setLatitudeLabel(Double latitude) {
        DecimalFormat df = new DecimalFormat("###.00");
        setTitle("Latitude: " + df.format(latitude));
    }

    private void setLocation(Location location) {
        if (location != null) {
            mLocation = location;
            Double lat = mLocation.getLatitude();
            setLatitudeLabel(lat);
            mSunDialView.setLatitude(lat);
        }
    }

    private final SensorListener mListener = new SensorListener() {

        @Override
        public void onSensorChanged(int sensor, float[] values) {
            Log.d(TAG, "sensorChanged (" + values[0] + ", " + values[1] + ", " + values[2] + ")");
            mSunDialView.setSensorValues(values);
        }

        @Override
        public void onAccuracyChanged(int sensor, int accuracy) {
        }

    };

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        this.mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
        this.mSensorManager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private float[] mGravity;
    private float[] mGeomagnetic;
    private int mRotation = 99;

    @Override
    public void onSensorChanged(SensorEvent event) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (rotation != mRotation) {
            mRotation = rotation;
            synchronized (mLockObject) {
                mSendLocations = true;
            }
        }
    }

    private JSONObject locationsToJSON() {
        JSONObject result = new JSONObject();
        try {
            JSONArray locations = new JSONArray();
            ArrayList<BasicLocation> locationsFromDb = LocationDbHelper.getLocations(mDbHelper);
            for (BasicLocation loc : locationsFromDb) {
                JSONObject item = new JSONObject();
                item.put("lat", loc.getLatitude());
                item.put("long", loc.getLongitude());
                item.put("time", loc.getTimestamp());
                locations.put(item);
            }
            result.put("locations", locations);
        } catch (JSONException ex) {
            result = null;
        }

        return result;
    }

    private void sendLocations() {
        if (isRemoteBound) {
            String jsonString = locationsToJSON().toString();
            Log.d(TAG, "JSON: " + jsonString);
            Bundle bundle = new Bundle();
            bundle.putString("locations", jsonString);
            Message msg = Message.obtain(null, SEND_LOCATIONS, 0, 0);
            msg.setData(bundle);
            try {
                remoteMessgener.send(msg);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Intent convertImplicitIntentToExplicitIntent(Intent implicitIntent, Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentServices(implicitIntent, 0);

        if (resolveInfoList == null || resolveInfoList.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfoList.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    private void startRemoteService() {
        try {
            Intent mIntent = new Intent();
            mIntent.setAction("net.leseonline.nasaclient.RemoteService");
            Intent explicitIntent = convertImplicitIntentToExplicitIntent(mIntent, getApplicationContext());
            bindService(explicitIntent, remoteServiceConnection, BIND_AUTO_CREATE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ServiceConnection remoteServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                isRemoteBound = true;
                remoteMessgener = new Messenger(service);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteServiceConnection = null;
            isRemoteBound = false;
        }
    };

}