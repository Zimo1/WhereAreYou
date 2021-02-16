package ru.yodata.whereareyou.viewmodel

import android.app.Application
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.yodata.whereareyou.TAG

// Класс определяет ViewModel для хранения последней полученной от датчика GPS локации (Location).
// ViewModel создана с использованием библиотеки Vita
class LastLocationViewModel(app: Application): AndroidViewModel(app) {
    private val mutableLastLocation = MutableLiveData<Location>()
    val location: LiveData<Location> get() = mutableLastLocation

    fun setLocation(newLocation: Location) {
        mutableLastLocation.value = newLocation
        Log.d(TAG,"Запись в ${this::class.java.simpleName}: ${newLocation.longitude}")
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG,"${this::class.java.simpleName} вьюмодель очищена")
    }
}

