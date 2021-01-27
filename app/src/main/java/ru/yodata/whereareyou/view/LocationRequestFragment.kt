package ru.yodata.whereareyou.view

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.method.DateTimeKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class LocationRequestFragment : Fragment(R.layout.fragment_location_request), LocationListenable {

    private val locationManager : LocationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE)
                as LocationManager}
    private val locationListener = MyLocationListener(this)
    private lateinit var myMap: GoogleMap
    private var mapReady = false
    private lateinit var marker: Marker

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->
        myMap = googleMap
        mapReady = true
    }

    /*@RequiresApi(Build.VERSION_CODES.N)
    private val gnssStatusCallback = GnssStatus.Callback {

    }*/

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Включить получение локаций от датчиков местоположения (отключение - в onDestroyView)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                Settings.locationMinTimeMs,
                Settings.locationMinDistanceM,
                locationListener)
        //locationManager.registerGnssStatusCallback()
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Выключить получение локаций от датчиков
        locationManager.removeUpdates(locationListener)
        // Необходимо для View Binding:
        _locationFrag = null
    }

    override fun onLocationChanged(newLocation: Location) {
        var point = LatLng(newLocation.latitude, newLocation.longitude)
        with (newLocation) {
            with(locationFrag) {
                latitudeTv.text = latitude.toString()
                longitudeTv.text = longitude.toString()
                altitudeTv.text = altitude.toString()
                speedTv.text = speed.toString()
                var moment = Date(time)
                timeTv.text = "${(moment.hours)}:${(moment.minutes)}:${(moment.seconds)}"
            }
        }
        if (mapReady) {
            if (::marker.isInitialized) marker.remove()
            with (myMap) {
                marker = addMarker(MarkerOptions().title("Это я!").position(point))
                moveCamera(CameraUpdateFactory.newLatLng(point))
                moveCamera(CameraUpdateFactory.zoomTo(18F))
            }
        }

    }

}