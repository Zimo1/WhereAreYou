package ru.yodata.whereareyou.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Класс определяет ViewModel для хранения последней полученной от датчика GPS локации (Location)
class LastLocationViewModel: ViewModel() {
    private val mutableLastLocation = MutableLiveData<Location>()
    val location: LiveData<Location> get() = mutableLastLocation

    fun setLocation(newLocation: Location) {
        mutableLastLocation.value = newLocation
    }
}

