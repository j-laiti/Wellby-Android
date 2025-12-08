package com.rcsi.wellby.hrvTab.helpers
// bluetooth permissions for connecting to the custom device used in this project

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PermissionHandler(private val activity: ComponentActivity, private val context: Context) {

    var onPermissionsGranted: (() -> Unit)? = null
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Activity result contracts handle the permission requests
    private val requestEnableBtLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            onPermissionsGranted?.invoke()
        } else {
            Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allPermissionsGranted = permissions.entries.all { it.value }
        if (allPermissionsGranted) {
            Toast.makeText(context, "All necessary permissions granted", Toast.LENGTH_SHORT).show()
            onPermissionsGranted?.invoke()
        } else {
            Toast.makeText(context, "Not all permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            requiredPermissions.add(Manifest.permission.BLUETOOTH)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Toast.makeText(context, "All permissions already granted", Toast.LENGTH_SHORT).show()
            onPermissionsGranted?.invoke()
        }
    }

    suspend fun enableBluetooth(): Boolean = suspendCancellableCoroutine { continuation ->
        if (bluetoothAdapter?.isEnabled == true) {
            continuation.resume(true)
        } else {
            showBluetoothRequestDialog { isEnabled ->
                continuation.resume(isEnabled)
            }
        }
    }

    private fun showBluetoothRequestDialog(onResult: (Boolean) -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle("Enable Bluetooth")
            setMessage("This feature requires Bluetooth. Please enable it to continue.")
            setPositiveButton("Enable") { dialog, which ->
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestEnableBtLauncher.launch(enableBtIntent)
                onResult(true) // Assume positive intent means user wants to enable
            }
            setNegativeButton("Cancel") { dialog, which ->
                onResult(false)
            }
            show()
        }
    }

}