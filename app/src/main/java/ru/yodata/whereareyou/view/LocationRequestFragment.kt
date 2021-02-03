package ru.yodata.whereareyou.view

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import ru.yodata.whereareyou.*
import ru.yodata.whereareyou.databinding.FragmentLocationRequestBinding
import ru.yodata.whereareyou.viewmodel.LastLocationViewModel
import java.util.*

// Инициализация View Binding
private var _locationFrag: FragmentLocationRequestBinding? = null
private val locationFrag get() = _locationFrag!!

// Фрагмент, в котором происходит работа с GPS и картой
class LocationRequestFragment : Fragment(R.layout.fragment_location_request) { //, LocationListenable {

    private val lastLocationViewModel: LastLocationViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var myMap: GoogleMap // Хранит ссылку на готовую карту
    private var mapReady = false // Флаг "Карта готова к работе"
    private var gpsFixed = false // Флаг "Фикс произошел"
    private lateinit var marker: Marker // Маркер положения пользователя на карте

    private val locationManager : LocationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE)
                as LocationManager}

    // Реализация объекта-лиснера локаций
    private val locationListener = object : LocationListener {
        // Функция отрабатывает включение провайдера GPS.
        // Ее реализация обязательна, даже пустая, иначе при включении GPS после его выключения
        // это приложение будет вылетать с ошибкой, т.к. вызываемый при этом метод onProviderEnabled
        // абстрактный.
        override fun onProviderEnabled(provider: String) {
            locationFrag.gpsStatusTv.text = getString(R.string.gps_provider_on_msg)
            locationFrag.gpsStatusTv.setTextColor(PREPARE_COLOR)
        }

        // Функция отрабатывает отключение провайдера GPS.
        // Ее реализация обязательна, даже пустая, иначе при выключеном GPS это приложение будет
        // вылетать на старте с ошибкой, т.к. вызываемый при этом метод onProviderDisabled абстрактный.
        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.gps_provider_off_msg),
                        Toast.LENGTH_LONG
                ).show()
                locationFrag.gpsStatusTv.text = getString(R.string.gps_provider_off_msg)
                locationFrag.gpsStatusTv.setTextColor(ALERT_COLOR)
            }
        }

        // Функция отрабатывала изменение статуса провайдера GPS, но более не применяется.
        // Вместо нее работает GnssStatus.Callback(), см. ниже
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
        }

        // Это основная функция лиснера - она вызывается для каждой новой локации, поступившей от
        // датчиков GPS. Здесь происходит вывод на экран сведений о локации, отрисовывается карта
        // и маркер положения пользователя на ней.
        override fun onLocationChanged(newLocation: Location) {
            // Переменная для отображения позиции на карте
            var point = LatLng(newLocation.latitude, newLocation.longitude)
            // Записать новую локацию во вьюмодель
            lastLocationViewModel.setLocation(newLocation)
            // Вывести на экран текстовые сведения о полученной локации
            with (newLocation) {
                with(locationFrag) {
                    latitudeTv.text = latitude.toString()
                    longitudeTv.text = longitude.toString()
                    accuracyTv.text = accuracy.toString()
                    altitudeTv.text = altitude.toString()
                    speedTv.text = (speed / 1000 * 3600).toString() // данные переводятся в км/ч
                    var moment = Date(time)
                    timeTv.text = String.format("%02d:%02d:%02d",
                                                moment.hours, moment.minutes, moment.seconds)
                }
            }
            // Вывести на карте маркер полученной локации
            if (mapReady) {
                if (::marker.isInitialized) marker.remove() // стереть предыдущий маркер с карты, если он есть
                with (myMap) {
                    marker = addMarker(MarkerOptions().title(getString(R.string.itsme_msg))
                            .position(point)
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.circle_arrow_red_32)).anchor(0.5F,0.5F)
                            //.flat(true)
                            //.rotation(newLocation.bearing)
                            )
                    // Настроить параметры камеры для отображения карты
                    val cameraPosition = CameraPosition.Builder()
                            .target(point)
                            .zoom(getCameraPosition().zoom)
                            .bearing(newLocation.bearing) // камера будет поворачиваться по направлению движения
                            .tilt(Settings.mapTilt)
                            .build()
                    animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
    }

    // Колбек срабатывает когда карта асинхронно создана вызовом getMapAsync
    private val mapReadyCallback = OnMapReadyCallback { googleMap ->
        myMap = googleMap // получить ссылку на готовую карту в myMap
        myMap.uiSettings.isZoomControlsEnabled = true // добавить на карту кнопки масштабирования
        mapReady = true // установить флаг готовности карты
    }

    // Колбек срабатывает при изменении статуса GPS.
    // Здесь используется только для отображения статуса на экране, поэтому не особо важен
    // для работы приложения
    @RequiresApi(Build.VERSION_CODES.N)
    private val gnssStatusCallback = object : GnssStatus.Callback() {
        /*override fun onSatelliteStatusChanged(status: GnssStatus) {
            //super.onSatelliteStatusChanged(status)
            locationFrag.gpsStatusTv.text =  String.format(getString(R.string.satellite_quan_msg),
                    status.satelliteCount)
        }*/

        // Функция отрабатывает начало процесса вычисления координат текущего положения пользователя
        // GPS-провайдером
        override fun onStarted() {
            //super.onStarted()
            with (locationFrag) {
                // Вывести на экран сообщение об установке GPS-статуса "Старт"
                gpsStatusTv.text = getString(R.string.gps_start_msg)
                gpsStatusTv.setTextColor(PREPARE_COLOR)
                // Включить прогрессбар
                startProgressBar.visibility = View.VISIBLE
                // Деактивировать кнопку перехода на экран передачи данных
                toSendLocationScreenBtn.isEnabled = false
            }
        }

        // Функция отрабатывает момент первого успешного завершения определения координат
        // текущего положения пользователя (фикс)
        override fun onFirstFix(ttffMillis: Int) {
            //super.onFirstFix(ttffMillis)
            with (locationFrag) {
                // Вывести на экран сообщение об установке GPS-статуса "Фикс"
                gpsStatusTv.text = getString(R.string.gps_fix_msg)
                gpsStatusTv.setTextColor(ALL_RIGHT_COLOR)
                // Убрать прогрессбар
                startProgressBar.visibility = View.INVISIBLE
                // Активировать кнопку перехода на экран передачи данных
                toSendLocationScreenBtn.isEnabled = true
            }
            gpsFixed = true
            // Установить первоначальный масштаб изображения карты (из настроек)
            myMap.moveCamera(CameraUpdateFactory.zoomTo(Settings.mapZoom))
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Необходимо для View Binding:
        _locationFrag = FragmentLocationRequestBinding.bind(view)
        // Запустить отрисовку карты
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)
        // Назначить лиснер кнопке перехода на экран передачи местоположения другому пользователю
        locationFrag.toSendLocationScreenBtn.setOnClickListener{ view ->
            view.findNavController()
                .navigate(R.id.action_locationRequestFragment_to_sendLocationFragment)}
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        // Включить получение локаций от датчиков местоположения (отключение - в onStop)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                Settings.locationMinTimeMs,
                Settings.locationMinDistanceM,
                locationListener)
        //locationManager.registerGnssStatusCallback(gnssStatusCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        // Включить отслеживание статуса GPS
        locationManager.registerGnssStatusCallback(gnssStatusCallback, null)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        // Отключить отслеживание статуса GPS
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
    }

    override fun onStop() {
        super.onStop()
        // Запомнить текущий масштаб карты, чтобы восстановиться в этом же масштабе.
        // Делать это нужно только если фикс уже произошел, иначе запомнится неверный масштаб (весь мир)
        if (gpsFixed) Settings.mapZoom = myMap.getCameraPosition().zoom
        // Выключить получение локаций от датчиков
        locationManager.removeUpdates(locationListener)
        gpsFixed = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Необходимо для View Binding:
        _locationFrag = null
        // Сбросить флаг готовности карты
        mapReady = false
    }

}