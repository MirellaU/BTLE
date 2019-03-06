package com.example.mirella.orthometr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class Force1Service extends IntentService {
    public static final String TAG = "BluetoothForce1Service";

    public Force1Service() {
        super("Force1Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList<Float> force1_tab = (ArrayList<Float>) intent.getSerializableExtra("FORCE1_VALUE");
        //Log.d(TAG, force1_tab.toString());
        Intent force1Intent = new Intent("Force1Tab");
        force1Intent.putExtra("FORCE1_VALUE", force1_tab);
        sendBroadcast(force1Intent);
    }
}
