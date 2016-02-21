package net.leseonline.bbstat;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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

    /**
     * This Handler handles the message from the remote entity.
     */
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SAY_HI: {
                    Toast.makeText(getApplicationContext(), "HI", Toast.LENGTH_LONG).show();
                    doWork();
                    break;
                }
                case SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "HELLO", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private void doWork() {
            try {
                ContactsReader reader = new ContactsReader(RemoteService.this);
                List<Contact> contacts = reader.readContacts();
                Log.d(TAG, "Reading contacts by remote invocation.");
            } catch(Exception ex) {
                Log.d(TAG, "Failed to read contacts by remote invocation.");
                ex.printStackTrace();
            }
        }
    }
}


