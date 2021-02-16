package ru.yodata.whereareyou.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.androidisland.vita.VitaOwner
import com.androidisland.vita.vita
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import ru.yodata.whereareyou.databinding.ActivityReceiverBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import ru.yodata.whereareyou.*
import ru.yodata.whereareyou.model.*
import ru.yodata.whereareyou.viewmodel.LastLocationViewModel
import ru.yodata.whereareyou.viewmodel.LocationMessageViewModel

// Вторая точка входа в приложение находится в постоянном броадкаст ресивере MySmsReceiver.
// Он просматривает все входящие SMS и если находит в них запрос локации, запускает данное
// ReceiverActivity, в котором происходит обработка запроса и ответ на него (если требуется)
class ReceiverActivity : AppCompatActivity() {
    // Инициализация View Binding
    private lateinit var curActivity: ActivityReceiverBinding

    // Класс настроек экрана для определенного режима работы
    class ReceiverActivitySettings (
            val screenHeader: String,
            val showSenderLocation: Boolean,
            val startMapScope: MapScope,
            )
    // Предопределенные режимы отображения экрана и их настройки
    private val screenModes: Map<LocationMessageType, ReceiverActivitySettings> by lazy {
        mapOf(
            LocationMessageType.EMPTY to ReceiverActivitySettings(
                    screenHeader = applicationContext.getString(R.string.receiver_screen_define_title),
                    showSenderLocation = false,
                    startMapScope = MapScope.ME_SCOPE
            ),
            LocationMessageType.REQUEST to ReceiverActivitySettings(
                    screenHeader = applicationContext.getString(R.string.receiver_screen_request_title),
                    showSenderLocation = false,
                    startMapScope = MapScope.ME_SCOPE
            ),
            LocationMessageType.FULL_REQUEST to ReceiverActivitySettings(
                    screenHeader = applicationContext.getString(R.string.receiver_screen_request_title),
                    showSenderLocation = true,
                    startMapScope = MapScope.ME_SCOPE
            ),
            LocationMessageType.ANSWER to ReceiverActivitySettings(
                    screenHeader = applicationContext.getString(R.string.receiver_screen_answer_title),
                    showSenderLocation = true,
                    startMapScope = MapScope.SENDER_SCOPE
            ),
            LocationMessageType.INFO to ReceiverActivitySettings(
                    screenHeader = applicationContext.getString(R.string.receiver_screen_info_title),
                    showSenderLocation = true,
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

    // Регистрация стандартного LocationManager, который обеспечивает работу с Location API
    private val locationManager : LocationManager by lazy {
        this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                curActivity.statusGpsTv.text = getString(R.string.gps_provider_on_msg)
                curActivity.statusGpsTv.setTextColor(PREPARE_COLOR)
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
                        baseContext,
                        getString(R.string.gps_provider_off_msg),
                        Toast.LENGTH_LONG
                ).show()
                curActivity.statusGpsTv.text = getString(R.string.gps_provider_off_msg)
                curActivity.statusGpsTv.setTextColor(ALERT_COLOR)
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
            with (curActivity) {
                // Вывести на экран сообщение об установке GPS-статуса "Поиск..."
                statusGpsTv.text = getString(R.string.gps_start_msg)
                statusGpsTv.setTextColor(PREPARE_COLOR)
                // Включить прогрессбар
                gpsProgressBar.visibility = View.VISIBLE
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
            with (curActivity) {
                // Вывести на экран сообщение об установке GPS-статуса "Фикс"
                statusGpsTv.text = getString(R.string.gps_fix_msg)
                statusGpsTv.setTextColor(ALL_RIGHT_COLOR)
                // Убрать прогрессбар
                gpsProgressBar.visibility = View.INVISIBLE
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
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onCreate(savedInstanceState)
        // Инициализация View Binding
        curActivity = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(curActivity.root)   //(R.layout.activity_receiver)

        // Если ReceiverActivity запущена в первый раз (т.е. это не поворот экрана и т.п.)
        if (savedInstanceState == null) {
            val curIntent = intent
            with (curIntent) {
                Log.d(TAG, "Интент: type: $type, action: $action, dataString: $dataString, component: $component")
            }
            // Если интент существует, значит активити было запущено при получении SMS
            if (false) {//(curIntent != null) {
                // Получить данные SMS из интента, посланного от MySmsReceiver
                val plainSms = curIntent.getSerializableExtra(SMS_BUNDLE_KEY) as PlainSms
                with(plainSms) {
                    // Проверить текст SMS - есть ли там данные LocationMessage или это что-то неподходящее
                    if (smsText.isConvertableToLocationMessage()) {
                        // Распарсить текст SMS в переменную типа LocationMessage
                        locationMessageViewModel.setLocationMessage(smsText.convertToLocationMessage(phoneNumber))

                    } else { // текст SMS не содержит LocationMessage - так не должно быть, произошел сбой
                        // TODO: обработать получение неправильной SMS
                    }
                }
            }
            else { // Интента нет - значит активити было запущено с главного экрана и позиции
                // абонента не существует.
                // Записать в locationMessageViewModel пустое значение LocationMessage
                locationMessageViewModel.setLocationMessage(LocationMessage())
            }
        }
        // Заполнить экранные элементы значениями из вьюмодели
        with(locationMessageViewModel.locationMessage.value!!) {
            with(curActivity) {
                // Заполнить элементы экрана, зависящие от типа входящего сообщения
                val curScreenMode = screenModes.getValue(type)
                screenHeaderTv.text = curScreenMode.screenHeader
                curMapScope = curScreenMode.startMapScope
                // Заполнить элементы экрана, не зависящие от типа входящего сообщения
                abonentTv.text = abonentPhoneNumber
                commentTv.text = comment
            }
        }
        // Запустить отрисовку карты
        val mapFragment = supportFragmentManager.findFragmentById(R.id.receiverMap)
                as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)
        // Назначить слуштели кнопкам на экране
        with (curActivity) {
            meScopeBtn.setOnClickListener {
                curMapScope = MapScope.ME_SCOPE
                showMapWithMarkers()
            }
            senderScopeBtn.setOnClickListener {
                curMapScope = MapScope.SENDER_SCOPE
                showMapWithMarkers()
            }
            togetherScopeBtn.setOnClickListener {
                curMapScope = MapScope.TOGETHER_SCOPE
                showMapWithMarkers()
            }
            cancelAnswerBtn.setOnClickListener {
                finish()
            }
            /*sendAnswerBtn.setOnClickListener { button ->
                button.findNavController()
                        .navigate(R.id.action_global_sendLocationFragment)}*/
            sendAnswerBtn.setOnClickListener { button ->
                val startIntent = Intent(baseContext, SendLocationFragment::class.java).apply {
                    //putExtra(SMS_BUNDLE_KEY, smsData)
                    //setClassName(context!!.packageName, ReceiverActivity::class.simpleName!!)
                    //setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)} // без этого флага будет вылет
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)}
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

    override fun onDestroy() {
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onDestroy()
        mapReady = false
        Log.d(TAG,"Финиш метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
    }
}