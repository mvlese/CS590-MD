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
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * This service provides an external port for remote access by a known application.
 */
public class RemoteService extends Service {
    private final String TAG = "RemoteService";

    private final int IDLE = 0;
    private final int LOGON = 1;
    private final int GET_ENTITY = 2;

    private int state = IDLE;
    private String token = "";

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
            String contactsAsJson = "not set";
            Bundle bundle = msg.getData();
            if (bundle != null) {
                contactsAsJson = bundle.getString("contacts");
            }
            switch (msg.what) {
                case SAY_HI: {
                    Toast.makeText(getApplicationContext(), "HI, BBSTAT", Toast.LENGTH_SHORT).show();
                    doWork(contactsAsJson);
                    break;
                }
                case SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "HELLO, BBSTAT", Toast.LENGTH_SHORT).show();
                    doWork(contactsAsJson);
                    break;
            }
        }

        private void getEntity(JsonHttpResponseHandler handler) {
            try {
                if (!token.isEmpty()) {
                    JSONObject req = new JSONObject();
                    req.put("method", "getEntity");
                    req.put("args", new JSONObject()
                            .put("token", token)
                            .put("key", "Hoss Cartwright"));
                    String jsonString = req.toString();
                    Log.d(TAG, jsonString);
                    String jsonStringEnc = URLEncoder.encode(jsonString, "US-ASCII");
                    Log.d(TAG, jsonStringEnc);

                    RequestParams rp = new RequestParams();
                    rp.add("api", jsonStringEnc);
                    Log.d(TAG, rp.toString());

                    state = GET_ENTITY;
                    HttpUtils.post("", rp, handler);
                }
            } catch (Exception ex){
                Log.d(TAG, "Failed to getEntity.");
                ex.printStackTrace();
            }
        }

        private void doWork(String contactsAsJson) {
            try {
                Log.d(TAG, "Received contact from remote service.");
                Log.d(TAG, contactsAsJson);

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
                                        getEntity(this);
                                        break;
                                    case GET_ENTITY:
                                        Log.d(TAG, "---------------- getEntity : ");
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

                JSONObject req = new JSONObject();
                req.put("method", "logon");
                req.put("args", new JSONObject()
//                        .put("username", "cs590")
//                        .put("password", "baseb@l!"));
                        .put("username", "mlese")
                        .put("password", "P@ssw0rd"));
                //String jsonString = "{\"method\":\"logon\",\"args\":{\"username\":\"cs590\",\"password\":\"baseb@l!\"}}";
//                String jsonStringEnc = "api=" + URLEncoder.encode(jsonString, "US-ASCII");
                String jsonString = req.toString();
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

