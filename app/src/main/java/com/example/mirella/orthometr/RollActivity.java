package com.example.mirella.orthometr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RollActivity extends AppCompatActivity {

    public static final String TAG = "BluetoothRollActivity";

    public static String ROLL_VALUE = "RollTab";
    public static String TILT_VALUE = "TiltTab";

    public ArrayList<Float> roll_tab = new ArrayList<Float>();
    public ArrayList<Float> tilt_tab = new ArrayList<Float>();

    IntentFilter rollIntentFilter;
    IntentFilter tiltIntentFilter;

    @BindView(R.id.roll_chart)
    LineChart roll_chart;
    @BindView(R.id.tilt_chart)
    LineChart tilt_chart;

    private BroadcastReceiver rollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ROLL_VALUE)) {
                roll_tab = (ArrayList<Float>) intent.getSerializableExtra("ROLL_VALUE");
                //Log.d(TAG, roll_tab.toString());
                //feedMultiple();
                addRollEntry();
            }
        }
    };

    private BroadcastReceiver tiltReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TILT_VALUE)) {
                tilt_tab = (ArrayList<Float>) intent.getSerializableExtra("TILT_VALUE");
                //Log.d(TAG, tilt_tab.toString());
                //feedMultiple();
                addTiltEntry();
            }
        }
    };

    //add data to roll graph
    private void addRollEntry() {
        LineData data = roll_chart.getData();

        if (data == null) {
            data = new LineData();
            roll_chart.setData(new LineData());
        }

            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < roll_tab.size(); i++) {
                values.add(new Entry(i,(roll_tab.get(i))));
            }

            removeDataSet(roll_chart);

            LineDataSet set = new LineDataSet(values, "Kąt główny lewo-prawo");
            set.setLineWidth(2.5f);
            set.setCircleRadius(4.5f);

            set.setColor(Color.BLUE);
            set.setCircleColor(Color.BLUE);
            set.setHighLightColor(Color.BLUE);
            set.setValueTextSize(0f);
            set.setDrawCircleHole(true);
            set.setCircleHoleColor(Color.BLUE);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        set.setValueTextColor(Color.RED);

            data.addDataSet(set);
            data.notifyDataChanged();
            roll_chart.notifyDataSetChanged();
            roll_chart.invalidate();
    }

    //add data to tilt graph
    private void addTiltEntry() {
        LineData data = tilt_chart.getData();

        if (data == null) {
            data = new LineData();
            tilt_chart.setData(new LineData());
        }

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < tilt_tab.size(); i++) {
            values.add(new Entry(i,(tilt_tab.get(i))));
        }

        removeDataSet(tilt_chart);

        LineDataSet set = new LineDataSet(values, "Kąt pomocniczy przód-tył");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);

        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setDrawCircleHole(true);
        set.setCircleHoleColor(Color.RED);
        set.setHighLightColor(Color.RED);
        set.setValueTextSize(0f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //set.setDrawCubic(false);
//        set.setValueTextColor(Color.RED);

        data.addDataSet(set);
        data.notifyDataChanged();
        tilt_chart.notifyDataSetChanged();
        tilt_chart.invalidate();
    }

    private void removeDataSet(LineChart chart) {
        LineData data = chart.getData();
        if (data != null) {
            data.removeDataSet(data.getDataSetByIndex(data.getDataSetCount() - 1));
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll);
        ButterKnife.bind(this);

        rollIntentFilter = new IntentFilter("RollTab");
        tiltIntentFilter = new IntentFilter("TiltTab");

        roll_chart.setKeepPositionOnRotation(true);
        tilt_chart.setKeepPositionOnRotation(true);

        roll_chart.getDescription().setEnabled(true);
        roll_chart.getDescription().setText("");
        tilt_chart.getDescription().setEnabled(true);
        tilt_chart.getDescription().setText("");

        LineData data = new LineData();
        roll_chart.setData(data);
        LineData data2 = new LineData();
        tilt_chart.setData(data2);

        YAxis leftAxis = roll_chart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // no grid lines
        leftAxis.setDrawZeroLine(true);   //draw a zero line
        leftAxis.setAxisMinimum(-180f); // start at -180
        leftAxis.setAxisMaximum(180f); // the axis maximum is 180

        YAxis rightAxis = roll_chart.getAxisRight();
        rightAxis.setDrawGridLines(false); // no grid lines
        rightAxis.setDrawZeroLine(true);   //draw a zero line
        rightAxis.setAxisMinimum(-180f); // start at -180
        rightAxis.setAxisMaximum(180f); // the axis maximum is 180

        XAxis xAxis = roll_chart.getXAxis();
        xAxis.setDrawGridLines(false); //no grid lines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x axis on the bottom of chart

        YAxis leftTiltAxis = tilt_chart.getAxisLeft();
        leftTiltAxis.setDrawGridLines(false); // no grid lines
        leftTiltAxis.setDrawZeroLine(true);   //draw a zero line
        leftTiltAxis.setAxisMinimum(-180f); // start at -180
        leftTiltAxis.setAxisMaximum(180f); // the axis maximum is 180

        YAxis rightTiltAxis = tilt_chart.getAxisRight();
        rightTiltAxis.setDrawGridLines(false); // no grid lines
        rightTiltAxis.setDrawZeroLine(true);   //draw a zero line
        rightTiltAxis.setAxisMinimum(-180f); // start at -180
        rightTiltAxis.setAxisMaximum(180f); // the axis maximum is 180

        XAxis xTiltAxis = tilt_chart.getXAxis();
        xTiltAxis.setDrawGridLines(false); //no grid lines
        xTiltAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        registerReceiver(rollReceiver, rollIntentFilter);
        registerReceiver(tiltReceiver,tiltIntentFilter);
        }

        @Override
        public void onPause () {
            super.onPause();
        }

        @Override
        public void onDestroy () {
            super.onDestroy();
            unregisterReceiver(rollReceiver);
            unregisterReceiver(tiltReceiver);
        }

//    private void feedMultiple() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            addRollEntry();
//                            addTiltEntry();
//                        }
//                    });
//                    try {
//                        Thread.sleep(35);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//            }
//        }).start();
//    }
}

