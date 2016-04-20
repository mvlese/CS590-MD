package net.leseonline.nasaclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by mlese on 4/20/2016.
 */
public class VolumeChangeReceiver extends BroadcastReceiver {
    private final String TAG = "VolumeChangeReceiver";
    private IVolumeChangeListener mListener;

    public interface IVolumeChangeListener {
        void onVolumeChanged();
    }

    public VolumeChangeReceiver(IVolumeChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "in onReceive");
        Log.d(TAG, context.toString());
        mListener.onVolumeChanged();
    }
}
