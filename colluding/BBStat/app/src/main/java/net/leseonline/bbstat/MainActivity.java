package net.leseonline.bbstat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Main Activity";

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                testGetContacts();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void testGetContacts() {

        try {
            checkForContactsPermissions();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private void readContacts() {
//        try {
//            ContentResolver cr = getContentResolver();
//            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
//                    null, null, null, null);
//            if (cur.getCount() > 0) {
//                while (cur.moveToNext()) {
//                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
//                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
//                    if (Integer.parseInt(cur.getString(
//                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                        Cursor pCur = cr.query(
//                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                                null,
//                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                                new String[]{id}, null);
//                        while (pCur.moveToNext()) {
//                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        }
//                        pCur.close();
//                    }
//                }
//            }
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};
    private static final int REQUEST_CONTACTS = 1;

    private void checkForContactsPermissions() {
        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            //given that Manifest.permission.READ_CONTACTS and Manifest.permission.WRITE_CONTACTS
            //are from the same permission group , on theory it is enough to check for only one of them
            //yet permission groups are subject to change therefore it is s good idea to check
            //for both permissions

            // Contacts permissions have not been granted.
            Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.");
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        } else {
            // Contact permissions have been granted. Show the contacts fragment.
            Log.i(TAG,
                    "Contact permissions have already been granted. Displaying contact details.");
            ContactsReader reader = new ContactsReader(this);
            List<Contact> contacts = reader.readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ContactsReader reader = new ContactsReader(this);
                    List<Contact> contacts = reader.readContacts();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            default:
                break;
        }
    }
}

