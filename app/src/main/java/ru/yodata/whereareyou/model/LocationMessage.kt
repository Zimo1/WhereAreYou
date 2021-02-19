package ru.yodata.whereareyou.model

import android.location.Location
import android.location.LocationManager
import android.util.Log
import ru.yodata.whereareyou.*

// Функция проверяет строку - содержит ли она сообщение о локации в формате данного приложения,
// либо в строке какое-то посторонее содержимое
fun String.isConvertableToLocationMessage() : Boolean {
    return this.take(SMS_HEADER_LENGHT) == SMS_HEADER
}

// Парсер текстовой строки из SMS в переменную типа LocationMessage.
// Перед запуском ОБЯЗАТЕЛЬНО нужно проверить строку на возможность конвертации: проверка
// производится при помощи isConvertableToLocationMessage()
fun String.convertToLocationMessage(phoneNumber: String) : LocationMessage {
    val result = LocationMessage()
    //if (this.isConvertableToLocationMessage()) {
    val line = this.substring(FULL_SMS_HEADER_LENGHT + SEPARATOR.length + PREFIX.length)
    val commands = line.split(SEPARATOR + PREFIX)
    with (result) {
        abonentPhoneNumber = phoneNumber
        for (command in commands) {
            val equaltyPosition = command.indexOf(EQALITY)
            if (equaltyPosition <= 0) {
                // TODO: в тексте команды не найдена позиция знака равенства. Так быть не может. Обработать ошибку
            }
            val commandName = command.take(equaltyPosition)
            val commandValue = command.substring(equaltyPosition + EQALITY_LENGHT)
            when (commandName) {
                MSG_TYPE -> when (commandValue) {
                    REQUEST -> type = LocationMessageType.REQUEST
                    ANSWER -> type = LocationMessageType.ANSWER
                    INFO -> type = LocationMessageType.INFO
                }
                LATITUDE -> location.latitude = commandValue.toDouble()
                LONGITUDE -> location.longitude = commandValue.toDouble()
                ALTITUDE -> location.altitude = commandValue.toDouble()
                SPEED -> location.speed = commandValue.toFloat()
                TIME -> location.time = commandValue.toLong()
                CHARGING -> chargingPercentage = commandValue.toInt()
                ID -> id = commandValue.toInt()
                REQUEST_ID -> requestId = commandValue.toInt()
                COMMENT -> comment = commandValue
                AUTODETECTED -> when (commandValue) {
                    TRUE_VALUE -> autoDetected = true
                    else -> autoDetected = false
                }
            }

        }
        // Если тип сообщения - запрос (REQUEST) и при этом переданы координаты запрашивающего, то
        // тип сообщения нужно изменить на FULL_REQUEST
        if ((type == LocationMessageType.REQUEST) &&
                (location.latitude > 0) && (location.longitude > 0)) {
            type = LocationMessageType.FULL_REQUEST
        }
    }
    //}
    //else result = null
    return result
}

data class LocationMessage(
        var id: Int = 0, // Идентификатор данного сообщения
        var requestId: Int = 0, // Идентификатор запроса, ответом на который является данное сообщение
        var type: LocationMessageType = LocationMessageType.EMPTY, // Тип сообщения, enam, см. ниже
        var incoming: Boolean = true, // Сообщение входящее или исходящее
        var location: Location = Location(LocationManager.GPS_PROVIDER), // Стандартные данные локации отправителя
        var chargingPercentage: Int = 0, // Процент зарядки аккумулятора смартфона отправителя
        var comment: String = "", // Комментарий получателю, передаваемый внутри сообщения
        var abonentPhoneNumber: String = "", // Номер телефона абонента, который прислал сообщение
                                            // или которому оно послано
        var autoDetected: Boolean = true // Автоматическое или ручное определение координат местоположения
        // var locationTimestamp: Timestamp, // возможно не нужен, время есть в Location
        /*
        var messageSendDate: Date, // Дата и время отсылки сообщения
        var messageSendTime: Time,
        var messageReceiveDate: Date, // Дата и время принятия сообщения
        var messageReceiveTime: Time*/
) {
    override fun toString(): String {
        val result = StringBuilder(SMS_HEADER + VERSION)
        result.append(SEPARATOR, PREFIX, MSG_TYPE, EQALITY, when (type) {
            LocationMessageType.REQUEST -> REQUEST
            LocationMessageType.FULL_REQUEST -> REQUEST
            LocationMessageType.ANSWER -> ANSWER
            LocationMessageType.INFO -> INFO
            else -> "?"// TODO: неизвестный тип сообщения, так быть не должно - обработать ошибку
        })
        // если есть данные о локации, добавить их в сообщение
        if ((location.latitude > 0) && (location.longitude > 0)) {
            result.append(SEPARATOR, PREFIX, LATITUDE, EQALITY, location.latitude) // широта
            result.append(SEPARATOR, PREFIX, LONGITUDE, EQALITY, location.longitude) // долгота
            result.append(SEPARATOR, PREFIX, ALTITUDE, EQALITY, location.altitude) // высота
            result.append(SEPARATOR, PREFIX, SPEED, EQALITY, location.speed) // скорость
            result.append(SEPARATOR, PREFIX, TIME, EQALITY, location.time) // UTC время и дата получения локации
        }
        if (chargingPercentage != 0 ) // процент зарядки аккумулятора
            result.append(SEPARATOR, PREFIX, CHARGING, EQALITY, chargingPercentage)
        if (id != 0 ) // идентификатор сообщения
            result.append(SEPARATOR, PREFIX, ID, EQALITY, id)
        if (requestId != 0 ) // идентификатор запроса, ответом на который является это сообщение
            result.append(SEPARATOR, PREFIX, REQUEST_ID, EQALITY, requestId)
        if (comment.isNotEmpty()) // комментарий получателю
            result.append(SEPARATOR, PREFIX, COMMENT, EQALITY, comment)
        result.append(SEPARATOR, PREFIX, AUTODETECTED, EQALITY, if (autoDetected) TRUE_VALUE
               else FALSE_VALUE) // // Координаты были определены автоматически или указаны вручную
        if (APPREF.isNotEmpty()) // ссылка в Google Play на установку этого приложения
            result.append(SEPARATOR, PREFIX, LINK, EQALITY, APPREF)

        val resultStr = result.toString()
        Log.d(TAG, resultStr)

        return resultStr  //result.toString()
    }

}

// Тип сообщения
enum class LocationMessageType {
    EMPTY, // Пустое
    REQUEST, // Запрос локации у абонента
    FULL_REQUEST, // Запрос локации у абонента, содержащий в себе локацию запрашивающего
    ANSWER, // Ответ на запрос локации абонентом
    INFO // Сведения о своей локации, передаваемые абоненту без запроса с его стороны
}