package ru.yodata.whereareyou.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import ru.yodata.whereareyou.*
import ru.yodata.whereareyou.databinding.FragmentReceiverBinding
import ru.yodata.whereareyou.model.*
import ru.yodata.whereareyou.viewmodel.LastLocationViewModel
import ru.yodata.whereareyou.viewmodel.LocationMessageViewModel

// Инициализация View Binding
private var _receiverFrag: FragmentReceiverBinding? = null
private val receiverFrag get() = _receiverFrag!!

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReceiverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReceiverFragment : Fragment(R.layout.fragment_receiver) {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Класс настроек экрана для определенного режима работы
    class ReceiverScreenSettings (
            val screenHeader: String,
            val showAbonentNumberAndComment: Int,
            val showScopeButtons: Int,
            //val showSenderLocation: Boolean,
            val startMapScope: MapScope,
    )
    // Предопределенные режимы отображения экрана и их настройки
    private val screenModes: Map<LocationMessageType, ReceiverScreenSettings> by lazy {
        mapOf(
                LocationMessageType.EMPTY to ReceiverScreenSettings(
                        screenHeader = requireContext().getString(R.string.receiver_screen_define_title),
                        //showSenderLocation = false,
                        showAbonentNumberAndComment = View.GONE,
                        showScopeButtons = View.GONE,
                        startMapScope = MapScope.ME_SCOPE
                ),
                LocationMessageType.REQUEST to ReceiverScreenSettings(
                        screenHeader = requireContext().getString(R.string.receiver_screen_request_title),
                        showAbonentNumberAndComment = View.VISIBLE,
                        showScopeButtons = View.GONE,
                        //showSenderLocation = false,
                        startMapScope = MapScope.ME_SCOPE
                ),
                LocationMessageType.FULL_REQUEST to ReceiverScreenSettings(
                        screenHeader = requireContext().getString(R.string.receiver_screen_request_title),
                        showAbonentNumberAndComment = View.VISIBLE,
                        showScopeButtons = View.VISIBLE,
                        //showSenderLocation = true,
                        startMapScope = MapScope.ME_SCOPE
                ),
                LocationMessageType.ANSWER to ReceiverScreenSettings(
                        screenHeader = requireContext().getString(R.string.receiver_screen_answer_title),
                        showAbonentNumberAndComment = View.VISIBLE,
                        showScopeButtons = View.VISIBLE,
                        //showSenderLocation = true,
                        startMapScope = MapScope.SENDER_SCOPE
                ),
                LocationMessageType.INFO to ReceiverScreenSettings(
                        screenHeader = requireContext().getString(R.string.receiver_screen_info_title),
                        showAbonentNumberAndComment = View.VISIBLE,
                        showScopeButtons = View.VISIBLE,
                        //showSenderLocation = true,
                        startMapScope = MapScope.SENDER_SCOPE
                )
        ) }

    // ViewModel хранит последнее полученное значение Location
    private val lastLocationViewModel  by lazy {
        vita.with(VitaOwner.Multiple(this)).getViewModel<LastLocationViewModel>()
    }
    // ViewModel хранит последнее полученное значение locationMessage
    private val locationMessageViewModel  by lazy {
        vita.with(VitaOwner.Multiple(this)).getViewModel<LocationMessageViewModel>()
    }
    //: LastLocationViewModel by activityViewModels() //  navGraphViewModels(R.id.nav_graph)
    //private lateinit var myLastLocation: Location
    //private lateinit var receivedLocationMessage: LocationMessage
    private lateinit var myMap: GoogleMap // Хранит ссылку на готовую карту
    private lateinit var curMapScope : MapScope // Текущая область видимости карты
    private var mapReady = false // Флаг "Карта готова к работе"
    private var gpsFixed = false // Флаг "Фикс произошел"
    private lateinit var myMarker: Marker // Маркер своего положения на карте
    private lateinit var myPosition: LatLng // Своя позиция для отображения маркера на карте
    private lateinit var senderPosition: LatLng // Позиция абонента для отображения маркера на карте

    // Создание стандартного LocationManager, который обеспечивает работу с Location API
    private val locationManager : LocationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    // Реализация объекта-лиснера своей локации
    private val locationListener = object : LocationListener {
        // Функция отрабатывает включение провайдера GPS.
        // Ее реализация обязательна, даже пустая, иначе при включении GPS после его выключения
        // это приложение будет вылетать с ошибкой, т.к. вызываемый при этом метод onProviderEnabled
        // абстрактный.
        override fun onProviderEnabled(provider: String) {
            Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
            if (provider == LocationManager.GPS_PROVIDER) {
                receiverFrag.statusGpsTv.text = getString(R.string.gps_provider_on_msg)
                receiverFrag.statusGpsTv.setTextColor(PREPARE_COLOR)
            }
            Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
        }

        // Функция отрабатывает отключение провайдера GPS.
        // Ее реализация обязательна, даже пустая, иначе при выключеном GPS это приложение будет
        // вылетать на старте с ошибкой, т.к. вызываемый при этом метод onProviderDisabled абстрактный.
        override fun onProviderDisabled(provider: String) {
            Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
            if (provider == LocationManager.GPS_PROVIDER) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.gps_provider_off_msg),
                        Toast.LENGTH_LONG
                ).show()
                receiverFrag.statusGpsTv.text = getString(R.string.gps_provider_off_msg)
                receiverFrag.statusGpsTv.setTextColor(ALERT_COLOR)
                gpsFixed = false
            }
            Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
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
            Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
            // Переменная для отображения своей позиции на карте
            myPosition = LatLng(newLocation.latitude, newLocation.longitude)
            // Записать новую локацию
            //myLastLocation = newLocation
            lastLocationViewModel.setLocation(newLocation)
            // Вывести на карте маркер полученной локации
            if (mapReady) {
                if (::myMarker.isInitialized) myMarker.remove() // стереть предыдущий маркер с карты,
                // если он есть
                with (myMap) {
                    myMarker = addMarker(MarkerOptions().title(getString(R.string.itsme_msg))
                            .position(myPosition)
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.circle_arrow_red_32)).anchor(0.5F, 0.5F)
                            //.flat(true)
                            //.rotation(newLocation.bearing)
                    )
                }
                showMapWithMarkers()
            }
            Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
        }
    }

    // Колбек срабатывает когда карта асинхронно создана вызовом getMapAsync
    private val mapReadyCallback = OnMapReadyCallback { googleMap ->
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        myMap = googleMap // получить ссылку на готовую карту в myMap
        myMap.uiSettings.isZoomControlsEnabled = true // добавить на карту кнопки масштабирования
        with (locationMessageViewModel.locationMessage.value!!) {
            // Если в текущем режиме экрана предусмотрен показ маркера местоположения другого
            // абонента, вывести маркер на экран
            val curScreenMode = screenModes.getValue(type)
            when (type) { // вывести на карте маркер полученной локации, если он есть
                LocationMessageType.FULL_REQUEST,
                LocationMessageType.ANSWER,
                LocationMessageType.INFO -> {
                    senderPosition = LatLng(location.latitude, location.longitude)
                    myMap.addMarker(MarkerOptions()
                            .title(abonentPhoneNumber)
                            .position(senderPosition)
                            //.icon(BitmapDescriptorFactory
                            //   .fromResource(R.drawable.circle_arrow_red_32))
                            //   .anchor(0.5F,0.5F)
                    )
                    // Настроить параметры камеры и отобразить карту с маркером абонента в центре
                    val cameraPosition = CameraPosition.Builder()
                            .target(senderPosition)
                            .zoom(Settings.mapZoom)
                            //.bearing(newLocation.bearing) // камера будет поворачиваться по направлению движения
                            .tilt(Settings.mapTilt) // угол наклона карты к наблюдателю
                            .build()
                    myMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
        mapReady = true // установить флаг готовности карты
        Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
    }

    // Колбек срабатывает при изменении статуса GPS.
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
            Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
            with (receiverFrag) {
                // Вывести на экран сообщение об установке GPS-статуса "Поиск..."
                statusGpsTv.text = getString(ru.yodata.whereareyou.R.string.gps_start_msg)
                statusGpsTv.setTextColor(ru.yodata.whereareyou.PREPARE_COLOR)
                // Включить прогрессбар
                gpsProgressBar.visibility = android.view.View.VISIBLE
                // Деактивировать кнопку перехода на экран передачи данных
                sendAnswerBtn.isEnabled = false
            }
            Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
        }

        // Функция отрабатывает момент первого успешного завершения определения координат
        // текущего положения пользователя (фикс)
        override fun onFirstFix(ttffMillis: Int) {
            Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
            with (receiverFrag) {
                // Вывести на экран сообщение об установке GPS-статуса "Фикс"
                statusGpsTv.text = getString(ru.yodata.whereareyou.R.string.gps_fix_msg)
                statusGpsTv.setTextColor(ru.yodata.whereareyou.ALL_RIGHT_COLOR)
                // Убрать прогрессбар
                gpsProgressBar.visibility = android.view.View.INVISIBLE
                // Активировать кнопку перехода на экран передачи данных
                sendAnswerBtn.isEnabled = true
            }
            gpsFixed = true
            // Установить первоначальный масштаб изображения карты (из настроек)
            if (mapReady) myMap.moveCamera(CameraUpdateFactory.zoomTo(Settings.mapZoom))
            Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                    "${object {}.javaClass.getEnclosingMethod().getName()}")
        }
    }

    // Функция отображает карту на экране с учетом текущего установленного скоупа, а также маркеры
    // местоположения своего и абонента (если есть)
    fun showMapWithMarkers() {
        with (myMap) {
            when (curMapScope) {
                // Настроить параметры камеры взависимости от текущего режима отображения
                MapScope.ME_SCOPE -> { // отобразить карту с маркером своего положения
                    // в центре
                    val cameraPosition = CameraPosition.Builder()
                            .target(myPosition)
                            .zoom(getCameraPosition().zoom)
                            .bearing(lastLocationViewModel.location.value!!.bearing) // камера будет
                            // поворачиваться по направлению движения
                            .tilt(Settings.mapTilt) // угол наклона карты к наблюдателю
                            .build()
                    animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
                MapScope.SENDER_SCOPE -> { // отобразить карту с маркером положения абонента
                    // в центре
                    val cameraPosition = CameraPosition.Builder()
                            .target(senderPosition)
                            .zoom(getCameraPosition().zoom)
                            //.bearing(myLastLocation.bearing) // камера будет поворачиваться
                            // по направлению движения
                            .tilt(Settings.mapTilt) // угол наклона карты к наблюдателю
                            .build()
                    animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
                MapScope.TOGETHER_SCOPE -> { // отобразить карту так, чтобы оба маркера были
                    // видны одновременно
                    val cameraPosition = CameraPosition.Builder()
                            .target(myPosition)
                            //.zoom(getCameraPosition().zoom)
                            //.bearing(myLastLocation.bearing) // камера будет поворачиваться
                            // по направлению движения
                            .tilt(Settings.mapTilt) // угол наклона карты к наблюдателю
                            .build()
                    var cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                    cameraUpdate = CameraUpdateFactory
                            .newLatLngBounds(correctLatLngBounds(myPosition, senderPosition),
                                    Settings.mapPadding)
                    animateCamera(cameraUpdate)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receiver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Необходимо для View Binding:
        _receiverFrag = FragmentReceiverBinding.bind(view)
        // Заполнить экранные элементы значениями из вьюмодели
        with (locationMessageViewModel.locationMessage.value!!) {
            with (receiverFrag) {
                // Заполнить элементы экрана, зависящие от типа входящего сообщения
                val curScreenMode = screenModes.getValue(type)
                screenHeaderTv.text = curScreenMode.screenHeader
                abonentTv.visibility = curScreenMode.showAbonentNumberAndComment
                commentTv.visibility = curScreenMode.showAbonentNumberAndComment
                scopeButtonsGroup.visibility = curScreenMode.showScopeButtons
                curMapScope = curScreenMode.startMapScope
                // Заполнить элементы экрана, не зависящие от типа входящего сообщения
                abonentTv.text = abonentPhoneNumber
                commentTv.text = comment
            }
        }
        // Запустить отрисовку карты
        val mapFragment = childFragmentManager.findFragmentById(R.id.receiverMap)
                as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)
        // Назначить слуштели кнопкам на экране
        with (receiverFrag) {
            meScopeBtn.setOnClickListener {
                curMapScope = ru.yodata.whereareyou.MapScope.ME_SCOPE
                showMapWithMarkers()
            }
            senderScopeBtn.setOnClickListener {
                curMapScope = ru.yodata.whereareyou.MapScope.SENDER_SCOPE
                showMapWithMarkers()
            }
            togetherScopeBtn.setOnClickListener {
                curMapScope = ru.yodata.whereareyou.MapScope.TOGETHER_SCOPE
                showMapWithMarkers()
            }
            cancelAnswerBtn.setOnClickListener {
                // Выход
                requireActivity().onBackPressed()
            }
            sendAnswerBtn.setOnClickListener { button ->
                button.findNavController()
                        .navigate(R.id.action_receiverFragment_to_sendLocationFragment)
            }

        }
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onStart()
        // Включить получение локаций от датчиков местоположения (отключение - в onStop)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                Settings.locationMinTimeMs,
                Settings.locationMinDistanceM,
                locationListener)
        Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
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
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onStop()
        // Запомнить текущий масштаб карты, чтобы восстановиться в этом же масштабе.
        // Делать это нужно только если фикс уже произошел, иначе запомнится неверный масштаб (весь мир)
        if (gpsFixed) Settings.mapZoom = myMap.getCameraPosition().zoom
        // Выключить получение локаций от датчиков
        locationManager.removeUpdates(locationListener)
        gpsFixed = false
        Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
    }

    override fun onDestroyView() {
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onDestroyView()
        // Необходимо для View Binding:
        _receiverFrag = null
        mapReady = false
        Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReceiverFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ReceiverFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}