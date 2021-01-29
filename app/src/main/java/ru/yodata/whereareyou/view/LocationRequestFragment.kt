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
import android.text.method.DateTimeKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.yodata.whereareyou.LocationListenable
import ru.yodata.whereareyou.MyLocationListener
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.Settings
import ru.yodata.whereareyou.databinding.FragmentLocationRequestBinding
import java.util.*

// Инициализация View Binding
private var _locationFrag: FragmentLocationRequestBinding? = null
private val locationFrag get() = _locationFrag!!

class LocationRequestFragment : Fragment(R.layout.fragment_location_request) { //, LocationListenable {

    private lateinit var myMap: GoogleMap
    private var mapReady = false
    private lateinit var marker: Marker

    private val locationManager : LocationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE)
                as LocationManager}

    private val locationListener = object : LocationListener { //MyLocationListener(this) {
        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.gps_provider_off_msg),
                        Toast.LENGTH_LONG
                ).show()
                locationFrag.gpsStatusTv.text = getString(R.string.gps_provider_off_msg)
            }
        }

        override fun onLocationChanged(newLocation: Location) {
            var point = LatLng(newLocation.latitude, newLocation.longitude)
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
            if (mapReady) {
                if (::marker.isInitialized) marker.remove()
                with (myMap) {
                    marker = addMarker(MarkerOptions().title("Это я!").position(point))
                    moveCamera(CameraUpdateFactory.newLatLng(point))
                    //moveCamera(CameraUpdateFactory.zoomTo(Settings.mapZoom))
                }
            }
        }
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->
        myMap = googleMap
        //val cameraUpdateFactory = CameraUpdateFactory.zoomTo(Settings.mapZoom)
        myMap.uiSettings.isZoomControlsEnabled = true
        mapReady = true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private val gnssStatusCallback = object : GnssStatus.Callback() {
        /*override fun onSatelliteStatusChanged(status: GnssStatus) {
            //super.onSatelliteStatusChanged(status)
            locationFrag.gpsStatusTv.text =  String.format(getString(R.string.satellite_quan_msg),
                    status.satelliteCount)
        }*/

        override fun onFirstFix(ttffMillis: Int) {
            //super.onFirstFix(ttffMillis)
            locationFrag.gpsStatusTv.text = "Фикс"
            // Установить первоначальный масштаб изображения карты
            myMap.moveCamera(CameraUpdateFactory.zoomTo(Settings.mapZoom))
        }

        override fun onStarted() {
            //super.onStarted()
            locationFrag.gpsStatusTv.text = "Старт..."
        }

        /*override fun onStopped() {
            //super.onStopped()
            locationFrag.gpsStatusTv.text = "Стоп"
        }*/
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
        locationManager.registerGnssStatusCallback(gnssStatusCallback, null)
    }

    override fun onStop() {
        super.onStop()
        // Запомнить текущий масштаб карты, чтобы восстановиться в этом же масштабе
        Settings.mapZoom = myMap.getCameraPosition().zoom
        // Выключить получение локаций от датчиков
        locationManager.removeUpdates(locationListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Необходимо для View Binding:
        _locationFrag = null
    }

}