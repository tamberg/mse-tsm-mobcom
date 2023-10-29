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
import android.content.Intent
import android.content.pm.PackageManager
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

class MyBlePermissionHelper(private val activity: ComponentActivity) {
    val TAG = this.javaClass.name

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

    // TODO
    // https://stackoverflow.com/questions/66551781/android-onrequestpermissionsresult-is-deprecated-are-there-any-alternatives
    //fun onRequestPermissionsResult(
    //    requestCode: Int,
    //    permissions: Array<String?>?,
    //    grantResults: IntArray?
    //) {
    //     ...
    //}

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
    val TAG = this.javaClass.name

    val info = mutableStateOf<String>("Scan for peripherals.")
    val command = mutableStateOf<String>("Scan")
    val enabled = mutableStateOf<Boolean>(true)

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
        return true // TODO how to check?
    }

    private fun hasPermission(): Boolean { // TODO move to helper?
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
            result = false // TODO true?
        }
        return result
    }

    private val SCAN_PERIOD_MS: Long = 10000

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult, result = " + result.device.address) // TODO
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "onScanFailed, errorCode = $errorCode") // TODO
            update()
        }
    }

    @SuppressLint("MissingPermission")
    private fun doScan() {
        val app = super.getApplication<Application>()
        val bluetoothManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ // TODO other approach?
            Log.d(TAG, "stop scan")
            scanner!!.stopScan(scanCallback) // TODO safe?
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
        } else if (!isBleEnabled()) {
            info.value = "Bluetooth not enabled."
            command.value = "Enable Bluetooth"
            enabled.value = true
        } else if (!hasPermission()) {
            info.value = "Permission not given."
            command.value = "Give permission"
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
        } else if (!isBleEnabled()) {
            helper.askToEnableBle()
        } else if (!hasPermission()) {
            helper.askForPermission()
        } else if (!isLocationEnabled()) {
            helper.askToEnableLocation()
        } else {
            doScan()
        }
    }
}
