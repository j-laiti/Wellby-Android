package com.rcsi.wellby.hrvTab.viewModels
// factory used for the initialisation of the bluetooth view model which takes the HRV view model
// as an argument

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
class BLEViewModelFactory(
    private val hrvDataManager: HRVDataManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothController::class.java)) {
            return BluetoothController(context, hrvDataManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
