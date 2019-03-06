package com.example.mirella.orthometr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class Force2Service extends IntentService {
    public static final String TAG = "BluetoothForce2Service";

    public Force2Service() {
        super("Force2Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList<Float> force2_tab = (ArrayList<Float>) intent.getSerializableExtra("FORCE2_VALUE");
        //Log.d(TAG, force2_tab.toString());
        Intent force2Intent = new Intent("Force2Tab");
        force2Intent.putExtra("FORCE2_VALUE", force2_tab);
        sendBroadcast(force2Intent);
    }
}
