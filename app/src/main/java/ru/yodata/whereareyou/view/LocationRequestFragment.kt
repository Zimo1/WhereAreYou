package ru.yodata.whereareyou.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.yodata.whereareyou.LocationListenable
import ru.yodata.whereareyou.MyLocationListener
import ru.yodata.whereareyou.R
import ru.yodata.whereareyou.Settings
import ru.yodata.whereareyou.databinding.FragmentLocationRequestBinding

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

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        myMap = googleMap
        mapReady = true
        /*val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10F))*/

    }

    /*@SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Settings.locationMinTimeMs,
                    Settings.locationMinDistanceM,
                    locationListener)
        }
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
        return inflater.inflate(R.layout.fragment_location_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Необходимо для View Binding:
        _locationFrag = FragmentLocationRequestBinding.bind(view)
        // Запустить отрисовку карты
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

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
        locationFrag.gpsCoordinatesTv.text =
                "${newLocation.latitude.toString()} : ${newLocation.longitude.toString()}"
        if (mapReady) {
            if (::marker.isInitialized) marker.remove()
            marker = myMap.addMarker(MarkerOptions().position(point).title("It's me!"))
            myMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            myMap.moveCamera(CameraUpdateFactory.zoomTo(17F))
        }

    }
}