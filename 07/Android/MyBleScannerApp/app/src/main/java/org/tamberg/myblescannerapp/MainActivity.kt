// https://fhnw.mit-license.org/

package org.tamberg.myblescannerapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import org.tamberg.myblescannerapp.ui.theme.MyBleScannerAppTheme

val LocalBlePermissionHelper = staticCompositionLocalOf<MyBlePermissionHelper> { null!! }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val helper = MyBlePermissionHelper(this) // hack
        setContent {
            MyBleScannerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalBlePermissionHelper provides helper) {
                        MyBleScannerView()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyBleScannerAppTheme {
        MyBleScannerView()
    }
}

@Composable
fun MyBleScannerView(model: MyBleScannerViewModel = viewModel()) {
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

class MyBleScannerViewModel(app: Application) : AndroidViewModel(app) {
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

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult, result = " + result.device.address) // TODO
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "onScanFailed, errorCode = $errorCode") // TODO
        }
    }

    @SuppressLint("MissingPermission")
    private fun doScan() {
        val app = super.getApplication<Application>()
        val bluetoothManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            Log.d(TAG, "stop scan")
            scanner!!.stopScan(scanCallback)
            enabled.value = true
        }, SCAN_PERIOD_MS)
        Log.d(TAG, "start scan")
        scanner.startScan(scanCallback)
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
