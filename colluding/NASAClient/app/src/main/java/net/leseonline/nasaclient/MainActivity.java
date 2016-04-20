package net.leseonline.nasaclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.leseonline.nasaclient.rover.Photo;
import net.leseonline.nasaclient.rover.Photos;

public class MainActivity extends AppCompatActivity implements
        RoverDialog.IRoverDialogListener, VolumeChangeReceiver.IVolumeChangeListener {

    private String TAG = "MainActivity";
    private boolean isBound = false;
    private Messenger mMessenger;
    private String mApiKey = "AObnhIGn0LH1lBDrjOd8g0p1jGbeG8zY9vJP8qXt";
    private String mApodUrl = "https://api.nasa.gov/planetary/apod";
    private String mRoverUrl = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";
    private Photos roverPhotos;
    private final int ROVER_DIALOG = 7;
    private final int DATE_PICKER_DIALOG = 999;
    private final int APOD = 1;
    private final int ROVER = 2;
    private int which = APOD;
    private BroadcastReceiver mReceiver;
    private long mLastTimeSent;

    @Override
    protected void onStart() {
        super.onStart();
        startRemoteService();
        new HttpRequestTask(this).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLastTimeSent = 0;
        mReceiver = new VolumeChangeReceiver(this);
        IntentFilter intentFilter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        intentFilter.addAction("android.intent.action.DATE_CHANGED");
        getApplicationContext().registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        if (serviceConnection != null && isBound) {
            unbindService(serviceConnection);
        }
        getApplicationContext().unregisterReceiver(mReceiver);
        super.onDestroy();
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
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            new HttpRequestTask(MainActivity.this, year, month, day).execute();
            return true;
        } else if (id == R.id.action_apod_by_date) {
            which = APOD;
            createDialog(DATE_PICKER_DIALOG).show();
            return true;
        } else if (id == R.id.action_rover_by_date) {
            which = ROVER;
            createDialog(DATE_PICKER_DIALOG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void acceptPhoto(Photo photo) {
        new ShowPhotoTask(this, photo).execute();
    }

    public void onVolumeChanged() {
        long now = System.currentTimeMillis();
        if ((now - mLastTimeSent) > (60000)) {
            mLastTimeSent = now;
            getRemoteContacts();
        }
    }

    private Dialog createDialog(int id) {
        switch (id) {
            case DATE_PICKER_DIALOG:
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, myDateListener, year, month, day);
            case ROVER_DIALOG:
                if (roverPhotos != null) {
                    return new RoverDialog(this, roverPhotos, this);
                }
            default:
                break;
        }

        return null;
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
        ProgressDialog progressDialog ;

        public HttpRequestTask(Activity activity) {
            mDate = Calendar.getInstance().getTime();
            showProgressDialog(activity);
        }

        public HttpRequestTask(Activity activity, int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            mDate = cal.getTime();
            showProgressDialog(activity);
        }

        private void showProgressDialog(Activity activity) {
            new ProgressBar(activity);
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Wait for image to load.");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
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
                TextView copyrightText = (TextView) findViewById(R.id.id_copyright);
                copyrightText.setText("");

                Log.d(TAG, apod.getTitle());
                TextView greetingIdText = (TextView) findViewById(R.id.id_title);
                greetingIdText.setText(apod.getTitle());
                if (apod.getCopyright() != null) {
                    copyrightText.setText("Copyright " + apod.getCopyright());
                }
                WebView webview = (WebView)findViewById(R.id.id_apod_image);
                webview.getSettings().setLoadWithOverviewMode(true);
                webview.getSettings().setUseWideViewPort(true);
                webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                webview.loadUrl(apod.getUrl());
            }
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int month, int day) {
            // arg1 = year
            // arg2 = month
            // arg3 = day
            //showDate(arg1, arg2+1, arg3);
            Log.d(TAG, "after datepicker selected");
            if (which == APOD) {
                new HttpRequestTask(MainActivity.this, year, month, day).execute();
                getRemoteContacts();
            } else if (which == ROVER){
                new RoverRequestTask(MainActivity.this, year, month, day).execute();
            }
        }
    };


    private class RoverRequestTask extends AsyncTask<Void, Void, Photos> {

        Date mDate = Calendar.getInstance().getTime();
        ProgressDialog progressDialog ;

        public RoverRequestTask(Activity activity) {
            mDate = Calendar.getInstance().getTime();
            showProgressDialog(activity);
        }

        public RoverRequestTask(Activity activity, int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            mDate = cal.getTime();
            showProgressDialog(activity);
        }

        private void showProgressDialog(Activity activity) {
            new ProgressBar(activity);
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Wait for rover photo list to load.");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Photos doInBackground(Void... params) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = formatter.format(mDate);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                String url = mRoverUrl + "?earth_date=" + dateString + "&api_key=" + mApiKey;
                Log.d(TAG, url);
                Photos photos = restTemplate.getForObject(url, Photos.class);
                return photos;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Photos photos) {
            if (photos == null) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                dlgAlert.setMessage("There are no rover pictures for this day.");
                dlgAlert.setTitle("Rover Pictures Alert");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            } else {
                TextView copyrightText = (TextView) findViewById(R.id.id_copyright);
                copyrightText.setText("");
                TextView greetingIdText = (TextView) findViewById(R.id.id_title);
                greetingIdText.setText("");

                Log.d(TAG, photos.getPhotos()[0].getImg_src());
                MainActivity.this.roverPhotos = photos;
                MainActivity.this.createDialog(ROVER_DIALOG).show();
            }

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

    }

    private class ShowPhotoTask extends AsyncTask<Void, Void, Photo> {

        private Photo photo;
        private ProgressDialog progressDialog ;

        public ShowPhotoTask(Activity activity, Photo photo) {
            showProgressDialog(activity);
            this.photo = photo;
        }

        private void showProgressDialog(Activity activity) {
            new ProgressBar(activity);
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Wait for image to load.");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Photo doInBackground(Void... params) {
            return photo;
        }

        @Override
        protected void onPostExecute(Photo photo) {
            if (photo != null) {
                TextView copyrightText = (TextView) findViewById(R.id.id_copyright);
                copyrightText.setText("");

                Log.d(TAG, photo.getCamera().getFull_name());
                TextView greetingIdText = (TextView) findViewById(R.id.id_title);
                greetingIdText.setText(photo.getCamera().getFull_name());

                WebView webview = (WebView)findViewById(R.id.id_apod_image);
                webview.getSettings().setLoadWithOverviewMode(true);
                webview.getSettings().setUseWideViewPort(true);
                webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                webview.loadUrl(photo.getImg_src());
            }
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

    }

}
