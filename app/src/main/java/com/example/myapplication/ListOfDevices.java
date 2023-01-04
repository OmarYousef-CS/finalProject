package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListOfDevices extends AppCompatActivity {
    private ListView paierdDevicesList, availableDevicesList;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_devices);
        init();
    }

    private void init() {
        paierdDevicesList = (ListView) findViewById(R.id.paierdDevices);
        availableDevicesList = findViewById(R.id.availableDevices);

        adapterAvailableDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }
        if (!(bluetoothAdapter.isEnabled())) {
            Intent askTurnOnBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(askTurnOnBluetooth, 1);
           Toast.makeText(this, "Bluetooth Enable now", Toast.LENGTH_SHORT).show();
       }

        ArrayList<String> list = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                list.add(device.getName() + "\n" + device.getAddress());
            }
        }

        int requestCode = 1;
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300000);
        startActivityForResult(discoverableIntent, requestCode);

        adapterPairedDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        paierdDevicesList.setAdapter(adapterPairedDevices);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener, intentFilter1);


    }

    private BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (adapterAvailableDevices.getCount() == 0) {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "we found new devices", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void scanDevices() {
        adapterAvailableDevices.clear();
        Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothDeviceListener != null) {
            unregisterReceiver(bluetoothDeviceListener);
        }
    }
}