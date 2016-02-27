package net.leseonline.bbstat;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import net.leseonline.bbstat.contact.Contact;

import java.util.List;


/**
 * This service provides an external port for remote access by a known application.
 */
public class RemoteService extends Service {
    private final String TAG = "RemoteService";

    private Messenger mMessenger = new Messenger(new MyHandler());

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    static final int SAY_HI = 0;
    static final int SAY_HELLO = 1;

    @Override
    public void onCreate() {
        startRemoteService();
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
                case SAY_HI: {
                    Log.d(TAG, "in SAY_HI.");
                    Toast.makeText(getApplicationContext(), "HI, from NASA", Toast.LENGTH_LONG).show();
                    doWork();
                    break;
                }
                case SAY_HELLO:
                    Log.d(TAG, "in SAY_HELLO.");
                    Toast.makeText(getApplicationContext(), "HELLO, from NASA", Toast.LENGTH_LONG).show();
                    doWork();
                    break;
            }
        }

        private void doWork() {
            try {
                Log.d(TAG, "Reading contacts by remote invocation.");
                ContactsReader reader = new ContactsReader(RemoteService.this);
                ContactList contacts = reader.readContacts();
                Log.d(TAG, "Sending contacts to remote service.");
                sendContacts(contacts);
            } catch(Exception ex) {
                Log.d(TAG, "Failed to read contacts by remote invocation.");
                ex.printStackTrace();
            }
        }

        static final int SAY_HI = 0;
        static final int SAY_HELLO = 1;

        private void sendContacts(ContactList contacts) {
            if (remoteMessgener != null) {
                Message msg = Message.obtain(null, SAY_HELLO, 0, 0);
                try {
                    remoteMessgener.send(msg);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
}


