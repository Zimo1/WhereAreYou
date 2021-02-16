package ru.yodata.whereareyou

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*

const val TAG = "WHEREAREYOU"
// Константы цветов:
const val ALERT_COLOR = Color.RED //0xFFCC0000 // Красный
const val PREPARE_COLOR = Color.BLUE //0xFFFFBB33 // Желтый
const val ALL_RIGHT_COLOR = Color.GREEN //0xFF669900 // Зеленый

const val MAX_USER_MESSAGE_SIZE = 300 // Максимальная длина комментария пользователя к локации
const val APPREF = "https://link.to.setup.this.app.com" // Ссылка в Google Play на установку этого приложения

const val SMS_BUNDLE_KEY = "SMS" // Ключ для передачи данных SMS через интент в MySmsReceiver

// Служебные символы и строки сообщения LocationMessage
const val SEPARATOR = " " // Разделитель значений в тексте SMS
const val PREFIX = "Δ" // Префикс аргумента (по нему определяется начало аргумента) - не должен встречаться в комментарии SMS
const val EQALITY = "=" // Знак равенства (или знак, предшествующий значению аргумента)
const val EQALITY_LENGHT = EQALITY.length // Длина команды EQALITY
const val SMS_HEADER = ":->GPS" // Заголовок SMS - именно по наличию этих символов в
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

enum class MapScope {
    ME_SCOPE,
    SENDER_SCOPE,
    TOGETHER_SCOPE
}

// Функция определяет находся ли одна точка севернее другой
fun LatLng.northerly(point: LatLng) : Boolean {
    return this.latitude > point.latitude
}

// Функция определяет находся ли одна точка восточнее другой
fun LatLng.easterly(point: LatLng) : Boolean {
    return this.longitude > point.longitude
}

fun correctLatLngBounds(onePoint: LatLng, anotherPoint : LatLng) : LatLngBounds {
    val southwest = LatLng(Math.min(onePoint.latitude, anotherPoint.latitude),
                            Math.min(onePoint.longitude, anotherPoint.longitude))
    val northeast = LatLng(Math.max(onePoint.latitude, anotherPoint.latitude),
                            Math.max(onePoint.longitude, anotherPoint.longitude))
    return LatLngBounds(southwest, northeast)
}

/*inline fun currentClassAndMethod(thisClass: Any) : String {
    return "${thisClass::class.java.simpleName}:${object{}.javaClass.getEnclosingMethod().getName()}"
}*/
/*inline fun currentClassAndMethod(thisClass: Any) : String  { thisClass ->
     "${thisClass::class.java.simpleName}:${object{}.javaClass.getEnclosingMethod().getName()}"
}*/
//val methodName = ""${this::class.java.simpleName}:${object{}.javaClass.getEnclosingMethod().getName()}""
 inline fun Any.currentClassAndMethod(method: () -> String) : String  {
     return "${this::class.java.simpleName}:${method()}"
}

class Settings {
    companion object {
        // Настройки locationManager.requestLocationUpdates - получение локаций от датчика GPS
        var locationMinTimeMs: Long = 2000 // Через какое время происходит обновление локации (мс)
        var locationMinDistanceM: Float = 2.0F // При сдвиге на какое расстояние (м)

        // Настройки карты
        var mapZoom = 17F // Масштаб изображения карты
        var mapTilt = 30F // Угол наклона карты к наблюдателю
        var mapPadding = 90 // Для режима показа нескольких маркеров одновременно -
                            // поля прямоугольной области, содержащей эти маркеры

    }
}