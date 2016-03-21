package net.leseonline.sundial;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import android.content.Context;
import android.location.Location;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import net.leseonline.R;

public class SunDialView extends View {
	
	private BitmapDrawable mSunny;
	private ShapeDrawable mDrawable;
    private static double _lat = 41.0; // degrees altitude
    private static double _dec = 36.0; // degrees sun declination
    private static double _L = 1.0;  // hypoteneuse
    private float[] mValues;
	private static String[] hours = {"VI", "VII", "VIII", "IX", "X", "XI", "XII", "I", "II", "III", "IV", "V", "VI"};
	
	public SunDialView(Context context) {
		super(context);
		
		mDrawable = new ShapeDrawable(new OvalShape());
		mDrawable.getPaint().setColor(0xFFc0c080);
		mSunny = (BitmapDrawable)getResources().getDrawable(R.drawable.sun);
	}

	protected void onDraw(Canvas canvas) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int cx = w / 2;
        int cy = h / 2;

        canvas.translate(cx, cy);
        if (mValues != null) {            
            canvas.rotate(-mValues[0]);
        }
        
        if (w > h) {
        	int temp = w;
        	w = h;
        	h = temp;
        }
        
		mDrawable.setBounds(-w/2, -w/2, w/2, w/2);
		mDrawable.draw(canvas);
		doIt(canvas);

		int sh = mSunny.getMinimumHeight();
		int sw = mSunny.getMinimumWidth();
		
		int left = -(sw / 2);
		int top = (w / 4) - (sh / 2);
		int right = left + sw;
		int bottom = top + sh;
		mSunny.setBounds(left, top, right, bottom);
		mSunny.draw(canvas);
	}
	
	public void setLatitude(double latitude) {
		_lat = latitude;
		this.invalidate();
	}
	
    private void doIt(Canvas canvas)
    {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w > h) {
        	int temp = w;
        	w = h;
        	h = temp;
        }
        
        PointF origin = new PointF(0.0f, 0.0f);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2.0f);
        
        Paint shadowPaint = new Paint();
        shadowPaint.setColor((Color.GRAY & 0x00ffffff) | 0xa0000000);
        shadowPaint.setStyle(Paint.Style.FILL);

        if (_lat != 0.0)
        {
            int n = -6;
            for (n = -6; n <= 6; n++)
            {
                double t = n * 15.0;
                switch (n)
                {
                    case -6:
                        t = -89.9999;
                        break;
                    case 6:
                        t = 89.9999;
                        break;
                    default:
                        t = n * 15.0;
                        break;
                }
                double beta = Math.atan(Math.sin(toRadian(_lat)) * Math.tan(toRadian(t)));
                double x = Math.sin(beta) * (w / 2.0 - 15.0);
                double y = Math.cos(beta) * (w / 2.0 - 15.0);
                
                float _x = (float)(x + origin.x);
                float _y = (float)(origin.y - y);
                PointF to = new PointF(_x, _y);
                canvas.drawLine(origin.x, origin.y, to.x, to.y, paint);
                
                x = Math.sin(beta) * (w / 2.0 - 12);
                y = Math.cos(beta) * (w / 2.0 - 12);
                _x = (float)(x + origin.x);
                _y = (float)(origin.y - y);

                //Integer _n = new Integer(n);
                //String sN = _n.toString();
                float widths[] = new float[5];
                paint.getTextWidths(hours[n + 6], widths);
                float sum = 0.0f;
                for (int m = 0; m < widths.length; m++) {
                	sum += widths[m];
                }
                x = (_x <= 0) ? _x - (sum / 2.0f) : _x - (sum / 2.0f);
                canvas.rotate((float)toDegrees(beta), _x, _y);
                canvas.drawText(hours[n + 6], (float)x, _y, paint);
                canvas.rotate((float)toDegrees(-beta), _x, _y);
            }
            
            java.util.Date date = new java.util.Date();
            int hour = date.getHours();
            hour -= 12;
            if (hour >= -6 && hour < 6) {
	        	double t = this.getRadiansFromNoon();
	            double beta = Math.atan(Math.sin(toRadian(_lat)) * Math.tan(t));
	            double x = Math.sin(beta) * (w / 2.0);
	            double y = Math.cos(beta) * (w / 2.0);
                float _x = (float)(x + origin.x);
                float _y = (float)(origin.y - y);
	            
	            // Draw the shadow.
	        	Path path = new Path();
	        	path.moveTo(0.0f, 0.0f);
	        	path.lineTo(0.0f, -(w / 2 - 15));
	        	path.lineTo(_x, _y);
	        	path.close();
	        	canvas.drawPath(path, shadowPaint);
            }
        }
    }

    private double toRadian(double angle)
    {
        return (angle * Math.PI / 180.0);
    }

    private double toDegrees(double radian)
    {
        return (radian * 180.0 / Math.PI);
    }

    public void setSensorValues(float[] values) {
    	mValues = values;
    	this.invalidate();
    }
    
	private float getRadiansFromNoon() {
		float t = getDegreesFromNoon();
		t = (float)(toRadian(t));
		return t;
	}
	
	private static float DEGREES_PER_SECOND = 1.0f / 240.0f;
	private float getDegreesFromNoon() {
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar ref = new GregorianCalendar();
		now.setTime(new Date());
		ref.setTime(now.getTime());
		ref.set(Calendar.HOUR_OF_DAY, 12);
		ref.set(Calendar.MINUTE, 0);
		ref.set(Calendar.SECOND, 0);
		ref.set(Calendar.MILLISECOND, 0);
		long diffSeconds = (now.getTimeInMillis() - ref.getTimeInMillis()) / 1000L;
		float t = diffSeconds * DEGREES_PER_SECOND;
		return t;
	}
    
}
