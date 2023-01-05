package com.example.myapplication;

import static android.util.Log.*;

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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListOfDevices extends AppCompatActivity {
    private ListView paierdDevicesList, availableDevicesList;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Context context;
    private ProgressBar scanDevicesBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_devices);
        initBluetooth();
        init();
    }

    private void init() {

        // initialization the variables
        paierdDevicesList = (ListView) findViewById(R.id.paierdDevices);
        availableDevicesList = (ListView) findViewById(R.id.availableDevices);
        scanDevicesBar = findViewById(R.id.progresBar);

        //=============================================
        scanDevicesBar.setVisibility(View.VISIBLE);
        //=============================================

        // initialization the Adapters
        adapterPairedDevices = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, getPairedDevices());
        adapterAvailableDevices = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1);

        // UnParied devices0
        getAvailableDevices();

        // set adapters for the ListViews
        paierdDevicesList.setAdapter(adapterPairedDevices);
        availableDevicesList.setAdapter(adapterAvailableDevices);



    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please Enable Bluetooth", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void makeDeviceDiscoverAble() {
        int requestCode = 1;
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, requestCode);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
            }
            else { Toast.makeText(context, "No Devices Found", Toast.LENGTH_SHORT).show(); }
        }
    };

    private ArrayList<String> getPairedDevices() {
        ArrayList<String> PairedDevices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                PairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }
        return PairedDevices;
    }

    private void getAvailableDevices() {
        makeDeviceDiscoverAble();
        bluetoothAdapter.startDiscovery();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intentFilter);
    }
}
