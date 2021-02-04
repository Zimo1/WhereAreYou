package ru.yodata.whereareyou.model

import android.location.Location
import android.util.Log
import ru.yodata.whereareyou.*
import java.sql.Time
import java.sql.Timestamp
import java.util.*

const val TAG = "SMS"

data class LocationMessage(
        val id: Int, // Идентификатор данного сообщения
        val requestId: Int, // Идентификатор запроса, ответом на который является данное сообщение
        val type: LocationMessageType, // Тип сообщения, enam, см. ниже
        val incoming: Boolean, // Сообщение входящее или исходящее
        val location: Location, // Стандартные данные локации отправителя
        val chargingPercentage: Int, // Процент зарядки аккумулятора смартфона отправителя
        val comment: String, // Комментарий получателю, передаваемый внутри сообщения
        val abonentPhoneNumber: String, // Номер телефона абонента, который прислал сообщение или которому оно послано
        // val locationTimestamp: Timestamp, // возможно не нужен, время есть в Location
        /* val autoDetection: Boolean // Автоматическое или ручное определение координат местоположения
        val messageSendDate: Date, // Дата и время отсылки сообщения
        val messageSendTime: Time,
        val messageReceiveDate: Date, // Дата и время принятия сообщения
        val messageReceiveTime: Time*/
) {
    override fun toString(): String {
        val result = StringBuilder(SMS_HEADER + VERSION)
        result.append(SEPARATOR, PREFIX, when (type) {
            LocationMessageType.REQUEST -> REQUEST
            LocationMessageType.ANSWER -> ANSWER
            LocationMessageType.INFO -> INFO
        })
        if (location != null) { // если есть данные о локации, добавить их
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
        if (!comment.isEmpty()) // комментарий получателю
            result.append(SEPARATOR, PREFIX, COMMENT, EQALITY, comment)
        if (!APPREF.isEmpty()) // ссылка в Google Play на установку этого приложения
            result.append(SEPARATOR, PREFIX, LINK, EQALITY, APPREF)

        val resultStr = result.toString()
        Log.d(TAG, resultStr)

        return resultStr  //result.toString()
    }
}

// Тип сообщения
enum class LocationMessageType {
    REQUEST, // Запрос локации у абонента
    ANSWER, // Ответ на запрос локации абонентом
    INFO // Сведения о своей локации, передаваемые абоненту без запроса с его стороны
}