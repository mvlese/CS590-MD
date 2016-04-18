package net.leseonline.nasaclient;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.ImageView;

import cz.msebera.android.httpclient.HttpRequest;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private boolean isBound = false;
    private Messenger mMessenger;
    private String mApiKey = "AObnhIGn0LH1lBDrjOd8g0p1jGbeG8zY9vJP8qXt";
    private String mApodUrl = "https://api.nasa.gov/planetary/apod";

    @Override
    protected void onStart() {
        super.onStart();
        startRemoteService();
        new HttpRequestTask().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRemoteContacts();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_apod) {
            return true;
        } else if (id == R.id.action_apod_by_date) {
            createDialog(999).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Dialog createDialog(int id) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(this, myDateListener, year, month, day);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                isBound = true;
                mMessenger = new Messenger(service);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            isBound = false;
        }
    };

    private void startRemoteService() {
        try {
            Intent intent = new Intent();
            intent.setAction("net.leseonline.bbstat.RemoteService");
            Intent explicitIntent = convertImplicitIntentToExplicitIntent(intent, getApplicationContext());
            bindService(explicitIntent, serviceConnection, BIND_AUTO_CREATE);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Intent convertImplicitIntentToExplicitIntent(Intent implicitIntent, Context context) {
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

    static final int SAY_HI = 0;
    static final int SAY_HELLO = 1;

    private void getRemoteContacts() {
        if (mMessenger != null) {
            Message msg = Message.obtain(null, SAY_HELLO, 0, 0);
            try {
                Log.d(TAG, "sending message");
                mMessenger.send(msg);
                Log.d(TAG, "sent message");
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class HttpRequestTask extends AsyncTask<Void, Void, Apod> {

        Date mDate = Calendar.getInstance().getTime();

        public HttpRequestTask() {
            mDate = Calendar.getInstance().getTime();
        }

        public HttpRequestTask(int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            mDate = cal.getTime();
        }

        @Override
        protected Apod doInBackground(Void... params) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = formatter.format(mDate);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                String url = mApodUrl + "?api_key=" + mApiKey + "&date=" + dateString;
                Apod greeting = restTemplate.getForObject(url, Apod.class);
                return greeting;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Apod apod) {
            if (apod != null) {
                Log.d(TAG, apod.getTitle());
                TextView greetingIdText = (TextView) findViewById(R.id.id_title);
                greetingIdText.setText(apod.getTitle());
                if (apod.getCopyright() != null) {
                    TextView copyrightText = (TextView) findViewById(R.id.id_copyright);
                    copyrightText.setText("Copyright " + apod.getCopyright());
                }
                WebView webview = (WebView)findViewById(R.id.id_apod_image);
                webview.getSettings().setLoadWithOverviewMode(true);
                webview.getSettings().setUseWideViewPort(true);
                webview.loadUrl(apod.getUrl());
                getRemoteContacts();
            }
        }

    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int month, int day) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            //showDate(arg1, arg2+1, arg3);
            Log.d(TAG, "after datepicker selected");
            new HttpRequestTask(year, month, day).execute();
        }
    };
}
