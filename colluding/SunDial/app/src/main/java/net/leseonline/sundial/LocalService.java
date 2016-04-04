package net.leseonline.sundial;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This service provides an external port for remote access by a known application.
 */
public class LocalService extends Service implements SensorEventListener {
    private final String TAG = "LocalService";
    private Messenger mRemoteMessenger = new Messenger(new MyRemoteHandler());
    private Messenger mLocalMessenger = new Messenger(new MyLocalHandler());
    private Thread mWorker;
    private Object mLockObject = new Object();
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float mAzimuthAngle = Float.MIN_VALUE;
    private boolean mSendLocations = false;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private SensorManager mSensorManager;
    private ArrayDeque<Location> mLocations;

    static final int SEND_LOCATIONS = 7;
    static final int RECEIVE_LOCATIONS = 8;


    @Override
    public IBinder onBind(Intent arg0) {
        return mRemoteMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        mWorker = new Thread() {
            int counter = 0;
            public void run() {
                try {
                    if ((counter % 10) == 0) {
                        getLocation();
                    }
                    counter++;
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {

                }
            }
        };

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLocations = new ArrayDeque<Location>();

        startRemoteService();
    }

    @Override
    public void onDestroy() {
        mWorker.interrupt();
        try {
            mWorker.join(1000);
        } catch (InterruptedException ex) {

        }
    }

    private void getLocation() {
        // Get the current location.
        // If different than the most recent in the deque, add to the deque and send
        // to the remote service if there are 10 or more in the deque.
        LocationSupervisor ls = LocationSupervisor.getHandle();
        Location location = ls.getLocation();
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
            bindService(explicitIntent, serviceConnection, BIND_AUTO_CREATE);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isBound = false;
    private Messenger remoteMessgener = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                isBound = true;
                remoteMessgener = new Messenger(service);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceConnection = null;
            isBound = false;
        }
    };

    /**
     * This Handler handles the message from the remote entity.
     * There should not be any.
     */
    class MyRemoteHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_LOCATIONS: {
                    Log.d(TAG, "in SEND_LOCATIONS.");
                    Toast.makeText(getApplicationContext(), "NASA client says please send locations.", Toast.LENGTH_LONG).show();
                    doWork();
                    break;
                }
            }
        }

        private void doWork() {
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation contains: azimuth, pitch and roll
                float azimuth_angle = orientation[0];
                if (Math.abs(azimuth_angle - mAzimuthAngle) > 0.001) {
                    Log.d(TAG, Arrays.toString(orientation));
                    synchronized (mLockObject) {
                        mSendLocations = true;
                    }
                }
                mAzimuthAngle = azimuth_angle;
            }
        }
    }

    private JSONObject locationsToJSON() {
        JSONObject result = new JSONObject();
        try {
            JSONArray locations = new JSONArray();
            for (Location loc : mLocations) {
                JSONObject item = new JSONObject();
                item.put("lat", loc.getLatitude());
                item.put("long", loc.getLongitude());
                item.put("time", loc.getTime());
                locations.put(item);
            }
            result.put("locations", locations);
        }
        catch(JSONException ex) {
            result = null;
        }

        return result;
    }

    private void sendLocations() {
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

    /**
     * This Handler handles the message from the local entity, which is the main activity.
     */
    class MyLocalHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECEIVE_LOCATIONS: {
                    Log.d(TAG, "in SEND_LOCATIONS.");
                    doWork();
                    break;
                }
            }
        }

        private void doWork() {
        }

    }
}


