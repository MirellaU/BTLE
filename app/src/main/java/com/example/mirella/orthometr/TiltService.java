package com.example.mirella.orthometr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class TiltService extends IntentService {

    public static final String TAG = "BluetoothTilt";

    public TiltService() {
        super("TiltService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList<Float> tilt_tab = (ArrayList<Float>) intent.getSerializableExtra("TILT_VALUE");
        //Log.d(TAG, tilt_tab.toString());
        Intent tiltIntent = new Intent("TiltTab");
        tiltIntent.putExtra("TILT_VALUE", tilt_tab);
        sendBroadcast(tiltIntent);
    }
}
