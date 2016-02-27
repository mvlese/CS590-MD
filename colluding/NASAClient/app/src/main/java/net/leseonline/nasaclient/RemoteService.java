package net.leseonline.nasaclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

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

    /**
     * This Handler handles the message from the remote entity.
     */
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SAY_HI: {
                    Toast.makeText(getApplicationContext(), "HI NASA", Toast.LENGTH_LONG).show();
                    doWork();
                    break;
                }
                case SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "HELLO NASA", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private void doWork() {
            try {
                Log.d(TAG, "Sending contacts by remote invocation.");
            } catch(Exception ex) {
                Log.d(TAG, "Failed to send contacts by remote invocation.");
                ex.printStackTrace();
            }
        }
    }
}

