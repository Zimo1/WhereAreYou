package ru.yodata.whereareyou

import android.R
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location.distanceBetween
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.io.Serializable
import java.lang.Math.round
import java.math.BigDecimal
import java.util.*


const val TAG = "WHEREAREYOU" // тег, используется везде, где нужен тег, указывающий на данное
            // приложение: для команды логирования Log.d(TAG, "..."), для создания notification
const val CHANNEL_ID = "ru.yodata.whereareyou" // id канала нотификаций
const val MY_PREFERENCES = "whereareyouprefs" // название файла SharedPreferences
lateinit var mySharedPreferences: SharedPreferences // сюда будет записана ссылка на
            // SharedPreferences, чтобы не вычислять ее каждый раз при обращении к SharedPreferences
const val MESSAGE_ID = "mid" // переменная счетчика id сообщений о локациях в SharedPreferences
const val START_MESSAGE_ID = 1 // начальное значение переменной MESSAGE_ID в SharedPreferences
lateinit var notificationSound: Uri // звук нотификации

// Константы цветов:
const val ALERT_COLOR = Color.RED //0xFFCC0000 // Красный
const val PREPARE_COLOR = Color.BLUE //0xFFFFBB33 // Желтый
const val ALL_RIGHT_COLOR = Color.GREEN //0xFF669900 // Зеленый

const val MAX_USER_MESSAGE_SIZE = 300 // Максимальная длина комментария пользователя к локации
const val APPREF = "https://link.to.setup.this.app.com" // Ссылка в Google Play на установку этого приложения

const val SMS_BUNDLE_KEY = "SMS" // Ключ для передачи данных SMS через интент из MySmsReceiver
                                // в ReceiverActivity
const val MODE_BUNDLE_KEY = "MODE" // Ключ для передачи режима запуска ReceiverActivity через
                            // интент (ReceiverActivity может быть запущено пользователем либо
                            // MySmsReceiver автоматически при получении SMS)

// Служебные символы и литералы, использующиеся в сообщении LocationMessage
const val SEPARATOR = " " // Разделитель значений в тексте SMS
const val PREFIX = "Δ" // Префикс аргумента (по нему определяется начало аргумента) - не должен
                    // встречаться в комментарии SMS
const val EQALITY = "=" // Знак равенства (т.е. любой знак, предшествующий значению аргумента)
const val EQALITY_LENGHT = EQALITY.length // Длина литерала EQALITY
const val SMS_HEADER = ":->GPS-" // Заголовок SMS - именно по наличию этих символов в
            // начале SMS обработчик определяет, что данное SMS имеет отношение к приложению
const val SMS_HEADER_LENGHT = SMS_HEADER.length // Длина заголовка SMS
const val VERSION = "01" // Версия приложения, в котором было создано SMS. Используется чтобы понимать
                        // может ли текущая версия приложения правильно обработать данное SMS
const val FULL_SMS_HEADER = SMS_HEADER + VERSION // Полный заголовок SMS (включая номер версии)
const val FULL_SMS_HEADER_LENGHT = FULL_SMS_HEADER.length // Длина полного заголовка SMS

// Аргументы сообщения LocationMessage (значения должны быть разными!):
const val MSG_TYPE = "K" // Тип сообщения
    // Возможные значения типа сообщения:
    const val REQUEST = "R" // Тип сообщения: Запрос
    const val ANSWER = "A" // Тип сообщения: Ответ
    const val INFO = "I" // Тип сообщения: Инфо
const val LATITUDE = "N" // Северная широта
const val LONGITUDE = "E" // Восточная долгота
const val ALTITUDE = "A" // Высота
const val SPEED = "S" // Скорость
const val TIME = "T"  // Время
const val CHARGING = "C" // Зарядка аккумулятора
const val ID = "D" // Идентификатор сообщения
const val REQUEST_ID ="Q" // Идентификатор запроса, ответом на который является это сообщение
const val COMMENT = "M" // Комментарий
const val AUTODETECTED = "U" // Координаты были определены автоматически или указаны вручную
    const val TRUE_VALUE = "T"
    const val FALSE_VALUE = "F"
const val LINK = "L" // Ссылка в Google Play на установку этого приложения

// Режимы отображения карты на экране
enum class MapScope {
    ME_SCOPE, // В центре пользователь
    SENDER_SCOPE, // В центре абонент
    TOGETHER_SCOPE // Карта автоматически масштабируется так, чтобы были одновременно видны обе точки
}
// Режимы запуска ReceiverActivity
enum class ReceiverActivityMode : Serializable {
    SMS, // при получении SMS
    EMPTY, // без данных LocationMessage
    VIEWING // режим просмотра сохраненного ранее LocationMessage
}

// Функция определяет находся ли одна точка севернее другой
fun LatLng.northerly(point: LatLng) : Boolean {
    return this.latitude > point.latitude
}

// Функция определяет находся ли одна точка восточнее другой
fun LatLng.easterly(point: LatLng) : Boolean {
    return this.longitude > point.longitude
}

// Функция усовершенствует стандартную функцию LatLngBounds.
// Теперь угловые диагональные точки прямоугольника можно указывать в любом порядке
fun correctLatLngBounds(onePoint: LatLng, anotherPoint: LatLng) : LatLngBounds {
    val southwest = LatLng(Math.min(onePoint.latitude, anotherPoint.latitude),
            Math.min(onePoint.longitude, anotherPoint.longitude))
    val northeast = LatLng(Math.max(onePoint.latitude, anotherPoint.latitude),
            Math.max(onePoint.longitude, anotherPoint.longitude))
    return LatLngBounds(southwest, northeast)
}

fun showDistance(onePoint: LatLng, anotherPoint: LatLng) : String {
    var result = floatArrayOf(0f,0f,0f,0f)
    Log.d(TAG, "Начинаю вычисление дистанции")
    distanceBetween(onePoint.latitude, onePoint.longitude,
            anotherPoint.latitude, anotherPoint.longitude, result )
    Log.d(TAG, "Закончил вычисление дистанции")
    val distance = result[0]
    Log.d(TAG, "Дистанция = $distance")
    if (distance > 50000F) return "${distance*0.001.toInt()} км"
    else
        if (distance >= 1000F) return "${"%.2f".format(distance)} км"
        else return "${distance.toInt()} м"
    //return "100 м"
}

class Settings {
    companion object {
        // Настройки метода locationManager.requestLocationUpdates - получение локаций от датчика GPS
        var locationMinTimeMs: Long = 2000 // Через какое время происходит обновление локации (мс)
        var locationMinDistanceM: Float = 2.0F // При сдвиге на какое расстояние (м)

        // Настройки карты
        var mapZoom = 17F // Масштаб изображения карты
        var mapTilt = 30F // Угол наклона карты к наблюдателю
        var mapPadding = 90 // Для режима показа нескольких маркеров одновременно -
                            // поля прямоугольной области, содержащей эти маркеры (в dp)
        var showShortestWay: Boolean = true // Показывать ли линию между маркерами своим и абонента

    }
}