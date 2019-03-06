package com.example.mirella.orthometr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class OrthometrActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "BluetoothActivity";
    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> new_devicesList = new ArrayList<>();
    private DeviceAdapter new_Device_Adapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothDevice mBTDevice;

    @BindView(R.id.devicesView)
    ListView devicesView;
    @BindView(R.id.bluetooth_on_btn)
    Button bluetooth_on_btn;
    @BindView(R.id.bluetooth_off_btn)
    Button bluetooth_off_btn;
    @BindView(R.id.discover_devices_btn)
    Button discover_devices_btn;

    @OnClick(R.id.bluetooth_on_btn)
    public void TurnBluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device has no bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                startActivity(discoverableIntent);
                startActivityForResult(bluetoothIntent, REQUEST_ENABLE_BT);
            } else {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                Toast.makeText(getApplicationContext(), "Bluetooth is on", Toast.LENGTH_SHORT).show();
                startActivity(discoverableIntent);
            }
        }
    }

    @OnClick(R.id.bluetooth_off_btn)
    public void TurnBluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already off", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.discover_devices_btn)
    public void NewDevices() {
        new_devicesList.clear();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startDiscovery();
            registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            new_Device_Adapter = new DeviceAdapter(this, R.layout.device_item, new_devicesList);
            devicesView.setAdapter(new_Device_Adapter);
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orthometr);
        ButterKnife.bind(this);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        devicesView.setOnItemClickListener(OrthometrActivity.this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();
        mBTDevice = new_devicesList.get(i);
        Log.d(TAG, "onItemClick: deviceName = " +  mBTDevice.getName());
        Log.d(TAG, "onItemClick: deviceAddress = " + mBTDevice.getAddress());
        //SendFrame(550151111021);
        final Intent intent = new Intent(this, DataActivity.class);
        intent.putExtra(DataActivity.EXTRAS_DEVICE_NAME, mBTDevice.getName());
        intent.putExtra(DataActivity.EXTRAS_DEVICE_ADDRESS, mBTDevice.getAddress());
        startActivity(intent);
        new_devicesList.clear();
        new_Device_Adapter.notifyDataSetChanged();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                new_devicesList.add(device);
                new_Device_Adapter.notifyDataSetChanged();
            }
            else {
                Log.d(TAG,"No device found");
                Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_SHORT).show();
            }
        }
    };

}