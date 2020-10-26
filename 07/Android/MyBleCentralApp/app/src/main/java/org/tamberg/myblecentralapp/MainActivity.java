// https://tamberg.mit-license.org/

package org.tamberg.myblecentralapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.tamberg.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD_MS = 10000;
    // Bluetooth SIG registered 16-bit "UUIDs" have base UUID 0000xxxx-0000-1000-8000-00805f9b34fb
    private static final UUID HRM_SERVICE_UUID =
        UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private static final UUID HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID =
        UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb"); // N
    private static final UUID BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID =
        UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb"); // R
    private static final UUID HEART_RATE_CONTROL_POINT_CHARACTERISTIC_UUID =
        UUID.fromString("00002A39-0000-1000-8000-00805f9b34fb"); // W

    private boolean mIsConnected = false;
    private BluetoothLeScanner mScanner;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "onScanResult, result = " + result.getDevice().getAddress());
            mBluetoothDevice = result.getDevice();
            connect(); // TODO: move to button handler
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed, errorCode = " + errorCode);
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange, STATE_CONNECTED\n\tgatt = " + gatt);
                mIsConnected = true;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange, STATE_DISCONNECTED\n\tgatt = " + gatt);
                disconnect();
            }
        }

        @Override
        public void onCharacteristicChanged(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d(TAG, "onCharacteristicChanged, UUID = " +  characteristic.getUuid());
            UUID uuid = characteristic.getUuid();
            if (uuid.equals(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                //byte[] value = characteristic.getValue();
                int formatType = BluetoothGattCharacteristic.FORMAT_UINT8;
                int value = characteristic.getIntValue(formatType, 1);
                Log.d(TAG, "value = " + value);
            }
        }

        @Override
        public void onCharacteristicRead(
            final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(TAG, "onCharacteristicRead, UUID = " +
                characteristic.getUuid() + ", status = " + status);
            if (status == GATT_SUCCESS) {
                UUID uuid = characteristic.getUuid();
                if (uuid.equals(BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID)) {
                    byte[] value = characteristic.getValue();
                    Log.d(TAG, value.toString());
                }
            }
        }

        @Override
        public void onCharacteristicWrite(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(TAG, "onCharacteristicWrite, UUID = " +
                characteristic.getUuid() + ",\nstatus = " + status);
        }

        private void init (final BluetoothGatt gatt) {
            // TODO: implement a queue or use a 3rd party BLE library
            readCharacteristic(gatt, HRM_SERVICE_UUID,
                BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID, 100);
            writeCharacteristic(gatt, HRM_SERVICE_UUID,
                HEART_RATE_CONTROL_POINT_CHARACTERISTIC_UUID,
                0, BluetoothGattCharacteristic.FORMAT_UINT16, 500);
            setCharacteristicNotification(gatt, HRM_SERVICE_UUID,
                HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID, 1000);
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services discovered, status = " + status);
            if (status == GATT_SUCCESS) {
                init(gatt);
            }
        }
    };

    private void readCharacteristic(
        final BluetoothGatt gatt, final UUID serviceUuid, final UUID characteristicUuid, int delayMs)
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothGattService gattService = gatt.getService(serviceUuid);
                if (gattService != null) {
                    BluetoothGattCharacteristic characteristic =
                        gattService.getCharacteristic(characteristicUuid);
                    if (characteristic != null) {
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }
        }, delayMs);
    }

    private void writeCharacteristic(
        final BluetoothGatt gatt, final UUID serviceUuid, final UUID characteristicUuid,
        final int value, final int formatType, int delayMs)
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothGattService gattService = gatt.getService(serviceUuid);
                if (gattService != null) {
                    BluetoothGattCharacteristic characteristic =
                        gattService.getCharacteristic(characteristicUuid);
                    if (characteristic != null) {
                        characteristic.setValue(value, formatType, 0);
                        gatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }, delayMs);
    }

    private void setCharacteristicNotification(
        final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int delayMs)
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gatt.setCharacteristicNotification(characteristic, true);
                // 0x2902 org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
                UUID configUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(configUuid);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }, delayMs);
    }

    private void setCharacteristicNotification(
        final BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, int delayMs)
    {
        setCharacteristicNotification(
            gatt, gatt.getService(serviceUuid).getCharacteristic(characteristicUuid), delayMs);
    }

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
        Assert.check(mHandler != null);
        Assert.check(mScanner != null);
        List<ScanFilter> filters = new ArrayList<>();
        //filters.add(new ScanFilter.Builder().setDeviceAddress("C9:1E:3F:18:61:9D").build());
        filters.add(new ScanFilter.Builder().setServiceUuid(
            new ParcelUuid(HRM_SERVICE_UUID)).build()); // 21+
        ScanSettings settings = (new ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY)).build();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "stop scan");
                mScanner.stopScan(mScanCallback);
            }
        }, SCAN_PERIOD_MS);
        Log.d(TAG, "start scan");
        mScanner.startScan(filters, settings, mScanCallback);
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
                mScanner = bluetoothAdapter.getBluetoothLeScanner();
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
            scan(); // TODO: move to button handler
        } else {
            Log.d(TAG, "Location not enabled");
        }
    }

    public void connect() {
        Log.d(TAG, "connect");
        Assert.check(mBluetoothDevice != null);
        Assert.check(mIsConnected == false);
        // call from main thread required to make connectGatt work
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // work-around to prevent multiple calls to mGattCallback
                // based on http://stackoverflow.com/questions/33274009
                if (mBluetoothGatt == null) {
                    // see https://stackoverflow.com/questions/22214254
                    boolean autoConnect = false; // see onConnectionStateChange
                    Log.d(TAG, "mBluetoothDevice.connectGatt");
                    try {
                        mBluetoothGatt = mBluetoothDevice.connectGatt(
                            MainActivity.this, autoConnect, mGattCallback);
                        Log.d(TAG, "mBluetoothGatt = " + (mBluetoothGatt != null ?
                            mBluetoothGatt.toString() : "null"));
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                } else {
                    Log.d(TAG, "mBluetoothGatt.connect");
                    mBluetoothGatt.connect();
                }
            }
        }, 1);
    }

    public void disconnect() {
        Log.d(TAG, "disconnect");
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mIsConnected = false;
    }
}
