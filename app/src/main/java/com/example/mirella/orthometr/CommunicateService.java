package com.example.mirella.orthometr;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class CommunicateService extends IntentService {

    public static final String TAG = "BluetoothCommunicate";

    public CommunicateService() {
        super("CommunicateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList<Float> roll_tab = (ArrayList<Float>) intent.getSerializableExtra("ROLL_VALUE");
        //Log.d(TAG,roll_tab.toString());
        Intent rollIntent = new Intent("RollTab");
        rollIntent.putExtra("ROLL_VALUE", roll_tab);
        sendBroadcast(rollIntent);
    }
}

