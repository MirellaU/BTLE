package com.example.mirella.orthometr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForceActivity extends AppCompatActivity {

    public static final String TAG = "BluetoothForceActivity";

    public ArrayList<Float> force1_tab = new ArrayList<Float>();
    public ArrayList<Float> force2_tab = new ArrayList<Float>();

    public static String FORCE1_VALUE = "Force1Tab";
    public static String FORCE2_VALUE = "Force2Tab";

    IntentFilter force1IntentFilter;
    IntentFilter force2IntentFilter;

    @BindView(R.id.force1_chart)
    LineChart force1_chart;
    @BindView(R.id.force2_chart)
    LineChart force2_chart;

    private BroadcastReceiver force1Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(FORCE1_VALUE)) {
                force1_tab = (ArrayList<Float>) intent.getSerializableExtra("FORCE1_VALUE");
                addForce1Entry();
            }
        }
    };

    private BroadcastReceiver force2Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(FORCE2_VALUE)) {
                force2_tab = (ArrayList<Float>) intent.getSerializableExtra("FORCE2_VALUE");
                //Log.d(TAG, force2_tab.toString());
                addForce2Entry();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_force);
        ButterKnife.bind(this);

        force1IntentFilter = new IntentFilter("Force1Tab");
        force2IntentFilter = new IntentFilter("Force2Tab");

        force1_chart.setKeepPositionOnRotation(true); //keep the position after rotate the device
        force2_chart.setKeepPositionOnRotation(true);

        force1_chart.getDescription().setEnabled(true);
        force1_chart.getDescription().setText("");
        force2_chart.getDescription().setEnabled(true);
        force2_chart.getDescription().setText("");

        LineData data = new LineData();
        force1_chart.setData(data);
        LineData data2 = new LineData();
        force2_chart.setData(data2);

        YAxis force1_leftYAxis = force1_chart.getAxisLeft();
        force1_leftYAxis.setDrawGridLines(false); // no grid lines
        force1_leftYAxis.setDrawZeroLine(true);   //draw a zero line
        force1_leftYAxis.setAxisMinimum(-10f); // start at -10
        force1_leftYAxis.setAxisMaximum(1000f); // the axis maximum is 1000

        YAxis force_1rightYAxis = force1_chart.getAxisRight();
        force_1rightYAxis.setDrawGridLines(false); // no grid lines
        force_1rightYAxis.setDrawZeroLine(true);   //draw a zero line
        force_1rightYAxis.setAxisMinimum(-10f); // start at -10
        force_1rightYAxis.setAxisMaximum(1000f); // the axis maximum is 1000

        XAxis force1_xAxis = force1_chart.getXAxis();
        force1_xAxis.setDrawGridLines(false); //no grid lines
        force1_xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis force2_leftYAxis = force2_chart.getAxisLeft();
        force2_leftYAxis.setDrawGridLines(false); // no grid lines
        force2_leftYAxis.setDrawZeroLine(true);   //draw a zero line
        force2_leftYAxis.setAxisMinimum(-10f); // start at -10
        force2_leftYAxis.setAxisMaximum(1000f); // the axis maximum is 1000

        YAxis force2_rightYAxis = force2_chart.getAxisRight();
        force2_rightYAxis.setDrawGridLines(false); // no grid lines
        force2_rightYAxis.setDrawZeroLine(true);   //draw a zero line
        force2_rightYAxis.setAxisMinimum(-10f); // start at -180
        force2_rightYAxis.setAxisMaximum(1000f); // the axis maximum is 1000

        XAxis Force2xAxis = force2_chart.getXAxis();
        Force2xAxis.setDrawGridLines(false); //no grid lines
        Force2xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        registerReceiver(force1Receiver, force1IntentFilter);
        registerReceiver(force2Receiver, force2IntentFilter);
    }

    //add data to roll graph
    private void addForce1Entry() {
        LineData data = force1_chart.getData();

        if (data == null) {
            data = new LineData();
            force1_chart.setData(new LineData());
        }

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < force1_tab.size(); i++) {
            values.add(new Entry(i,(force1_tab.get(i))));
        }

        removeDataSet(force1_chart);

        LineDataSet set = new LineDataSet(values, "Siła zmierzona przez czujnik nacisku nr 2");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);

        set.setColor(Color.BLUE);
        set.setCircleColor(Color.BLUE);
        set.setHighLightColor(Color.BLUE);
        set.setValueTextSize(0f);
        set.setDrawCircleHole(true);
        set.setCircleHoleColor(Color.BLUE);
        //set.setValueTextColor(Color.BLUE);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        data.addDataSet(set);
        data.notifyDataChanged();
        force1_chart.notifyDataSetChanged();
        force1_chart.invalidate();
    }

    //add data to tilt graph
    private void addForce2Entry() {
        LineData data = force2_chart.getData();

        if (data == null) {
            data = new LineData();
            force2_chart.setData(new LineData());
        }

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < force2_tab.size(); i++) {
            values.add(new Entry(i,(force2_tab.get(i))));
        }

        removeDataSet(force2_chart);

        LineDataSet set = new LineDataSet(values, "Siła zmierzona przez czujnik nacisku nr 2");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);

        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setHighLightColor(Color.RED);
        set.setValueTextSize(0f);
        set.setDrawCircleHole(true);
        set.setCircleHoleColor(Color.RED);
        //set.setValueTextColor(Color.RED);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        data.addDataSet(set);
        data.notifyDataChanged();
        force2_chart.notifyDataSetChanged();
        force2_chart.invalidate();
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
    public void onPause () {
        super.onPause();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        unregisterReceiver(force1Receiver);
        unregisterReceiver(force2Receiver);
    }
}
