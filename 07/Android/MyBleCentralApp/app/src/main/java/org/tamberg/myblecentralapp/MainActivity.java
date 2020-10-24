package org.tamberg.myblecentralapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD_MS = 10000;
    // Bluetooth SIG registered 16-bit "UUIDs" all have the base UUID 0000xxxx-0000-1000-8000-00805f9b34fb
    private static final UUID HRM_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");

    private BluetoothLeScanner scanner;
    private Handler handler = new Handler(Looper.getMainLooper());

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "onScanResult, result = " + result.getDevice().getAddress());

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed, errorCode = " + errorCode);
        }
    };

    private boolean isLocationEnabled() {
        // Based on https://stackoverflow.com/questions/10311834
        boolean enabled;
        int locationMode = 0;
        try { // 19+
            locationMode = Settings.Secure.getInt(
                this.getContentResolver(), Settings.Secure.LOCATION_MODE);
            enabled = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } catch (Settings.SettingNotFoundException e) {
            enabled = false;
        }
        return enabled;
    }

    private void scan() {
        List<ScanFilter> filters = new ArrayList<>();
        //filters.add(new ScanFilter.Builder().setDeviceAddress("C9:1E:3F:18:61:9D").build());
        filters.add(new ScanFilter.Builder().setServiceUuid(
            new ParcelUuid(HRM_SERVICE_UUID)).build()); // 21+
        ScanSettings settings = (new ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY)).build();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "stop scan");
                scanner.stopScan(scanCallback);
            }
        }, SCAN_PERIOD_MS);
        Log.d(TAG, "start scan");
        scanner.startScan(filters, settings, scanCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        boolean hasBle = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        // Or <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
        if (hasBle) {
            Log.d(TAG, "BLE available");
            BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "BLE enabled");
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
                };
                int requestCode = 0;
                ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
            } else {
                Log.d(TAG, "BLE not enabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            Log.d(TAG, "BLE not available");
        }
    }

    @Override
    public void onRequestPermissionsResult (
        int requestCode, String[] permissions, int[] grantResults)
    {
        if (isLocationEnabled()) {
            scan();
        } else {
            Log.d(TAG, "Location not enabled");
        }
    }
}