package net.leseonline.bbstat;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;


/**
 * This service provides an external port for remote access by a known application.
 */
public class RemoteService extends Service {
    Messenger mMessenger = new Messenger(new MyHandler());

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
                case SAY_HI:
                    Toast.makeText(getApplicationContext(), "HI", Toast.LENGTH_LONG).show();
                    break;
                case SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "HELLO", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}


