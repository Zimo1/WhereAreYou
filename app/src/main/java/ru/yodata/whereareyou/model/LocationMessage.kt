package ru.yodata.whereareyou.model

import android.location.Location
import java.sql.Time
import java.sql.Timestamp
import java.util.*

data class LocationMessage(
        val id: Int, // Идентификатор данного сообщения
        val requestId: Int, // Идентификатор запроса, ответом на который является данное сообщение
        val type: LocationMessageType, // Тип сообщения, enam, см. ниже
        val incoming: Boolean, // Сообщение входящее или исходящее
        val location: Location, // Стандартные данные локации отправителя
        val chargingPercentage: Int, // Процент зарядки аккумулятора смартфона отправителя
        val message: String, // Комментарий, передаваемый внутри сообщения
        val phoneNumber: String, // Номер телефона абонента, который прислал сообщение или которому оно послано
        val locationTimestamp: Timestamp, // возможно не нужен, время есть в Location
        val messageSendDate: Date, // Дата и время отсылки сообщения
        val messageSendTime: Time,
        val messageReceiveDate: Date, // Дата и время принятия сообщения
        val messageReceiveTime: Time
)

// Тип сообщения
enum class LocationMessageType {
    REQUEST, // Запрос локации у абонента
    ANSWER, // Ответ на запрос локации абонентом
    INFO // Сведения о своей локации, передаваемые абоненту без запроса с его стороны
}