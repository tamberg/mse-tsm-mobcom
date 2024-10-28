// https://fhnw.mit-license.org/

package org.tamberg.myblecentralapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.tamberg.myblecentralapp.ui.theme.MyBleCentralAppTheme
import java.util.*

val LocalBlePermissionHelper = staticCompositionLocalOf<MyBlePermissionHelper> { null!! }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val helper = MyBlePermissionHelper(this) // hack
        setContent {
            MyBleCentralAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalBlePermissionHelper provides helper) {
                        MyBleCentralView()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyBleCentralAppTheme {
        MyBleCentralView()
    }
}

@Composable
fun MyBleCentralView(model: MyBleCentralViewModel = viewModel()) {
    val helper = LocalBlePermissionHelper.current // hack
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    {
        Row {
            Text(model.info.value)
        }
        Row {
            Button(
                enabled = model.enabled.value,
                onClick = { model.scan(helper) })
            {
                Text(model.command.value)
            }
        }
    }
}

class MyBlePermissionHelper(activity: ComponentActivity) {

    // helper class for functions that need a reference to the activity
    // there's probably a better way to do this right in Compose, e.g.
    // https://stackoverflow.com/questions/69075984

    lateinit var update: () -> Unit

    fun setUpdateCallback(updateCallback: () -> Unit) {
        update = updateCallback
    }

    private val startActivityForResultLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        update()
    }

    fun askToEnableBle() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResultLauncher.launch(enableBtIntent)
    }

    fun askToEnableLocation() {
        val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResultLauncher.launch(enableLocationIntent)
    }

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        update()
    }

    private val requestMultiplePermissionsLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        update()
    }

    // See https://developer.android.com/develop/connectivity/bluetooth/bt-permissions,
    // https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner
    // #startScan(android.bluetooth.le.ScanCallback)
    fun askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissionsLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}

class MyBleCentralViewModel(app: Application) : AndroidViewModel(app) {
    private val TAG = this.javaClass.name
    private val SCAN_PERIOD_MS: Long = 10000

    val info = mutableStateOf("Scan for peripherals.")
    val command = mutableStateOf("Scan")
    val enabled = mutableStateOf(true)

    // Note: This code has been converted from Java.

    private fun hasBle(): Boolean {
        val app = super.getApplication<Application>()
        return app.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun isBleEnabled(): Boolean {
        val app = super.getApplication<Application>()
        val bluetoothManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    private fun isLocationEnabled(): Boolean {
        val result: Boolean
        val app = super.getApplication<Application>()
        val locationManager = app.getSystemService(LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            result = locationManager.isLocationEnabled
        } else {
            result = true
        }
        return result
    }

    // See https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner#startScan(android.bluetooth.le.ScanCallback)
    private fun hasPermission(): Boolean {
        val result: Boolean
        val app = super.getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            result = (app.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) &&
                    (app.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            result = app.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = app.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            result = true // older versions had permission by default
        }
        return result
    }

    // Bluetooth SIG registered 16-bit "UUIDs" have base UUID 0000xxxx-0000-1000-8000-00805f9b34fb
    private val HRM_SERVICE_UUID =
        UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID =
        UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb") // N
    private val BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID =
        UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb") // R
    private val HEART_RATE_CONTROL_POINT_CHARACTERISTIC_UUID =
        UUID.fromString("00002A39-0000-1000-8000-00805f9b34fb") // W

    private var mIsConnected = false
    private var mBluetoothDevice: BluetoothDevice? = null
    private var mBluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    private fun readCharacteristic(
        gatt: BluetoothGatt, serviceUuid: UUID, characteristicUuid: UUID, delayMs: Long
    ) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val gattService = gatt.getService(serviceUuid)
            if (gattService != null) {
                val characteristic = gattService.getCharacteristic(characteristicUuid)
                if (characteristic != null) {
                    gatt.readCharacteristic(characteristic)
                }
            }
        }, delayMs)
    }

    @SuppressLint("MissingPermission")
    private fun writeCharacteristic(
        gatt: BluetoothGatt, serviceUuid: UUID, characteristicUuid: UUID,
        value: Int, formatType: Int, delayMs: Long
    ) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val gattService = gatt.getService(serviceUuid)
            if (gattService != null) {
                val characteristic = gattService.getCharacteristic(characteristicUuid)
                if (characteristic != null) {
                    characteristic.setValue(value, formatType, 0)
                    gatt.writeCharacteristic(characteristic)
                }
            }
        }, delayMs)
    }

    @SuppressLint("MissingPermission")
    private fun setCharacteristicNotification(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, delayMs: Long
    ) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            gatt.setCharacteristicNotification(characteristic, true)
            // 0x2902 org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
            val configUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val descriptor = characteristic.getDescriptor(configUuid)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }, delayMs)
    }

    private fun setCharacteristicNotification(
        gatt: BluetoothGatt, serviceUuid: UUID, characteristicUuid: UUID, delayMs: Long
    ) {
        setCharacteristicNotification(
            gatt, gatt.getService(serviceUuid).getCharacteristic(characteristicUuid), delayMs
        )
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange, STATE_CONNECTED\n\tgatt = $gatt")
                mIsConnected = true
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange, STATE_DISCONNECTED\n\tgatt = $gatt")
                disconnect()
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            Log.d(TAG, "onCharacteristicChanged, UUID = " + characteristic.uuid)
            val uuid = characteristic.uuid
            if (uuid == HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID) {
                //byte[] value = characteristic.getValue();
                val formatType = BluetoothGattCharacteristic.FORMAT_UINT8
                val value = characteristic.getIntValue(formatType, 1)
                Log.d(TAG, "value = $value")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            Log.d(
                TAG, "onCharacteristicRead, UUID = " +
                        characteristic.uuid + ", status = " + status
            )
            if (status == GATT_SUCCESS) {
                val uuid = characteristic.uuid
                if (uuid == BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID) {
                    val value = characteristic.value
                    Log.d(TAG, value.toString())
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            Log.d(
                TAG, """
                onCharacteristicWrite, UUID = ${characteristic.uuid},
                status = $status
                """.trimIndent()
            )
        }

        private fun init(gatt: BluetoothGatt) {
            // TODO: implement a queue or use a 3rd party BLE library
            readCharacteristic(
                gatt, HRM_SERVICE_UUID,
                BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID, 100
            )
            writeCharacteristic(
                gatt, HRM_SERVICE_UUID,
                HEART_RATE_CONTROL_POINT_CHARACTERISTIC_UUID,
                0, BluetoothGattCharacteristic.FORMAT_UINT16, 500
            )
            setCharacteristicNotification(
                gatt, HRM_SERVICE_UUID,
                HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID, 1000
            )
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "Services discovered, status = $status")
            if (status == GATT_SUCCESS) {
                init(gatt)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        Log.d(TAG, "connect")
        assert(mBluetoothDevice != null)
        assert(mIsConnected == false)
        // call from main thread required to make connectGatt work
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // work-around to prevent multiple calls to mGattCallback
            // based on http://stackoverflow.com/questions/33274009
            if (mBluetoothGatt == null) {
                // see https://stackoverflow.com/questions/22214254
                val autoConnect = false // see onConnectionStateChange
                Log.d(TAG, "mBluetoothDevice.connectGatt")
                try {
                    val app = super.getApplication<Application>()
                    mBluetoothGatt = mBluetoothDevice!!.connectGatt(
                        app, autoConnect, mGattCallback
                    )
                    Log.d(
                        TAG,
                        "mBluetoothGatt = " + if (mBluetoothGatt != null) mBluetoothGatt.toString() else "null"
                    )
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            } else {
                Log.d(TAG, "mBluetoothGatt.connect")
                mBluetoothGatt!!.connect()
            }
        }, 1)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        Log.d(TAG, "disconnect")
        mBluetoothGatt!!.disconnect()
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
        mIsConnected = false
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult, result = " + result.device.address)
            mBluetoothDevice = result.device // pick first
            connect()
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "onScanFailed, errorCode = $errorCode") // TODO
        }
    }

    @SuppressLint("MissingPermission")
    fun doScan() {
        val app = super.getApplication<Application>()
        val bluetoothManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val handler = Handler(Looper.getMainLooper())
        val filters: MutableList<ScanFilter> = ArrayList()
        //filters.add(new ScanFilter.Builder().setDeviceAddress("C9:1E:3F:18:61:9D").build());
        filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(HRM_SERVICE_UUID)).build()) // 21+
        val settings = ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY
        ).build()
        handler.postDelayed({
            Log.d(TAG, "stop scan")
            scanner.stopScan(scanCallback)
            enabled.value = true
        }, SCAN_PERIOD_MS)
        Log.d(TAG, "start scan")
        scanner.startScan(filters, settings, scanCallback)
    }

    private fun update() {
        if (!hasBle()) {
            info.value = "Bluetooth not available."
            command.value = "Scan"
            enabled.value = false
        } else if (!hasPermission()) {
            info.value = "Permission not given."
            command.value = "Give permission"
            enabled.value = true
        } else if (!isBleEnabled()) {
            info.value = "Bluetooth not enabled."
            command.value = "Enable Bluetooth"
            enabled.value = true
        } else if (!isLocationEnabled()) {
            info.value = "Location not enabled."
            command.value = "Enable location"
            enabled.value = true
        } else {
            info.value = "Scan for peripherals."
            command.value = "Scan"
            enabled.value = true
        }
    }

    fun scan(helper: MyBlePermissionHelper) {
        helper.setUpdateCallback { update() }
        enabled.value = false
        if (!hasBle()) {
            update()
        } else if (!hasPermission()) {
            helper.askForPermission()
        } else if (!isBleEnabled()) {
            helper.askToEnableBle()
        } else if (!isLocationEnabled()) {
            helper.askToEnableLocation()
        } else {
            doScan()
        }
    }
}
