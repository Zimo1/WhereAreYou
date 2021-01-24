package ru.yodata.whereareyou

import android.location.Location
import android.location.LocationListener
/*
 Класс-потомок встроенного класса android.location.LocationListener для создания слушателей новых
 локаций (Location), поступающих от датчиков позиционирования.
 Содержит в себе интерфейс LocationListenable, что позволяет подменить у LocationListener
 встроенный метод onLocationChanged одноименным методом интерфейса и тем самым передать
 поступающие значения Location во фрагмент, в котором будет производиться их обработка.
 Фрагмент должен реализовывать этот интерфейс, предоставляя свою реализацию
 метода onLocationChanged, в котором и происходит обработка поступающих Location.
 Внутри класса фрагмента объявление слушателя делается следующим образом (сразу после заголовка
 класса):
 private val locationListener = MyLocationListener(this)
 Там же создается location manager:
 private val locationManager : LocationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager}
*/

class MyLocationListener (
    private val locationListenableInterface : LocationListenable) : LocationListener {

    override fun onLocationChanged(newLocation: Location) {
        locationListenableInterface.onLocationChanged(newLocation)
    }
}
