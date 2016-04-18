package net.leseonline.nasaclient;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.*;
import org.json.*;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * This service provides an external port for remote access by a known application.
 */
public class RemoteService extends Service {
    private final String TAG = "RemoteService";

    private final int IDLE = 0;
    private final int LOGON = 1;
    private final int STORE_ENTITY = 2;

    private int state = IDLE;
    private String token = "";

    private Messenger mMessenger = new Messenger(new MyHandler());

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    static final int SAY_HELLO = 1;
    static final int SEND_LOCATIONS = 7;

    /**
     * This Handler handles the message from the remote entity.
     */
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "NASA: received message: " + String.valueOf(msg.what));
            String contactsAsJson = null;
            String locationsAsJson = null;
            Bundle bundle = msg.getData();
            if (bundle != null) {
                contactsAsJson = bundle.getString("contacts");
                locationsAsJson = bundle.getString("locations");
            }
            switch (msg.what) {
//                case SAY_HI: {
//                    Toast.makeText(getApplicationContext(), "HI, BBSTAT", Toast.LENGTH_SHORT).show();
//                    doWork(contactsAsJson);
//                    break;
//                }
                case SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "HELLO, BBSTAT", Toast.LENGTH_SHORT).show();
                    if (contactsAsJson != null) {
                        doWork(contactsAsJson, "Contacts");
                    }
                    break;
                case SEND_LOCATIONS:
                    Log.d(TAG, "received locations: " + locationsAsJson);
                    Toast.makeText(getApplicationContext(), "HELLO, SunDial", Toast.LENGTH_SHORT).show();
                    if (locationsAsJson != null) {
                        doWork(locationsAsJson, "Locations");
                    }
                    break;
            }
        }

        private void storeEntity(JsonHttpResponseHandler handler, String contactsAsJson, String tag) {
            try {
                if (!token.isEmpty()) {
                    String key = tag + android.text.format.DateFormat.format("yyyyMMddTHHmmss", new java.util.Date());
                    JotRequest req = new JotStoreEntityRequest(token, key, contactsAsJson);
                    String jsonString = req.toJSONString();
                    Log.d(TAG, jsonString);
                    String jsonStringEnc = URLEncoder.encode(jsonString, "US-ASCII");
                    Log.d(TAG, jsonStringEnc);

                    RequestParams rp = new RequestParams();
                    rp.add("api", jsonStringEnc);
                    Log.d(TAG, rp.toString());

                    state = STORE_ENTITY;
                    HttpUtils.post("", rp, handler);
                }
            } catch (Exception ex){
                Log.d(TAG, "Failed to getEntity.");
                ex.printStackTrace();
            }
        }

        private void doWork(final String jsonStringToSend, final String tag) {
            try {
                Log.d(TAG, "Received data from remote service.");
                Log.d(TAG, jsonStringToSend);

                JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.d(TAG, "---------------- this is response : " + response);
                        try {
                            JSONObject serverResp = new JSONObject(response.toString());
                            String errorMessage = serverResp.getString("errorMessage");
                            if (errorMessage.isEmpty()) {
                                switch(state){
                                    case LOGON:
                                        token = serverResp.getString("retval");
                                        Log.d(TAG, "---------------- token : " + token);
                                        storeEntity(this, jsonStringToSend, tag);
                                        break;
                                    case STORE_ENTITY:
                                        String retval = serverResp.getString("retval");
                                        Log.d(TAG, "---------------- storeEntity : " + retval);
                                        break;
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                        // Pull out the first event on the public timeline
                        Log.d(TAG, "---------------- in onSuccess");
                    }
                };

                JotRequest req = new JotLogonRequest("cs590", "baseb@l!");
                String jsonString = req.toJSONString();
                Log.d(TAG, jsonString);
                String jsonStringEnc = URLEncoder.encode(jsonString, "US-ASCII");
                Log.d(TAG, jsonStringEnc);

                RequestParams rp = new RequestParams();
                rp.add("api", jsonStringEnc);
                Log.d(TAG, rp.toString());

                token = "";
                state = LOGON;
                HttpUtils.post("", rp, handler);

            } catch (Exception ex) {
                Log.d(TAG, "Failed to send contacts by remote invocation.");
                ex.printStackTrace();
            }
        }
    }
}

