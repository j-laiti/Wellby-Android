package com.rcsi.wellby.hrvTab.viewModels
// view model to contain the opperations associated with bluetooth connection between the app and a
// custom wearable device in this project
import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.util.*

class BluetoothController(
    private val context: Context,
    private val hrvDataManager: HRVDataManager
) : ViewModel() {
    private val appContext = context.applicationContext
    private var bluetoothAdapter: BluetoothAdapter? = getBluetoothAdapter(context)
    private var bluetoothGatt: BluetoothGatt? = null

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private var _startRecording = MutableStateFlow(false)
    val startRecording = _startRecording.asStateFlow()

    private var isRecording: Boolean = false
    var sessionID: UUID = UUID.randomUUID()

    private val operationQueue: Queue<() -> Unit> = LinkedList()
    private var operationInProgress = false

    private var recordingControlCharacteristic: BluetoothGattCharacteristic? = null

    //ppg metrics
    private val _rawPPGReadings = MutableStateFlow<List<Double>>(emptyList())
    val rawPPGReadings = _rawPPGReadings.asStateFlow()

    private val rawPPGBuffer = mutableListOf<Double>()
    private val smoothingWindowSize = 1

    private fun enqueueOperation(operation: () -> Unit) {
        operationQueue.add(operation)
        if (!operationInProgress) {
            processNextOperation()
        }
    }

    private fun processNextOperation() {
        if (operationQueue.isNotEmpty() && !operationInProgress) {
            operationInProgress = true
            operationQueue.poll()?.invoke()
        }
    }

    private fun completeOperation() {
        operationInProgress = false
        processNextOperation()
    }

    enum class ConnectionState {
        Connected,
        Disconnected,
        Connecting,
        ConnectionFailed
    }

    private fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            }

            if (permissionGranted) {
                result?.device?.let { device ->
                    val existingDevices = _devices.value
                    if (existingDevices.none { it.address == device.address }) {
                        val updatedList = existingDevices + device
                        _devices.value = updatedList
                    }
                }
            } else {
                Log.e("BluetoothController", "Missing Bluetooth permission")
            }
        }
    }

    fun startScanning() {
        if (_isScanning.value) return

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val xiaoServiceUUID = UUID.fromString("2ef946af-49fc-43f4-95c1-882a483f0a76")
            val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(xiaoServiceUUID)).build()
            val scanSettings = ScanSettings.Builder().build()

            bluetoothAdapter?.bluetoothLeScanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
            _isScanning.value = true
            Log.d("BluetoothController", "Started scanning for devices")
        } else {
            Log.e("BluetoothController", "Missing ACCESS_FINE_LOCATION permission")
        }
    }

    fun stopScanning() {
        if (!_isScanning.value) return

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            _isScanning.value = false
            Log.d("BluetoothController", "Stopped scanning for devices")
        } else {
            Log.e("BluetoothController", "Missing ACCESS_FINE_LOCATION permission")
        }
    }


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    } else {
                        ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    }

                    if (permissionGranted) {
                        _connectionState.value = ConnectionState.Connected
                        gatt.discoverServices()
                        stopScanning()
                        Log.d("BluetoothController", "Connected to GATT server")
                    } else {
                        _connectionState.value = ConnectionState.ConnectionFailed
                        Log.e("BluetoothController", "Missing Bluetooth permission")
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.Disconnected
                    Log.d("BluetoothController", "Disconnected from GATT server")
                }
                else -> {
                    _connectionState.value = ConnectionState.ConnectionFailed
                    Log.e("BluetoothController", "Connection state change failed with status $status")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceUUID = UUID.fromString(context.getString(com.rcsi.wellby.R.string.xiao_service_id))
                gatt.services.forEach { service ->
                    if (service.uuid == serviceUUID) {
                        service.characteristics.forEach { characteristic ->
                            when (characteristic.uuid) {
                                UUID.fromString("684c8f42-a60c-431c-b8ed-251e966d6a9a") -> { // Recording Control Characteristic UUID
                                    recordingControlCharacteristic = characteristic
                                    Log.d("BluetoothController", "Recording Control Characteristic found.")
                                }
                                else -> Log.d("BluetoothController", "Other characteristic: ${characteristic.uuid}")
                            }

                            val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                            } else {
                                ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                            }

                            if (permissionGranted) {
                                gatt.setCharacteristicNotification(characteristic, true)
                                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                                if (descriptor != null) {
                                    val notificationValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    writeDescriptor(gatt, descriptor, notificationValue)
                                } else {
                                    Log.w("BluetoothController", "Descriptor not found for ${characteristic.uuid}.")
                                }
                            } else {
                                Log.e("BluetoothController", "Missing Bluetooth permission")
                            }
                        }
                    }
                }
            } else {
                Log.e("BluetoothController", "Service discovery failed with status $status")
            }
        }

        private fun writeDescriptor(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, value: ByteArray) {
            enqueueOperation {
                val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                }

                if (permissionGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(descriptor, value)
                    } else {
                        @Suppress("DEPRECATION")
                        descriptor.value = value
                        @Suppress("DEPRECATION")
                        gatt.writeDescriptor(descriptor)
                    }
                } else {
                    Log.e("BluetoothController", "Missing Bluetooth permission")
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "Descriptor write successful for ${descriptor.characteristic.uuid}")
            } else {
                Log.e("Bluetooth", "Descriptor write failed for ${descriptor.characteristic.uuid} with status $status")
            }
            completeOperation()
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.d("Bluetooth","characteristic changed:" + characteristic.uuid)

            when (characteristic.uuid) {
                UUID.fromString("8881ab16-7694-4891-aebe-b0b11c6549d4") -> {
//                    updateHRVMetrics(value)
                }
                UUID.fromString("4aa76196-2777-4205-8260-8e3274beb327") -> {
                    if (value.isNotEmpty() && isRecording) {
                        handleRawPpgData(value)
                    }
                }
                else -> Log.d("Bluetooth", "Unhandled characteristic: ${characteristic.uuid}")
            }
        }
    }

//    private fun updateHRVMetrics(value: ByteArray) {
//        try {
//            val dataString = String(value, StandardCharsets.UTF_8)
//            val filteredDataString = dataString.filter { it.isDigit() || it == ' ' || it == '.' || it in listOf('G', 'P', 'E', 'I') }
//            val parts = filteredDataString.split(" ")
//
//            if (parts.size == 4) {
//                _sdnn.value = parts[0]
//                _rmssd.value = parts[1]
//                _heartRate.value = parts[2]
//                _signalQuality.value = describeSignalQuality(parts[3].first())
//            }
//        } catch (e: Exception) {
//            Log.e("BluetoothController", "Error parsing HRV data: ${e.localizedMessage}")
//        }
//    }

//    private fun describeSignalQuality(signalChar: Char): String {
//        return when (signalChar) {
//            'I' -> "Invalid"
//            'P' -> "Low"
//            'G' -> "Good"
//            'E' -> "Excellent"
//            else -> "Unknown"
//        }
//    }

    fun connectToDevice(device: BluetoothDevice) {
        _connectionState.value = ConnectionState.Connecting
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }

        if (permissionGranted) {
            bluetoothGatt = device.connectGatt(appContext, false, gattCallback)
        } else {
            _connectionState.value = ConnectionState.ConnectionFailed
            Log.e("BluetoothController", "Missing Bluetooth permission")
        }
    }

    fun disconnect() {
        bluetoothGatt?.let { gatt ->
            val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            }

            if (permissionGranted) {
                gatt.disconnect()
                gatt.close()
                bluetoothGatt = null
                _connectionState.value = ConnectionState.Disconnected
                _devices.value = emptyList()
                Log.d("BluetoothController", "Disconnected and resources freed.")
            } else {
                Log.e("BluetoothController", "Missing Bluetooth permission")
            }
        }
    }

    fun connectedDeviceName(): String {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }

        return if (permissionGranted) {
            bluetoothGatt?.device?.name ?: ""
        } else {
            ""
        }
    }

    private fun handleRawPpgData(value: ByteArray) {
        // Convert byte array to hex string
        val hexString = value.joinToString("") { String.format("%02x", it) }

        // Extract readings using regex for both hex and doubles
        val regex = Regex("(\\w{4})(fe)")
        val matches = regex.findAll(hexString)

        // Hex string readings for Firebase
        val hexReadings = matches.map { it.groups[1]?.value ?: "" }.filter { it.isNotEmpty() }.toList()

        // Double readings for smoothing and visualization
        val doubleReadings = matches.mapNotNull { match ->
            match.groups[1]?.value?.toIntOrNull(16)?.toDouble()
        }.toList()

        // Add double readings to buffer and smooth for visualization
        rawPPGBuffer.addAll(doubleReadings)
        if (rawPPGBuffer.size >= smoothingWindowSize) {
            val smoothedValue = rawPPGBuffer.takeLast(smoothingWindowSize).average()
            _rawPPGReadings.value = (_rawPPGReadings.value + smoothedValue).takeLast(20) // Keep the last 20 readings
            rawPPGBuffer.removeAt(0) // Maintain buffer size
        }

        // Upload hex readings to Firebase
        hrvDataManager.uploadRawDataToFirebase(sessionID.toString(), hexReadings)
    }


    fun startRecording() {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }

        val characteristic = recordingControlCharacteristic

        if (characteristic != null && bluetoothGatt != null && permissionGranted) {
            val startValue: Byte = 0x01 // Value to start recording
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use the new writeCharacteristic method for API 33+
                bluetoothGatt?.writeCharacteristic(
                    characteristic,
                    byteArrayOf(startValue),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                // Use the older setValue and writeCharacteristic methods for pre-API 33
                characteristic.value = byteArrayOf(startValue)
                bluetoothGatt?.writeCharacteristic(characteristic)
            }
            isRecording = true
            Log.d("BluetoothController", "Start recording command sent.")
        } else {
            Log.e("BluetoothController", "Recording Control Characteristic not found or GATT not connected.")
        }
    }

    fun stopRecording() {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }

        val characteristic = recordingControlCharacteristic
        if (characteristic != null && bluetoothGatt != null && permissionGranted) {
            val stopValue: Byte = 0x00 // Value to stop recording
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use the new writeCharacteristic method for API 33+
                bluetoothGatt?.writeCharacteristic(
                    characteristic,
                    byteArrayOf(stopValue),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                // Use the older setValue and writeCharacteristic methods for pre-API 33
                characteristic.value = byteArrayOf(stopValue)
                bluetoothGatt?.writeCharacteristic(characteristic)
            }
            isRecording = false
        } else {
            Log.e("BluetoothController", "Recording Control Characteristic not found or GATT not connected.")
        }
    }


//    fun startSession() {
//        isRecording = true
//        _startRecording.value = true
//        sessionID = UUID.randomUUID()
//        Log.d("BluetoothController", "Session started with ID: $sessionID")
//    }
//
//    fun stopSession() {
//        isRecording = false
//        Log.d("BluetoothController", "Session stopped")
//        _startRecording.value = false
//    }
}