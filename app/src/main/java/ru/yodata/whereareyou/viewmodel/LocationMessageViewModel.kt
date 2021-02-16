package ru.yodata.whereareyou.viewmodel

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.yodata.whereareyou.TAG
import ru.yodata.whereareyou.model.LocationMessage

// Класс определяет ViewModel для хранения сообщения о локации (LocationMessage).
// ViewModel создана с использованием библиотеки Vita
class LocationMessageViewModel(app: Application): AndroidViewModel(app) {
    private val mutableLocationMessage = MutableLiveData<LocationMessage>()
    val locationMessage: LiveData<LocationMessage> get() = mutableLocationMessage

    fun setLocationMessage(newLocationMessage: LocationMessage) {
        mutableLocationMessage.value = newLocationMessage
        Log.d(TAG,"Запись в ${this::class.java.simpleName}: ${newLocationMessage.abonentPhoneNumber}")
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG,"${this::class.java.simpleName} вьюмодель очищена")
    }
}