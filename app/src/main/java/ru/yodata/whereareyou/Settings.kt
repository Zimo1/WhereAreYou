package ru.yodata.whereareyou

import android.app.Application
import android.graphics.Color
import android.graphics.Color.pack
import android.graphics.Color.toArgb
import android.location.Location

// Константы цветов:
const val ALERT_COLOR = Color.RED //0xFFCC0000 // Красный
const val PREPARE_COLOR = Color.BLUE //0xFFFFBB33 // Желтый
const val ALL_RIGHT_COLOR = Color.GREEN //0xFF669900 // Зеленый

const val MAX_USER_MESSAGE_SIZE = 100 // Максимальная длина комментария пользователя к локации
const val APPREF = "https://..." // Ссылка в Google Play на установку этого приложения

const val SEPARATOR = " " // Разделитель значений в тексте SMS
const val PREFIX = "*" // Префикс аргумента (по нему определяется начало аргумента)
const val EQALITY = "=" // Знак равенства (или знак, предшествующий значению аргумента)
const val SMS_HEADER = PREFIX + "GPS" // Заголовок SMS
const val VERSION = "1" // Версия приложения, в котором было создано SMS. Используется чтобы понимать
                        // может ли текущая версия приложения правильно обработать данное SMS

// Аргументы:
const val LATITUDE = "N" // Северная широта
const val LONGITUDE = "E" // Восточная долгота
const val ALTITUDE = "A" // Высота
const val SPEED = "S" // Скорость
const val TIME = "T"  // Время
const val CHARGING = "C" // Зарядка аккумулятора
const val REQUEST = "R" // Тип сообщения: Запрос
const val ANSWER = "W" // Тип сообщения: Ответ
const val INFO = "I" // Тип сообщения: Инфо
const val REQUEST_ID ="#" // Идентификатор запроса
const val MESSAGE = "M" // Комментарий
const val LINK = "L" // Ссылка в Google Play на установку этого приложения


class Settings {
    companion object {
        // Настройки locationManager.requestLocationUpdates - получение локаций от датчика GPS
        var locationMinTimeMs: Long = 2000 // Через какое время происходит обновление локации (мс)
        var locationMinDistanceM: Float = 2.0F // При сдвиге на какое расстояние (м)

        // Настройки карты
        var mapZoom = 17F // Масштаб изображения карты
        var mapTilt = 30F // Угол наклона карты к наблюдателю

    }
}