package net.leseonline.nasaclient;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import net.leseonline.nasaclient.rover.Photo;
import net.leseonline.nasaclient.rover.Photos;

import java.util.ArrayList;

/**
 * Created by mlese on 4/20/2016.
 */
public class RoverDialog extends Dialog {

    public interface IRoverDialogListener {
        void acceptPhoto(Photo photo);
    }

    private Photo[] photos;
    private IRoverDialogListener listener;

    public RoverDialog(Context context, Photos photos, IRoverDialogListener listener) {
        super(context);
        this.photos = photos.getPhotos();
        this.listener = listener;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rover_dialog);
        setTitle("Select Rover Picture");

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final ArrayList<String> list = new ArrayList<String>();
        for (int n = 0; n < photos.length; n++) {
            String id = String.valueOf(photos[n].getId());
            list.add(id);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


        Button button = (Button)findViewById(R.id.RoverCancel);
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                dismiss();
            }

        });
        button = (Button)findViewById(R.id.RoverOk);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                int index = spinner.getSelectedItemPosition();
               listener.acceptPhoto(photos[index]);
                cancel();
            }

        });
    }
}
