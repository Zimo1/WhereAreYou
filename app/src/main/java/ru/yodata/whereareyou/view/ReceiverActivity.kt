package ru.yodata.whereareyou.view

import android.annotation.SuppressLint
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
// ReceiverActivity, которое в свою очередь запускает ReceiverFragment, в котором происходит
// обработка запроса и ответ на него (если требуется)
class ReceiverActivity : AppCompatActivity() {
    // Инициализация View Binding
    //private lateinit var curActivity: ActivityReceiverBinding

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
    /*private lateinit var myMap: GoogleMap // Хранит ссылку на готовую карту
    private lateinit var curMapScope : MapScope // Текущая область видимости карты
    private var mapReady = false // Флаг "Карта готова к работе"
    private var gpsFixed = false // Флаг "Фикс произошел"
    private lateinit var myMarker: Marker // Маркер своего положения на карте
    private lateinit var myPosition: LatLng // Своя позиция для отображения маркера на карте
    private lateinit var senderPosition: LatLng // Позиция абонента для отображения маркера на карте*/

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
        super.onCreate(savedInstanceState)
        // Инициализация View Binding
        //curActivity = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_receiver)  //(curActivity.root)   //

        // Если ReceiverActivity запущена в первый раз (т.е. это не поворот экрана и т.п.)
        if (savedInstanceState == null) {
            val curIntent = intent
            with (curIntent) {
                Log.d(TAG, "Интент: type: $type, action: $action, dataString: $dataString, component: $component")
            }
            // Если интент существует, значит активити было запущено при получении SMS
            if (curIntent != null) {
                val receiverActivityMode = curIntent.getSerializableExtra(MODE_BUNDLE_KEY)
                        as ReceiverActivityMode
                when (receiverActivityMode) {
                    ReceiverActivityMode.SMS -> {
                        // Получить данные SMS из интента, посланного от MySmsReceiver
                        val plainSms = curIntent.getSerializableExtra(SMS_BUNDLE_KEY) as PlainSms
                        with(plainSms) {
                            // Проверить текст SMS - есть ли там данные LocationMessage или это
                            // что-то неподходящее
                            if (smsText.isConvertableToLocationMessage()) {
                                // Распарсить текст SMS во вьюмодель, содержащую LocationMessage
                                locationMessageViewModel.setLocationMessage(
                                    smsText.convertToLocationMessage(phoneNumber)
                                )
                            }
                            else { // текст SMS не содержит LocationMessage - так не должно быть,
                                // произошел сбой, либо у пользователя устаревшая версия приложения
                                // TODO: обработать получение неправильной SMS
                            }
                        }
                    }
                    ReceiverActivityMode.EMPTY -> {
                        // Записать в locationMessageViewModel пустое значение LocationMessage
                        locationMessageViewModel.setLocationMessage(LocationMessage())
                    }
                    ReceiverActivityMode.VIEWING -> {
                        // Режим просмотра сохраненной ранее LocationMessage.
                        // Данные должны быть уже записаны во вьюмодель.
                        // Ничего делать не нужно.
                    }
                }
            }
            else {
                // TODO: Интента нет - такого быть не может, обработать сбой
            }
        }
        Log.d(TAG,"Старт метода: ${this::class.java.simpleName}:" +
                "${object {}.javaClass.getEnclosingMethod().getName()}")
    }
}