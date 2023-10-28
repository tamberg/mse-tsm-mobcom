package org.tamberg.myblescannerapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityOptionsCompat
import org.tamberg.myblescannerapp.ui.theme.MyBleScannerAppTheme


class MainActivity : ComponentActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_ENABLE_BT = 1
    private val SCAN_PERIOD_MS: Long = 10000

    private var scanner: BluetoothLeScanner? = null
    private val handler: Handler? = Handler(Looper.getMainLooper())

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "onScanResult, result = " + result.device.address)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "onScanFailed, errorCode = $errorCode")
        }
    }

    @SuppressLint("MissingPermission") // TODO remove?
    private fun scan() {
        assert(handler != null)
        assert(scanner != null)
        handler!!.postDelayed({
            Log.d(TAG, "stop scan")
            scanner!!.stopScan(scanCallback)
        }, SCAN_PERIOD_MS)
        Log.d(TAG, "start scan")
        scanner!!.startScan(scanCallback)
    }

    /* https://stackoverflow.com/questions/66551781/android-onrequestpermissionsresult-is-deprecated-are-there-any-alternatives
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray?
    ) {
        scan()
    }
     */

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "isGranted = ${isGranted}")
        if (isGranted) {
            scan() // TODO
        }
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d(TAG, "${it.key} = ${it.value}") // TODO
        }
        scan() // TODO
    }

    private val startActivityForResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "result.resultCode = ${result.resultCode}"); // TODO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContent {
            MyBleScannerAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        // TODO: refactor this part
        val hasBle = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        // Or <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
        if (hasBle) {
            Log.d(TAG, "BLE available")
            val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                Log.d(TAG, "BLE enabled")
                scanner = bluetoothAdapter.bluetoothLeScanner
                /* https://stackoverflow.com/questions/66551781/android-onrequestpermissionsresult-is-deprecated-are-there-any-alternatives
                val permissions = arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
                */
                // TODO: check permissions, only ask if not given?
                // TODO: request permissions for other OS versions?
                // TODO: ask user to adapt location permission
                //val enableIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                //startActivityForResultLauncher2.launch(enableIntent)
                scan() // TODO
            } else {
                Log.d(TAG, "BLE not enabled")
                // https://stackoverflow.com/questions/68095709/startactivityforresult-is-deprecated-and-unable-to-request-for-bluetooth-connec
                // TODO: refactor to allow multiple tries
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestMultiplePermissionsLauncher.launch(arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                startActivityForResultLauncher.launch(enableBtIntent)
            }
        } else {
            Log.d(TAG, "BLE not available")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyBleScannerAppTheme {
        Greeting("Android")
    }
}