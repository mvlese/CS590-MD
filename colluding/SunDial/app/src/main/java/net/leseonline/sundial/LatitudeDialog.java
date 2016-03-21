package net.leseonline.sundial;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.leseonline.R;

public class LatitudeDialog extends Dialog {

	public LatitudeDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
		setTitle("Set Latitude");
    
		Button button = (Button)findViewById(R.id.SetLatButtonSet);
		button.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
			
		});
		button = (Button)findViewById(R.id.SetLatButtonCancel);
		button.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				EditText et = (EditText)findViewById(R.id.EditText01);
				et.setText("0.0");
				cancel();
			}
			
		});
    }
    
    public Double getLatitude() {
    	Double d = 0.0;
		EditText et = (EditText)findViewById(R.id.EditText01);
		try {
			d = Double.parseDouble(et.getText().toString());
		} catch(Exception e) {
			
		}
    	return d;
    }
}
