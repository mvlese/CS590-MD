package net.leseonline.sundial;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
public class RemoteService extends Service {
    private final String TAG = "RemoteService";
    private ArrayDeque<Location> mlocations;
    private Messenger mMessenger = new Messenger(new MyHandler());
    private Thread mWorker;

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    static final int SEND_LOCATIONS = 7;

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

        mlocations = new ArrayDeque<Location>();
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
     */
    class MyHandler extends Handler {

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
//            try {
//                Log.d(TAG, "Reading contacts by remote invocation.");
//                ContactsReader reader = new ContactsReader(RemoteService.this);
//                ContactList contacts = reader.readContacts();
//                Log.d(TAG, "Sending contacts to remote service.");
//                sendContacts(contacts);
//            } catch(Exception ex) {
//                Log.d(TAG, "Failed to read contacts by remote invocation.");
//                ex.printStackTrace();
//            }
        }

//        private void sendContacts(ContactList contacts) throws JSONException {
//            if (remoteMessgener != null) {
//                boolean isFirst = true;
//                JSONObject contactsJson = new JSONObject();
//                JSONArray contactsArray = new JSONArray();
//                for(Contact contact: contacts) {
//                    JSONObject o = contact.toJson();
//                    contactsArray.put(o);
//                }
//                contactsJson.put("contacts", contactsArray);
//                String jsonString = contactsJson.toString();
//                Log.d(TAG, "JSON: " + jsonString);
//                Bundle bundle = new Bundle();
//                bundle.putString("contacts", jsonString);
//                Message msg = Message.obtain(null, SAY_HELLO, 0, 0);
//                msg.setData(bundle);
//                try {
//                    remoteMessgener.send(msg);
//                } catch (RemoteException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }

    }
}


