package net.leseonline.bbstat;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import net.leseonline.bbstat.contact.Contact;
import net.leseonline.bbstat.contact.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvlese on 2/20/2016.
 */
public class ContactsReader {

    private Context context;

    public ContactsReader(Context context) {
        this.context = context;
    }

    public ContactList readContacts() {
        ContactList contacts = new ContactList();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String email = getEmail(cr, id);
                String phoneNo = getPhoneNumber(cr, id);

                Contact c = new Contact();
                c.setPhoneNumber(phoneNo);
                c.setName(name);
                c.setEmail(email);
                contacts.add(c);
            }
        }
        cur.close();

        return contacts;
    }

    private String getPhoneNumber(ContentResolver cr, String id) {
        String phoneNo = "";
        try {
            Cursor cur1 = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            if (cur1.moveToNext()) {
                //to get the contact names
                //name = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phoneNo = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            cur1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return phoneNo;
    }

    private String getEmail(ContentResolver cr, String id) {
        String email = "";

        try {
            Cursor cur1 = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            if (cur1.moveToNext()) {
                //to get the contact names
                //name = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            cur1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return email;
    }

}
