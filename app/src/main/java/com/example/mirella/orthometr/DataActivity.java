package com.example.mirella.orthometr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DataActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothDataActivity";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic, necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mGattCharacteristicsTx;
    private BluetoothGattCharacteristic mGattCharacteristicsRx;

    // receiving data properties
    public int header;            //Naglowek z numerem 0xAA01
    public int status;              //Status urzadzenia:tryb pracy, przyciski, podlaczony czujnik nacisku
    public int signal;            //[dB]Sila syganlu Bluetooth
    public float battery;           //[V]Stan naladowania akumulatora
    public float shake;             //[g]Przyspieszenie działające na urzadzenie
    public float roll;              //[°]Kat glowny lewo-prawo
    public float roll_offset;       //[°]Wartosc ofssetu kat glownego lewo-prawo
    public float tilt;              //[°]Kat pomocniczy przod-tyl
    public int way;               //[mm]Przebyta droga zmierzona przez rolke drogi
    public int space;             //[mm]Rozsuniecie nog urzadzenia
    public float force1;            //[N]Sila zmieorzona przez czujnik nacisku nr1
    public float force2;            //[N]Sila zmieorzona przez czujnik nacisku nr2
    public int counter;           //Licznik wyslanych ramek
    public int crc;               //Suma kontrolna CRC

    public ArrayList <Float> roll_tab=new ArrayList<Float>();
    public ArrayList <Float> tilt_tab=new ArrayList<Float>();
    public ArrayList <Float> force1_tab=new ArrayList<Float>();
    public ArrayList <Float> force2_tab=new ArrayList<Float>();

    Intent communicateIntent;
    Intent tiltIntent;
    Intent force1Intent;
    Intent force2Intent;

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.link_status)
    TextView link_status;
    @BindView(R.id.roll_btn)
    Button roll_btn;
    @BindView(R.id.force_btn)
    Button force_btn;
    @BindView(R.id.way_btn)
    Button way_btn;
    @BindView(R.id.offset_btn)
    Button offset_btn;
    @BindView(R.id.battery_btn)
    Button battery_btn;
    @BindView(R.id.signal_btn)
    Button signal_btn;


    @OnClick(R.id.roll_btn)
    public void DrawRollFunction() {
        Intent intent = new Intent(this,RollActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.force_btn)
    public void DrawForceFunction() {
        Intent intent = new Intent(this,ForceActivity.class);
        startActivity(intent);
    }

    // Main BTLE device callback
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG,"State connected");
                // Discover services.
                if (!gatt.discoverServices()) {
                    Log.d(TAG,"Failed to start discovering services");
                    displayData("Failed to start discovering services");
                    mBluetoothAdapter.startLeScan(scanCallback);
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG,"State disconnected");
                displayData("State disconnected.");
                mBluetoothAdapter.startLeScan(scanCallback);
            } else {
                Log.d(TAG,"Connection state changed.  New state: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG,"Service discovery completed ");
            } else {
                Log.d(TAG,"Service discovery failed with status: " + status);
                displayData("Service discovery failed with status: " + status);
                mBluetoothAdapter.startLeScan(scanCallback);
            }
            // Save reference to each characteristic.
            mGattCharacteristicsTx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            mGattCharacteristicsRx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            if (!gatt.setCharacteristicNotification(mGattCharacteristicsRx, true)) {
                Log.d(TAG,"Couldn't set notifications for RX characteristic");
                displayData("Couldn't set notifications for RX characteristic");
                mBluetoothAdapter.startLeScan(scanCallback);
            }
            if (mGattCharacteristicsRx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = mGattCharacteristicsRx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    Log.d(TAG,"Disconnected");
                    displayData("Disconnected.");
                    mBluetoothAdapter.startLeScan(scanCallback);
                }
            } else {
                Log.d(TAG,"Couldn't get RX client descriptor");
                displayData("Couldn't get RX client descriptor.");
                mBluetoothAdapter.startLeScan(scanCallback);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //if (status == BluetoothGatt.GATT_SUCCESS && RX_UUID.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                if (data != null) {
                    final String dataString = FrameDecoder(data);
                    SendArray("ROLL_VALUE", roll_tab, communicateIntent);
                    SendArray("TILT_VALUE", tilt_tab, tiltIntent);
                    SendArray("FORCE1_VALUE", force1_tab, force1Intent);
                    SendArray("FORCE2_VALUE", force2_tab, force2Intent);
                    roll_tab = FillTab(roll, roll_tab);
                    tilt_tab = FillTab(tilt, tilt_tab);
                    force1_tab = FillTab(force1, force1_tab);
                    force2_tab = FillTab(force2, force2_tab);
                    Write((String.format("Kąt główny lewo-prawo: %.2f [°]", roll) + String.format("\n" + "Kąt pomocniczy przód-tył: %.2f [°]", tilt)), roll_btn);
                    Write((String.format("Siła zmierzona przez czujnik nacisku nr 1: %.2f N", force1) + String.format("\n" + "Siła zmierzona przez czujnik nacisku nr 2: %.2f N", force2)), force_btn);
                    Write((String.format("Wartość offsetu kąta lewo-prawo: %.2f [°]", roll_offset)), offset_btn);
                    Write("Droga zmierzona przez rolkę drogi: " + way + " mm", way_btn);
                    Write((String.format("Stan naładowania akumulatora: %.2f V", battery)), battery_btn);
                    Write("Siła sygnału Bluetooth: " + signal + " dB", signal_btn);
                    //Log.d(TAG,dataString);
                } else {
                    displayData("Device does not send data");
                }
            //}
        }
    };

    public String FrameDecoder (byte [] data) {
        if (data[0] == -86 && data[1] == 1 && data.length == 44) {
            header = (int) (((data[0]) + (data[1])));
            status = (int) ((data[2]) + ((data[3] << 8)) + ((data[4] << 16)) + ((data[5] << 24)));
            signal = (int) (((data[6])) + ((data[7])));
            int batteryInt = ((data[8] & 0xFF) | ((data[9] & 0xFF) << 8) | ((data[10] & 0xFF) << 16) | ((data[11] & 0xFF) << 24));
            battery = Float.intBitsToFloat(batteryInt);
            int shakeInt = ((data[12] & 0xFF) | ((data[13] & 0xFF) << 8) | ((data[14] & 0xFF) << 16) | ((data[15] & 0xFF) << 24));
            shake = Float.intBitsToFloat(shakeInt);
            int rollInt = (data[16] & 0xFF) | ((data[17] & 0xFF) << 8) | ((data[18] & 0xFF) << 16) | ((data[19] & 0xFF) << 24);
            roll = Float.intBitsToFloat(rollInt);
            roll_offset = Float.intBitsToFloat(data[20] + (data[21] << 8) + (data[22] << 16) + (data[23] << 24));
            tilt = Float.intBitsToFloat(data[24] + (data[25] << 8) + (data[26] << 16) + (data[27] << 24));
            way = (int) ((data[28] & 0xff) + ((data[29] << 8) & 0xff));
            space = (int) ((data[30] & 0xff) + (data[31] << 8) & 0xff);
            force1 = Float.intBitsToFloat(data[32] + (data[33] << 8) + (data[34] << 16) + (data[35] << 24));
            force2 = Float.intBitsToFloat(data[36] + (data[37] << 8) + (data[38] << 16) + (data[39] << 24));
            counter = (int) ((data[40] & 0xff) + ((data[41] << 8) & 0xff));
            crc = (int) ((data[42] & 0xff) + ((data[43] << 8) & 0xff));
            String dataString = (header + "\n" + "Status urządzenia: " + status + "\n" + "Siła sygnału Bluetooth: " + signal + "\n" + "Stan naładowania akumulatora: " + battery + "\n"
                    + "Przyspieszenie działające na urządzenie: " + shake + "\n" + "Kąt główny lewo-prawo: " + roll + "\n" + "Wartość offsetu kąta głównego lewo-prawo: " + roll_offset + "\n"
                    + "Kąt pomocniczy przód-tył: " + tilt + "\n" + "Przebyta droga zmierzona przez rolkę drogi: " + way + "\n" + "Rozsunięcie nóg urządzenia: " + space + "\n"
                    + "Siła zmierzona przez czujnik nacisku nr 1: " + force1 + "\n" + "Siła zmierzona przez czujnik nacisku nr 2: " + force2 + "\n"
                    + "Licznik wyslanych ramek: " + counter + "\n" + "Suma kontrolna CRC: " + crc);
            Log.d(TAG, dataString);
            return dataString;
        } else {
            displayData("Wrong data frame");
            return null;
        }
    }

    private void SendArray (String name, ArrayList<Float> tab, Intent intent ) {
        if(tab.size()==15) {
            intent.putExtra(name, tab);
            //Log.d(TAG, tab.toString());
            startService(intent);
        } else { }
    }

    private ArrayList<Float> FillTab(float value,ArrayList<Float> tab){
        if(tab.size()<15) {
            tab.add(value);
        }  else { tab.clear(); }
        return  tab;
    }

    private void displayData(String data) {
        if (data != null) {
            link_status.setText(data);
        }
    }

    private void Write(final String text, Button button) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setText(text);
            }
        });
    }

    // BTLE device scanning callback.
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            mBluetoothAdapter.stopLeScan(scanCallback);
            if (mBluetoothAdapter == null || mDeviceAddress == null) {
                Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
                displayData("BluetoothAdapter not initialized or unspecified address.");
            }
            bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
            if (bluetoothDevice == null) {
                Log.d(TAG, "Device not found.  Unable to connect.");
                displayData("Device not found.  Unable to connect.");
            }
            mBluetoothGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
            Log.d(TAG,"Connected with " + mDeviceAddress);
            displayData("Connected."); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ButterKnife.bind(this);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        name.setText(mDeviceName);
        address.setText(mDeviceAddress);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        communicateIntent = new Intent(this,CommunicateService.class);
        tiltIntent=new Intent(this,TiltService.class);
        force1Intent = new Intent(this,Force1Service.class);
        force2Intent = new Intent(this,Force2Service.class);
    }

    // OnResume, start the BTLE connection.
    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothAdapter.startLeScan(scanCallback);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(mBluetoothGatt!=null) {
            try {
                Log.d(TAG,"Closing the connection");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                mGattCharacteristicsTx = null;
                mGattCharacteristicsRx = null;
            } catch (Exception e) {
                mBluetoothGatt=null; }
        } else {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothGatt!=null) {
            try {
                Log.d(TAG,"Closing the connection");
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                mGattCharacteristicsTx = null;
                mGattCharacteristicsRx = null;
            } catch (Exception e) {
                mBluetoothGatt=null; }
        } else {}
    }
}
