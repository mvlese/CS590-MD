package net.leseonline;

import java.security.Permissions;
import java.util.Iterator;
import java.util.List;
import java.text.DecimalFormat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Config;
import android.util.Log;
import android.widget.Button;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationAvailability;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.FusedLocationProviderApi;

//import static com.google.android.gms.location.LocationServices.*;

//public class SunDial extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
public class SunDial extends Activity implements SensorEventListener {
	private SunDialView mSunDialView;
	private LocationManager mLocationManager;
	private SensorManager mSensorManager;
    private Location mLocation;
	private LatitudeDialog setLatDialog;
    private Sensor mOrientation;

    private static final String TAG = "Compass";
    private static final String LAT = "Latitude";
    private static final int DIALOG_SET_LAT = 1;
    private static final int MENU_SET_LAT = 1;
    private static final int MENU_SET_MINE = 2;
	private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;

    private enum State {
        IDLE,
        SETUP,
        USE,
        USE_GOOGLE
    };
    private State mState = State.IDLE;
//    private GoogleApiClient mGoogleApiClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mSunDialView = new SunDialView(this);
        mState = State.SETUP;
        checkForLocationPermissions();

        setContentView(mSunDialView);
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

// Create an instance of GoogleAPIClient.
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(API)
//                    .build();
//        }
    }
    
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    		case DIALOG_SET_LAT:
    			setLatDialog = new LatitudeDialog(this);
    			setLatDialog.setOnDismissListener(new OnDismissListener(){

					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						Double d = setLatDialog.getLatitude();
						if (d != 0.0) {
				            Log.d(LAT, "Lat Changed (" + d.toString() + ")");
							mSunDialView.setLatitude(d);
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
            switch (mState) {
                case IDLE:
                    break;
                case SETUP: {
                    mLocationManager = (LocationManager)
                            this.getSystemService(Context.LOCATION_SERVICE);
                    List<String> providers = mLocationManager.getAllProviders();
                    Iterator<String> iter = providers.iterator();
                    while (iter.hasNext() == true) {
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
                            Location location = mLocationManager.getLastKnownLocation(s);
                            setLocation(location);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                    break;
                }
                case USE: {
                    List<String> providers = mLocationManager.getAllProviders();
                    Iterator<String> iter = providers.iterator();
                    while (iter.hasNext() == true) {
                        String s = iter.next();
                        Location location = mLocationManager.getLastKnownLocation(s);
                        setLocation(location);
                    }
                    break;
                }
//                case USE_GOOGLE:
//                {
//                    LocationAvailability av = FusedLocationApi.getLocationAvailability(mGoogleApiClient);
//                    mLastLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);
//                    if (mLastLocation != null) {
//                        mLatitudeText = String.valueOf(mLastLocation.getLatitude());
//                        mLongitudeText = String.valueOf(mLastLocation.getLongitude());
//                    }
//                    break;
//                }
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

    private void setLocation(Location location) {
    	if (location != null) {
			mLocation = location;
			Double lat = mLocation.getLatitude();
			DecimalFormat df = new DecimalFormat("###.00");
			setTitle("Latitude: " + df.format(lat));
			mSunDialView.setLatitude(location.getLatitude());
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
    protected void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();
        this.mSensorManager.registerListener(this, mOrientation,
//        		SensorManager.SENSOR_ORIENTATION,
        		SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(this);
//        mGoogleApiClient.disconnect();
        super.onStop();
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float azimuth_angle = event.values[0];
		float pitch_angle = event.values[1];
		float roll_angle = event.values[2];
		// Do something with these orientation angles.
	}

//    @Override
//    public void onConnected(Bundle bundle) {
//        mState = State.USE_GOOGLE;
//        checkForLocationPermissions();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }

}